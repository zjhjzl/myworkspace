package com.btctools.trade;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.btcrobot.runner.HttpClientPool;

public class Tools {

	byte[] b = new byte[4096];
	public JSONObject getJsonObject(String url) throws IOException {  

		CloseableHttpClient httpclient1 = HttpClientPool.getHttpClient();

        HttpGet get = new HttpGet(url);  
        JSONObject json = null;  
        CloseableHttpResponse res = null;
        try {  
        	//System.out.println("开始从网上取数据");
        	RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(400).setConnectTimeout(400).build();
        	get.setConfig(requestConfig);
        	res = httpclient1.execute(get);
            //System.out.println("结束从网上取数据");
            
            //有的时候服务器返回的是502，那数据肯定是空。
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	
                HttpEntity entity = res.getEntity();  
                InputStream in = entity.getContent();
                
                StringBuffer out = new StringBuffer(); 
                byte[] b = new byte[4096]; 
                for(int n; (n = in.read(b))!= -1;) { 
                        out.append(new String(b, 0, n,"utf-8")); 
                } 
                String result = out.toString(); 
                
                int begin = result.indexOf("trades");
                int end = result.indexOf("p_new");

                result = "{\"trades\":[" + result.substring(begin+9, end-3) + "]}";
                
                json = new JSONObject(new JSONTokener(result));
            }  
       
            
        } catch (Exception e) {  
            //e.printStackTrace();
              
        } finally{  
            //关闭连接释放资源  
        	try{
        	res.close();//response关闭后，httpclient1,不用关闭了，在重复使用好像。但不关闭response是不行的，就会都停止了。
        	}catch(Exception e){
        		
        	}
        	//httpclient1.close();//如果close就只能运行两次
        }  
        return json;  
    }  
	
	public JSONObject getOkCoinJsonObject(String url) throws IOException {  

		CloseableHttpClient httpclient1 = HttpClientPool.getHttpClient();

        HttpGet get = new HttpGet(url);  
        JSONObject json = null;  
        CloseableHttpResponse res = null;
        try {  
        	//System.out.println("开始从网上取数据");
        	RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(400).setConnectTimeout(400).build();
        	get.setConfig(requestConfig);
        	res = httpclient1.execute(get);
            //System.out.println("结束从网上取数据");
            
            //有的时候服务器返回的是502，那数据肯定是空。
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	
                HttpEntity entity = res.getEntity();  
                InputStream in = entity.getContent();
                
                StringBuffer out = new StringBuffer(); 
                
                for(int n; (n = in.read(b))!= -1;) { 
                        out.append(new String(b, 0, n,"utf-8")); 
                } 
                String result = out.toString(); 
                

                result = "{\"trades\":" + result + "}";
                
                json = new JSONObject(new JSONTokener(result));
            }  
       
            
        } catch (Exception e) {  
            //e.printStackTrace();
              
        } finally{  
            //关闭连接释放资源  
        	try{
        	res.close();//response关闭后，httpclient1,不用关闭了，在重复使用好像。但不关闭response是不行的，就会都停止了。
        	}catch(Exception e){
        		
        	}
        	//httpclient1.close();//如果close就只能运行两次
        }  
        return json;  
    } 

	public static void main(String[] args) {
//		Tools tools = new Tools();
//		try {
//			JSONObject json = tools.getOkCoinJsonObject("https://www.okcoin.com/api/trades.do");
//		    //try {
//		    	JSONArray trades =  json.getJSONArray("trades");
//		    	System.out.println("json size : " + trades.length());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(1393938909L*1000);
		long actualCloseTime = Long.valueOf(sdf.format(date));
		System.out.println(actualCloseTime);
	}

}
