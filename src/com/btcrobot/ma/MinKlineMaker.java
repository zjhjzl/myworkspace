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
		System.out.println("spring ����������Դ��");
		this.dataSource = dataSource;
		this.conn = this.dataSource.getConnection(); 
	}

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = new FileSystemXmlApplicationContext("./WebRoot/WEB-INF/applicationContext.xml");     
		MaTools maTools = (MaTools) ctx.getBean("MaTools");
		MaDao maDao = (MaDao) ctx.getBean("MaDao");
		MinKlineMaker minKlineMaker = (MinKlineMaker) ctx.getBean("MinKlineMaker");

		String beginTimeString = "2013��12��07��-12:42:00";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��MM��dd��-HH:mm:ss");
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

			//������ hprice��lprice
			
			String sql3 = "select min(tid) as opid,max(tid) as cpid,max(price) as hprice,min(price) as lprice, sum(amount) as amount from btcrobot.hbtrademem where ctime >= ? and ctime < ?";           

			st =  conn.prepareStatement(sql3);    // ��������ִ�о�̬sql����Statement����  

			st.setLong(1, closeTimePoint);
			st.setLong(2, closeTime);

			st.executeQuery();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

			ResultSet rs3 = st.executeQuery();

			while (rs3.next()){   
				hprice = rs3.getDouble("hprice"); 
				lprice = rs3.getDouble("lprice");
				amount = rs3.getDouble("amount");
				otid = rs3.getLong("opid");
				ctid = rs3.getLong("cpid");
				
				System.out.println("�õ�hprice��" + hprice);
				System.out.println("�õ�lprice��" + lprice);
				System.out.println("�õ�amount��" + amount);
			}
			
			///////////////////////////////////////////////////
			
			String sql7 = "select price from btcrobot.hbtrademem where tid = ?";           

			st =  conn.prepareStatement(sql7);    // ��������ִ�о�̬sql����Statement����  

			st.setLong(1, otid);

			st.executeQuery();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

			ResultSet rs7 = st.executeQuery();

			while (rs7.next()){   
				oprice = rs7.getDouble("price"); 
				System.out.println("�õ�oprice��" + oprice);
			}
			
			//////////////////////////////////////////////////////////////////////
			
			String sql8 = "select price from btcrobot.hbtrademem where tid = ?";           

			st =  conn.prepareStatement(sql8);    // ��������ִ�о�̬sql����Statement����  

			st.setLong(1, ctid);

			st.executeQuery();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

			ResultSet rs8 = st.executeQuery();

			while (rs8.next()){   
				cprice = rs8.getDouble("price"); 
				System.out.println("�õ�cprice��" + cprice);
			}
			
			
			//����k��ͼ��
			
			 String sql4 = "INSERT INTO kline1min(date, oprice, hprice, lprice, cprice, amount, volume, ext, ext2)VALUES(?,?,?,?,?,?,'0.0','1','1')";  // �������ݵ�sql���  
	           
             
	            st =  conn.prepareStatement(sql4);    // ��������ִ�о�̬sql����Statement����  
	            st.setLong(1, closeTimePoint);
	            st.setDouble(2, oprice);
	            st.setDouble(3, hprice);
	            st.setDouble(4, lprice);
	            st.setDouble(5, cprice);
	            st.setDouble(6, amount);

	            System.out.println("�������");
	              
	            st.execute();  // ִ�в��������sql��䣬�����ز������ݵĸ���  
	         
			
			rs7.close();
			rs8.close();
			rs3.close();
//			st.close();
//			conn.close();   //�ر����ݿ�����  

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
