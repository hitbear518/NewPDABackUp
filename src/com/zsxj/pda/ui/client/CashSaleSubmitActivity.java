package com.zsxj.pda.ui.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zsxj.pda.R;
import com.zsxj.pda.provider.ProviderContract.CashSaleGoods;
import com.zsxj.pda.util.ArithUtil;
import com.zsxj.pda.util.ConstParams.Extras;
import com.zsxj.pda.util.ConstParams.Interface;
import com.zsxj.pda.util.Globals;
import com.zsxj.pda.util.Util;
import com.zsxj.pda.wdt.Customer;

public class CashSaleSubmitActivity extends ActionBarActivity {
	
	private TextView mNoDiscountTotalTv;
	private TextView mDiscountTotalTv;
	private EditText mExtraDiscountEdit;
	private TextView mAllShouldPayTv;
	private EditText mCashFeeEdit;
	private TextView mChangeMoneyTv;
	private Button mOkBtn;
	private ProgressDialog mProgressDialog;

	private double mNoDiscountTotal;
	private double mDiscountTotal;
	private double mExtraDiscount;
	private double mAllShouldPay;
	
	private String mContent;
	private String mSign;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cash_sale_submit_activity);
		
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.cash_sale);
		
		final View rootView = findViewById(R.id.root);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				Rect r = new Rect();
				rootView.getWindowVisibleDisplayFrame(r);
				int heightDiff = rootView.getRootView().getHeight() - (r.bottom - r.top);
				if (heightDiff > 300) {
					actionBar.hide();
				} else {
					actionBar.show();
				}
			}
		});
		
		mNoDiscountTotalTv = (TextView) findViewById(R.id.goods_total_tv);
		mDiscountTotalTv = (TextView) findViewById(R.id.discount_sum_tv);
		mExtraDiscountEdit = (EditText) findViewById(R.id.extra_discount_edit);
		mAllShouldPayTv = (TextView) findViewById(R.id.all_should_pay_tv);
		mCashFeeEdit = (EditText) findViewById(R.id.cash_fee_edit);
		mChangeMoneyTv = (TextView) findViewById(R.id.change_money_tv);
		mOkBtn = (Button) findViewById(R.id.ok_btn);
		
		mNoDiscountTotal = getIntent().getDoubleExtra(Extras.NO_DISCOUNT_TOTAL, -1);
		mNoDiscountTotalTv.setText(String.format("%.2f", mNoDiscountTotal));

		mDiscountTotal = getIntent().getDoubleExtra(Extras.DISCOUNT_TOTAL, -1);
		mDiscountTotalTv.setText(String.format("%.2f", mDiscountTotal));

		mAllShouldPay = mNoDiscountTotal - mDiscountTotal;
		mAllShouldPayTv.setText(String.format("%.2f", mAllShouldPay));

		mChangeMoneyTv.setText(String.format("%.2f", 0 - mAllShouldPay));
		mExtraDiscountEdit.setText("0");
		mExtraDiscountEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				countPayAndChange();
			}
		});
		
		mCashFeeEdit.setText("0");
		mCashFeeEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				countPayAndChange();
			}
		});
		
		mOkBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submit();
			}
		});
		
		// Init progress dialog
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setTitle(R.string.please_wait);
		mProgressDialog.setMessage(getString(R.string.submit) + "...");
		mProgressDialog.setCancelable(false);
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
	
	private void submit() {
		
		if (mAllShouldPay < 0) {
			Util.toast(getApplicationContext(), "应收金额错误");
			return;
		}
		
		ConnectivityManager connMgr = (ConnectivityManager) 
	        getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.isConnected()) {
			mContent = getContent();
			Log.i("Content", mContent);
			mSign = getSign();
            new SubmitTask().execute();
	    } else {
	    	Util.toast(getApplicationContext(), R.string.no_conn);
	    }
	}
	
	private void countPayAndChange() {
		String extraDiscountStr = mExtraDiscountEdit.getText().toString();
		if (TextUtils.isEmpty(extraDiscountStr) || extraDiscountStr.equals("."))
			mExtraDiscount = 0d;
		else 
			mExtraDiscount = Double.parseDouble(extraDiscountStr);
		String cashFeeStr = mCashFeeEdit.getText().toString();
		double cashFee;
		if (TextUtils.isEmpty(cashFeeStr) || cashFeeStr.equals("."))
			cashFee = 0d;
		else
			cashFee = Double.parseDouble(cashFeeStr);
		mAllShouldPay = mNoDiscountTotal - mDiscountTotal - mExtraDiscount;
		mAllShouldPayTv.setText(String.format("%.2f", mAllShouldPay));
		mChangeMoneyTv.setText(String.format("%.2f", cashFee - mAllShouldPay));
	}
	
	private class SubmitTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			String urlStr = Interface.getPrefix(Globals.getSellerNick());
			String post = Interface.METHOD_NEW_ORDER + Interface.getSellerIdSeg() + Interface.INTERFACE_ID + 
				Interface.SIGN + mSign + "&" + Interface.CONTENT + mContent;
			
			Log.i("URL", urlStr);
			Log.i("POST", post);
			
			try {
				URL url = new URL(urlStr);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				
				conn.setDoOutput(true);
				byte[] postBytes = post.getBytes("UTF-8");
				conn.setFixedLengthStreamingMode(postBytes.length);
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-length", String.valueOf(postBytes.length));
				
				OutputStream out = conn.getOutputStream();
				out.write(post.getBytes("UTF-8"));
				
				String result = readResult(conn.getInputStream());
				conn.disconnect();
				return result;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} 
		}
		
		@Override
		protected void onPostExecute(String result) {
			mProgressDialog.dismiss();
			if (null == result) {
				Util.toast(getApplicationContext(), "网络出错");
				return;
			}
			try {
				JSONObject resultJson = new JSONObject(result);
				int resultCode = resultJson.getInt("ResultCode");
				if (0 == resultCode) {
					String selection = CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID + "=?";
					String[] selectionArgs = {
							Globals.getModuleUseWarehouseId() + ""
					};
					getContentResolver().delete(CashSaleGoods.CONTENT_URI, selection, selectionArgs);
					Util.toast(getApplicationContext(), "订单提交成功");
					finish();
				} else {
					String resultMessage = resultJson.getString("ResultMsg");
					Util.toast(getApplicationContext(), resultMessage);
				}
			} catch (JSONException e) {
				Util.toast(getApplicationContext(), "服务器返回数据解析错误");
				e.printStackTrace();
			}
		}
	}
	
	private String readResult(InputStream in) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				Log.d("Response Line", line);
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getSign() {
		String sign = null;
		try {
			byte[] bytesOfMessage = (mContent + Interface.getKey(Globals.getSellerNick())).getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytesOfMessage);
			byte[] digestResult = md.digest();
			// Create Hex String
	        StringBuilder hexString = new StringBuilder();
	        for (byte aMessageDigest : digestResult) {
	            String h = Integer.toHexString(0xFF & aMessageDigest);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
			String base64Str = Base64.encodeToString(hexString.toString().getBytes("UTF-8"), Base64.DEFAULT);
			base64Str = base64Str.substring(0, base64Str.length() - 1);
//			System.out.println(base64Str);
			sign = URLEncoder.encode(base64Str, "UTF-8") ;
//			System.out.println(sign);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sign;
	}
	
	private String getContent() {
		JSONObject content = new JSONObject();
		try {
			content.put(Interface.OUT_IN_FLAG, Interface.FLAG_SALE_OUT);
			content.put(Interface.IF_ORDER_CODE, Interface.getPdaTradeOrderCode(this));
			content.put(Interface.WAREHOUSE_NO, Globals.getCashSaleWarehouseNO());
			content.put(Interface.GOODS_TOTAL, mNoDiscountTotal);
			content.put(Interface.COD_FLAG, 0);// 非货到付款
			content.put(Interface.ORDER_PAY, mAllShouldPay);
			content.put(Interface.LOGISTICS_PAY, 0);
			content.put(Interface.SHOP_NAME, Globals.getShopName());
			content.put(Interface.FAVOURABLE_TOTAL, mDiscountTotal + mExtraDiscount);
			
			Customer customer = Globals.getCustomer();
			if (customer == null) {
				customer = new Customer(-1, null, 
					"临时客户", "", "", "门店", "门店", "门店", "门店", "");
			}
			
			content.put(Interface.BUYER_NAME, customer.customerName);
			content.put(Interface.BUYER_TEL, customer.tel);
			content.put(Interface.BUYER_POST_CODE, customer.zip);
			content.put(Interface.BUYER_PROVINCE, 
				TextUtils.isEmpty(customer.province) ? 
				"门店" : customer.province);
			content.put(Interface.BUYER_CITY, 
				TextUtils.isEmpty(customer.city) ? 
				"门店" : customer.city);
			content.put(Interface.BUYER_DISTRICT, 
				TextUtils.isEmpty(customer.district) ? 
				"门店" : customer.district);
			content.put(Interface.BUYER_ADDR, 
				TextUtils.isEmpty(customer.address) ? 
				"门店" : customer.address);
			content.put(Interface.BUYER_EMAIL, customer.email);
			String selection = CashSaleGoods.COLUMN_NAME_WAREHOUSE_ID + "=?";
			String[] selectionArgs = {
					Globals.getModuleUseWarehouseId() + ""
			};
			Cursor cursor = getContentResolver().query(CashSaleGoods.CONTENT_URI, null, selection, selectionArgs, null);
			content.put(Interface.ITEM_COUNT, cursor.getCount());
			
			JSONObject itemList = new JSONObject();
			JSONArray items = new JSONArray();
			double goodsFee = ArithUtil.sub(mNoDiscountTotal, mDiscountTotal);
			while (cursor.moveToNext()) {
				JSONObject json = new JSONObject();
				String specBarcoe = cursor.getString(cursor.getColumnIndex(CashSaleGoods.COLUMN_NAME_SPEC_BARCODE));
				json.put(Interface.SKU_CODE, specBarcoe);
				
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
				
				double unitPrice = Double.parseDouble(unitPriceStr);
				json.put(Interface.SKU_PRICE, String.format("%.4f", unitPrice));
				
				String quantityStr = cursor.getString(cursor.getColumnIndex(CashSaleGoods.COLUMN_NAME_COUNT));
				double quantity = Double.parseDouble(quantityStr);
				json.put(Interface.QTY, String.format("%.4f", quantity));
				
				String discountStr = cursor.getString(cursor.getColumnIndex(CashSaleGoods.COLUMN_NAME_DISCOUNT));
				double discount = Double.parseDouble(discountStr);
//				double total = unitPrice * quantity * discount;
				double noDiscount = ArithUtil.mul(unitPrice, quantity);
				double singleGoodsTotal = ArithUtil.mul(noDiscount, discount);
				
				if (mExtraDiscount > 0) {
//					total -= mExtraDiscount * (total / (mNoDiscountTotal - mDiscountTotal));
					
					double shareRatio = ArithUtil.div(singleGoodsTotal, goodsFee);
					double shareExtraDiscount = ArithUtil.mul(mExtraDiscount, shareRatio);
					singleGoodsTotal = ArithUtil.sub(singleGoodsTotal, shareExtraDiscount);
//					discount = singleGoodsTotal / (unitPrice * quantity);
					discount = ArithUtil.div(singleGoodsTotal, noDiscount);
				}
				
				json.put(Interface.DISCOUNT, String.format("%.4f", discount));
				
				json.put(Interface.TOTAL, String.format("%.4f", singleGoodsTotal));
				items.put(json);
			}
			cursor.close();
			cursor = null;
			itemList.put(Interface.ITEM, items);
			content.put(Interface.ITEM_LIST, itemList);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return content.toString();
	}
}