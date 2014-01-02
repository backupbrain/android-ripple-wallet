package com.phonebank.ripple;

import java.net.URI;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

/**
 * This is our ripple bank - it communicates with the Ripple network
 * @author Tony Gaitatzis
 * @editor Shiyao Qi
 * @date 2013.12.22
 * @email qishiyao2008@126.com
 *
 */
public class RippleBank implements WebSocketClient.Listener {
	private String address; // Account address.
	private String ripple_server_uri = "wss://s1.ripple.com:51233"; // Ripple server uri.
	private WebSocketClient websocket;
	private RippleBank.Listener bankListener;
	
	private boolean isConnected = false;
	private Activity activity;
	
	// rippled reports XRP values as being 1000000 times higher than the website does
	public static int XRP_DECIMAL_OFFSET = 1000000; 
	
	private static final int ID_ACCOUNT_INFO = 100;
	private static final int ID_ACCOUNT_LINES = 101;
	private static final int ID_ACCOUNT_OFFERS = 200;
	private static final int ID_ACCOUNT_TRANSACTIONS = 201;
	private static final int ID_SIGN = 300; 
	private static final int ID_SUBMIT = 301;

	public static final String CURRENCY_BTC = "BTC"; // Currency BTC.
	public static final String CURRENCY_XRP = "XRP"; // Currency XRP.
	public static final String CURRENCY_EUR = "EUR"; // Currency EUR.
	public static final String CURRENCY_CNY = "CNY"; //	Currency CNY.
    public static final String CURRENCY_USD; // Currency USD.

    static {
        CURRENCY_USD = "USD";
    }

    public static final String CURRENCY_CAD = "CAD"; // Currency CAD.
	public static final String CURRENCY_DEFAULT = CURRENCY_XRP; // Default currency XRP.
	
	private static final String ID_LINES = "lines";
	
	public final static String ADDRESS_ONE = "rrrrrrrrrrrrrrrrrrrrBZbvji"; // The ripple address one.
	
	private RippleAccount account; // Ripple account. 

	public interface Listener {
		public void onConnect();
		public void onMessage(JSONObject object);
		public void onMessage(byte[] data);
		public void onDisconnect(int code, String reason);
		public void onError(Exception error);

		public void onRippleAccountRetrieved(RippleAccount rippleAccount);
		public void onUserAccountWalletsListRetrieved(RippleWallet[] wallets);
		public void onTransactionSigned(int transactionID, String tx_blob);
		public void onTransactionSubmitted();
	}

	public RippleBank(Activity activity, RippleBank.Listener listener) {
		this.activity = activity;
		this.bankListener = listener;
	}
	
	/**
	 * Get the account from JSON.
	 * @param accountJSON JSONObject.
	 * @throws JSONException
	 */
	public void loadAccountFromJSON(JSONObject accountJSON) throws JSONException {
		RippleAccount account = RippleAccount.fromJSON(accountJSON);
		setAccount(account);
	}
	
	public boolean isConnected() {
		return isConnected;
	}

	public void setServerURI(String uri) {
		this.ripple_server_uri = uri;
	}

	public String getServerURI() {
		return ripple_server_uri;
	}

	public void setRippleAddress(String address) {
		this.address = address;
	}

	public String getRippleAddress() {
		return address;
	}

	/**
	 * Connect to the Ripple server uri.
	 */
	public void connect() {
		if (isNetworkAvailable()) { // The network is not available.
			if (ripple_server_uri.isEmpty()) // Ripple server uri is empty.
				throw new NullPointerException("No ripple_server_uri specified");

			List<BasicNameValuePair> extraHeaders = null;
			websocket = new WebSocketClient(URI.create(ripple_server_uri),
					this, extraHeaders); // Construct a new WebSocketClient.
			websocket.connect(); // Connect to the Ripple server.
		}
	}

	public void disconnect() {

	}
	public RippleAccount getAccount() {
		return account;
	}
	
	/**
	 * Set account.
	 * @param account The account to set.
	 */
	public void setAccount(RippleAccount account) {
		this.account = account;
	}

	/**
	 * Fetch accoun information.
	 * @param address Account address.
	 * @throws JSONException
	 */
	public void fetchAccountInfo(String address) throws JSONException {
		Log.v("Wallet","Bank is attempting to fetch "+address);
		JSONObject json = new JSONObject();
		json.put("id", ID_ACCOUNT_INFO);
		json.put("command", "account_info");
		json.put("account", address);
		sendMessage(json);
	}

	/**
	 * Fetch account lines.
	 * @param address Account address.
	 * @throws JSONException
	 */
	public void fetchAccountLines(String address) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", ID_ACCOUNT_LINES);
		json.put("command", "account_lines");
		json.put("account", address);
		sendMessage(json);
	}
	
	/**
	 * 
	 * @throws JSONException
	 */
	public void fetchAccountLines() throws JSONException {
		if (getAccount() == null && getAccount().getAccountAddress().equals("")) {
			//FIXME: use R.string for errors
			throw new NullPointerException("No account retrieved yet");
		}
		fetchAccountLines(getAccount().getAccountAddress());
	}

	public void fetchAccountOffers(String address) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", ID_ACCOUNT_OFFERS);
		json.put("command", "account_offers");
		json.put("account", address);
		sendMessage(json);
	}

	public void fetchAccountTransactions(String address) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", ID_ACCOUNT_TRANSACTIONS);
		json.put("command", "account_tx");
		json.put("account", address);
		sendMessage(json);
	}

	
	public void signTransaction(String fromAddress, String secret, int amount, String destinationAddress) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", ID_SIGN);
		json.put("command", "sign");
		json.put("secret", secret);
		
		JSONObject tx = new JSONObject();
		tx.put("TransactionType", "Payment");
		tx.put("Account", fromAddress);
		tx.put("Amount", amount);
		tx.put("Destination", destinationAddress);
		
		json.put("tx_json", tx);
		
		sendMessage(json);
	}


    public void signTransaction(String fromAddress, String secret, int value, String currency, String issuerAddress, String destinationAddress) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", ID_SIGN);
        json.put("command", "sign");
        json.put("secret", secret);

        JSONObject tx = new JSONObject();
        tx.put("TransactionType", "Payment");
        tx.put("Account", fromAddress);
        tx.put("Destination", destinationAddress);

        JSONObject amount = new JSONObject();
        json.put("value", value);
        json.put("currency", currency);
        json.put("issuer", issuerAddress);
        tx.put("Amount", amount);

        json.put("tx_json", tx);

        sendMessage(json);
    }
	
	public void submitTransaction(String tx_blob) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", ID_SUBMIT);
		json.put("command", "submit");
		json.put("tx_blob", tx_blob);
		sendMessage(json);
	}

	
	public void sendMessage(String message) {
		if (websocket.isConnected()) {
			Log.v("Wallet","sending "+message);
			websocket.send(message);
		} else {
			Log.v("Wallet","can't send message: websocket not connected");
		}
	}

	public void sendMessage(JSONObject json) {
		sendMessage(json.toString());
	}

	// this was called from a separate thread in websocket 
	// so we must sync it up with the main thread in order
	// to do UI changes
	// http://stackoverflow.com/questions/14220794/exceptionininitializererror-when-instanciating-asynctask-inside-plugin-execute
	@Override
	public void onConnect() {
		isConnected = true;
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
		    	bankListener.onConnect();
		    }
		});
	}


	@Override
	public void onMessage(String message) {
		JSONObject object = null;
		try {
			object = new JSONObject(message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		final JSONObject jsonobject = object;
		
		
		// can we figure out what kind of message this was+
		Log.v("ripplewallet","json: "+jsonobject.toString());
		
		// this is weak as we are using the id to figure out what 
		// *type* of transaction this is, rather than actually
		// using it to identify the request
		int transactionID_temp = 0;
		JSONObject result_temp = new JSONObject();
		try {
			transactionID_temp = jsonobject.getInt("id");
			result_temp = jsonobject.getJSONObject("result");					
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final int transactionID = transactionID_temp;
		final JSONObject result = result_temp;
		
		
		
		// omg this is so messy!
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() { 
		    	

				switch (transactionID) { 
				case ID_ACCOUNT_INFO:
					try {
						JSONObject accountData = result.getJSONObject("account_data");
						bankListener.onRippleAccountRetrieved(RippleAccount.fromJSON(accountData));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				break;
				case ID_ACCOUNT_LINES:
			  		//bankListener.onMessage(jsonobject);
			  		// we are expecting an array called lines
			  		JSONArray wallet_array;
			  		RippleAccount account = getAccount();
					try { 
						if (account == null) account = new RippleAccount();
						wallet_array = result.getJSONArray(ID_LINES);
						Log.v("Wallet","JSONArray: "+wallet_array);
						RippleWallet[] wallets = new RippleWallet[wallet_array.length()];
				  	    for(int i = 0 ; i < wallet_array.length(); i++) {
				  	    	wallets[i] = RippleWallet.fromJSON(wallet_array.getJSONObject(i));
				  	    }
				  	    bankListener.onUserAccountWalletsListRetrieved(wallets);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				break;
				case ID_ACCOUNT_OFFERS:
				break;
				case ID_ACCOUNT_TRANSACTIONS:
				break;
				case ID_SIGN:
					try {
						String tx_blob = result.getString("tx_blob");
						bankListener.onTransactionSigned(transactionID, tx_blob);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				break;
				case ID_SUBMIT:
					bankListener.onTransactionSubmitted();
					
				break;
				default:
				}
				
		    }
		});
	}

	@Override
	public void onMessage(byte[] data) {
		bankListener.onMessage(data);
		
		final byte[] databytes = data;
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {  
		  		bankListener.onMessage(databytes);
		    }
		});
	}

	@Override
	public void onDisconnect(int code, String reason) {
		isConnected = false;
		bankListener.onDisconnect(code, reason);
		
		final int finalcode = code;
		final String finalreason = reason;
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {  
		  		bankListener.onDisconnect(finalcode, finalreason);
		    }
		});
	}

	@Override
	public void onError(Exception error) {
		bankListener.onError(error);
	}
	

	/**
	 * Returns a boolean true if networking is available and false if networking
	 * is not.
	 * 
	 * @return true if networking is available, false otherwise
	 */
	public boolean isNetworkAvailable() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null
						&& netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}

	

}
