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
 * 这个类是均线的处理，均线本质上并不需要k线，只不过从逻辑上讲，均线是从k线来的。
 * 均线的描述数据：时间区间、时间周期、均线周期。
 * 均线的数据结构
 * 
 * 
 * 
 * */

public class MaTools {
	/*
	 * 计算方法：
	 * 1.根据klinewidth，找出一堆收盘价（收盘价列表）
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
	 * 为了模拟，该方法的作用是，当两条ma线出现交叉的时候，发出告知。
	 * @param ma1
	 * @param ma2
	 */
	public void timeAxis(){

	}

	public boolean maCompare(Long timeAxisPoing, Ma maLess, Ma maMore) throws TimeAxisNotHitException{
		//由于是对于固定时间段的模拟，所以，肯定是相同的klineTimeSpan。那么大均线的点一定比小均线少，所以，以大均线起始时间为准。		
		//1. 先判断两条均线的klineTimeSpan是否相等，如果不想等，没有比对的意义

		if(maLess.getKlineTimeSpan() != maMore.getKlineTimeSpan()){
			throw new IllegalArgumentException("Time Span 不相等"); 
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
			//如果有一个是空的，那么就没办法比较，返回空值
			throw new TimeAxisNotHitException("时间轴没有命中");
		}
		
		//System.out.println("满足比较条件");

		//如果能走到这一步，说明两条ma均线都被时间轴命中了。

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
		//找到所有收盘点的价格

//		System.out.println("所有收盘点的数量" + allCprice.size());
//		System.out.println("ma TimeSpan: " + klineTimeSpan);
//		System.out.println("ma Period: " + maPeriod);

		Set<Long> keySet = allCprice.keySet();
		ArrayList<Long> keyList = new ArrayList<Long>(keySet);

		for(int i = 0; i<keySet.size()-maPeriod+1;i++){//这个循环决定了有多少个ma值
			Long thisKey = keyList.get(i);
			Double temp = 0.0;
			for (int j = 0; j < maPeriod; j++){//这个循环是每个ma值得计算
				temp = temp + allCprice.get(keyList.get(i+j));
			}
			BigDecimal b = new BigDecimal(temp/maPeriod); 
			double value = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();//四舍五入保留两位
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
	 * 这个方法通过起始时间点，和kline柱子时间宽度，得到每一个收盘价格。
	 * *
	 * @param klineTimeSpan
	 * @param beginTime
	 * @param endTime
	 * @return
	 * @throws SQLException
	 */

	private LinkedHashMap<Long, Double> getAllCprices(long klineTimeSpan, long beginTime, long endTime) throws SQLException{
		LinkedHashMap<Long, Double> allCprice = new LinkedHashMap<Long, Double>();
		
        //首先得到所有的计算时间点
		ArrayList<Long> allCloseTimePoints = this.getAllCloseTimePoints(klineTimeSpan, beginTime, endTime);
		
	    

		//找每一个收盘时间点的收盘价格
		for(int i = 0; i < allCloseTimePoints.size(); i++){
			try {
				//下面这一段代码是加上Span的偏移量，例如：13:15分的收盘价格。是13:30之前最后一笔。而不是13:15分之前最后一笔。

				long actualCloseTime = this.getActualCloseTime(allCloseTimePoints.get(i), klineTimeSpan);

				//通过实际时间找到收盘价格。
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
	 * 最后一分钟的kline标识
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
		long actualCloseTimeSecond = closeTimeSecond + klineTimeSpan-60;//之所以减去60秒，是为了从分钟kline里面找到收盘价格。
		date = new Date(actualCloseTimeSecond*1000);
		long actualCloseTime = Long.valueOf(sdf.format(date));
		return actualCloseTime;
	}
	/**
	 * @param klineTimeSpan
	 * @param beginTime
	 * @param endTime
	 * @return 时间点列表，格式yyyyMMddHHmmss，晚的时间点在前面
	 */

	public ArrayList<Long> getAllCloseTimePoints(long klineTimeSpan, long beginTime, long endTime){
		ArrayList<Long> allCloseTimePoints = new ArrayList<Long>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Calendar calendar = Calendar.getInstance();
		Date date = new Date();
		//1. 先都变成绝对秒数。
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

		long firstCloseTimeSecond = endTimeSecond - endTimeSecond % klineTimeSpan;//第一个收盘时间的绝对秒数,倒序
		date = new Date(firstCloseTimeSecond*1000);
		long firstCloseTime = Long.valueOf(sdf.format(date));

		allCloseTimePoints.add(firstCloseTime);//把第一个收盘点加进去

		long countSecond = firstCloseTimeSecond;

		//通过循环加入所有其它收盘时间点
		while((countSecond - klineTimeSpan) > beginTimeSecond){
			countSecond = countSecond - klineTimeSpan;
			date = new Date(countSecond*1000);
			allCloseTimePoints.add(Long.valueOf(sdf.format(date)));//加入时间点,晚的时间点在前面。
		}
		return allCloseTimePoints;
	}

	public static void main(String[] args) throws ParseException {
		

		@SuppressWarnings("resource")
		ApplicationContext ctx = new FileSystemXmlApplicationContext("./WebRoot/WEB-INF/applicationContext.xml");     
		MaTools maTools = (MaTools) ctx.getBean("MaTools");



		String beginTimeString = "2014年03月07日-21:00:00";
		String endTimeString = "2014年03月08日-20:30:00";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日-HH:mm:ss");
		Date beginTimeDate = sdf.parse(beginTimeString);
		Date endTimeDate = sdf.parse(endTimeString);

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");


		long beginTime = Long.parseLong(sdf2.format(beginTimeDate));
		long endTime = Long.parseLong(sdf2.format(endTimeDate));

		try {
			System.out.println("第一个MA");
			Ma maLessSpanLong = maTools.getMa(KlineTimeSpan.THIRTY_MIN,5, beginTime, endTime);
			
			System.out.println("第二个MA");
			Ma maMoreSpanLong = maTools.getMa(KlineTimeSpan.THIRTY_MIN,15, beginTime, endTime);
			
			System.out.println("第三个MA");
			Ma maLessSpanShort = maTools.getMa(KlineTimeSpan.ONE_MIN,5, beginTime, endTime);
			
			System.out.println("第四个MA");
			Ma maMoreSpanShort = maTools.getMa(KlineTimeSpan.ONE_MIN,15, beginTime, endTime);

			//			System.out.println("maLessSpanLong size: " + maLessSpanLong.getMaValues().keySet().size());
			//			System.out.println("maMoreSpanLong size: " + maMoreSpanLong.getMaValues().keySet().size());
			//			System.out.println("maLessSpanShort size: " + maLessSpanShort.getMaValues().keySet().size());
			//			System.out.println("maMoreSpanShort size: " + maMoreSpanShort.getMaValues().keySet().size());

			Iterator<Long> interator = maLessSpanShort.getMaValues().keySet().iterator();

			ArrayList<Long> timeAxisList = new ArrayList<Long>();

			while(interator.hasNext()){//用最小TimeSpan的值来做时间轴，目前由于模拟用的是一分钟，所以时间轴就是一分钟

				Long now = interator.next();//取出所有的时间轴。

				//if(ma5Span10Values.containsKey(now)&&ma10Span10Values.containsKey(now)&&ma5Span5Values.containsKey(now)&&ma10Span5Values.containsKey(now)){
				//如果都包含一个时间点，就把这个时间点加进去，目的是为了找到一个key的最小集合，而且新的timeAxisList顺序是从小到大的。
				timeAxisList.add(now);
				//System.out.println("axislist is " + now);
				//}
				//得到时间轴之后开始按照时间轴训话你对比

			}
			
			//System.out.println("时间轴长度" + timeAxisList.size());

			boolean canbuy = true;//true 代表可以买 false 代表可以卖
			double money = 0.0;//初始money是0

			//System.out.println("时间轴长度为 ： "  + timeAxisList.size());
			for(int i=0;i < timeAxisList.size(); i++){//开始时间轴的主程序循环，以每分钟为粒度。
				int j = 0;
				try{

					if(maTools.maCompare(timeAxisList.get(i), maLessSpanLong, maMoreSpanLong)){//去看大的TimeSpan的对比结果。

						//打通的了第一关。
						if(maTools.maCompare(timeAxisList.get(i), maLessSpanShort, maMoreSpanShort)){//如果小的也看多
							//做出购买决定
							if(canbuy){//如果处于可以买状态

								Double price = maTools.maDao.getOpenPrice(timeAxisList.get(i));
								money = money - price;
								System.out.println(timeAxisList.get(i) + "购买决定 价格： " + price +" 钱包： " +  money);
								canbuy = false;

							}
						}else{
							//System.out.println("大均线窗口打开，但小均线窗口关闭，无法购买 ");
						}

					}else{
						//System.out.println("大均线窗口关闭,无法购买");
					}
					if(!maTools.maCompare(timeAxisList.get(i), maLessSpanShort, maMoreSpanShort)){//如果小的看空
						if(!canbuy){//如果处于可以卖的状态
							//做出卖出决定
							Double price = maTools.maDao.getOpenPrice(timeAxisList.get(i));
							money = money + price;
							System.out.println(timeAxisList.get(i) + "卖出决定 价格： " + price +" 钱包： " +  money);
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
