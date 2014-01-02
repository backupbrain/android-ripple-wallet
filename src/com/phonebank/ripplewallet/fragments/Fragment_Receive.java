package com.phonebank.ripplewallet.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.zxing.WriterException;
import com.phonebank.ripple.RippleAccount;
import com.phonebank.ripple.RippleBank;
import com.phonebank.ripple.RippleWallet;
import com.phonebank.ripplewallet.R;
import com.phonebank.ripplewallet.RippleWalletActivity;
import com.phonebank.ripplewallet.RippleWalletActivity.BankResponseListener;
import com.phonebank.ripplewallet.View_Account;

/**
 * Recieve money screen
 * @author Tony Gaitatzis
 *
 */
public class Fragment_Receive extends Fragment implements BankResponseListener {
	private RippleBank rippleBank;
	private RippleAccount account;
	private View fragmentView;
	private ImageView qrcode;
	private View_Account accountView;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		((RippleWalletActivity)getActivity()).showTabs();

		rippleBank = ((RippleWalletActivity)getActivity()).getRippleBank();
		((RippleWalletActivity)getActivity()).setBankResponseListener(this);

		String fromAccountAddress = ((RippleWalletActivity)getActivity()).getAccountAddressFromPrefs();
		
		
		fragmentView = inflater.inflate(R.layout.fragment_receive, container, false);
		
		qrcode = (ImageView)fragmentView.findViewById(R.id.qrcode);

		accountView = (View_Account)getActivity().findViewById(R.id.accountView);
		

		// Display the account address and build a QR code
		// we *should* already have an account
		// if we don't, let's ask for one
		populateInfo(fromAccountAddress);

		// Inflate the layout for this fragment
		return fragmentView;
		
		
	}
	

	
	@Override
	public void onResume() {
		super.onResume();
		accountView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	
	
	public void populateInfo(String accountAddress) {
		// generate a QR code
		int qrCodeDimention = 500;
		try {
			Bitmap qr = ((RippleWalletActivity)getActivity()).generateQRCode(accountAddress, qrCodeDimention);
			qrcode.setImageBitmap(qr);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	@Override
	public void onRippleAccountRetrieved(RippleAccount rippleAccount) {		
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
	public void onConnected() {
		// TODO Auto-generated method stub
		
	}

}
