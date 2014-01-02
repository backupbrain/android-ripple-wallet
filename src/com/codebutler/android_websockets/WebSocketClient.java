package com.codebutler.android_websockets;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

/**
 * @editor Shiyao Qi
 * @date 2013.12.31
 * @email qishiyao2008@126.com
 */
public class WebSocketClient {
    private static final String TAG = "WebSocketClient";

    private URI                      mURI;
    private Listener                 mListener; // WebSocket listener.
    private Socket                   mSocket; 
    private Thread                   mThread;
    private HandlerThread            mHandlerThread;
    private Handler                  mHandler;
    private List<BasicNameValuePair> mExtraHeaders;
    private HybiParser               mParser;
    private boolean                  mConnected;

    private final Object mSendLock = new Object();

    private static TrustManager[] sTrustManagers;

    public static void setTrustManagers(TrustManager[] tm) {
        sTrustManagers = tm;
    }

    public WebSocketClient(URI uri, Listener listener, List<BasicNameValuePair> extraHeaders) {
        mURI          = uri;
        mListener     = listener;
        mExtraHeaders = extraHeaders;
        mConnected    = false;
        mParser       = new HybiParser(this);

        mHandlerThread = new HandlerThread("websocket-thread");
        mHandlerThread.start(); // Start a new thread.
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public Listener getListener() {
        return mListener;
    }

    /**
     * Create an instance of Thread an run it.
     */
    public void connect() {
        if (mThread != null && mThread.isAlive()) { // The thread is running.
            return;
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                	// Get the port of the uri.
                    int port = (mURI.getPort() != -1) ? mURI.getPort() : ((mURI.getScheme().equals("wss") || mURI.getScheme().equals("https")) ? 443 : 80);

                    // Get the path of the uri.
                    String path = TextUtils.isEmpty(mURI.getPath()) ? "/" : mURI.getPath();
                    
                    // Update the path if the query of the uri is not null.
                    if (!TextUtils.isEmpty(mURI.getQuery())) {
                        path += "?" + mURI.getQuery(); // Use the query to construct the path.
                    }

                    // Get the scheme of the uri.
                    String originScheme = mURI.getScheme().equals("wss") ? "https" : "http";
                    
                    // Create a new uri instance according to the scheme and host.
                    URI origin = new URI(originScheme, "//" + mURI.getHost(), null);

                    // Get the socket factory.
                    SocketFactory factory = (mURI.getScheme().equals("wss") || mURI.getScheme().equals("https")) ? getSSLSocketFactory() : SocketFactory.getDefault();
                    mSocket = factory.createSocket(mURI.getHost(), port); // Create a new socket connected to the host and port.

                    PrintWriter out = new PrintWriter(mSocket.getOutputStream());
                    out.print("GET " + path + " HTTP/1.1\r\n");
                    out.print("Upgrade: websocket\r\n");
                    out.print("Connection: Upgrade\r\n");
                    out.print("Host: " + mURI.getHost() + "\r\n");
                    out.print("Origin: " + origin.toString() + "\r\n");
                    out.print("Sec-WebSocket-Key: " + createSecret() + "\r\n");
                    out.print("Sec-WebSocket-Version: 13\r\n");
                    if (mExtraHeaders != null) { // Add the name-value pair.
                        for (NameValuePair pair : mExtraHeaders) {
                            out.print(String.format("%s: %s\r\n", pair.getName(), pair.getValue()));
                        }
                    }
                    out.print("\r\n");
                    out.flush(); // Flush the data to the target.

                    // Get the data of the InputStream.
                    HybiParser.HappyDataInputStream stream = new HybiParser.HappyDataInputStream(mSocket.getInputStream());

                    // Read HTTP response status line.
                    StatusLine statusLine = parseStatusLine(readLine(stream));
                    if (statusLine == null) {
                        throw new HttpException("Received no reply from server.");
                    } else if (statusLine.getStatusCode() != HttpStatus.SC_SWITCHING_PROTOCOLS) {
                        throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                    }

                    // Read HTTP response headers.
                    String line;
                    while (!TextUtils.isEmpty(line = readLine(stream))) {
                        Header header = parseHeader(line);
                        if (header.getName().equals("Sec-WebSocket-Accept")) {
                            // FIXME: Verify the response...
                        }
                    }

                    mListener.onConnect();

                    mConnected = true; // Update the connect status.

                    // Now decode websocket frames.
                    mParser.start(stream);

                } catch (EOFException ex) {
                    Log.d(TAG, "WebSocket EOF!", ex);
                    ex.printStackTrace();
                    mListener.onDisconnect(0, "EOF");
                    mConnected = false;

                } catch (SSLException ex) {
                    // Connection reset by peer
                    Log.d(TAG, "Websocket SSL error!", ex);
                    mListener.onDisconnect(0, "SSL");
                    mConnected = false;

                } catch (Exception ex) {
                    mListener.onError(ex);
                }
            }
        });
        mThread.start();
    }

    /**
     * Disconnect the socket.
     */
    public void disconnect() {
        if (mSocket != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mSocket.close(); // Close the socket.
                        mSocket = null;
                        mConnected = false; // Set the connected status to false.
                    } catch (IOException ex) {
                        Log.d(TAG, "Error while disconnecting", ex);
                        mListener.onError(ex);
                    }
                }
            });
        }
    }

    public void send(String data) {
        sendFrame(mParser.frame(data));
    }

    public void send(byte[] data) {
        sendFrame(mParser.frame(data));
    }

    public boolean isConnected() {
        return mConnected;
    }

    /**
     * Get the StatusLine of an HTTP response.
     * @param line The HTTP response.
     * @return The StatusLine.
     */
    private StatusLine parseStatusLine(String line) {
        if (TextUtils.isEmpty(line)) {
            return null;
        }
        return BasicLineParser.parseStatusLine(line, new BasicLineParser());
    }

    private Header parseHeader(String line) {
        return BasicLineParser.parseHeader(line, new BasicLineParser());
    }

    // Can't use BufferedReader because it buffers past the HTTP data.
    private String readLine(HybiParser.HappyDataInputStream reader) throws IOException {
        int readChar = reader.read(); // Read a byte.
        if (readChar == -1) {
            return null;
        }
        StringBuilder string = new StringBuilder("");
        while (readChar != '\n') { // Read all the data to a StringBuilder.
            if (readChar != '\r') {
                string.append((char) readChar);
            }

            readChar = reader.read(); // Read another byte.
            if (readChar == -1) {
                return null;
            }
        }
        return string.toString(); // Return the content.
    }

    /**
     * Create random bytes and encoded by Base64.
     * @return Random bytes encoded by Base64.
     */
    private String createSecret() {
        byte[] nonce = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce[i] = (byte) (Math.random() * 256);
        }
        return Base64.encodeToString(nonce, Base64.DEFAULT).trim();
    }

    void sendFrame(final byte[] frame) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mSendLock) {
                        OutputStream outputStream = mSocket.getOutputStream();
                        outputStream.write(frame);
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    mListener.onError(e);
                }
            }
        });
    }

    public interface Listener {
        public void onConnect();
        public void onMessage(String message);
        public void onMessage(byte[] data);
        public void onDisconnect(int code, String reason);
        public void onError(Exception error);
    }

    /**
     * Get an SSLSocketFactory instance.
     * @return An SSLSocketFactory instance.
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS"); // Create a new SSLContext instance according to the protocol TLS.
        context.init(null, sTrustManagers, null); // Initialize the SSLContext instance.
        return context.getSocketFactory(); // Get a SocketFactory of the instance.
    }
}
