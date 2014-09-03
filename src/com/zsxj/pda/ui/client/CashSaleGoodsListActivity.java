package com.zsxj.pda.ui.client;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zsxj.pda.R;
import com.zsxj.pda.provider.ProviderContract.CashSaleGoods;
import com.zsxj.pda.service.ScanService;
import com.zsxj.pda.service.ScanService.LocalBinder;
import com.zsxj.pda.util.ArithUtil;
import com.zsxj.pda.util.ConstParams.Extras;
import com.zsxj.pda.util.ConstParams.ScanType;
import com.zsxj.pda.util.Globals;
import com.zsxj.pda.util.Util;

public class CashSaleGoodsListActivity extends ActionBarActivity {
	
	private TextView mTotalTv;
	private TextView mGoodsTotalTv;
	private ListView mGoodsListView;
	private TextView mEmptyTextView;
	
	private Cursor mCursor = null;
	
	private double mNoDiscountTotal;
	private double mDiscountTotal;
	
	private String mBarcode;
	private ScanService mScanService = null;
	private IntentFilter mScanIntentFilter = new IntentFilter(ScanService.SCAN_OVER_ACTION);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cash_sale_goods_list_activity);
		
		mTotalTv = (TextView) findViewById(R.id.total_tv);
		mGoodsTotalTv = (TextView) findViewById(R.id.goods_total_tv);
		mGoodsListView = (ListView) findViewById(R.id.goods_list);
		mEmptyTextView = (TextView) findViewById(R.id.empty_text);
		
		mGoodsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCursor.moveToPosition(position);
				
				int specId = mCursor.getInt(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_SPEC_ID));
				String specBarcode = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_SPEC_BARCODE));
				String goodsNum = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_GOODS_NUM));
				String goodsName = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_GOODS_NAME));
				String specCode = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_SPEC_CODE));
				String specName = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_SPEC_NAME));
				String countStr = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_COUNT));
				String retailPrice = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_RETAILE_PRICE));
				String wholesalePrice = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_WHOLESALE_PRICE));
				String memberPrice = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_MEMBER_PRICE));
				String purchasePrice = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_PURCHASE_PRICE));
				String price1 = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_PRICE_1));
				String price2 = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_PRICE_2));
				String price3 = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_PRICE_3));
				String discountStr = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_DISCOUNT));
				String stock = mCursor.getString(mCursor.getColumnIndex(CashSaleGoods.COLUMN_NAME_CASH_SALE_STOCK));
				String barcode = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_BARCODE));
				
				Intent intent = new Intent(CashSaleGoodsListActivity.this,CashSaleGoodsActivity.class);
				intent.putExtra(Extras.SPEC_ID, specId);
				intent.putExtra(Extras.SPEC_BARCODE, specBarcode);
				intent.putExtra(Extras.GOODS_NUM, goodsNum);
				intent.putExtra(Extras.GOODS_NAME, goodsName);
				intent.putExtra(Extras.SPEC_CODE, specCode);
				intent.putExtra(Extras.SPEC_NAME, specName);
				intent.putExtra(Extras.COUNT, countStr);
				intent.putExtra(Extras.RETAIL_PRICE, retailPrice);
				intent.putExtra(Extras.WHOLESALE_PRICE, wholesalePrice);
				intent.putExtra(Extras.MEMBER_PRICE, memberPrice);
				intent.putExtra(Extras.PURCHASE_PRICE, purchasePrice);
				intent.putExtra(Extras.PRICE_1, price1);
				intent.putExtra(Extras.PRICE_2, price2);
				intent.putExtra(Extras.PRICE_3, price3);
				intent.putExtra(Extras.DISCOUNT, discountStr);
				intent.putExtra(Extras.CASH_SALE_STOCK, stock);
				intent.putExtra(Extras.BARCODE, barcode);
				startActivity(intent);
			}
		});
		mGoodsListView.setEmptyView(mEmptyTextView);
		registerForContextMenu(mGoodsListView);
		
		Intent intent = new Intent(this, ScanService.class);
		bindService(intent, mConnection, BIND_AUTO_CREATE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(
			R.menu.cash_sale_list_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.submit_action:
			startSubmit();
			break;
		case R.id.clear_order_action:
			clearOrder();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.delete_action_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo(); 
		switch (item.getItemId()) {
		case R.id.action_delete:
			deleteItem(info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (ScanService.KEY_F1 == keyCode && event.getRepeatCount() == 0 && mScanService != null) {
			mScanService.triggerOn();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (ScanService.KEY_F1 == keyCode && mScanService != null) {
			mScanService.triggerOff();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();	
		requery();
		countInfo();
		LocalBroadcastManager.getInstance(this).registerReceiver(mBarcodeReceiver, mScanIntentFilter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBarcodeReceiver);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		mCursor.close();
		super.onDestroy();
	}
	
	private void clearOrder() {
		String selection = CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID + "=?";
		String[] selectionArgs = {
				Globals.getModuleUseWarehouseId() + ""
		};
		getContentResolver().delete(CashSaleGoods.CONTENT_URI, 
			selection, selectionArgs);
		requery();
	}

	private void deleteItem(long id) {
		getContentResolver().delete(
			ContentUris.withAppendedId(CashSaleGoods.CONTENT_URI, id), 
			null, null);
		String selection = CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID + "=?";
		String[] selectionArgs = {
				Globals.getModuleUseWarehouseId() + ""
		};
		if (null != mCursor)
			mCursor.close();
		mCursor = getContentResolver().query(CashSaleGoods.CONTENT_URI, 
				null, selection, selectionArgs, null);
		CursorAdapter adapter = new MyListAdapter(this, mCursor);
		mGoodsListView.setAdapter(adapter);
		
		countInfo();
	}

	private void startSubmit() {
		if (mGoodsListView.getAdapter().getCount() == 0) {
			Util.toast(getApplicationContext(), "没有可提交的条目");
			return;
		}
		Intent intent = new Intent(this, CashSaleSubmitActivity.class);
		intent.putExtra(Extras.NO_DISCOUNT_TOTAL, mNoDiscountTotal);
		intent.putExtra(Extras.DISCOUNT_TOTAL, mDiscountTotal);
		startActivity(intent);
	}

	private void countInfo() {
			mCursor.moveToPosition(-1);
			int total = 0;
			double noDiscountTotal = 0d;
			double discountTotal = 0d;
			while (mCursor.moveToNext()) {
				String countStr = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_COUNT));
				int count = Integer.parseInt(countStr);
				total += count;
				String unitPriceStr = null;
				switch (Globals.getWhichPrice()) {
				case 0:
					unitPriceStr = mCursor.getString(mCursor.getColumnIndex(
						CashSaleGoods.COLUMN_NAME_RETAILE_PRICE));
					break;
				case 1:
					unitPriceStr = mCursor.getString(mCursor.getColumnIndex(
						CashSaleGoods.COLUMN_NAME_WHOLESALE_PRICE));
					break;
				case 2:
					unitPriceStr = mCursor.getString(mCursor.getColumnIndex(
						CashSaleGoods.COLUMN_NAME_MEMBER_PRICE));
					break;
				case 3:
					unitPriceStr = mCursor.getString(mCursor.getColumnIndex(
						CashSaleGoods.COLUMN_NAME_PURCHASE_PRICE));
					break;
				case 4:
					unitPriceStr = mCursor.getString(mCursor.getColumnIndex(
						CashSaleGoods.COLUMN_NAME_PRICE_1));
					break;
				case 5:
					unitPriceStr = mCursor.getString(mCursor.getColumnIndex(
						CashSaleGoods.COLUMN_NAME_PRICE_2));
					break;
				case 6:
					unitPriceStr = mCursor.getString(mCursor.getColumnIndex(
						CashSaleGoods.COLUMN_NAME_PRICE_3));
					break;
				default:
					break;
				}
				String discountStr = mCursor.getString(mCursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_DISCOUNT));
				double unitPrice = Double.parseDouble(unitPriceStr);
				double discount = Double.parseDouble(discountStr);
				
	//			double noDiscount = unitPrice * count;
				double noDiscount = ArithUtil.mul(unitPrice, count);
	//			noDiscountTotal += noDiscount;
				noDiscountTotal = ArithUtil.add(noDiscountTotal, noDiscount);
	//			double discountMoney = noDiscount * (1 - discount);
				double antiDiscount = ArithUtil.sub(1, discount);
				double discountMoney = ArithUtil.mul(noDiscount, antiDiscount);
	//			discountTotal += discountMoney;
				discountTotal = ArithUtil.add(discountTotal, discountMoney);
			}
			mCursor.moveToPosition(-1);
			
			mTotalTv.setText(String.valueOf(total));
	
			mNoDiscountTotal = noDiscountTotal;
			mDiscountTotal = discountTotal;
			double goodsTotal = noDiscountTotal - discountTotal;
			mGoodsTotalTv.setText(String.format("%.2f 元", goodsTotal));
		}

	private void requery() {
		String selection = CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID + "=?";
		String[] selectionArgs = {
				Globals.getModuleUseWarehouseId() + ""
		};
		if (null != mCursor)
			mCursor.close();
		mCursor = getContentResolver().query(CashSaleGoods.CONTENT_URI, 
				null, selection, selectionArgs, null);
		CursorAdapter adapter = new MyListAdapter(this, mCursor);
		mGoodsListView.setAdapter(adapter);
	}

	private void scanAndList() {
		Intent intent = new Intent(getApplicationContext(), 
			ScanAndListActivity.class);
		intent.putExtra(Extras.BARCODE, mBarcode);
		intent.putExtra(Extras.SCAN_TYPE, ScanType.TYPE_CASH_SALE);
		startActivity(intent);
	}
	
	private class MyListAdapter extends CursorAdapter {
		
		private LayoutInflater mInflater;

		public MyListAdapter(Context context, Cursor c) {
			super(context, c, false);
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView specBarcodeTv = (TextView) 
				view.findViewById(R.id.spec_barcode_tv);
			specBarcodeTv.setText(cursor.getString(
				cursor.getColumnIndex(
				CashSaleGoods.COLUMN_NAME_SPEC_BARCODE)));
			
			TextView goodsNumTv = (TextView) 
				view.findViewById(R.id.goods_num_tv);
			goodsNumTv.setText(cursor.getString(
				cursor.getColumnIndex(
				CashSaleGoods.COLUMN_NAME_GOODS_NUM)));
			
			TextView goodsNameTv = (TextView)
				view.findViewById(R.id.goods_name_tv);
			goodsNameTv.setText(cursor.getString(
				cursor.getColumnIndex(
				CashSaleGoods.COLUMN_NAME_GOODS_NAME)));
			
			TextView specCodeTv = (TextView)
				view.findViewById(R.id.spec_code_tv);
			specCodeTv.setText(cursor.getString(
				cursor.getColumnIndex(
				CashSaleGoods.COLUMN_NAME_SPEC_CODE)));
			
			TextView specNameTv = (TextView)
				view.findViewById(R.id.spec_name_tv);
			specNameTv.setText(cursor.getString(
				cursor.getColumnIndex(
				CashSaleGoods.COLUMN_NAME_SPEC_NAME)));
			
			TextView stockTv = (TextView) view.findViewById(R.id.stock_can_order_btn);
			stockTv.setText(cursor.getString(cursor.getColumnIndex(CashSaleGoods.COLUMN_NAME_CASH_SALE_STOCK)));
			
			String countStr = cursor.getString(
				cursor.getColumnIndex(
				CashSaleGoods.COLUMN_NAME_COUNT));
			TextView countTv = (TextView)
				view.findViewById(R.id.count_tv);
			countTv.setText(countStr);
			
			DecimalFormat df = new DecimalFormat("#.####");
			df.setRoundingMode(RoundingMode.HALF_UP);

			String unitPriceStr = null;
			switch (Globals.getWhichPrice()) {
			case 0:
				unitPriceStr = cursor.getString(cursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_RETAILE_PRICE));
				break;
			case 1:
				unitPriceStr = cursor.getString(cursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_WHOLESALE_PRICE));
				break;
			case 2:
				unitPriceStr = cursor.getString(cursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_MEMBER_PRICE));
				break;
			case 3:
				unitPriceStr = cursor.getString(cursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_PURCHASE_PRICE));
				break;
			case 4:
				unitPriceStr = cursor.getString(cursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_PRICE_1));
				break;
			case 5:
				unitPriceStr = cursor.getString(cursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_PRICE_2));
				break;
			case 6:
				unitPriceStr = cursor.getString(cursor.getColumnIndex(
					CashSaleGoods.COLUMN_NAME_PRICE_3));
				break;
			default:
				break;
			}
		
			TextView unitPriceTv = (TextView)
				view.findViewById(R.id.unit_price_tv);
			
			String discountStr = cursor.getString(
				cursor.getColumnIndex(
				CashSaleGoods.COLUMN_NAME_DISCOUNT));
			TextView discountTv = (TextView)
				view.findViewById(R.id.discount_tv);
			
			int count = Integer.parseInt(countStr);
			Double unitPrice = Double.parseDouble(unitPriceStr);
			Double discount = Double.parseDouble(discountStr);
			Double price = count * unitPrice * discount;
			TextView priceTv = (TextView)
				view.findViewById(R.id.price_tv);
			
			unitPriceTv.setText(df.format(unitPrice));
			discountTv.setText(df.format(discount));
			priceTv.setText(df.format(price) + " 元");
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			 View view = mInflater.inflate(
				 R.layout.cash_sale_goods_list_item, parent, false);
             bindView(view, context, cursor);
             return view;
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mScanService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder  binder = (LocalBinder) service;
			mScanService = binder.getService();
		}
	};
	
	private BroadcastReceiver mBarcodeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScanService.SCAN_OVER_ACTION)) {
				String barcode = intent.getStringExtra(ScanService.EXTRA_BARCODE);
				mBarcode = barcode;
				scanAndList();
			}
		}
	};
}
