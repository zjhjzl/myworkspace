package com.btctools.price;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.btctools.dao.KlineDao;
import com.btctools.kline.Kline;

public class Hands {
	
	private KlineDao klineDao = new KlineDao();
	private Kline kline = new Kline();
	private Map<String, String> keys = new HashMap<String, String>(){
	    {
	        put("1min", "001");
	        put("5min", "005");
	        put("15min", "015");
	        put("30min", "030");
	        put("hour", "060");
	        put("day", "100");
	        put("week", "200");
	        put("month", "300");
	        put("year", "400");
	        }            
	    };
	
	public Map<String, String> getKeys() {
		return keys;
	}

	public void setKeys(Map<String, String> keys) {
		this.keys = keys;
	}

	public boolean getKline(String period) throws IOException, SQLException{
		
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpGet = new HttpGet("http://market.huobi.com/staticmarket/kline"+ this.keys.get(period) +".html");
            CloseableHttpResponse response1 = httpclient.execute(httpGet);


            try {
                System.out.println(response1.getStatusLine());
                HttpEntity entity1 = response1.getEntity();
                
                InputStream in = entity1.getContent();
                in.skip(22);
                
                StringBuffer out = new StringBuffer(); 
                byte[] b = new byte[4096]; 
                for(int n; (n = in.read(b))!= -1;) { 
                        out.append(new String(b, 0, n,"utf-8")); 
                } 
                String result = out.toString(); 

                String[] lines = result.split("\n");
                String[] keys = null;
                int lineCount = lines.length;
                for(int i = 0;i < lineCount; i++){
                	keys = lines[i].split(",");
                	kline.setDate(keys[0]);
                	kline.setTime(keys[1]);
                	kline.setOprice(Double.parseDouble(keys[2]));
                	kline.setHprice(Double.parseDouble(keys[3]));
                	kline.setLprice(Double.parseDouble(keys[4]));
                	kline.setCprice(Double.parseDouble(keys[5]));
                	kline.setVolume(Double.parseDouble(keys[6]));
                	kline.setAmount(Double.parseDouble(keys[7]));
                	
                	klineDao.setKline(kline);
                	klineDao.addKline(period);
                	
                	
                }
                } finally {
                response1.close();
                
            }

        } finally {
            httpclient.close();
        }
    

		
		return true;
	}
	
	public boolean getOkCoinTrades(){
		
		return true;
	}

	public static void main(String[] args) throws SQLException {
		
		Hands hand = new Hands();

		try {
			hand.getKline("1min");
			hand.getKline("5min");
			hand.getKline("15min");
			hand.getKline("30min");
			hand.getKline("hour");
			hand.getKline("day");
			hand.getKline("week");
			hand.getKline("month");
			hand.getKline("year");
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

}
