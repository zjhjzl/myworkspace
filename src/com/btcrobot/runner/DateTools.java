package com.btcrobot.runner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTools {
	
	
	public static long getTimeFromMillise(long millise){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(millise);
		long actualCloseTime = Long.valueOf(sdf.format(date));
		return actualCloseTime;
		
	}
	
	public static long getMilliseFromTime(long Time){
		long TimeMillise = 0L;
		try {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = sdf.parse(String.valueOf(Time));
	
		Calendar c = Calendar.getInstance(); 
		c.setTime(date);
		TimeMillise =  c.getTimeInMillis();

		
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return TimeMillise;
		
	}

	public static void main(String[] args) {
		System.out.println(DateTools.getTimeFromMillise(1385993759000L));
		
		System.out.println(DateTools.getMilliseFromTime(20131202221559L));

	}

}
