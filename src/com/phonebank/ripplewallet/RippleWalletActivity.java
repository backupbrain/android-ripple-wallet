package com.phonebank.ripplewallet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.phonebank.ripple.RippleAccount;
import com.phonebank.ripple.RippleBank;
import com.phonebank.ripple.RippleWallet;
import com.phonebank.ripplewallet.fragments.Fragment_Login;
import com.phonebank.ripplewallet.fragments.Fragment_Receive;
import com.phonebank.ripplewallet.fragments.Fragment_Send;
import com.phonebank.ripplewallet.fragments.Fragment_Wallet;

/**
 * http://arvid-g.de/12/android-4-actionbar-with-tabs-example
 * The main activity, that deals with login.
 * @author Tony Gaitatzis
 * @editor Shiyao Qi
 * @date 2013.12.22
 * @email qishiyao2008@126.com
 */
public class RippleWalletActivity extends Activity implements RippleBank.Listener {

	public static final String PREFS_KEY_RIPPLE_ADDRESS = "rippleAddress";
	public static final String PREFS_KEY_RIPPLE_SECRET = "rippleSecret";

	public static final String PREFS_KEY_ACCOUNT = "storedRippleAccount"; // Account key.

	public static Context appContext;
	private SharedPreferences prefs; // Used to store account information.
	private RippleBank rippleBank;

	private View_Account accountView; // Self-defined account address view.
	private View_Notifier notifierView;

	private TextView actionBarTitle;
	private ProgressBar actionBarProgress;
	private Button actionBarRefresh;

	private Camera mCamera;
	private boolean previewing = true;
	private Handler autoFocusHandler;

	private boolean tabsshown = false; // Indicate whether the tab is shown.

	private boolean networkAvailable = false; // Indicate whether the network is available.

	private BankResponseListener rippleBankResponseListener;

	/**
	 * BankResponseListener.
	 */
	public interface BankResponseListener {
		public void onConnected();

		public void onRippleAccountRetrieved(RippleAccount rippleAccount);

		public void onUserAccountWalletsListRetrieved(RippleWallet[] wallets);

		public void onTransactionSigned(int transactionID, String tx_blob);

		public void onTransactionSubmitted();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); // Contains accountView and fragment_container.

		accountView = (View_Account) findViewById(R.id.accountView); // AccountView.

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		ActionBar actionbar = getActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		/*
		 * actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		 * actionbar.setDisplayShowTitleEnabled(true); //
		 * actionBar.setTitle("Hello");
		 * actionbar.setCustomView(R.layout.actionbar_layout); actionBarTitle =
		 * (TextView) findViewById(R.id.actionBarTitle);
		 * setActionBarTitle(getString(R.string.app_name));
		 * 
		 * actionBarProgress = (ProgressBar)
		 * findViewById(R.id.actionBarProgress); actionBarRefresh = (Button)
		 * findViewById(R.id.actionBarRefresh); hideActionBarProgress();
		 * 
		 * notifierView = (View_Notifier) findViewById(R.id.notifier);
		 * hideNotifier();
		 */
		setRippleBank(new RippleBank(this, this)); // Set RippleBank

		if (!rippleBank.isNetworkAvailable()) { // Network is not available.
			Notifier.error(this, getString(R.string.error_no_internet));
		}
		showTabs(); // Call the function to show tabs.
	}

	@Override
	public void onResume() {
		super.onResume();
		// load our wallets and such from local storage
		loadAccountFromPrefs();
		getRippleBank().connect(); // Call the function to connect to Ripple server.
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera(); // 
		// store our wallets into local storage
		saveAccountToPrefs();

		getRippleBank().disconnect();
	}
	
	/**
	 * Get the account value from SharedPreferences.
	 */
	public void loadAccountFromPrefs() {
		String savedAccountString = prefs.getString(PREFS_KEY_ACCOUNT, ""); // Get the account value.
		Log.v("Wallet","fonud account: "+savedAccountString);
		if (savedAccountString != "") {
			try {
				JSONObject accountJSON = new JSONObject(savedAccountString);
				getRippleBank().loadAccountFromJSON(accountJSON);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Save account information to SharedPreferences.
	 */
	public void saveAccountToPrefs() {
		if (getRippleBank().getAccount() != null) {
			try {
				SharedPreferences.Editor editor = prefs.edit(); // Create an editor for SharedPreferences.
				editor.putString(PREFS_KEY_ACCOUNT, getRippleBank().getAccount().toJSON().toString()); // Put account into editor.
				editor.commit(); // Commit the change.
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set BankResponseListener.
	 * @param listener The BankResponseListener to set.
	 */
	public void setBankResponseListener(BankResponseListener listener) {
		this.rippleBankResponseListener = listener;
	}

	/**
	 * Get BankResponseListener.
	 * @return BankResponseListener.
	 */
	public BankResponseListener getBankResponseListener() {
		return rippleBankResponseListener;
	}

	public void hideNotifier() {
		notifierView.collapse();
	}

	public void showNotification(String message) {
		notifierView.setMessage(message);
		notifierView.expand();
	}

	public void setActionBarTitle(String title) {
		actionBarTitle.setText(title);
	}

	public void showActionBarProgress() {
		actionBarRefresh.setVisibility(View.GONE);
		actionBarProgress.setVisibility(View.VISIBLE);

	}

	public void hideActionBarProgress() {
		actionBarProgress.setVisibility(View.GONE);
		actionBarRefresh.setVisibility(View.VISIBLE);
	}

	/**
	 * Show the tab.
	 */
	public void showTabs() {
		if (!tabsshown) {
			ActionBar actionbar = getActionBar(); // Get the ActionBar.
			// initiate tabs
			ActionBar.Tab tab_wallet = actionbar.newTab().setText(
					R.string.tab_wallet);
			Fragment fragment_wallet = new Fragment_Wallet();
			tab_wallet.setTabListener(new MyTabsListener(fragment_wallet));
			actionbar.addTab(tab_wallet);

			// Tab send.
			ActionBar.Tab tab_send = actionbar.newTab().setText(
					R.string.tab_send);
			Fragment fragment_send = new Fragment_Send();
			tab_send.setTabListener(new MyTabsListener(fragment_send));
			actionbar.addTab(tab_send);

			// Tab receive.
			ActionBar.Tab tab_receive = actionbar.newTab().setText(
					R.string.tab_receive);
			Fragment fragment_receive = new Fragment_Receive();
			tab_receive.setTabListener(new MyTabsListener(fragment_receive));
			actionbar.addTab(tab_receive);

			tabsshown = true; // Change the tab status.
			
			// if we aren't logged in, we must force a login
			if (this.getAccountAddressFromPrefs() == "") {
				Fragment fragment_login = new Fragment_Login();
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(R.id.fragment_container, fragment_login); // Show the login view.
				ft.commit(); // Sechedule a commit.
			}
		}
	}

	/**
	 * Hide the tab.
	 */
	public void hideTabs() {
		if (tabsshown) {
			ActionBar actionbar = getActionBar();
			actionbar.removeAllTabs(); // Remove all tabs and deselect the tab.
            tabsshown = false; // Change the tab status.
		}

	}

	/**
	 * Set RippleAccount and account address.
	 * @param account
	 */
	public void setAccount(RippleAccount account) {
		rippleBank.setAccount(account); // Set account
		accountView.setAccountAddress(account.getAccountAddress()); // Set account address.
	}

	public void setRippleBank(RippleBank rippleBank) {
		this.rippleBank = rippleBank;
	}

	public RippleBank getRippleBank() {
		return rippleBank;
	}

	/**
	 * Save Ripple address to SharedPreferences.
	 * @param rippleAddress The Ripple Address to save.
	 */
	public void saveWalletAddressToPrefs(String rippleAddress) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREFS_KEY_RIPPLE_ADDRESS, rippleAddress);
		editor.commit();
	}
	
	/**
	 * Get Ripple address from SharedPreferences.
	 * @return
	 */
	public String getAccountAddressFromPrefs() {
		return prefs.getString(PREFS_KEY_RIPPLE_ADDRESS, "");
	}
	
	/**
	 * Save Ripple wallet secret to SharedPreferences.
	 * @param rippleSecret The Ripple wallet secret to save.
	 */
	public void saveWalletSecretToPrefs(String rippleSecret) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREFS_KEY_RIPPLE_SECRET, rippleSecret);
		editor.commit();	
	}
	
	/**
	 * Get Ripple wallet secret from SharedPreferences.
	 * @return
	 */
	public String getAccountSecretFromPrefs() {
		return prefs.getString(PREFS_KEY_RIPPLE_SECRET, "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Generate the QRCode according to the Ripple wallet address.
	 * @param address Ripple wallet address.
	 * @param qrCodeDimention QRCode dimention.
	 * @return The generated QRCode.
	 * @throws WriterException
	 */
	public Bitmap generateQRCode(String address, int qrCodeDimention)
			throws WriterException {
		QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(address, null,
				Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
				qrCodeDimention);

		Bitmap qrcode = qrCodeEncoder.encodeAsBitmap();
		return qrcode;
	}

	public boolean isNetworkAvaliable() {
		return networkAvailable;
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // Creates a new Camera object.
		} catch (Exception e) {
		}
		return c;
	}

	/**
	 * Release the camera.
	 */
	private void releaseCamera() {
		if (mCamera != null) { // There is a camera.
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release(); // Disconnect and release the camera object resource.
			mCamera = null;
		}
	}

	/**
	 * Starts camera auto-focus and register a callback.
	 */
	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB); // Starts camera auto-focus and registers a callback function to run.
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	@Override
	public void onConnect() {
		Log.v("Wallet", "Activity noticed that Bank connected");
		getBankResponseListener().onConnected();

	}

	@Override
	public void onMessage(JSONObject object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnect(int code, String reason) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void onError(Exception error) {
		Log.v("Wallet", "Activity noticed Bank had error:"+error.toString());
		// TODO Auto-generated method stub
	
	}

	@Override
	public void onRippleAccountRetrieved(RippleAccount rippleAccount) {
		Log.v("Wallet", "Activity noticed that Bank retrieved Account: "+rippleAccount.toString());
		setAccount(rippleAccount);
		getBankResponseListener().onRippleAccountRetrieved(rippleAccount);
	}

	@Override
	public void onUserAccountWalletsListRetrieved(RippleWallet[] wallets) {
		Log.v("Wallet", "Activity noticed that Bank opened wallets");
		getBankResponseListener().onUserAccountWalletsListRetrieved(wallets);
	}

	@Override
	public void onTransactionSigned(int transactionID, String tx_blob) {
		Log.v("Wallet", "Activity noticed that Bank signed transaction");
		getBankResponseListener().onTransactionSigned(transactionID, tx_blob);
	}

	@Override
	public void onTransactionSubmitted() {
		Log.v("Wallet", "Activity noticed that Bank signed transaction");
		getBankResponseListener().onTransactionSubmitted();
	}
}
