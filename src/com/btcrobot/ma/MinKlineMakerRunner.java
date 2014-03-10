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
	String beginTimeString = "2013��12��07��-12:42:00";

	public void setBeginTimeString(String beginTimeString) {
		this.beginTimeString = beginTimeString;
	}

	public void setDataSource(DataSource dataSource) throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// ����Mysql��������  

		String DB_URL = "jdbc:mysql://localhost:3306/btcrobot";
		String DB_USER = "root";
		String DB_PASSWORD = "123456";
		this.conn = DriverManager.getConnection(  
				DB_URL, DB_USER, DB_PASSWORD);// ������������  
		System.out.println("spring ����������Դ��");
	}

	public void run(){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��MM��dd��-HH:mm:ss");
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

			//������ hprice��lprice

			String sql3 = "select min(tid) as opid,max(tid) as cpid,max(price) as hprice,min(price) as lprice, sum(amount) as amount from btcrobot.hbtrademem where ctime >= ? and ctime < ?";           

			st =  conn.prepareStatement(sql3);    // ��������ִ�о�̬sql����Statement����  

			st.setLong(1, closeTimePoint);
			st.setLong(2, closeTime);

			st.executeQuery();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

			ResultSet rs3 = st.executeQuery();

			while(rs3.next()){   
				hprice = rs3.getDouble("hprice"); 
				lprice = rs3.getDouble("lprice");
				amount = rs3.getDouble("amount");
				otid = rs3.getLong("opid");
				ctid = rs3.getLong("cpid");

				System.out.println("�õ�hprice��" + hprice);
				System.out.println("�õ�lprice��" + lprice);
				System.out.println("�õ�amount��" + amount);

				if(hprice==0&&lprice==0&&amount==0){

					hprice = cprice;
					lprice = cprice;
					oprice = cprice;

					amount = 0.0;
				}else{

					//��һ�����н��ף��Ž��������ѯ������ֱ���ֿ�
					String sql7 = "select price from btcrobot.hbtrademem where tid = ? or tid = ? order by tid";           

					st =  conn.prepareStatement(sql7);    // ��������ִ�о�̬sql����Statement����  

					st.setLong(1, otid);
					st.setLong(2, ctid);

					st.executeQuery();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

					ResultSet rs7 = st.executeQuery();

					if (rs7.next()){   
						oprice = rs7.getDouble("price"); 
						System.out.println("�õ�oprice��" + oprice);
					}		
					if (rs7.next()){   
						cprice = rs7.getDouble("price"); 
						System.out.println("�õ�cprice��" + cprice);
					}

					rs7.close();

				}
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

	public static void main(String[] args) throws SQLException{
		ApplicationContext ctx = new FileSystemXmlApplicationContext("./WebRoot/WEB-INF/applicationContext.xml");    
		DataSource dataSource = (DataSource) ctx.getBean("dataSourceDev");

		System.out.println("�߳�1 ����......");
		MinKlineMakerRunner minKlineMakerRunner1 = new MinKlineMakerRunner();
		minKlineMakerRunner1.setBeginTimeString("2014��01��03��-20:53:00");
		minKlineMakerRunner1.setDataSource(dataSource);
		minKlineMakerRunner1.start();

		System.out.println("�߳�2 ����......");
		MinKlineMakerRunner minKlineMakerRunner2 =  new MinKlineMakerRunner();
		minKlineMakerRunner2.setBeginTimeString("2014��02��01��-19:06:00");
		minKlineMakerRunner2.setDataSource(dataSource);
		minKlineMakerRunner2.start();
		//			
		//				System.out.println("�߳�3 ����......");
		//				MinKlineMakerRunner minKlineMakerRunner3 =  new MinKlineMakerRunner();
		//				minKlineMakerRunner3.setBeginTimeString("2014��01��15��-00:00:00");
		//				minKlineMakerRunner3.setDataSource(dataSource);
		//				minKlineMakerRunner3.start();
		//				
		//				System.out.println("�߳�4 ����......");
		//				MinKlineMakerRunner minKlineMakerRunner4 =  new MinKlineMakerRunner();
		//				minKlineMakerRunner4.setBeginTimeString("2014��02��15��-00:00:00");
		//				minKlineMakerRunner4.setDataSource(dataSource);
		//				minKlineMakerRunner4.start();
	}

}
