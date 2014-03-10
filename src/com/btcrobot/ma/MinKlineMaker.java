package com.btcrobot.ma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.btcrobot.runner.DateTools;

public class MinKlineMaker {
	
	static Connection conn;   
	static PreparedStatement st;  

	private DataSource dataSource;
	static Logger fatalLogger = LogManager.getLogger("Fetal");  

	public void setDataSource(DataSource dataSource) throws SQLException {
		System.out.println("spring 给设置数据源了");
		this.dataSource = dataSource;
		this.conn = this.dataSource.getConnection(); 
	}

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new FileSystemXmlApplicationContext("./WebRoot/WEB-INF/applicationContext.xml");     
		MaTools maTools = (MaTools) ctx.getBean("MaTools");
		MaDao maDao = (MaDao) ctx.getBean("MaDao");
		MinKlineMaker minKlineMaker = (MinKlineMaker) ctx.getBean("MinKlineMaker");

		String beginTimeString = "2013年12月07日-12:42:00";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日-HH:mm:ss");
		Date beginTimeDate = sdf.parse(beginTimeString);

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");


		long beginTime = Long.parseLong(sdf2.format(beginTimeDate));
		
		
		
		
		while(true){
			System.out.println(beginTime);
			
			long nextTimeAxisMilliseDate = DateTools.getMilliseFromTime(beginTime) + 60*1000;
			long nextTimeAxis = DateTools.getTimeFromMillise(nextTimeAxisMilliseDate);
			minKlineMaker.addKline(beginTime, nextTimeAxis);
			beginTime = nextTimeAxis;
            
		
		}
	}
	
	public boolean addKline(long closeTimePoint,long closeTime) throws SQLException{  
		Double cprice = 0.0;
		Double oprice = 0.0;
		Double hprice = 0.0;
		Double lprice = 0.0;
		Double amount = 0.0;
		long otid = 0L;
		long ctid = 0L;
		try {  

			//下面找 hprice和lprice
			
			String sql3 = "select min(tid) as opid,max(tid) as cpid,max(price) as hprice,min(price) as lprice, sum(amount) as amount from btcrobot.hbtrademem where ctime >= ? and ctime < ?";           

			st =  conn.prepareStatement(sql3);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTimePoint);
			st.setLong(2, closeTime);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs3 = st.executeQuery();

			while (rs3.next()){   
				hprice = rs3.getDouble("hprice"); 
				lprice = rs3.getDouble("lprice");
				amount = rs3.getDouble("amount");
				otid = rs3.getLong("opid");
				ctid = rs3.getLong("cpid");
				
				System.out.println("得到hprice了" + hprice);
				System.out.println("得到lprice了" + lprice);
				System.out.println("得到amount了" + amount);
			}
			
			///////////////////////////////////////////////////
			
			String sql7 = "select price from btcrobot.hbtrademem where tid = ?";           

			st =  conn.prepareStatement(sql7);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, otid);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs7 = st.executeQuery();

			while (rs7.next()){   
				oprice = rs7.getDouble("price"); 
				System.out.println("得到oprice了" + oprice);
			}
			
			//////////////////////////////////////////////////////////////////////
			
			String sql8 = "select price from btcrobot.hbtrademem where tid = ?";           

			st =  conn.prepareStatement(sql8);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, ctid);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs8 = st.executeQuery();

			while (rs8.next()){   
				cprice = rs8.getDouble("price"); 
				System.out.println("得到cprice了" + cprice);
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
	         
			
			rs7.close();
			rs8.close();
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
	

	

}
