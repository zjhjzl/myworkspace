package com.btcrobot.ma;

import java.util.LinkedHashMap;
import java.util.TreeMap;

public class Ma {
	

	/*
	 *这个参数用klineTimeSpan的名字，来存储ma的计算粒度。也就是k线的宽度，单位是：秒。分钟线就是60。 
	 */
	private long klineTimeSpan;


	public long getKlineTimeSpan() {
		return klineTimeSpan;
	}

	public void setKlineTimeSpan(long klineTimeSpan) {
		this.klineTimeSpan = klineTimeSpan;
	}

	/*
	 *maPeriod 表明了ma的计算周期。例如；klineWidth 是60 那么就是分钟线。maPeriod是5.那么这个ma对象代表的就是分钟线的ma5.
	 */
	private long maPeriod;
	public long getMaPeriod() {
		return maPeriod;
	}

	public void setMaPeriod(long maPeriod) {
		this.maPeriod = maPeriod;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/*
	 *beginTime 表明了开始计算的时间，是一个精确到秒的日期串，例如：20140225103245 如果是0，就从数据库中最早数据开始计算
	 */
	private long beginTime;
	
	/*
	 *endTime 表明了结束计算的时间，是一个精确到秒的日期串，例如：20140225103245 如果是0，就是计算当下的ma；
	 */
	private long endTime;
	
	/*
	 *maValues 存储了计算出来的ma值序列。key为时间点（精确到秒）value为ma值。
	 */
	
	//private LinkedHashMap<Long, Double> maValues;
	private TreeMap<Long, Double> maValues;//TreeMap这个容器类型能自动排序，小的时间点在前面，所以最后用了TreeMap这个类型
	
	//public LinkedHashMap<Long, Double> getMaValues() {
	public TreeMap<Long, Double> getMaValues() {
		return maValues;
	}

	//public void setMaValues(LinkedHashMap<Long, Double> maValues) {
	public void setMaValues(TreeMap<Long, Double> maValues) {
		this.maValues = maValues;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
