package com.btcrobot.ma;

import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;


public class Test {



	public static void main(String[] args) {
		//treeMap ”√∑®≤‚ ‘°£
		TreeMap<Long, Double> maLessValues = new TreeMap<Long, Double>();
		maLessValues.put(5L, 500.0);
		maLessValues.put(1L, 100.0);
		maLessValues.put(3L, 300.0);
		maLessValues.put(7L, 500.0);
		
		//maLessValues.put(2L, 200.0);
		
		System.out.println(maLessValues.lowerKey(4L));
		
		if(maLessValues.higherKey(7L) == null){
			System.out.println("woshikong");
		}

		Set<Long> keys = maLessValues.keySet();
		Iterator<Long> iterator = keys.iterator();

		while(iterator.hasNext()){
			
			Long key = iterator.next();
			System.out.println(key);

		}
		


		TreeMap<Long, Double> maMoreValues = new TreeMap<Long, Double>();

	}

}
