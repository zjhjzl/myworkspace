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
	static boolean minOpen = true;//��ʼ�ǿ���
	static boolean fomerLeverM = true;

	/* ��ȡ���ݿ����ӵĺ���*/  
	public boolean checkHourBegin(long id, String width) throws SQLException{
		if(this.maCompare(id, width)){//��ǰ�ж��Ǹߵ�
			if(!ModelTestsDao.fomerLeverH){//���ԭ���ǵ͵�
				ModelTestsDao.fomerLeverH = true;//���ú�ʵ������Ǹߵ�
				ModelTestsDao.hourBegin = true;
				System.out.println("��ʼ��" + String.valueOf(id));
				return true;
			}else{
				ModelTestsDao.fomerLeverH = true;//����������óɵ�ǰ���
				return false;
			}

		}else{//�����ǰ�ǵ͵ģ����ú�ʵ�������
			ModelTestsDao.fomerLeverH = false;
			return false;//����û��ʼ
		}

	}

	public boolean checkMinEnd(long id, String width) throws SQLException{
		if(ModelTestsDao.hourBegin){//����ǿ���״̬����ȥ�ж��Ƿ������
			if(!this.maCompare(id, width)){//�����ǰ�ǵ��ˣ�ȥ�ж��Ƿ�Ҫ����
				if(ModelTestsDao.fomerLeverM){//���ǰһ���Ǹߵģ���ô��Ӧ�ùرմ���
					ModelTestsDao.fomerLeverM = false;
					ModelTestsDao.hourBegin = false;//�ر��ܴ���
					System.out.println("������" + String.valueOf(id));
					return true;
				}else{//�����ǰ�Ǹߵ�
					this.maCompare(id, width);//���úõ�ǰ״̬�Ǵ���û����״̬

					return false;
				}
			}else{//���min�ߵ�ǰ�Ǹߵ�
				ModelTestsDao.fomerLeverM = true;
				return false;
			}
		}else{
			ModelTestsDao.fomerLeverM = this.maCompare(id, width);
			return false;
		}
	}

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

	public boolean testPrice() throws SQLException{
		conn = getConnection(); // ����Ҫ��ȡ���ӣ������ӵ����ݿ�  

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
					if(this.maCompare(id,"hour")&&this.maCompare(id, "5min")){//�ж�ma�ȽϽ��

						Double k = 0.0;
						//Double chazhi = lprice - oprice;

						//if (chazhi < stopPoint){
						//                                 k = stopPoint;//����ֹ���s
						//                                 stopsell = stopsell + 1;
						//                                 System.out.println("ֹ����������" + String.valueOf(id));


						// }else{
						k = cprice - oprice;//������
						System.out.println("������"+String.valueOf(k) + String.valueOf(id) + "cprice"+String.valueOf(cprice));
						closesell = closesell + 1;

						//}

						result = result + k;
					}}
			}


			System.out.println("result is " + String.valueOf(result));
			System.out.println("stopsell is " + String.valueOf(stopsell));
			System.out.println("closesell is " + String.valueOf(closesell));



			st.close();
			conn.close();   //�ر����ݿ�����  

		} catch (SQLException e) {  
			e.printStackTrace();
			st.close();
			conn.close();
			return false;
		}  

		return true;
	}



	public boolean maCompare(Long id, String width) throws SQLException{
		conn = getConnection(); // ����Ҫ��ȡ���ӣ������ӵ����ݿ�  
		Double ma5Value = 0.0;
		Double ma60Value = 0.0;
		//////////////////////////ȡ��ma5��ֵ/////////////////////////////////////               
		String sql_1 = "select value from kline"+width+"ma5 where id = ?";
		st_1 =  conn.prepareStatement(sql_1);
		st_1.setLong(1, id);

		ResultSet rs_1 =  st_1.executeQuery();
		while(rs_1.next()){
			ma5Value = rs_1.getDouble("value");
		}

		////////////////////////////////////////////////////////////////////////////  
		//////////////////////////ȡ��ma60��ֵ/////////////////////////////////////               
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
