package com.phonebank.ripplewallet;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.widget.Toast;

/**
 * Handle Tab clicking 
 * @author Tony Gaitatzis
 *
 */
class MyTabsListener implements ActionBar.TabListener {
	public Fragment fragment;

	public MyTabsListener(Fragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
        /*
		Toast.makeText(RippleWalletActivity.appContext, "Reselected!",
				Toast.LENGTH_LONG).show();
				*/
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		//ft.addToBackStack("one");
		ft.replace(R.id.fragment_container, fragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		ft.remove(fragment);
	}

}