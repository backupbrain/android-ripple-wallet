package com.phonebank.ripple;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Ripple Accounts
 * @author Tony Gaitatzis
 * @editor Shiyao Qi
 * @date 2013.12.22
 * @email qishiyao2008@126.com
 */
public class RippleAccount {

	public static final String ID_ACCOUNT_ADDRESS = "Account"; // Account address constant.
	public static final String ID_BALANCE = "Balance"; // Account balance constant.
	
	private String accountAddress = "";
	private String accountSecret = "";
	private int balance = 0;
	
	private ArrayList<RippleWallet> wallets = new ArrayList<RippleWallet>();
	
	public RippleAccount() {
		
	}
	
	/**
	 * Contruct a Ripple wallet from JSONObject.
	 * @param json The JSONObject that contains the account address and balance.
	 * @return RippleAccount.
	 * @throws JSONException
	 */
	public static RippleAccount fromJSON(JSONObject json) throws JSONException {
		
		// Set the RippleAccount.
		RippleAccount account = new RippleAccount();
		account.setAccountAddress(json.getString(ID_ACCOUNT_ADDRESS));
		account.setBalance(json.getInt(ID_BALANCE));
		
		// account JSON typically contains a wallet
		RippleWallet wallet = RippleWallet.fromAccountJSON(json);
		account.addWallet(wallet); // Add the RippleWallet to account.
		
		return account;
	}

	public void addWallet(RippleWallet wallet) {
		wallets.add(wallet);
	}
	
	public ArrayList<RippleWallet> getWallets() {
		return wallets;
	}
	
	public RippleWallet getWallet(int position) {
		return wallets.get(position);
	}
	
	public void removeWallet(int position) {
		wallets.remove(position);
	}
	
	public void removeWallet(RippleWallet wallet) {
		wallets.remove(wallet);
	}
	
	public void setAccountAddress(String accountAddress) {
		this.accountAddress = accountAddress;
	}
	
	public String getAccountAddress() {
		return accountAddress;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}
	
	public int getBalance() {
		return balance;
	}

	public void setAccountSecret(String accountSecret) {
		this.accountSecret = accountSecret;
	}
	
	public String getAccountSecret() {
		return accountSecret;
	}

	/**
	 * Get all the wallets and convert them to JSONObject.
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(ID_ACCOUNT_ADDRESS, getAccountAddress()); // Add account address.
		json.put(ID_BALANCE, getBalance()); // Add account balance.
		
		ArrayList<RippleWallet> foundWallets = getWallets();
		ArrayList<JSONObject> jsonWallets = new ArrayList<JSONObject>();
		Iterator<RippleWallet> walletIterator = foundWallets.iterator();
		while (walletIterator.hasNext()) {
			jsonWallets.add(walletIterator.next().toJSON());
		}
		json.put("wallets", jsonWallets); // Add wallets.
		
		return json;
	}

}


