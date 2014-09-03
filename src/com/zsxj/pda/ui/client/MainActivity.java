package com.zsxj.pda.ui.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zsxj.pda.R;
import com.zsxj.pda.util.ConstParams.Extras;
import com.zsxj.pda.util.ConstParams.ScanType;
import com.zsxj.pda.util.Globals;
import com.zsxj.pda.wdt.WDTQuery;


public class MainActivity extends ActionBarActivity {
	
	private static final Integer TAG_EMPTY = 0;
	private static final Integer TAG_TITLE = 1;
	
	private String[] TITLES;
	
	private ListView titlesLv;
	
	private static Activity instance;
	
	private int[] mModules;
	
	public static Activity getInstance() {
		return instance;
	};
	
	WDTQuery wdtQuery = new WDTQuery();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.function_list);
		actionBar.setDisplayHomeAsUpEnabled(false);
		
		TITLES = getResources().getStringArray(R.array.titles);
		mModules = Globals.getModules(this, Globals.getSellerNick());
		
		instance = this;
		titlesLv = (ListView) findViewById(R.id.titles_lv);
		titlesLv.setAdapter(new MyListAdapter(this));
		int warehouseId = getIntent().getIntExtra(Extras.WAREHOUSE_ID, -1);
		final Intent intent = new Intent();
		intent.putExtra(Extras.WAREHOUSE_ID, warehouseId);
		titlesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (mModules[position]) {
				case 0:
					intent.setClass(getApplicationContext(), ScanAndListActivity.class);
					intent.putExtra(Extras.SCAN_TYPE, ScanType.TYPE_FAST_PD);
					break;
				case 1:
					intent.setClass(getApplicationContext(), ScanAndListActivity.class);
					intent.putExtra(Extras.SCAN_TYPE, ScanType.TYPE_QUERY_SPECS);
					break;
				case 2:
					intent.setClass(getApplicationContext(), 
						FastInExamineGoodsSetActivity.class);
					break;
				case 3:
					intent.setClass(getApplicationContext(), ScanAndListActivity.class);
					intent.putExtra(Extras.SCAN_TYPE, ScanType.TYPE_PICK_GOODS);
					break;
				case 4:
					intent.setClass(getApplicationContext(), ScanAndListActivity.class);
					intent.putExtra(Extras.SCAN_TYPE, ScanType.TYPE_OUT_EXAMINE_GOODS);
					break;
				case 5:
					intent.setClass(getApplicationContext(), CashSaleSetActivity.class);
					break;
				default:
					intent.setClass(getBaseContext(), SettingsActivity.class);
					break;
				}
				startActivity(intent);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.check_quit);
		builder.setNegativeButton(android.R.string.cancel, null);
		final AlertDialog dialog = builder.create();
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), 
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				MainActivity.super.onBackPressed();
			}
		});
		dialog.show();
	}
	
	private class MyListAdapter extends BaseAdapter {
		
		private final Context mContext;

		public MyListAdapter(Context context) {
			mContext = context;
		}
		
		@Override
		public int getCount() {
			return mModules.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {	
			if (convertView == null || convertView.getTag() == TAG_EMPTY) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.simple_list_item_1, parent, false);
				convertView.setTag(TAG_TITLE);
			}
			
			TextView titleTv = (TextView) convertView.findViewById(R.id.item_tv);
			if (99 == mModules[position]) {
				titleTv.setText(R.string.settings);
			} else {
				titleTv.setText(TITLES[mModules[position]]);	
			}
			return convertView;
		}
		
	}
}
