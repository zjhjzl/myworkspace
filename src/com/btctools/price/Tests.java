package com.btctools.price;

import java.text.SimpleDateFormat;

public class Tests {

	public static void main(String[] args) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm"); 
		String dateTime = df.format(1392719974000L); 
		System.out.println(dateTime);

	}

}
