package com.zsxj.pda.ui.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zsxj.pda.R;
import com.zsxj.pda.util.ConstParams.Extras;
import com.zsxj.pda.util.ConstParams.PrefKeys;
import com.zsxj.pda.util.Globals;

public class SettingsActivity extends ActionBarActivity {
	
	private ListView mSettingListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.settings);
		
		mSettingListView = (ListView) findViewById(R.id.setting_list);
		mSettingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onSettingListItemClick(position);
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		String[] from = {
				"label",
				"value"
		};
		int[] to = { 
				R.id.label_tv,
				R.id.value_tv
		};
		
		SharedPreferences prefs = getSharedPreferences(Globals.getUserPrefsName(), MODE_PRIVATE);
		String warehouseName = prefs.getString(PrefKeys.WAREHOUSE_NAME, null);
		List<Map<String, String>> fillMaps = new ArrayList<Map<String, String>>();
		Map<String, String> map = new HashMap<String, String>();
		map.put("label", "当前仓库：");
		map.put("value", warehouseName);
		fillMaps.add(map);
		map = new HashMap<String, String>();
		map.put("label", "数据同步");
		fillMaps.add(map);
		map = new HashMap<String, String>();
		map.put("label", "当前版本：");
		map.put("value", getString(R.string.app_version_name));
		fillMaps.add(map);
		map = new HashMap<String, String>();
		map.put("label", getString(R.string.logout));
		fillMaps.add(map);
		SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.lable_value_row, from, to);
		mSettingListView.setAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void onSettingListItemClick(int position) {
		switch (position) {
		case 0:
			Intent selectWarehouse = new Intent(this, SelectWarehouseActivity.class);
			selectWarehouse.putExtra(Extras.RESET_WAREHOUSE, true);
			startActivity(selectWarehouse);
			break;
		case 1:
			Intent startSync = new Intent(this, SyncDataActivity.class);
			startActivity(startSync);
			break;
		case 2:
			break;
		case 3:
			promptLogout();
			break;
		default:
			break;
		}
	}
	
	private void promptLogout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.confirm_logout)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					logout();
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}
	
	private void logout() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PrefKeys.SELLER_NICK, "");
		editor.putString(PrefKeys.USER_NAME, "");
		editor.putString(PrefKeys.PASSWORD, "");
		editor.commit();

		// Go to login activity
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);

		this.finish();
		MainActivity.getInstance().finish();
	}
}
