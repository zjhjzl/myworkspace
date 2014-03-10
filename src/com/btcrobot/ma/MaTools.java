package com.btcrobot.ma;

import java.awt.List;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * ������Ǿ��ߵĴ������߱����ϲ�����Ҫk�ߣ�ֻ�������߼��Ͻ��������Ǵ�k�����ġ�
 * ���ߵ��������ݣ�ʱ�����䡢ʱ�����ڡ��������ڡ�
 * ���ߵ����ݽṹ
 * 
 * 
 * 
 * */

public class MaTools {
	/*
	 * ���㷽����
	 * 1.����klinewidth���ҳ�һ�����̼ۣ����̼��б�
	 * 
	 * */
	private MaDao maDao;

	public MaDao getMaDao() {
		return maDao;
	}

	public void setMaDao(MaDao maDao) {
		this.maDao = maDao;
	}

	/**
	 * 
	 * Ϊ��ģ�⣬�÷����������ǣ�������ma�߳��ֽ����ʱ�򣬷�����֪��
	 * @param ma1
	 * @param ma2
	 */
	public void timeAxis(){

	}

	public boolean maCompare(Long timeAxisPoing, Ma maLess, Ma maMore) throws TimeAxisNotHitException{
		//�����Ƕ��ڹ̶�ʱ��ε�ģ�⣬���ԣ��϶�����ͬ��klineTimeSpan����ô����ߵĵ�һ����С�����٣����ԣ��Դ������ʼʱ��Ϊ׼��		
		//1. ���ж��������ߵ�klineTimeSpan�Ƿ���ȣ��������ȣ�û�бȶԵ�����

		if(maLess.getKlineTimeSpan() != maMore.getKlineTimeSpan()){
			throw new IllegalArgumentException("Time Span �����"); 
		}

		TreeMap<Long, Double> maLessValues = maLess.getMaValues();
		TreeMap<Long, Double> maMoreValues = maMore.getMaValues();
		
		if(maLessValues.containsKey(timeAxisPoing)&&maMoreValues.containsKey(timeAxisPoing)){
			if(maLessValues.get(timeAxisPoing) > maMoreValues.get(timeAxisPoing)){
				return true;
			}else{
				return false;
			}
		}

		if(maLessValues.lowerKey(timeAxisPoing)==null||maLessValues.higherKey(timeAxisPoing)==null
				||maMoreValues.higherKey(timeAxisPoing)==null||maMoreValues.lowerKey(timeAxisPoing)==null){
			//�����һ���ǿյģ���ô��û�취�Ƚϣ����ؿ�ֵ
			throw new TimeAxisNotHitException("ʱ����û������");
		}
		
		//System.out.println("����Ƚ�����");

		//������ߵ���һ����˵������ma���߶���ʱ���������ˡ�

		if(maLessValues.lowerEntry(timeAxisPoing).getValue() > maMoreValues.lowerEntry(timeAxisPoing).getValue()){
			return true;
		}else{
			return false;
		}


	}

	public Ma getMa(long klineTimeSpan, int maPeriod, long beginTime, long endTime) throws Exception{

		//LinkedHashMap<Long, Double> maValues = new LinkedHashMap<Long, Double>();
		TreeMap<Long, Double> maValues = new TreeMap<Long, Double>();

		LinkedHashMap<Long, Double> allCprice = this.getAllCprices(klineTimeSpan, beginTime, endTime);
		//�ҵ��������̵�ļ۸�

//		System.out.println("�������̵������" + allCprice.size());
//		System.out.println("ma TimeSpan: " + klineTimeSpan);
//		System.out.println("ma Period: " + maPeriod);

		Set<Long> keySet = allCprice.keySet();
		ArrayList<Long> keyList = new ArrayList<Long>(keySet);

		for(int i = 0; i<keySet.size()-maPeriod+1;i++){//���ѭ���������ж��ٸ�maֵ
			Long thisKey = keyList.get(i);
			Double temp = 0.0;
			for (int j = 0; j < maPeriod; j++){//���ѭ����ÿ��maֵ�ü���
				temp = temp + allCprice.get(keyList.get(i+j));
			}
			BigDecimal b = new BigDecimal(temp/maPeriod); 
			double value = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();//�������뱣����λ
			maValues.put(thisKey, value);
			System.out.println("thisKey is :" + thisKey + "this maValue is :" + value);
		}


		Ma ma = new Ma();
		ma.setBeginTime(beginTime);
		ma.setEndTime(endTime);
		ma.setKlineTimeSpan(klineTimeSpan);
		ma.setMaPeriod(maPeriod);
		ma.setMaValues(maValues);

		return ma;
	}
	/**
	 * �������ͨ����ʼʱ��㣬��kline����ʱ���ȣ��õ�ÿһ�����̼۸�
	 * *
	 * @param klineTimeSpan
	 * @param beginTime
	 * @param endTime
	 * @return
	 * @throws SQLException
	 */

	private LinkedHashMap<Long, Double> getAllCprices(long klineTimeSpan, long beginTime, long endTime) throws SQLException{
		LinkedHashMap<Long, Double> allCprice = new LinkedHashMap<Long, Double>();
		
        //���ȵõ����еļ���ʱ���
		ArrayList<Long> allCloseTimePoints = this.getAllCloseTimePoints(klineTimeSpan, beginTime, endTime);
		
	    

		//��ÿһ������ʱ�������̼۸�
		for(int i = 0; i < allCloseTimePoints.size(); i++){
			try {
				//������һ�δ����Ǽ���Span��ƫ���������磺13:15�ֵ����̼۸���13:30֮ǰ���һ�ʡ�������13:15��֮ǰ���һ�ʡ�

				long actualCloseTime = this.getActualCloseTime(allCloseTimePoints.get(i), klineTimeSpan);

				//ͨ��ʵ��ʱ���ҵ����̼۸�
				Double price = this.maDao.getClosePrice(actualCloseTime);
				
				allCprice.put(allCloseTimePoints.get(i), price);
				System.out.println("all cprice " + allCloseTimePoints.get(i) + "price: " +price);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return allCprice;
	}
	
	/*
	 * ���һ���ӵ�kline��ʶ
	 * 
	 * */

	public long getActualCloseTime(long closeTimePoint, long klineTimeSpan){
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		try {
			date = sdf.parse(String.valueOf(closeTimePoint));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		calendar.setTime(date);
		long closeTimeSecond = calendar.getTimeInMillis()/1000;
		long actualCloseTimeSecond = closeTimeSecond + klineTimeSpan-60;//֮���Լ�ȥ60�룬��Ϊ�˴ӷ���kline�����ҵ����̼۸�
		date = new Date(actualCloseTimeSecond*1000);
		long actualCloseTime = Long.valueOf(sdf.format(date));
		return actualCloseTime;
	}
	/**
	 * @param klineTimeSpan
	 * @param beginTime
	 * @param endTime
	 * @return ʱ����б���ʽyyyyMMddHHmmss�����ʱ�����ǰ��
	 */

	public ArrayList<Long> getAllCloseTimePoints(long klineTimeSpan, long beginTime, long endTime){
		ArrayList<Long> allCloseTimePoints = new ArrayList<Long>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		//1. �ȶ���ɾ���������
		try {
			date = sdf.parse(String.valueOf(beginTime));
		} catch (ParseException e) {

			e.printStackTrace();
		}
		calendar.setTime(date);
		long beginTimeSecond = calendar.getTimeInMillis()/1000;// beginSecond

		try {
			date = sdf.parse(String.valueOf(endTime));
		} catch (ParseException e) {

			e.printStackTrace();
		}
		calendar.setTime(date);
		long endTimeSecond = calendar.getTimeInMillis()/1000;// endSecond

		long firstCloseTimeSecond = endTimeSecond - endTimeSecond % klineTimeSpan;//��һ������ʱ��ľ�������,����
		date = new Date(firstCloseTimeSecond*1000);
		long firstCloseTime = Long.valueOf(sdf.format(date));

		allCloseTimePoints.add(firstCloseTime);//�ѵ�һ�����̵�ӽ�ȥ

		long countSecond = firstCloseTimeSecond;

		//ͨ��ѭ������������������ʱ���
		while((countSecond - klineTimeSpan) > beginTimeSecond){
			countSecond = countSecond - klineTimeSpan;
			date = new Date(countSecond*1000);
			allCloseTimePoints.add(Long.valueOf(sdf.format(date)));//����ʱ���,���ʱ�����ǰ�档
		}
		return allCloseTimePoints;
	}

	public static void main(String[] args) throws ParseException {
		

		@SuppressWarnings("resource")
		ApplicationContext ctx = new FileSystemXmlApplicationContext("./WebRoot/WEB-INF/applicationContext.xml");     
		MaTools maTools = (MaTools) ctx.getBean("MaTools");



		String beginTimeString = "2014��03��07��-21:00:00";
		String endTimeString = "2014��03��08��-20:30:00";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��MM��dd��-HH:mm:ss");
		Date beginTimeDate = sdf.parse(beginTimeString);
		Date endTimeDate = sdf.parse(endTimeString);

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");


		long beginTime = Long.parseLong(sdf2.format(beginTimeDate));
		long endTime = Long.parseLong(sdf2.format(endTimeDate));

		try {
			System.out.println("��һ��MA");
			Ma maLessSpanLong = maTools.getMa(KlineTimeSpan.THIRTY_MIN,5, beginTime, endTime);
			
			System.out.println("�ڶ���MA");
			Ma maMoreSpanLong = maTools.getMa(KlineTimeSpan.THIRTY_MIN,15, beginTime, endTime);
			
			System.out.println("������MA");
			Ma maLessSpanShort = maTools.getMa(KlineTimeSpan.ONE_MIN,5, beginTime, endTime);
			
			System.out.println("���ĸ�MA");
			Ma maMoreSpanShort = maTools.getMa(KlineTimeSpan.ONE_MIN,15, beginTime, endTime);

			//			System.out.println("maLessSpanLong size: " + maLessSpanLong.getMaValues().keySet().size());
			//			System.out.println("maMoreSpanLong size: " + maMoreSpanLong.getMaValues().keySet().size());
			//			System.out.println("maLessSpanShort size: " + maLessSpanShort.getMaValues().keySet().size());
			//			System.out.println("maMoreSpanShort size: " + maMoreSpanShort.getMaValues().keySet().size());

			Iterator<Long> interator = maLessSpanShort.getMaValues().keySet().iterator();

			ArrayList<Long> timeAxisList = new ArrayList<Long>();

			while(interator.hasNext()){//����СTimeSpan��ֵ����ʱ���ᣬĿǰ����ģ���õ���һ���ӣ�����ʱ�������һ����

				Long now = interator.next();//ȡ�����е�ʱ���ᡣ

				//if(ma5Span10Values.containsKey(now)&&ma10Span10Values.containsKey(now)&&ma5Span5Values.containsKey(now)&&ma10Span5Values.containsKey(now)){
				//���������һ��ʱ��㣬�Ͱ����ʱ���ӽ�ȥ��Ŀ����Ϊ���ҵ�һ��key����С���ϣ������µ�timeAxisList˳���Ǵ�С����ġ�
				timeAxisList.add(now);
				//System.out.println("axislist is " + now);
				//}
				//�õ�ʱ����֮��ʼ����ʱ����ѵ����Ա�

			}
			
			//System.out.println("ʱ���᳤��" + timeAxisList.size());

			boolean canbuy = true;//true ��������� false ���������
			double money = 0.0;//��ʼmoney��0

			//System.out.println("ʱ���᳤��Ϊ �� "  + timeAxisList.size());
			for(int i=0;i < timeAxisList.size(); i++){//��ʼʱ�����������ѭ������ÿ����Ϊ���ȡ�
				int j = 0;
				try{

					if(maTools.maCompare(timeAxisList.get(i), maLessSpanLong, maMoreSpanLong)){//ȥ�����TimeSpan�ĶԱȽ����

						//��ͨ���˵�һ�ء�
						if(maTools.maCompare(timeAxisList.get(i), maLessSpanShort, maMoreSpanShort)){//���С��Ҳ����
							//�����������
							if(canbuy){//������ڿ�����״̬

								Double price = maTools.maDao.getOpenPrice(timeAxisList.get(i));
								money = money - price;
								System.out.println(timeAxisList.get(i) + "������� �۸� " + price +" Ǯ���� " +  money);
								canbuy = false;

							}
						}else{
							//System.out.println("����ߴ��ڴ򿪣���С���ߴ��ڹرգ��޷����� ");
						}

					}else{
						//System.out.println("����ߴ��ڹر�,�޷�����");
					}
					if(!maTools.maCompare(timeAxisList.get(i), maLessSpanShort, maMoreSpanShort)){//���С�Ŀ���
						if(!canbuy){//������ڿ�������״̬
							//������������
							Double price = maTools.maDao.getOpenPrice(timeAxisList.get(i));
							money = money + price;
							System.out.println(timeAxisList.get(i) + "�������� �۸� " + price +" Ǯ���� " +  money);
							canbuy = true;
						}
					}
				}catch(TimeAxisNotHitException e){
					//j = j+1;
					//System.out.println(e.getMessage() + j);

				}

			}


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
