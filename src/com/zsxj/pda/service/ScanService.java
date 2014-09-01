package com.zsxj.pda.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.serialport.SerialPort;
import android.support.v4.content.LocalBroadcastManager;

import com.zsxj.pda.scan.DeviceControl;

public class ScanService extends Service {
	
	public static final int KEY_F1 = 112;
	public static final String SCAN_OVER_ACTION = "com.zsxj.pda.SCAN_OVER";
	
	public static final String EXTRA_BARCODE = "com.zsxj.pda.BARCODE";
	
	private final IBinder mBinder = new LocalBinder();
	
	private DeviceControl mDeviceControl;
	private boolean mDeviceOpened;
	private SerialPort mSerialPort;
	private int mFileDecriptor;
	private boolean mSerialPortOpened;
	private ReadThread mReadThread;
	private boolean mPowerOn;
	private boolean mTriggerOn;

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Open DeviceControl
        try {
			mDeviceControl = new DeviceControl("/proc/driver/scan");
			mDeviceOpened = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        mSerialPort = new SerialPort();
		try {
			mSerialPort.OpenSerial("/dev/eser0", 9600);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mFileDecriptor = mSerialPort.getFd();
		if (mFileDecriptor > 0) {
			mSerialPortOpened = true;
			
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		if (mSerialPortOpened) {
			mReadThread = new ReadThread();
			mReadThread.start();
		}
		
		// Power On
		if (!mPowerOn) {
			try {
				mDeviceControl.PowerOnDevice();
				mPowerOn = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		if (mSerialPortOpened) {
    		try {
				mDeviceControl.TriggerOffDevice();
				mTriggerOn = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    		try {
				mDeviceControl.PowerOffDevice();
				mPowerOn = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
    		mReadThread.interrupt();
    	}
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		mSerialPort.CloseSerial(mFileDecriptor);
		mSerialPortOpened = false;
		
		if (mDeviceOpened) {
    		try {
				mDeviceControl.DeviceClose();
				mDeviceOpened = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
		super.onDestroy();
	}
	
	public void tryTriggerOn() {
		if (!mTriggerOn) {
			try {
				mDeviceControl.TriggerOnDevice();
				mTriggerOn = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void triggerOff() {
		if (mTriggerOn) {
			try {
				mDeviceControl.TriggerOffDevice();
				mTriggerOn = false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendBackBarcode(String barcode) {
		Intent localIntent = new Intent(SCAN_OVER_ACTION)
			.putExtra(EXTRA_BARCODE, barcode.trim());
		LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	}
	
	private class ReadThread extends Thread {
		@Override
    	public void run() {
    		while (!isInterrupted()) {
    			try {
    				String barcode = mSerialPort.ReadSerialString(mFileDecriptor, 1024, 100);
    				if (barcode != null) {
    					sendBackBarcode(barcode);
    					mTriggerOn = false;
    				}
    			} catch (UnsupportedEncodingException e) {
    				e.printStackTrace();
    			}
    		}
    	}
	}
	
	public class LocalBinder extends Binder {
        public ScanService getService() {
            return ScanService.this;
        }
    }
}
