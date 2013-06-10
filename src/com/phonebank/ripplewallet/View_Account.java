package com.phonebank.ripplewallet;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Account Information
 * @author Tony Gaitatzis
 *
 */
public class View_Account extends LinearLayout  {

	Context context;

	private TextView accountAddress;


	public View_Account(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.View_Account, 0, 0);
		a.recycle();

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_account, this, true);

		accountAddress = (TextView) findViewById(R.id.accountAddress);
	}

	public View_Account(Context context) {
		this(context, null);
	}

	public void setAccountAddress(String accountAddress) {
		this.accountAddress.setText(accountAddress);
	}
	public CharSequence getAccountAddress() {
		return accountAddress.getText();
	}
	

}