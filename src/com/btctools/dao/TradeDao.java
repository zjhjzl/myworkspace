package com.btctools.dao;

import java.sql.Connection;
import java.sql.Date;
//import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;



//import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.springframework.jdbc.core.JdbcTemplate;



import com.btctools.trade.Trade;

public class TradeDao {


	static Connection conn;   
	static PreparedStatement st;  

	private DataSource dataSource;
	//private JdbcTemplate jdbcTemplate;

	static Logger logger = LogManager.getLogger(); 
	static Logger fatalLogger = LogManager.getLogger("Fetal");  

	public void setDataSource(DataSource dataSource) {
		logger.trace("spring ����������Դ��");
		this.dataSource = dataSource;
	}


	//    public static Connection getConnection() {  
	//        Connection con = null;  //���������������ݿ��Connection����  
	//        XMLConfiguration config = null;
	//		try {
	//			config = new XMLConfiguration("config.xml");
	//			
	//		} catch (ConfigurationException e1) {
	//			fatalLogger.fatal("�����ļ�û���ҵ�");
	//			e1.printStackTrace();
	//		}
	//        try {  
	//            Class.forName("com.mysql.jdbc.Driver");// ����Mysql��������  
	//            
	//            String DB_URL = config.getString("mysql.url");
	//            String DB_USER = config.getString("mysql.user");
	//            String DB_PASSWORD = config.getString("mysql.pwd");
	//            con = DriverManager.getConnection(  
	//            		DB_URL, DB_USER, DB_PASSWORD);// ������������  
	// 
	//              
	//        } catch (Exception e) {  
	//        	fatalLogger.fatal("���ݿ�����ʧ��" + e.getMessage());
	//        }  
	//        return con; //���������������ݿ�����  
	//    }  

	public boolean addTrade(String exchangeName,Trade trade) throws SQLException{
		logger.debug("����addTradeDao1");
		//conn = getConnection(); // ����Ҫ��ȡ���ӣ������ӵ����ݿ�  //��jdbc����Դ
		try {
			conn = this.dataSource.getConnection();
			logger.debug("����addTrade2Dap");

			//conn = DataSourceUtil.getDataSource(1).getConnection();//��ѡ�ⲿ����Դdurid
		} catch (Exception e1) {

			e1.printStackTrace();
		}
		try {  

			String sql = "INSERT INTO " + exchangeName + "trade(type, amount, price, ctime, tid)VALUES(?,?,?,?,?)";

			 System.currentTimeMillis();

			st =  conn.prepareStatement(sql);  
			st.setInt(1, trade.getType());
			st.setDouble(2, trade.getAmount());
			st.setDouble(3, trade.getPrice());
			st.setLong(4, trade.getCtime());
			st.setLong(5, System.currentTimeMillis());

			logger.debug("��ʼִ�в������");

			st.execute();  // ִ�в��������sql��䣬�����ز������ݵĸ���  

			logger.debug("����ִ�в������");
			st.close();
			conn.close();   //�ر����ݿ�����  

		} catch (SQLException e) {  
			logger.warn("������������û�в���ɹ�" + e.getMessage());
			st.close();
			conn.close();
			return false;
		}finally{
			st.close();
			conn.close();
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("�������ļ�");
		XMLConfiguration config = new XMLConfiguration("config.xml");
		System.out.println(config.getString("mysql.user"));

	}

}
