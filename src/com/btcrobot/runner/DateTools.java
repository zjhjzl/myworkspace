package com.btcrobot.runner;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTools {
	
	public static long getCtimeFromMillise(long millise){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(millise);
		long actualCloseTime = Long.valueOf(sdf.format(date));
		return actualCloseTime;
		
	}

	public static void main(String[] args) {
		System.out.println(DateTools.getCtimeFromMillise(1385993759000L));

	}

}
