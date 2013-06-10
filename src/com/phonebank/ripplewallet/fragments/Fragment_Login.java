package com.phonebank.ripplewallet.fragments;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.phonebank.ripple.RippleAccount;
import com.phonebank.ripple.RippleBank;
import com.phonebank.ripple.RippleWallet;
import com.phonebank.ripplewallet.R;
import com.phonebank.ripplewallet.RippleWalletActivity;
import com.phonebank.ripplewallet.View_Account;
import com.phonebank.ripplewallet.View_CameraPreview;

/**
 * Login Screen
 * @author Tony Gaitatzis
 *
 */
public class Fragment_Login extends Fragment implements OnClickListener, RippleWalletActivity.BankResponseListener,  View_CameraPreview.ScanListener {
	private View fragmentView;
	private Button buttonLogin, buttonScanAddress;
	private TextView editAddress, editSecret;
	private RippleBank rippleBank;
	private View_CameraPreview scannerPane;
	private View_Account accountView;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		fragmentView = inflater.inflate(R.layout.fragment_login, container, false);

		buttonLogin = (Button)fragmentView.findViewById(R.id.button_login);
		buttonLogin.setOnClickListener(this);
		buttonScanAddress = (Button)fragmentView.findViewById(R.id.button_scanAddress);
		buttonScanAddress.setOnClickListener(this);
		
		editAddress = (EditText)fragmentView.findViewById(R.id.edit_accountAddress);
		editSecret = (EditText)fragmentView.findViewById(R.id.edit_accountSecret);
		
		scannerPane = (View_CameraPreview)fragmentView.findViewById(R.id.camerapreview);
		accountView = (View_Account)getActivity().findViewById(R.id.accountView);
		
		
		rippleBank = ((RippleWalletActivity)getActivity()).getRippleBank();
        if (!rippleBank.isConnected()) {
            rippleBank.connect();
        }

        ((RippleWalletActivity)getActivity()).hideTabs();
		return fragmentView;
	}

	@Override
	public void onResume() {
		super.onResume();
		((RippleWalletActivity)getActivity()).setBankResponseListener(this);
		accountView.setVisibility(View.GONE);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_login:
			// attempt login
			String accountAddress = editAddress.getText().toString();
			try {
				rippleBank.fetchAccountInfo(accountAddress);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		case R.id.button_scanAddress:
			startCameraPreview();
		break;
		default:
		}
		
	}
	
	

	public void startCameraPreview() {
		scannerPane.startPreview(this);
	}
	
	
	// error handling
	/*
	@Override
	public void onRippleBankError(String errorMessage) {
		
	}
	/* */

	@Override
	public void onRippleAccountRetrieved(RippleAccount rippleAccount) {
        try {
            Log.v("Wallet", "onRippleAccountRetrieved(" + rippleAccount.toJSON() + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("Wallet","account address: "+rippleAccount.getAccountAddress());

		// we got here because we are testing the Ripple Account
		if (!rippleAccount.getAccountAddress().equals("")) {
			rippleBank.setAccount(rippleAccount);
			// now save the account so we don't have to log in in the future
			((RippleWalletActivity)getActivity()).saveAccountToPrefs();
			
			// save account info
			((RippleWalletActivity)getActivity()).saveWalletAddressToPrefs(rippleAccount.getAccountAddress());
			((RippleWalletActivity)getActivity()).saveWalletSecretToPrefs(editSecret.getText().toString());
			

			getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);	
			((RippleWalletActivity)getActivity()).showTabs();
			ActionBar.Tab tab = getActivity().getActionBar().getTabAt(0);
			getActivity().getActionBar().selectTab(tab);
		}
		
	}

	@Override
	public void onUserAccountWalletsListRetrieved(RippleWallet[] wallets) {
		
	}

	@Override
	public void onTransactionSigned(int transactionID, String tx_blob) {
		
	}

	@Override
	public void onTransactionSubmitted() {
	}



	@Override
	public void handleScan(String message) {
		String address = message;
		// if this has an http:// in it, we need to extract the ripple address
		String search = "to=";
		if (!address.startsWith("r")) {
			address = address.substring(address.indexOf(search)+search.length(),address.length());
		}
		editAddress.setText(address);
		
	}

	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
		
	}

}
