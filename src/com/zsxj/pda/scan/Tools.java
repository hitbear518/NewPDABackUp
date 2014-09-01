package com.zsxj.pda.scan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class Tools {

	public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat formatd = new SimpleDateFormat("yyyyMMdd");
	public static final DateFormat formatw = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final DateFormat formatw2 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	public static final DateFormat formatws = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public static final DateFormat formatws2 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	public static final DateFormat formatm = new SimpleDateFormat("MM-dd HH:mm");
	public static final DateFormat formats = new SimpleDateFormat("MM-dd");
	public static final DateFormat matter_cn = new SimpleDateFormat("现在时间:yyyy年MM月dd日E HH时mm分ss秒");
	public static final DateFormat formatrqxq = new SimpleDateFormat("yyyy年MM月dd日 E");
	public static final DateFormat formatsj = new SimpleDateFormat("HH:mm:ss");
	public static final DateFormat formatsjfz = new SimpleDateFormat("HH:mm");
	
	/**
	 * "yyyy年MM月dd日 E"
	 * @return
	 */
	public static String getNowDateWeek()
	{
		Date date = new Date();
		return formatrqxq.format(date);
	}
	
	/**
	 * "HH:mm:ss"
	 * @return
	 */
	public static String getNowTime()
	{
		Date date = new Date();
		return formatsj.format(date);
	}
	
	/**
	 * HH:mm
	 * @return
	 */
	public static String getNowTimeFz()
	{
		Date date = new Date();
		return formatsjfz.format(date);
	}
	
	/**
	 * "yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static String getNowTimeString()
	{
		Date date = new Date();
		return formatw.format(date);
	}
	
	/**
	 * yyyy-MM-dd_HH:mm:ss
	 * @return
	 */
	public static String getNowTimeHString() {
		Date date = new Date();
		return formatw2.format(date);
	}
	
	/**
	 * "yyyy-MM-dd"
	 * @return
	 */
	public static String getNowDateHString()
	{
		Date date = new Date();
		return format.format(date);
	}
	
	/**
	 * "yyyyMMdd"
	 * @return
	 */
	public static String getNowDateString()
	{
		Date date = new Date();
		return formatd.format(date);
	}
	
	/**
	 * "yyyyMMddHHmmssSSS"
	 * @return
	 */
	public static String getDefaultBh()
	{
		Date date = new Date();
		return formatws2.format(date);
	}
	
	public static Date getNowDate()
	{
		return new Date();
	}
	 public static String AddZero(int Num)
     {
         String ret = "";
         for (int i = 1; i <= Num; i++)
         {
             ret = "0" + ret;
         }
         return ret;
     }
	 
		public static byte stringToByte(String str){
			byte[] tem0 = str.substring(0, 1).getBytes();
			byte[] tem1 = str.substring(1, 2).getBytes();
			byte b0 = Byte.decode("0x" + new String(tem0)).byteValue();
			b0 = (byte)(b0 << 4);
			byte b1 = Byte.decode("0x" + new String(tem1)).byteValue();
			byte ret = (byte)(b0 ^ b1);
			return ret;
		}
		public static byte uniteBytes(byte src0, byte src1) {  
		    byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();  
		    _b0 = (byte)(_b0 << 4);  
		    byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();  
		    byte ret = (byte)(_b0 ^ _b1);  
		    return ret;  
		    }  
	
	public static boolean isEmpty(String input) {
		return input == null || input.trim().length() < 1;
	}
 	public static String byteToString(byte bytes){
 		String hex = Integer.toHexString(bytes & 0xFF);
 	     if (hex.length() == 1) {
 	       hex = '0' + hex;
 	     }
 	     Log.i("info", "hex == "+hex);
 	     return hex;
 	}
}
