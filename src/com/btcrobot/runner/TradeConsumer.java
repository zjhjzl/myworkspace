package com.btcrobot.runner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;







import com.btctools.trade.Trade;


public class TradeConsumer extends Thread{

	private BlockingQueue<Trade> tradesQueue;



	public void setTradesQueue(BlockingQueue<Trade> tradesQueue) {
		this.tradesQueue = tradesQueue;
	}


	static Logger logger = LogManager.getLogger(); 
	static Logger fatalLoger = LogManager.getLogger("Fetal");  


	static Connection conn;   
	static PreparedStatement st;  

	private DataSource dataSource;
	static Logger fatalLogger = LogManager.getLogger("Fetal");  

	public void setDataSource(DataSource dataSource) {
		logger.trace("spring 给设置数据源了");
		System.out.println("spring 给设置数据源了");
		this.dataSource = dataSource;
	}


	public void run() {


		try {
			conn = this.dataSource.getConnection();

		} catch (Exception e1) {

			e1.printStackTrace();
		}
		while(true){
		try {  
		
		
				Trade trade = this.tradesQueue.take();
				System.out.println("消费者队列的长度" + this.tradesQueue.size());
				
				//这里将来要计算实时的各种MA的值，用来做判断。

				String sql = "INSERT INTO hbtrade(type, amount, price, ctime, tid)VALUES(?,?,?,?,?)";

				System.currentTimeMillis();

				st =  conn.prepareStatement(sql);  
				st.setInt(1, trade.getType());
				st.setDouble(2, trade.getAmount());
				st.setDouble(3, trade.getPrice());
				st.setLong(4, trade.getCtime());
				st.setLong(5, trade.getTid());


				st.execute();  // 执行插入操作的sql语句，并返回插入数据的个数  
				
				//判断ctime是不是跨国了一个分钟界限，如果是的话，那么就要出一根分钟线的柱子。
				
				//20140307190000  20140307190034
				
//				if(Tid.ctime==0L){
//					System.out.println("第一次给赋值了");
//					Tid.ctime = trade.getCtime();
//				}else{
//					System.out.println("第二次进来了" + Tid.ctime);
//					Calendar calendar = Calendar.getInstance();
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//					Date previousTime = new Date();
//					Date nowTime = new Date();
//					try {
//						previousTime = sdf.parse(String.valueOf(Tid.ctime));
//						nowTime = sdf.parse(String.valueOf(trade.getCtime()));
//					} catch (ParseException e) {
//						e.printStackTrace();
//					}
//					calendar.setTime(previousTime);
//					long previousTimeMillise = calendar.getTimeInMillis();
//					calendar.setTime(nowTime);
//					long nowTimeMillise = calendar.getTimeInMillis();
//					long timeSpanMillis = nowTimeMillise - previousTimeMillise;
//					if(timeSpanMillis > 0 && timeSpanMillis < 60000){//确认差距没有一分钟，也就保证了不缺kline，但这里面有个前提，就是每分钟都有交易。这个不知道在后半夜能否实现。
//						System.out.println("判断" + timeSpanMillis);
//						if (((nowTimeMillise/60000)-(previousTimeMillise/60000))==1){//说明跨分钟了，要触发出kline
//							long klineMinTimePoint = DateTools.getCtimeFromMillise(previousTimeMillise)/100*100;
//							long klineMinTimeClose = DateTools.getCtimeFromMillise(nowTimeMillise)/100*100;
//							
//							System.out.println(klineMinTimePoint + "&&" + klineMinTimeClose);
//							
//							this.addKline(klineMinTimePoint, klineMinTimeClose);
//							System.out.println("到加分钟kline的时间了，加了一个" + klineMinTimePoint);
//						}
//					}else{//如果缺的话
//						
//					}
//					
//					
//				}
//
//				Tid.ctime = trade.getCtime();
			//st.close();
			//conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			e.printStackTrace();
			//System.out.println("本条交易数据没有插入成功" + e.getMessage());
			//e.printStackTrace();
			//st.close();
			//conn.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			//st.close();
			//conn.close();
		}
		}


	}
	public boolean addKline(long closeTimePoint,long closeTime) throws SQLException{
		//conn = this.dataSource.getConnection(); // 首先要获取连接，即连接到数据库  
		Double cprice = 0.0;
		Double oprice = 0.0;
		Double hprice = 0.0;
		Double lprice = 0.0;
		Double amount = 0.0;
		try {  

			//select * from btcrobot.hbtrade where ctime <= 20140225133944 order by ctime desc limit 1;
			String sql = "select price,tid from btcrobot.hbtrade where ctime < ? order by tid desc limit 1";           

			st =  conn.prepareStatement(sql);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTime);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs = st.executeQuery();

			while (rs.next()){   
				cprice = rs.getDouble("price"); 
			}
			
			//下面找oprice
			
			String sql2 = "select price,tid from btcrobot.hbtrade where ctime >= ? order by tid limit 1";           

			st =  conn.prepareStatement(sql2);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTimePoint);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs2 = st.executeQuery();

			while (rs2.next()){   
				oprice = rs2.getDouble("price"); 
			}
			//下面找 hprice和lprice
			
			String sql3 = "select max(price) as hprice,min(price) as lprice, sum(amount) as amount from btcrobot.hbtrade where ctime >= ? and ctime < ?";           

			st =  conn.prepareStatement(sql3);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTimePoint);
			st.setLong(2, closeTime);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs3 = st.executeQuery();

			while (rs3.next()){   
				hprice = rs3.getDouble("hprice"); 
				lprice = rs3.getDouble("lprice");
				amount = rs3.getDouble("amount");
			}
			
			//插入k线图。
			
			 String sql4 = "INSERT INTO kline1min(date, oprice, hprice, lprice, cprice, amount, volume, tid, ext2)VALUES(?,?,?,?,?,?,'0.0','1','1')";  // 插入数据的sql语句  
	           
             
	            st =  conn.prepareStatement(sql4);    // 创建用于执行静态sql语句的Statement对象  
	            st.setLong(1, closeTimePoint);
	            st.setDouble(2, oprice);
	            st.setDouble(3, hprice);
	            st.setDouble(4, lprice);
	            st.setDouble(5, cprice);
	            st.setDouble(6, amount);

	            System.out.println("插入操作");
	              
	            st.execute();  // 执行插入操作的sql语句，并返回插入数据的个数  
	         
			
			rs.close();
			rs2.close();
			rs3.close();
//			st.close();
//			conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			e.printStackTrace();
			logger.warn("本条交易数据没有插入成功" + e.getMessage());
			//st.close();
			//conn.close();
			return true;
		}finally{
			//st.close();
			///conn.close();
		}
		return true;

	}

}
