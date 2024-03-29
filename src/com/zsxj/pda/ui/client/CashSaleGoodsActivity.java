package com.zsxj.pda.ui.client;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zsxj.pda.R;
import com.zsxj.pda.provider.ProviderContract.CashSaleGoods;
import com.zsxj.pda.service.ScanService;
import com.zsxj.pda.service.ScanService.LocalBinder;
import com.zsxj.pda.util.ConstParams.Extras;
import com.zsxj.pda.util.ConstParams.HandlerCases;
import com.zsxj.pda.util.ConstParams.ScanType;
import com.zsxj.pda.util.Globals;
import com.zsxj.pda.util.Util;
import com.zsxj.pda.wdt.WDTException;
import com.zsxj.pda.wdt.WDTQuery;
import com.zsxj.pda.wdt.WDTQuery.QueryCallBack;

public class CashSaleGoodsActivity extends ActionBarActivity implements QueryCallBack {

	private RelativeLayout mProgressLayout;
	private TextView mSpecBarcodeTv;
	private TextView mGoodsNumTv;
	private TextView mGoodsNameTv;
	private TextView mSpecCodeTv;
	private TextView mSpecNameTv;
	private EditText mCountEdit;
	private EditText mUnitPriceEdit;
	private EditText mDiscountEdit;
	private TextView mMoneyTv;
	private Button mStockCanOrderBtn;
	private Button mOkBtn;
	
	private int mSpecId;
	private String mSpecBarcode;
	private String mGoodsNum;
	private String mGoodsName;
	private String mSpecCode;
	private String mSpecName;
	private String mRetailPrice;
	private String mWholesalePrice;
	private String mMemberPrice;
	private String mPurchasePrice;
	private String mPrice1;
	private String mPrice2;
	private String mPrice3;
	private String mStockCanOrder;
	
	private String mBarcode;
	private ScanService mScanService = null;
	private IntentFilter mScanIntentFilter = new IntentFilter(ScanService.SCAN_OVER_ACTION);
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			mProgressLayout.setVisibility(View.GONE);
			switch (msg.what) {
			case HandlerCases.NO_CONN:
				Util.toast(getApplicationContext(), R.string.no_conn);
				break;
			case HandlerCases.PREPARE_QUERY_ERROR:
				Util.toast(getApplicationContext(), R.string.query_specs_failed);
				break;
			case HandlerCases.QUERY_FAIL:
				Util.toast(getApplicationContext(), R.string.query_specs_failed);
				break;
			case HandlerCases.EMPTY_RESULT:
				Util.toast(getApplicationContext(), "刷新可订购库存失败");
				break;
			case HandlerCases.QUERY_SUCCESS:
				dealData();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cash_sale_goods_activity);
		
		final ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle(R.string.cash_sale);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		final View rootView = findViewById(R.id.root);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				rootView.getWindowVisibleDisplayFrame(r);
				int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top); 
				if (heightDiff > 300) {
					getSupportActionBar().hide();
				} else {
					getSupportActionBar().show();
				}
			}
		});
		
		mProgressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
		
		mSpecId = getIntent().getIntExtra(Extras.SPEC_ID, -1);
		mSpecBarcode = getIntent().getStringExtra(Extras.SPEC_BARCODE);
		mGoodsNum = getIntent().getStringExtra(Extras.GOODS_NUM);
		mGoodsName = getIntent().getStringExtra(Extras.GOODS_NAME);
		mSpecCode = getIntent().getStringExtra(Extras.SPEC_CODE);
		mSpecName = getIntent().getStringExtra(Extras.SPEC_NAME);
		mRetailPrice = getIntent().getStringExtra(Extras.RETAIL_PRICE);
		mWholesalePrice = getIntent().getStringExtra(Extras.WHOLESALE_PRICE);
		mMemberPrice = getIntent().getStringExtra(Extras.MEMBER_PRICE);
		mPurchasePrice = getIntent().getStringExtra(Extras.PURCHASE_PRICE);
		mPrice1 = getIntent().getStringExtra(Extras.PRICE_1);
		mPrice2 = getIntent().getStringExtra(Extras.PRICE_2);
		mPrice3 = getIntent().getStringExtra(Extras.PRICE_3);
		mStockCanOrder = getIntent().getStringExtra(Extras.CASH_SALE_STOCK);
		mBarcode = getIntent().getStringExtra(Extras.BARCODE);
		
		mSpecBarcodeTv = (TextView) findViewById(R.id.spec_barcode_tv);
		mSpecBarcodeTv.setText(mSpecBarcode);
		
		mGoodsNumTv = (TextView) findViewById(R.id.goods_num_tv);
		mGoodsNumTv.setText(mGoodsNum);
		
		mGoodsNameTv = (TextView) findViewById(R.id.goods_name_tv);
		mGoodsNameTv.setText(mGoodsName);
		
		mSpecCodeTv = (TextView) findViewById(R.id.spec_code_tv);
		mSpecCodeTv.setText(mSpecCode);
		
		mSpecNameTv = (TextView) findViewById(R.id.spec_name_tv);
		mSpecNameTv.setText(mSpecName);
		
		mStockCanOrderBtn = (Button) findViewById(R.id.stock_can_order_btn);
		double stockCanOrder = Double.parseDouble(mStockCanOrder);
		mStockCanOrderBtn.setText((int) stockCanOrder + "");
		mStockCanOrderBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mProgressLayout.setVisibility(View.VISIBLE);
				WDTQuery.getinstance().queryCashSaleSpecStock(CashSaleGoodsActivity.this, 
					mSpecId, Globals.getModuleUseWarehouseId());
			}
		});

		mCountEdit = (EditText) findViewById(R.id.count_edit);
		String countStr = getIntent().getStringExtra(Extras.COUNT);
		if (!TextUtils.isEmpty(countStr))
			mCountEdit.setText(countStr);

		mUnitPriceEdit = (EditText) findViewById(R.id.unit_price_edit);
		double unitPrice = -1;
		switch (Globals.getWhichPrice()) {
		case 0:
            unitPrice = Double.parseDouble(mRetailPrice);
            break;
		case 1:
            unitPrice = Double.parseDouble(mWholesalePrice);
            break;
		case 2:
            unitPrice = Double.parseDouble(mMemberPrice);
            break;
		case 3:
            unitPrice = Double.parseDouble(mPurchasePrice);
            break;
		case 4:
			unitPrice = Double.parseDouble(mPrice1);
            break;
		case 5:
			unitPrice = Double.parseDouble(mPrice2);
            break;
		case 6:
			unitPrice = Double.parseDouble(mPrice3);
            break;
		default:
			break;
		}
		mUnitPriceEdit.setText(String.format("%.2f", unitPrice));

		mDiscountEdit = (EditText) findViewById(R.id.discount_edit);
		String discountStr = getIntent().getStringExtra(Extras.DISCOUNT);
		if (TextUtils.isEmpty(discountStr))
			mDiscountEdit.setText("1.0");
		else 
			mDiscountEdit.setText(discountStr);
		mMoneyTv = (TextView) findViewById(R.id.money_tv);
		mCountEdit.addTextChangedListener(new MyTextWatcher());
		mUnitPriceEdit.addTextChangedListener(new MyTextWatcher());
		mDiscountEdit.addTextChangedListener(new MyTextWatcher());
		countMoney();
		
		mOkBtn = (Button) findViewById(R.id.ok_btn);
		mOkBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                checkWriteEntry();
			}
		});
		
		Intent intent = new Intent(this, ScanService.class);
		bindService(intent, mConnection, BIND_AUTO_CREATE);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
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
		LocalBroadcastManager.getInstance(this).registerReceiver(mBarcodeReceiver, mScanIntentFilter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBarcodeReceiver);
	}
	
	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}
	
	private void checkWriteEntry() {
		String stockStr = mStockCanOrderBtn.getText().toString();
		String countStr = mCountEdit.getText().toString();
		
		if (TextUtils.isEmpty(countStr)) {
			deleteRow();
			Intent intent = new Intent(this, CashSaleGoodsListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return;
		} 
		
		Double count = Double.valueOf(countStr);
		if (count < 0) {
			Util.toast(getApplicationContext(), R.string.please_reenter_goods_count);
			return;
		} else if (count.equals(Double.valueOf(0))) {
			deleteRow();
			Intent intent = new Intent(this, CashSaleGoodsListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return;
		}
		
		Double stock = Double.valueOf(stockStr);
		if (count.compareTo(stock) > 0) {
		    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	builder.setMessage("可订购量不足，确定销售吗？")
		    		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				    
				    @Override
				    public void onClick(DialogInterface arg0, int arg1) {
					writeEntry();
				    }
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();	
		} else {
		   	writeEntry();
		}
	}
	
	private void writeEntry() {
		String countStr = mCountEdit.getText().toString();
		
		String unitPriceStr = mUnitPriceEdit.getText().toString();
		if (TextUtils.isEmpty(unitPriceStr) || unitPriceStr.equals(".")) {
			unitPriceStr = "0";
		}

        switch (Globals.getWhichPrice()) {
        case 0:
            mRetailPrice = unitPriceStr;
            break;
        case 1:
        	mWholesalePrice = unitPriceStr;
            break;
        case 2:
        	mMemberPrice = unitPriceStr;
        	break;
        case 3:
            mPurchasePrice = unitPriceStr;
            break;
        case 4:
            mPrice1 = unitPriceStr;
            break;
        case 5:
            mPrice2 = unitPriceStr;
            break;
        case 6:
            mPrice3 = unitPriceStr;
            break;
        default:
            break;
        }
		String discountStr = mDiscountEdit.getText().toString();
		if (TextUtils.isEmpty(discountStr) || discountStr.equals(".")) {
			discountStr = "0";
		}
		String stockCanOrderStr = mStockCanOrderBtn.getText().toString();
		
		String selection = CashSaleGoods.COLUMN_NAME_SPEC_ID + "=? AND " +
                        CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID + "=?";
		
		String[] selectionArgs = {
			mSpecId + "",
			Globals.getModuleUseWarehouseId() + ""
		};
		
		Cursor cursor = getContentResolver().query(CashSaleGoods.CONTENT_URI, 
			null, selection, selectionArgs, null);
		ContentValues values = new ContentValues();
		values.put(CashSaleGoods.COLUMN_NAME_SPEC_ID, mSpecId);
		values.put(CashSaleGoods.COLUMN_NAME_SPEC_BARCODE, mSpecBarcode);
		values.put(CashSaleGoods.COLUMN_NAME_GOODS_NUM, mGoodsNum);
		values.put(CashSaleGoods.COLUMN_NAME_GOODS_NAME, mGoodsName);
		values.put(CashSaleGoods.COLUMN_NAME_SPEC_CODE, mSpecCode);
		values.put(CashSaleGoods.COLUMN_NAME_SPEC_NAME, mSpecName);
		values.put(CashSaleGoods.COLUMN_NAME_COUNT, countStr);
		values.put(CashSaleGoods.COLUMN_NAME_RETAILE_PRICE, mRetailPrice);
		values.put(CashSaleGoods.COLUMN_NAME_WHOLESALE_PRICE, mWholesalePrice);
		values.put(CashSaleGoods.COLUMN_NAME_MEMBER_PRICE, mMemberPrice);
		values.put(CashSaleGoods.COLUMN_NAME_PURCHASE_PRICE, mPurchasePrice);
		values.put(CashSaleGoods.COLUMN_NAME_PRICE_1, mPrice1);
		values.put(CashSaleGoods.COLUMN_NAME_PRICE_2, mPrice2);
		values.put(CashSaleGoods.COLUMN_NAME_PRICE_3, mPrice3);
		values.put(CashSaleGoods.COLUMN_NAME_DISCOUNT, discountStr);
		values.put(CashSaleGoods.COLUMN_NAME_CASH_SALE_STOCK, stockCanOrderStr);
		values.put(CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID, Globals.getModuleUseWarehouseId());
		values.put(CashSaleGoods.COLUMN_NAME_BARCODE, mBarcode);
		if (0 == cursor.getCount()) {
			getContentResolver().insert(CashSaleGoods.CONTENT_URI, 
				values);
		} else {
			getContentResolver().update(CashSaleGoods.CONTENT_URI, 
				values, selection, selectionArgs);
		}
		
		Intent intent = new Intent(this, CashSaleGoodsListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	private void deleteRow() {
		int specId = getIntent().getIntExtra(Extras.SPEC_ID, -1);
		
		String where = CashSaleGoods.COLUMN_NAME_SPEC_ID + "=? AND " +
			CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID + "=?";
		String[] selectionArgs = {
			specId + "",
			Globals.getModuleUseWarehouseId() + ""
		};
		
		getContentResolver().delete(CashSaleGoods.CONTENT_URI, where, selectionArgs);
	}
	
	private void countMoney() {
		String quantityStr = mCountEdit.getText().toString();
		String unitPriceStr = mUnitPriceEdit.getText().toString();
		String discountStr = mDiscountEdit.getText().toString();
		if (TextUtils.isEmpty(quantityStr) ||
			TextUtils.isEmpty(unitPriceStr) ||
			TextUtils.isEmpty(discountStr) || 
			quantityStr.equals(".") || 
			unitPriceStr.equals(".") || 
			discountStr.equals(".")) {
			
			mMoneyTv.setText("0.00");
			return;
		}
		Double quantity = Double.parseDouble(quantityStr);
		Double unitPrice = Double.parseDouble(unitPriceStr);
		Double discount = Double.parseDouble(discountStr);
		Double money = quantity * unitPrice * discount;
		mMoneyTv.setText(String.format("%.2f", money));
	}
	
	private void dealData() {
		double stockCanOrder = Double.parseDouble(mStockCanOrder);
		mStockCanOrderBtn.setText((int) stockCanOrder + "");
		ContentValues values = new ContentValues();
		values.put(CashSaleGoods.COLUMN_NAME_CASH_SALE_STOCK, mStockCanOrder);
		String where = CashSaleGoods.COLUMN_NAME_SPEC_ID + "=? AND " + 
			CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID + "=?";
		String[] selectionArgs = {
			mSpecId + "",
			Globals.getModuleUseWarehouseId() + ""
		};
		getContentResolver().update(CashSaleGoods.CONTENT_URI, values, where, selectionArgs);
	}

	private class MyTextWatcher implements TextWatcher {
		

		@Override
		public void afterTextChanged(Editable s) {
			countMoney();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
		}
	}

	@Override
	public void onQuerySuccess(Object qr) {
		if (qr == null) {
			mHandler.sendEmptyMessage(HandlerCases.EMPTY_RESULT);
			return;
		}
		
		if (qr instanceof String) {
			mStockCanOrder = (String) qr;
			mHandler.sendEmptyMessage(HandlerCases.QUERY_SUCCESS);
		} else {
			throw new ClassCastException("Wrong query result type");
		}
	}

	@Override
	public void onQueryFail(int type, WDTException wdtEx) {
		if (null == wdtEx) {
			mHandler.sendEmptyMessage(type);
			return;
		}
		switch (wdtEx.getStatus()) {
		case 1064:
			mHandler.sendEmptyMessage(HandlerCases.QUERY_FAIL);
			break;
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
				
				Intent startScan = 
					new Intent(getApplicationContext(), ScanAndListActivity.class);
				startScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startScan.putExtra(Extras.BARCODE, mBarcode);
				startScan.putExtra(Extras.SCAN_TYPE, ScanType.TYPE_CASH_SALE);
				startActivity(startScan);
				finish();
			}
		}
	};
}
