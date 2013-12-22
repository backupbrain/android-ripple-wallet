package com.phonebank.ripplewallet;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Self-defined Account View.
 * @author Tony Gaitatzis
 * @editor Shiyao Qi
 * @date 2013.12.22
 * @email qishiyao2008@126.com
 *
 */
public class View_Account extends LinearLayout  {

	Context context;

	private TextView accountAddress; // Ripple account address.

	public View_Account(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.View_Account, 0, 0); // Get the attribute information.
		a.recycle();

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_account, this, true); // Inflate the view_account.

		accountAddress = (TextView) findViewById(R.id.accountAddress); // Ripple account address.
	}

	public View_Account(Context context) {
		this(context, null);
	}
	
	/**
	 * Set account address.
	 * @param accountAddress The Ripple address to set.
	 */
	public void setAccountAddress(String accountAddress) {
		this.accountAddress.setText(accountAddress);
	}
	
	/**
	 * Get the account address.
	 * @return Account address.
	 */
	public CharSequence getAccountAddress() {
		return accountAddress.getText();
	}
}