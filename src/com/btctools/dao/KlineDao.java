package com.btctools.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;

import com.btctools.kline.Kline;

public class KlineDao {
	
    static Connection conn;   
    static PreparedStatement st;  

	public Kline getKline() {
		return kline;
	}

	public void setKline(Kline kline) {
		this.kline = kline;
	}
	private Kline kline;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	 /* 获取数据库连接的函数*/  
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
	
	public boolean addKline(String period) throws SQLException{
		   
        conn = getConnection(); // 首先要获取连接，即连接到数据库  
  
        try {  
            String sql = "INSERT INTO kline" + period + "(date, time, oprice, hprice, lprice, cprice, volume, amount, id, ex2, ex3)VALUES(?,?,?,?,?,?,?,?,?,'1','1')";  // 插入数据的sql语句  
           
             
            st =  conn.prepareStatement(sql);    // 创建用于执行静态sql语句的Statement对象  
            st.setString(1, kline.getDate());
            st.setString(2, kline.getTime());
            st.setDouble(3, kline.getOprice());
            st.setDouble(4, kline.getHprice());
            st.setDouble(5, kline.getLprice());
            st.setDouble(6, kline.getCprice());
            st.setDouble(7, kline.getVolume());
            st.setDouble(8, kline.getAmount());
            st.setLong(9, Long.parseLong(kline.getDate() + kline.getTime()));
            
              
            st.execute();  // 执行插入操作的sql语句，并返回插入数据的个数  
              
            //System.out.println("向test表中插入 " + count + " 条数据"); //输出插入操作的处理结果  
            
            this.addMaLine(period, 5);
            this.addMaLine(period, 10);
            this.addMaLine(period, 20);
            this.addMaLine(period, 30);
            this.addMaLine(period, 60);
            
            st.close();
            conn.close();   //关闭数据库连接  
              
        } catch (SQLException e) {  
            e.printStackTrace();
            st.close();
            conn.close();
            return false;
        }  
		System.out.println("成功插入一条");
		return true;
	}
    public boolean addMaLine(String period, int fineness) throws SQLException{
        conn = getConnection(); // 首先要获取连接，即连接到数据库  
        
        try {  
    	String sqlMa = "SELECT cprice FROM kline" + period + " where id < ? order by id DESC LIMIT 0,?";
        
        st =  conn.prepareStatement(sqlMa); 
        st.setLong(1, Long.parseLong(kline.getDate() + kline.getTime()));
        
        st.setInt(2, fineness-1);
        ResultSet rs = st.executeQuery();
        ArrayList<Double> cprices = new ArrayList<Double>();


        while (rs.next()){   
         cprices.add(rs.getDouble("cprice"));   
        }
        Double sum = 0.0;
        if(cprices.size() == fineness-1){
       		for(int j = 0; j < cprices.size();j++){
        		sum = sum + cprices.get(j);
        
        	}
        	sum = sum + kline.getCprice();
        	String sqlMaAdd="INSERT INTO kline"+period+"ma"+String.valueOf(fineness)+"(id, value, date, time)VALUES(?,?,?,?)";
        	//String sqlMaAdd="INSERT INTO kline001ma5(id, value, date, time)VALUES(?,?,?,?)";
        	 st =  conn.prepareStatement(sqlMaAdd); 
        	 st.setLong(1, Long.parseLong(kline.getDate() + kline.getTime()));
        	 st.setDouble(2,sum/fineness);
             st.setString(3, kline.getDate());
             st.setString(4, kline.getTime());
             st.execute();
             
             st.close();
             conn.close();   //关闭数据库连接  
        }
             
          } catch (SQLException e) {  
              e.printStackTrace();
              st.close();
              conn.close();
              return false;
          }  
  		System.out.println("成功插入一条");
  		return true;
        	 
    }
}
