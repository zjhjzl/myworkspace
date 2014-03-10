package com.btcrobot.ma;

import java.sql.SQLException;

public interface MaDao {
	
	public Double getClosePrice(long lastMinutesTime) throws SQLException;
	public Double getOpenPrice(long closeTime) throws SQLException; 
	public Double getLowPrice(long closeTime) throws SQLException;
	public Double getHighPrice(long closeTime) throws SQLException;

}
