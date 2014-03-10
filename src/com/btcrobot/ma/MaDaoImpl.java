package com.btcrobot.ma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MaDaoImpl implements MaDao {

	static Connection conn;   
	static PreparedStatement st;  

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private DataSource dataSource;

	static Logger logger = LogManager.getLogger(); 
	static Logger fatalLogger = LogManager.getLogger("Fetal");  

	@Override
	public Double getClosePrice(long closeTime) throws SQLException {
		conn = this.dataSource.getConnection(); // 首先要获取连接，即连接到数据库  
		Double cprice = 0.0;
//		Double oprice = 0.0;
//		Double hprice = 0.0;
//		Double lprice = 0.0;
//		Double amount = 0.0;

		try {  

			//select * from btcrobot.hbtrade where ctime <= 20140225133944 order by ctime desc limit 1;
			String sql = "select cprice from btcrobot.kline1min where date = ?";           

			st =  conn.prepareStatement(sql);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTime);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs = st.executeQuery();

			while (rs.next()){   
				cprice = rs.getDouble("cprice"); 
			}
			
//			//下面找oprice
//			
//			String sql2 = "select price,tid from btcrobot.hbtrade where ctime >= ? order by tid limit 1";           
//
//			st =  conn.prepareStatement(sql2);    // 创建用于执行静态sql语句的Statement对象  
//
//			st.setLong(1, closeTimePoint);
//
//			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  
//
//			ResultSet rs2 = st.executeQuery();
//
//			while (rs2.next()){   
//				oprice = rs2.getDouble("price"); 
//			}
//			//下面找 hprice和lprice
//			
//			String sql3 = "select max(price) as hprice,min(price) as lprice, sum(amount) as amount from btcrobot.hbtrade where ctime >= ? and ctime < ?";           
//
//			st =  conn.prepareStatement(sql3);    // 创建用于执行静态sql语句的Statement对象  
//
//			st.setLong(1, closeTimePoint);
//			st.setLong(2, closeTime);
//
//			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  
//
//			ResultSet rs3 = st.executeQuery();
//
//			while (rs3.next()){   
//				hprice = rs3.getDouble("hprice"); 
//				lprice = rs3.getDouble("lprice");
//				amount = rs3.getDouble("amount");
//			}
//			
//			//插入k线图。
//			
//			 String sql4 = "INSERT INTO kline1min(date, oprice, hprice, lprice, cprice, amount, volume, tid, ext2)VALUES(?,?,?,?,?,?,'0.0','1','1')";  // 插入数据的sql语句  
//	           
//             
//	            st =  conn.prepareStatement(sql4);    // 创建用于执行静态sql语句的Statement对象  
//	            st.setLong(1, closeTimePoint);
//	            st.setDouble(2, oprice);
//	            st.setDouble(3, hprice);
//	            st.setDouble(4, lprice);
//	            st.setDouble(5, cprice);
//	            st.setDouble(6, amount);
//
//	            System.out.println("插入操作");
//	              
//	            st.execute();  // 执行插入操作的sql语句，并返回插入数据的个数  
//	         
			
			rs.close();
			st.close();
			conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			e.printStackTrace();
			logger.warn("本条交易数据没有插入成功" + e.getMessage());
			st.close();
			conn.close();
			return cprice;
		}finally{
			st.close();
			conn.close();
		}
		return cprice;

	}
	
	public Double getOpenPrice(long closeTime) throws SQLException {
		conn = this.dataSource.getConnection(); // 首先要获取连接，即连接到数据库  
		Double oprice = 0.0;

		try {  

			//select * from btcrobot.hbtrade where ctime <= 20140225133944 order by ctime desc limit 1;
			String sql = "select oprice from btcrobot.kline1min where date = ?";           

			st =  conn.prepareStatement(sql);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTime);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs = st.executeQuery();

			while (rs.next()){   
				oprice = rs.getDouble("oprice"); 
			}
			
			rs.close();
			st.close();
			conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			e.printStackTrace();
			logger.warn("本条交易数据没有插入成功" + e.getMessage());
			st.close();
			conn.close();
			return oprice;
		}finally{
			st.close();
			conn.close();
		}
		return oprice;

	}
	public Double getHighPrice(long closeTime) throws SQLException {
		conn = this.dataSource.getConnection(); // 首先要获取连接，即连接到数据库  
		Double hprice = 0.0;

		try {  

			//select * from btcrobot.hbtrade where ctime <= 20140225133944 order by ctime desc limit 1;
			String sql = "select hprice from btcrobot.kline1min where date = ?";           

			st =  conn.prepareStatement(sql);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTime);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs = st.executeQuery();

			while (rs.next()){   
				hprice = rs.getDouble("hprice"); 
			}
			
			rs.close();
			st.close();
			conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			e.printStackTrace();
			logger.warn("本条交易数据没有插入成功" + e.getMessage());
			st.close();
			conn.close();
			return hprice;
		}finally{
			st.close();
			conn.close();
		}
		return hprice;

	}
	public Double getLowPrice(long closeTime) throws SQLException {
		conn = this.dataSource.getConnection(); // 首先要获取连接，即连接到数据库  
		Double lprice = 0.0;

		try {  

			//select * from btcrobot.hbtrade where ctime <= 20140225133944 order by ctime desc limit 1;
			String sql = "select lprice from btcrobot.kline1min where date = ?";           

			st =  conn.prepareStatement(sql);    // 创建用于执行静态sql语句的Statement对象  

			st.setLong(1, closeTime);

			st.executeQuery();  // 执行插入操作的sql语句，并返回插入数据的个数  

			ResultSet rs = st.executeQuery();

			while (rs.next()){   
				lprice = rs.getDouble("lprice"); 
			}
			
			rs.close();
			st.close();
			conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			e.printStackTrace();
			logger.warn("本条交易数据没有插入成功" + e.getMessage());
			st.close();
			conn.close();
			return lprice;
		}finally{
			st.close();
			conn.close();
		}
		return lprice;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
