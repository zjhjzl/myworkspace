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
		logger.trace("spring 给设置数据源了");
		this.dataSource = dataSource;
	}


	//    public static Connection getConnection() {  
	//        Connection con = null;  //创建用于连接数据库的Connection对象  
	//        XMLConfiguration config = null;
	//		try {
	//			config = new XMLConfiguration("config.xml");
	//			
	//		} catch (ConfigurationException e1) {
	//			fatalLogger.fatal("配置文件没有找到");
	//			e1.printStackTrace();
	//		}
	//        try {  
	//            Class.forName("com.mysql.jdbc.Driver");// 加载Mysql数据驱动  
	//            
	//            String DB_URL = config.getString("mysql.url");
	//            String DB_USER = config.getString("mysql.user");
	//            String DB_PASSWORD = config.getString("mysql.pwd");
	//            con = DriverManager.getConnection(  
	//            		DB_URL, DB_USER, DB_PASSWORD);// 创建数据连接  
	// 
	//              
	//        } catch (Exception e) {  
	//        	fatalLogger.fatal("数据库连接失败" + e.getMessage());
	//        }  
	//        return con; //返回所建立的数据库连接  
	//    }  

	public boolean addTrade(String exchangeName,Trade trade) throws SQLException{
		logger.debug("进入addTradeDao1");
		//conn = getConnection(); // 首先要获取连接，即连接到数据库  //纯jdbc数据源
		try {
			conn = this.dataSource.getConnection();
			logger.debug("进入addTrade2Dap");

			//conn = DataSourceUtil.getDataSource(1).getConnection();//可选外部数据源durid
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

			logger.debug("开始执行插入语句");

			st.execute();  // 执行插入操作的sql语句，并返回插入数据的个数  

			logger.debug("结束执行插入语句");
			st.close();
			conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			logger.warn("本条交易数据没有插入成功" + e.getMessage());
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
		System.out.println("找配置文件");
		XMLConfiguration config = new XMLConfiguration("config.xml");
		System.out.println(config.getString("mysql.user"));

	}

}
