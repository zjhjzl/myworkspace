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
		logger.trace("spring ����������Դ��");
		System.out.println("spring ����������Դ��");
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
				System.out.println("�����߶��еĳ���" + this.tradesQueue.size());
				
				//���ｫ��Ҫ����ʵʱ�ĸ���MA��ֵ���������жϡ�

				String sql = "INSERT INTO hbtrade(type, amount, price, ctime, tid)VALUES(?,?,?,?,?)";

				System.currentTimeMillis();

				st =  conn.prepareStatement(sql);  
				st.setInt(1, trade.getType());
				st.setDouble(2, trade.getAmount());
				st.setDouble(3, trade.getPrice());
				st.setLong(4, trade.getCtime());
				st.setLong(5, trade.getTid());


				st.execute();  // ִ�в��������sql��䣬�����ز������ݵĸ���  
				
				//�ж�ctime�ǲ��ǿ����һ�����ӽ��ޣ�����ǵĻ�����ô��Ҫ��һ�������ߵ����ӡ�
				
				//20140307190000  20140307190034
				
//				if(Tid.ctime==0L){
//					System.out.println("��һ�θ���ֵ��");
//					Tid.ctime = trade.getCtime();
//				}else{
//					System.out.println("�ڶ��ν�����" + Tid.ctime);
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
//					if(timeSpanMillis > 0 && timeSpanMillis < 60000){//ȷ�ϲ��û��һ���ӣ�Ҳ�ͱ�֤�˲�ȱkline�����������и�ǰ�ᣬ����ÿ���Ӷ��н��ס������֪���ں��ҹ�ܷ�ʵ�֡�
//						System.out.println("�ж�" + timeSpanMillis);
//						if (((nowTimeMillise/60000)-(previousTimeMillise/60000))==1){//˵��������ˣ�Ҫ������kline
//							long klineMinTimePoint = DateTools.getCtimeFromMillise(previousTimeMillise)/100*100;
//							long klineMinTimeClose = DateTools.getCtimeFromMillise(nowTimeMillise)/100*100;
//							
//							System.out.println(klineMinTimePoint + "&&" + klineMinTimeClose);
//							
//							this.addKline(klineMinTimePoint, klineMinTimeClose);
//							System.out.println("���ӷ���kline��ʱ���ˣ�����һ��" + klineMinTimePoint);
//						}
//					}else{//���ȱ�Ļ�
//						
//					}
//					
//					
//				}
//
//				Tid.ctime = trade.getCtime();
			//st.close();
			//conn.close();   //�ر����ݿ�����  

		} catch (SQLException e) {  
			e.printStackTrace();
			//System.out.println("������������û�в���ɹ�" + e.getMessage());
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
		//conn = this.dataSource.getConnection(); // ����Ҫ��ȡ���ӣ������ӵ����ݿ�  
		Double cprice = 0.0;
		Double oprice = 0.0;
		Double hprice = 0.0;
		Double lprice = 0.0;
		Double amount = 0.0;
		try {  

			//select * from btcrobot.hbtrade where ctime <= 20140225133944 order by ctime desc limit 1;
			String sql = "select price,tid from btcrobot.hbtrade where ctime < ? order by tid desc limit 1";           

			st =  conn.prepareStatement(sql);    // ��������ִ�о�̬sql����Statement����  

			st.setLong(1, closeTime);

			st.executeQuery();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

			ResultSet rs = st.executeQuery();

			while (rs.next()){   
				cprice = rs.getDouble("price"); 
			}
			
			//������oprice
			
			String sql2 = "select price,tid from btcrobot.hbtrade where ctime >= ? order by tid limit 1";           

			st =  conn.prepareStatement(sql2);    // ��������ִ�о�̬sql����Statement����  

			st.setLong(1, closeTimePoint);

			st.executeQuery();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

			ResultSet rs2 = st.executeQuery();

			while (rs2.next()){   
				oprice = rs2.getDouble("price"); 
			}
			//������ hprice��lprice
			
			String sql3 = "select max(price) as hprice,min(price) as lprice, sum(amount) as amount from btcrobot.hbtrade where ctime >= ? and ctime < ?";           

			st =  conn.prepareStatement(sql3);    // ��������ִ�о�̬sql����Statement����  

			st.setLong(1, closeTimePoint);
			st.setLong(2, closeTime);

			st.executeQuery();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

			ResultSet rs3 = st.executeQuery();

			while (rs3.next()){   
				hprice = rs3.getDouble("hprice"); 
				lprice = rs3.getDouble("lprice");
				amount = rs3.getDouble("amount");
			}
			
			//����k��ͼ��
			
			 String sql4 = "INSERT INTO kline1min(date, oprice, hprice, lprice, cprice, amount, volume, tid, ext2)VALUES(?,?,?,?,?,?,'0.0','1','1')";  // �������ݵ�sql���  
	           
             
	            st =  conn.prepareStatement(sql4);    // ��������ִ�о�̬sql����Statement����  
	            st.setLong(1, closeTimePoint);
	            st.setDouble(2, oprice);
	            st.setDouble(3, hprice);
	            st.setDouble(4, lprice);
	            st.setDouble(5, cprice);
	            st.setDouble(6, amount);

	            System.out.println("�������");
	              
	            st.execute();  // ִ�в��������sql��䣬�����ز������ݵĸ���  
	         
			
			rs.close();
			rs2.close();
			rs3.close();
//			st.close();
//			conn.close();   //�ر����ݿ�����  

		} catch (SQLException e) {  
			e.printStackTrace();
			logger.warn("������������û�в���ɹ�" + e.getMessage());
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
