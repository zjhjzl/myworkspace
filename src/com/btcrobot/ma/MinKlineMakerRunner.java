package com.btcrobot.ma;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.btcrobot.runner.DateTools;
import com.btctools.trade.TradeCatcher;

public class MinKlineMakerRunner extends Thread{

	private Connection conn;   
	private PreparedStatement st;  

	private Double cprice = 0.0;
	private Double oprice = 0.0;
	private Double hprice = 0.0;
	private Double lprice = 0.0;
	private Double amount = 0.0;
	private long otid = 0L;
	private long ctid = 0L;

	private DataSource dataSource;
	String beginTimeString = "2013年12月07日-12:42:00";

	public void setBeginTimeString(String beginTimeString) {
		this.beginTimeString = beginTimeString;
	}

	public void setDataSource(DataSource dataSource) throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// 加载Mysql数据驱动  

		String DB_URL = "jdbc:mysql://localhost:3306/btcrobot";
		String DB_USER = "root";
		String DB_PASSWORD = "123456";
		this.conn = DriverManager.getConnection(  
				DB_URL, DB_USER, DB_PASSWORD);// 创建数据连接  
		System.out.println("spring 给设置数据源了");
	}

	public void run(){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日-HH:mm:ss");
		Date beginTimeDate = new Date();
		try {
			beginTimeDate = sdf.parse(beginTimeString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");


		long beginTime = Long.parseLong(sdf2.format(beginTimeDate));




		while(true){
			System.out.println(beginTime);

			long nextTimeAxisMilliseDate = DateTools.getMilliseFromTime(beginTime) + 60*1000;
			long nextTimeAxis = DateTools.getTimeFromMillise(nextTimeAxisMilliseDate);
			try {
				this.addKline(beginTime, nextTimeAxis);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			beginTime = nextTimeAxis;


		}
	}

	public boolean addKline(long closeTimePoint,long closeTime) throws SQLException{  

		try {  

			//下面找 hprice和lprice

			String sql3 = "select min(tid) as opid,max(tid) as cpid,max(price) as hprice,min(price) as lprice, sum(amount) as amount from btcrobot.hbtrademem where ctime >= ? and ctime < ?";           

			st =  conn.prepareStatement(sql3);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTimePoint);
			st.setLong(2, closeTime);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs3 = st.executeQuery();

			while(rs3.next()){   
				hprice = rs3.getDouble("hprice"); 
				lprice = rs3.getDouble("lprice");
				amount = rs3.getDouble("amount");
				otid = rs3.getLong("opid");
				ctid = rs3.getLong("cpid");

				System.out.println("得到hprice了" + hprice);
				System.out.println("得到lprice了" + lprice);
				System.out.println("得到amount了" + amount);

				if(hprice==0&&lprice==0&&amount==0){

					hprice = cprice;
					lprice = cprice;
					oprice = cprice;

					amount = 0.0;
				}else{

					//这一分钟有交易，才进行这个查询，否则，直接轮空
					String sql7 = "select price from btcrobot.hbtrademem where tid = ? or tid = ? order by tid";           

					st =  conn.prepareStatement(sql7);    // 创建用于执行静态sql语句的Statement对象  

					st.setLong(1, otid);
					st.setLong(2, ctid);

					st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

					ResultSet rs7 = st.executeQuery();

					if (rs7.next()){   
						oprice = rs7.getDouble("price"); 
						System.out.println("得到oprice了" + oprice);
					}		
					if (rs7.next()){   
						cprice = rs7.getDouble("price"); 
						System.out.println("得到cprice了" + cprice);
					}

					rs7.close();

				}
			}



			//插入k线图。

			String sql4 = "INSERT INTO kline1min(date, oprice, hprice, lprice, cprice, amount, volume, ext, ext2)VALUES(?,?,?,?,?,?,'0.0','1','1')";  // 插入数据的sql语句  


			st =  conn.prepareStatement(sql4);    // 创建用于执行静态sql语句的Statement对象  
			st.setLong(1, closeTimePoint);
			st.setDouble(2, oprice);
			st.setDouble(3, hprice);
			st.setDouble(4, lprice);
			st.setDouble(5, cprice);
			st.setDouble(6, amount);

			System.out.println("插入操作");

			st.execute();  // 执行插入操作的sql语句，并返回插入数据的个数  



			rs3.close();
			//			st.close();
			//			conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			e.printStackTrace();

			//st.close();
			//conn.close();
			return true;
		}finally{
			//st.close();
			///conn.close();
		}
		return true;

	}

	public static void main(String[] args) throws SQLException{
		ApplicationContext ctx = new FileSystemXmlApplicationContext("./WebRoot/WEB-INF/applicationContext.xml");    
		DataSource dataSource = (DataSource) ctx.getBean("dataSourceDev");

		System.out.println("线程1 启动......");
		MinKlineMakerRunner minKlineMakerRunner1 = new MinKlineMakerRunner();
		minKlineMakerRunner1.setBeginTimeString("2014年01月03日-20:53:00");
		minKlineMakerRunner1.setDataSource(dataSource);
		minKlineMakerRunner1.start();

		System.out.println("线程2 启动......");
		MinKlineMakerRunner minKlineMakerRunner2 =  new MinKlineMakerRunner();
		minKlineMakerRunner2.setBeginTimeString("2014年02月01日-19:06:00");
		minKlineMakerRunner2.setDataSource(dataSource);
		minKlineMakerRunner2.start();
		//			
		//				System.out.println("线程3 启动......");
		//				MinKlineMakerRunner minKlineMakerRunner3 =  new MinKlineMakerRunner();
		//				minKlineMakerRunner3.setBeginTimeString("2014年01月15日-00:00:00");
		//				minKlineMakerRunner3.setDataSource(dataSource);
		//				minKlineMakerRunner3.start();
		//				
		//				System.out.println("线程4 启动......");
		//				MinKlineMakerRunner minKlineMakerRunner4 =  new MinKlineMakerRunner();
		//				minKlineMakerRunner4.setBeginTimeString("2014年02月15日-00:00:00");
		//				minKlineMakerRunner4.setDataSource(dataSource);
		//				minKlineMakerRunner4.start();
	}

}
