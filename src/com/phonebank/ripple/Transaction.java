package com.phonebank.ripple;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Ripple network transactions
 * @author Tony Gaitatzis
 *
 */
public class Transaction extends JSONObject {

	public static final String TXTYPE_PAYMENT = "Payment";
	public static final String TXTYPE_ACCOUNT_SET = "AccountSet";
	public static final String TXTYPE_REGULAR_KEY_SET = "RegularKeySet";
	public static final String TXTYPE_OFFER_CREATE = "OfferCreate";
	public static final String TXTYPE_OFFER_CANCEL = "OfferCancel";
	public static final String TXTYPE_OFFER_SIGN = "OfferSign";
	public static final String TXTYPE_TRUST_SET = "TrustSet";
	
	protected static final String KEY_TXTYPE = "TransactionType";
	protected static final String KEY_ACCOUNT = "Account";
	protected static final String KEY_DESTINATION = "Account";
	protected static final String KEY_AMOUNT = "Destination";
	
	public Transaction(String transactionType) throws JSONException {
		put(KEY_TXTYPE, transactionType);
	}
	public Transaction(String transactionType, String json) throws JSONException {
		super(json);
		put(KEY_TXTYPE, transactionType);
	}
	
	

	/*
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(TXTYPE_PAYMENT, getPayment());
		json.put(TXTYPE_ACCOUNT_SET, getAccountSet());
		json.put(TXTYPE_REGULAR_KEY_SET, getRegularKeySet());
		return json;
	}
	*/
	
}

