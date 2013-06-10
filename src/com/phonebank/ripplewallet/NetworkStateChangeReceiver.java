package com.phonebank.ripplewallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Tells Android when network state has changed
 * @author tonyg
 *
 */
public class NetworkStateChangeReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {
		Log.d("app", "Network connectivity change");
		if (intent.getExtras() != null) {
			
			NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {

				//Notifier.notify(context, "Network " + ni.getTypeName() + " connected");
			}
		}
		if (intent.getExtras().getBoolean(
				ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {

			//Notifier.notify(context, "There's no network connectivity");
		}
	}
	
	
}