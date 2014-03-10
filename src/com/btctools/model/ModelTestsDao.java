package com.btctools.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ModelTestsDao {
	static Connection conn;   
	static PreparedStatement st; 
	static PreparedStatement st_1;  
	static PreparedStatement st_2;  
	static boolean hourBegin = false;
	static boolean fomerLeverH = false;
	static boolean nowLever = false;
	static boolean minEnd = false;
	static boolean minOpen = true;//开始是开的
	static boolean fomerLeverM = true;

	/* 获取数据库连接的函数*/  
	public boolean checkHourBegin(long id, String width) throws SQLException{
		if(this.maCompare(id, width)){//当前判断是高的
			if(!ModelTestsDao.fomerLeverH){//如果原来是低的
				ModelTestsDao.fomerLeverH = true;//设置好实际情况是高的
				ModelTestsDao.hourBegin = true;
				System.out.println("开始点" + String.valueOf(id));
				return true;
			}else{
				ModelTestsDao.fomerLeverH = true;//无论如何设置成当前情况
				return false;
			}

		}else{//如果当前是低的，设置好实际情况，
			ModelTestsDao.fomerLeverH = false;
			return false;//返回没开始
		}

	}

	public boolean checkMinEnd(long id, String width) throws SQLException{
		if(ModelTestsDao.hourBegin){//如果是开口状态，再去判断是否结束点
			if(!this.maCompare(id, width)){//如果当前是低了，去判断是否要结束
				if(ModelTestsDao.fomerLeverM){//如果前一个是高的，那么就应该关闭窗口
					ModelTestsDao.fomerLeverM = false;
					ModelTestsDao.hourBegin = false;//关闭总窗口
					System.out.println("结束点" + String.valueOf(id));
					return true;
				}else{//如果当前是高的
					this.maCompare(id, width);//设置好当前状态是窗口没结束状态

					return false;
				}
			}else{//如果min线当前是高的
				ModelTestsDao.fomerLeverM = true;
				return false;
			}
		}else{
			ModelTestsDao.fomerLeverM = this.maCompare(id, width);
			return false;
		}
	}

	public static Connection getConnection() {  
		Connection con = null;  //创建用于连接数据库的Connection对象  
		try {  
			Class.forName("com.mysql.jdbc.Driver");// 加载Mysql数据驱动  

			con = DriverManager.getConnection(  
					"jdbc:mysql://localhost:3306/btcrobot", "root", "123456");// 创建数据连接  

		} catch (Exception e) {  
			System.out.println("数据库连接失败" + e.getMessage());  
		}  
		return con; //返回所建立的数据库连接  
	}  

	public boolean testPrice() throws SQLException{
		conn = getConnection(); // 首先要获取连接，即连接到数据库  

		try {  
			String sql = "select oprice, cprice, lprice,id from kline5min";  

			st = conn.prepareStatement(sql);

			ResultSet rs =  st.executeQuery();

			Double maxLost = 0.0;
			Double allLost = 0.0;
			Double stopPoint = -5.0;

			Double result = 0.0;
			int stopsell = 0;
			int closesell = 0;
			while (rs.next()){   
				Double oprice = rs.getDouble("oprice");  
				Double cprice = rs.getDouble("cprice"); 
				Double lprice = rs.getDouble("lprice"); 
				long id = rs.getLong("id"); 
				if(this.checkHourBegin(id, "hour")&&!this.checkMinEnd(id, "5min")){
					if(this.maCompare(id,"hour")&&this.maCompare(id, "5min")){//判断ma比较结果

						Double k = 0.0;
						//Double chazhi = lprice - oprice;

						//if (chazhi < stopPoint){
						//                                 k = stopPoint;//出现止损点s
						//                                 stopsell = stopsell + 1;
						//                                 System.out.println("止损卖掉日期" + String.valueOf(id));


						// }else{
						k = cprice - oprice;//收盘卖
						System.out.println("结算金额"+String.valueOf(k) + String.valueOf(id) + "cprice"+String.valueOf(cprice));
						closesell = closesell + 1;

						//}

						result = result + k;
					}}
			}


			System.out.println("result is " + String.valueOf(result));
			System.out.println("stopsell is " + String.valueOf(stopsell));
			System.out.println("closesell is " + String.valueOf(closesell));



			st.close();
			conn.close();   //关闭数据库连接  

		} catch (SQLException e) {  
			e.printStackTrace();
			st.close();
			conn.close();
			return false;
		}  

		return true;
	}



	public boolean maCompare(Long id, String width) throws SQLException{
		conn = getConnection(); // 首先要获取连接，即连接到数据库  
		Double ma5Value = 0.0;
		Double ma60Value = 0.0;
		//////////////////////////取得ma5的值/////////////////////////////////////               
		String sql_1 = "select value from kline"+width+"ma5 where id = ?";
		st_1 =  conn.prepareStatement(sql_1);
		st_1.setLong(1, id);

		ResultSet rs_1 =  st_1.executeQuery();
		while(rs_1.next()){
			ma5Value = rs_1.getDouble("value");
		}

		////////////////////////////////////////////////////////////////////////////  
		//////////////////////////取得ma60的值/////////////////////////////////////               
		String sql_2 = "select value from kline"+width+"ma10 where id = ?";
		st_2 =  conn.prepareStatement(sql_2);
		st_2.setLong(1, id);

		ResultSet rs_2 =  st_2.executeQuery();
		while(rs_2.next()){
			ma60Value = rs_2.getDouble("value");
		}

		//////////////////////////////////////////////////////////////////////////// 

		if(!rs_1.wasNull()&&!rs_2.wasNull()&&ma5Value > ma60Value){
			return true;
		}
		return false;
	}

	public static void main(String[] args) throws SQLException {
		ModelTestsDao mtd = new ModelTestsDao();
		mtd.testPrice();

	}

}
