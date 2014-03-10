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
	
	 /* ��ȡ���ݿ����ӵĺ���*/  
    public static Connection getConnection() {  
        Connection con = null;  //���������������ݿ��Connection����  
        try {  
            Class.forName("com.mysql.jdbc.Driver");// ����Mysql��������  
              
            con = DriverManager.getConnection(  
                    "jdbc:mysql://localhost:3306/btcrobot", "root", "123456");// ������������  
              
        } catch (Exception e) {  
            System.out.println("���ݿ�����ʧ��" + e.getMessage());  
        }  
        return con; //���������������ݿ�����  
    }  
	
	public boolean addKline(String period) throws SQLException{
		   
        conn = getConnection(); // ����Ҫ��ȡ���ӣ������ӵ����ݿ�  
  
        try {  
            String sql = "INSERT INTO kline" + period + "(date, time, oprice, hprice, lprice, cprice, volume, amount, id, ex2, ex3)VALUES(?,?,?,?,?,?,?,?,?,'1','1')";  // �������ݵ�sql���  
           
             
            st =  conn.prepareStatement(sql);    // ��������ִ�о�̬sql����Statement����  
            st.setString(1, kline.getDate());
            st.setString(2, kline.getTime());
            st.setDouble(3, kline.getOprice());
            st.setDouble(4, kline.getHprice());
            st.setDouble(5, kline.getLprice());
            st.setDouble(6, kline.getCprice());
            st.setDouble(7, kline.getVolume());
            st.setDouble(8, kline.getAmount());
            st.setLong(9, Long.parseLong(kline.getDate() + kline.getTime()));
            
              
            st.execute();  // ִ�в��������sql��䣬�����ز������ݵĸ���  
              
            //System.out.println("��test���в��� " + count + " ������"); //�����������Ĵ�����  
            
            this.addMaLine(period, 5);
            this.addMaLine(period, 10);
            this.addMaLine(period, 20);
            this.addMaLine(period, 30);
            this.addMaLine(period, 60);
            
            st.close();
            conn.close();   //�ر����ݿ�����  
              
        } catch (SQLException e) {  
            e.printStackTrace();
            st.close();
            conn.close();
            return false;
        }  
		System.out.println("�ɹ�����һ��");
		return true;
	}
    public boolean addMaLine(String period, int fineness) throws SQLException{
        conn = getConnection(); // ����Ҫ��ȡ���ӣ������ӵ����ݿ�  
        
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
             conn.close();   //�ر����ݿ�����  
        }
             
          } catch (SQLException e) {  
              e.printStackTrace();
              st.close();
              conn.close();
              return false;
          }  
  		System.out.println("�ɹ�����һ��");
  		return true;
        	 
    }
}
