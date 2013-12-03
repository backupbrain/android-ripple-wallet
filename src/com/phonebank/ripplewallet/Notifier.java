package com.phonebank.ripplewallet;

import android.content.Context;
import android.widget.Toast;

/**
 * UI alerts
 * @author Tony Gaitatzis
 *
 */
public class Notifier {
	private Context context;
	public static final int TOAST_LENGTH = Toast.LENGTH_LONG;
	
	public Notifier(Context context) {
		this.context = context;
	}
	
	public void error(String message) {
		Toast.makeText(context, message, TOAST_LENGTH).show();
	}


	public void notify(String message) {
		Toast.makeText(context, message, TOAST_LENGTH).show();
	}
	public static void notify(RippleWalletActivity context, String message) { 
		Toast.makeText(context, message, Notifier.TOAST_LENGTH).show();	
	}

	public static void error(RippleWalletActivity context, String message) {
		Toast.makeText(context, message, Notifier.TOAST_LENGTH).show();	
	}

}
