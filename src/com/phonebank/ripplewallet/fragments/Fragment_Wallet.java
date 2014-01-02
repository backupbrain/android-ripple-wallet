package com.phonebank.ripplewallet.fragments;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.phonebank.ripple.RippleAccount;
import com.phonebank.ripple.RippleBank;
import com.phonebank.ripple.RippleWallet;
import com.phonebank.ripplewallet.R;
import com.phonebank.ripplewallet.RippleWalletActivity;
import com.phonebank.ripplewallet.View_Account;
import com.phonebank.ripplewallet.WalletArrayAdapter;

/**
 * Wallet List screen
 * @author Tony Gaitatzis
 *
 */
public class Fragment_Wallet extends Fragment implements RippleWalletActivity.BankResponseListener {

	private RippleBank rippleBank;
	private View fragmentView;

	private View_Account accountView;
	private ListView walletList;

	String ripple_address;
	
	boolean walletFetched = false;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		((RippleWalletActivity)getActivity()).showTabs();
		
		ripple_address = ((RippleWalletActivity)getActivity()).getAccountAddressFromPrefs();

		rippleBank = ((RippleWalletActivity)getActivity()).getRippleBank();
		
		// Inflate the layout for this fragment
		View fragmentView = inflater.inflate(R.layout.fragment_wallet,
				container, false);
		this.fragmentView = fragmentView;
		findUIElements();

		

		if (rippleBank.getAccount() != null) {
			updateWalletList();
		}

		return fragmentView;

	}
	
	@Override
	public void onResume() {
		super.onResume();
		accountView.setVisibility(View.VISIBLE);
		((RippleWalletActivity)getActivity()).setBankResponseListener(this);
		Log.v("Wallet ARGH","resuming wallet fragment");
		if (rippleBank.isConnected()) {
			fetchAccountInfo();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	public void findUIElements() {
		walletList = (ListView) fragmentView.findViewById(R.id.walletList);

		accountView = (View_Account)getActivity().findViewById(R.id.accountView);
	}
	
	public void updateWalletList() {
		ArrayList<RippleWallet> wallets = rippleBank.getAccount().getWallets();
		WalletArrayAdapter walletListAdapter = new WalletArrayAdapter(
				this.getActivity(), wallets);
		Log.v("Wallet","NOW HAVE "+wallets.size()+" WALLETS.  UPDATING UI");
		walletList.setAdapter(walletListAdapter);
	}
	
	// we want to grab the amount of currencies in the account
	public void fetchAccountInfo() {
		try {
			Log.v("Wallet","attempting to fetch address"+ripple_address);
			rippleBank.fetchAccountInfo(ripple_address);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//Notifier.error(getActivity(), e.getLocalizedMessage());
			e.printStackTrace();
		}
	}


	@Override
	public void onRippleAccountRetrieved(RippleAccount rippleAccount) {
		try {
			rippleBank.fetchAccountLines(ripple_address);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void onUserAccountWalletsListRetrieved(RippleWallet[] wallets) {
		RippleAccount account = rippleBank.getAccount();
		Log.v("Wallet","adding wallets to "+account.toString());
  	    for(int i = 0 ; i < wallets.length; i++) {
  	    	account.addWallet(wallets[i]);
  	    }
  	    
		updateWalletList();
		walletFetched = true;
	}

	@Override
	public void onTransactionSigned(int transactionID, String tx_blob) {		
	}


	@Override
	public void onTransactionSubmitted() {
	}

	@Override
	public void onConnected() {
		if (!walletFetched) {
			fetchAccountInfo();
		}
		
	}

}
