package com.phonebank.ripplewallet;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.phonebank.ripple.RippleWallet;

/**
 * Formats groups of wallets to be displayed in a list
 * @author Tony Gaitatzis
 *
 */
public class WalletArrayAdapter extends BaseAdapter {

	private ArrayList<RippleWallet> wallets;
	private static LayoutInflater inflater = null;
	private static DecimalFormat df;

	public WalletArrayAdapter(Context context, ArrayList<RippleWallet> wallets) {
		this.wallets = wallets;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		df = new DecimalFormat(context.getString(R.string.currency_format));
	}

	public int getCount() {
		return wallets.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {
		public TextView balance;
		// public TextView account;
		public TextView account;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// for comparison of listViews, check
		// http://stackoverflow.com/questions/11722885/what-is-difference-between-android-r-layout-simple-list-item-1-and-android-r-lay
		View rowView = inflater
				.inflate(android.R.layout.simple_list_item_2, parent, false);
		TextView balance = (TextView) rowView.findViewById(android.R.id.text1);
		TextView account = (TextView) rowView.findViewById(android.R.id.text2);

		double account_balance = wallets.get(position).getBalance();
		balance.setText(df.format(account_balance)+" "+wallets.get(position).getCurrency());
		account.setText(wallets.get(position).getAddress());

		return rowView;

	}
}
