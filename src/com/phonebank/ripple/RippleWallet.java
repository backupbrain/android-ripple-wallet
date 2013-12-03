package com.phonebank.ripple;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Ripple wallet -contains currencies
 * @author Tony Gaitatzis
 *
 */
public class RippleWallet {

	public static final String ID_ACCOUNT = "account";
	public static final String ID_BALANCE = "balance";
	public static final String ID_CURRENCY = "currency";
	public static final String ID_LIMIT = "limit";
	public static final String ID_LIMIT_PEER = "limit_peer";
	public static final String ID_QUALITY_IN = "quality_in";
	public static final String ID_QUALITY_OUT = "quality_out";
	
	private String currency;
	private double balance = 0.0d;
	private String address;
	
	public RippleWallet() {
		
	}
	
	public static RippleWallet fromJSON(JSONObject json) throws JSONException {
		RippleWallet wallet = new RippleWallet();
		wallet.setAddress(json.getString(ID_ACCOUNT));
		wallet.setBalance(json.getDouble(ID_BALANCE));
		wallet.setCurrency(json.getString(ID_CURRENCY));
		return wallet;
		
	}
	public static RippleWallet fromAccountJSON(JSONObject json) throws JSONException {
		RippleWallet wallet = new RippleWallet();
		wallet.setAddress(json.getString(RippleAccount.ID_ACCOUNT_ADDRESS));
		wallet.setBalance(json.getDouble(RippleAccount.ID_BALANCE));
		wallet.setCurrency(RippleBank.CURRENCY_DEFAULT);
		return wallet;
		
	}

	public void setAddress(String address) {
		this.address = address;
	}
	public String getAddress() {
		return address;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getCurrency() {
		return currency;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public double getBalance() {
		double value = balance;
		if (getCurrency() == RippleBank.CURRENCY_XRP) {
			value = balance/RippleBank.XRP_DECIMAL_OFFSET;
		} 
		return value;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(ID_ACCOUNT, getAddress());
		json.put(ID_CURRENCY, getCurrency());
		json.put(ID_BALANCE, getBalance());
		return json;
	}

}
