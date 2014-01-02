package com.phonebank.ripplewallet.fragments;

import org.json.JSONException;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.phonebank.ripple.RippleAccount;
import com.phonebank.ripple.RippleBank;
import com.phonebank.ripple.RippleWallet;
import com.phonebank.ripplewallet.Notifier;
import com.phonebank.ripplewallet.R;
import com.phonebank.ripplewallet.RippleWalletActivity;
import com.phonebank.ripplewallet.RippleWalletActivity.BankResponseListener;
import com.phonebank.ripplewallet.View_Account;
import com.phonebank.ripplewallet.View_CameraPreview;

/**
 * Send money screen
 * @author Tony Gaitatzis
 *
 */
public class Fragment_Send extends Fragment implements OnClickListener, AdapterView.OnItemSelectedListener, BankResponseListener, View_CameraPreview.ScanListener {
	private View fragmentView;
	private Button buttonConfirm, buttonCancel, buttonScanDestinationAddress;
	private EditText editAmount, editDestinationAddress;
    private Spinner spinnerCurrency;
	private View_CameraPreview scannerPane;
	
	private RippleBank rippleBank;
	private RippleAccount account;
	private View_Account accountView;

    private String currency = RippleBank.CURRENCY_DEFAULT;
    private String[] currencies = new String[5];
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		((RippleWalletActivity)getActivity()).showTabs();
		// Inflate the layout for this fragment
		fragmentView = inflater.inflate(R.layout.fragment_send, container, false);

		buttonConfirm = (Button)fragmentView.findViewById(R.id.button_confirm);
		buttonConfirm.setOnClickListener(this);
		buttonCancel = (Button)fragmentView.findViewById(R.id.button_cancel);
		buttonCancel.setOnClickListener(this);
		buttonScanDestinationAddress = (Button)fragmentView.findViewById(R.id.button_scanDestinationAddress);
		buttonScanDestinationAddress.setOnClickListener(this);

        editAmount = (EditText)fragmentView.findViewById(R.id.edit_amount);
		editDestinationAddress = (EditText)fragmentView.findViewById(R.id.edit_destinationAddress);

		scannerPane = (View_CameraPreview)fragmentView.findViewById(R.id.camerapreview);
		
		rippleBank = ((RippleWalletActivity)getActivity()).getRippleBank();
		account = rippleBank.getAccount();
		

		accountView = (View_Account)getActivity().findViewById(R.id.accountView);


        spinnerCurrency = (Spinner)fragmentView.findViewById(R.id.spinner_currency);
        // set up the spinner
        currencies[0] = RippleBank.CURRENCY_XRP;
        currencies[1] = RippleBank.CURRENCY_BTC;
        currencies[2] = RippleBank.CURRENCY_USD;
        currencies[3] = RippleBank.CURRENCY_CAD;
        currencies[4] = RippleBank.CURRENCY_EUR;

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, currencies);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(currencyAdapter);
		
		return fragmentView;
	}
	

	
	@Override
	public void onResume() {
		super.onResume();
		accountView.setVisibility(View.VISIBLE);
		((RippleWalletActivity)getActivity()).setBankResponseListener(this);
        // figure out which currency is selected
        int currencyPos = 0;
        for (int i=0; i < currencies.length; i++) {
            if (currencies[i] == currency) {
                currencyPos = i;
            }
        }
        spinnerCurrency.setSelection(currencyPos);
	}

	@Override
	public void onPause() {
		super.onPause();
		scannerPane.stopPreview();
	}

	
	public void resetForm() {
		editAmount.setText("");
		editDestinationAddress.setText("");
	}
	
	public void submitForm() {
		// check values

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String accountSecret = prefs.getString(RippleWalletActivity.PREFS_KEY_RIPPLE_SECRET, "");
		
		String fromAccountAddress = ((RippleWalletActivity)getActivity()).getAccountAddressFromPrefs();
		
		
		String editAmountText = editAmount.getText().toString();
		if (editAmountText.equals("")) {
			editAmountText = "0";
		}

        int amount = Integer.valueOf(editAmountText);
        if (currency == RippleBank.CURRENCY_XRP) {
		    // rippled server talks about XRP without decimal places
		    amount = amount * RippleBank.XRP_DECIMAL_OFFSET;
        }
		String destinationAddress = editDestinationAddress.getText().toString();
		
		try {
            if (currency == RippleBank.CURRENCY_XRP) {
                rippleBank.signTransaction(fromAccountAddress, accountSecret, amount, destinationAddress);
            } else {
                rippleBank.signTransaction(fromAccountAddress, accountSecret, amount, currency, fromAccountAddress, destinationAddress);
            }
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_confirm:
			submitForm();
		break;
		case R.id.button_cancel:
			resetForm();
		break;
		case R.id.button_scanDestinationAddress:
			startCameraPreview();
		break;
		default:
			
		}
		
		
	}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // set the current currency
        currency = currencies[pos];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

	
	public void startCameraPreview() {
		scannerPane.startPreview(this);
	}

	@Override
	public void onRippleAccountRetrieved(RippleAccount rippleAccount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUserAccountWalletsListRetrieved(RippleWallet[] wallets) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTransactionSigned(int transactionID, String tx_blob) {
		// TODO Auto-generated method stub
		Notifier.notify((RippleWalletActivity)getActivity(), "Transaction signed");
		Log.v("Wallet","Transaction signed! "+tx_blob);
		// submit the transaction
		try {
			rippleBank.submitTransaction(tx_blob);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onTransactionSubmitted() {
		Notifier.notify((RippleWalletActivity)getActivity(), "Transaction submitted");
		Log.v("Wallet","Transaction submitted! ");
		resetForm();
		
	}


	@Override
	public void handleScan(String message) {
		String address = message;
		// if this has an http:// in it, we need to extract the ripple address
		String search = "to=";
		if (!address.startsWith("r")) {
			address = address.substring(address.indexOf(search)+search.length(),address.length());
		}
		editDestinationAddress.setText(address);
		
	}



	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
		
	}



	

}
