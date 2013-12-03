package com.phonebank.ripple.transactions;

import org.json.JSONException;

import com.phonebank.ripple.Transaction;

/**
 * Ripple PaymentTransaction
 * @author Tony Gaitatzis
 *
 */
public class PaymentTransaction extends Transaction {

	public PaymentTransaction() throws JSONException {
		super(Transaction.TXTYPE_PAYMENT);
	}
	public PaymentTransaction(String jsontext) throws JSONException {
		super(Transaction.TXTYPE_PAYMENT);
		// import the content
		
	}
	
	public void setSendingAccount(String senderAccount) throws JSONException {
		put(Transaction.KEY_ACCOUNT, senderAccount);
	}
	public String getSenderAccount() throws JSONException { 
		return getString(Transaction.KEY_ACCOUNT); 
	}
	

	public void setDestinationAccount(String senderAccount) throws JSONException {
		put(Transaction.KEY_DESTINATION, senderAccount);
	}
	public String getDestinationAccount() throws JSONException { 
		return getString(Transaction.KEY_DESTINATION); 
	}
	

	public void setAmount(long amount) throws JSONException {
		put(Transaction.KEY_AMOUNT, amount);
	}
	public long getAmount() throws JSONException { 
		return getLong(Transaction.KEY_AMOUNT); 
	}
}
