package com.phonebank.ripple;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Ripple LedgerEntry
 * @author tonyg
 *
 */
public class LedgerEntry {

	protected static final String KEY_ACCOUNT = "Account";
	protected static final String KEY_BOOK_DIRECTORY = "BookDirectory";
	protected static final String KEY_BOOK_NODE = "BookNode";
	protected static final String KEY_FLAGS = "Flags";
	protected static final String KEY_LEDGER_ENTRY_TYPE = "LedgerEntryType";
	protected static final String KEY_PREVIOUS_TXN_ID = "PreviousTxnID";
	protected static final String KEY_PREVIOUS_TXN_LGR_SEQ = "PreviousTxnLgrSeq";
	protected static final String KEY_SEQUENCE = "Sequence";
	protected static final String KEY_TAKER_GETS = "TakerGets";
	protected static final String KEY_TAKER_PAYS = "TakerPays";
	protected static final String KEY_INDEX = "index";
	protected static final String KEY_QUALITY = "quality";
	
	private String accountAddress;
	private String bookDirectory;
	private String bookNode;
	private String takerGets;
	private String takerPays;
	
	public LedgerEntry() {
		super();
	}
	public static LedgerEntry fromJSON(JSONObject json) throws JSONException {
		LedgerEntry ledger = new LedgerEntry();
		ledger.setBookDirectory(json.getString(KEY_BOOK_DIRECTORY));
		return ledger;
	}

	public void setAccountAddress(String accountAddress) {
		this.accountAddress = accountAddress;
	}
	public String getAccountAddress(){
		return accountAddress;
	}
	


	public void setBookDirectory(String bookDirectory) {
		this.bookDirectory = bookDirectory;
	}
	public String getBookDirectory(){
		return bookDirectory;
	}

	public void setBookNode(String bookNode) {
		this.bookNode = bookNode;
	}
	public String getBookNode(){
		return bookNode;
	}
	


	public void setTakerGets(String takerGets) {
		this.takerGets = takerGets;
	}
	public String getTakerGets(){
		return takerGets;
	}

	public void seTakerPays(String takerPays) {
		this.takerPays = takerPays;
	}
	public String getTakerPays(){
		return takerPays;
	}
	

	
	
}
