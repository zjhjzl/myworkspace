package com.btcrobot.ma;

import java.util.LinkedHashMap;
import java.util.TreeMap;

public class Ma {
	

	/*
	 *���������klineTimeSpan�����֣����洢ma�ļ������ȡ�Ҳ����k�ߵĿ�ȣ���λ�ǣ��롣�����߾���60�� 
	 */
	private long klineTimeSpan;


	public long getKlineTimeSpan() {
		return klineTimeSpan;
	}

	public void setKlineTimeSpan(long klineTimeSpan) {
		this.klineTimeSpan = klineTimeSpan;
	}

	/*
	 *maPeriod ������ma�ļ������ڡ����磻klineWidth ��60 ��ô���Ƿ����ߡ�maPeriod��5.��ô���ma�������ľ��Ƿ����ߵ�ma5.
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
	 *beginTime �����˿�ʼ�����ʱ�䣬��һ����ȷ��������ڴ������磺20140225103245 �����0���ʹ����ݿ����������ݿ�ʼ����
	 */
	private long beginTime;
	
	/*
	 *endTime �����˽��������ʱ�䣬��һ����ȷ��������ڴ������磺20140225103245 �����0�����Ǽ��㵱�µ�ma��
	 */
	private long endTime;
	
	/*
	 *maValues �洢�˼��������maֵ���С�keyΪʱ��㣨��ȷ���룩valueΪmaֵ��
	 */
	
	//private LinkedHashMap<Long, Double> maValues;
	private TreeMap<Long, Double> maValues;//TreeMap��������������Զ�����С��ʱ�����ǰ�棬�����������TreeMap�������
	
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
