package com.btctools.trade;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.btcrobot.runner.Tid;
import com.btctools.dao.TradeDao;

public class TradeCatcher {

	static Logger logger = LogManager.getLogger(); 
	static Logger fatalLoger = LogManager.getLogger("Fetal");  
	
	private TradeDao tradeDao;
	
	private Tools tools;	
	public Tools getTools() {
		return tools;
	}

	public void setTools(Tools tools) {
		this.tools = tools;
	}

	public TradeDao getTradeDao() {
		return tradeDao;
	}

	public void setTradeDao(TradeDao tradeDao) {
		this.tradeDao = tradeDao;
	}
	HashMap<String, Trade> tradeMapFormer = new HashMap<String, Trade>(); 

	public ArrayList<Trade> getHuoBiTradesByJson(){


		ArrayList<Trade> tradeMap = new ArrayList<Trade>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		JSONObject json;
		try {
			
			json = tools.getJsonObject("http://market.huobi.com/staticmarket/detail.html");

			logger.trace("开始连接http，请求json格式的交易数据");
			try {
				if(json==null){
					//System.out.println("火币网返回交易数据为空");
					return tradeMap;
				}else{
					JSONArray trades =  json.getJSONArray("trades");
					logger.trace("开始处理json数据");
					if(trades!=null){
						for(int i = 0; i < trades.length(); i++){
							String time = trades.getJSONObject(i).getString("time");
							String typeS = trades.getJSONObject(i).getString("type");
							int type = 1;
							if(typeS.equals("买入")){
								type = 0;
							}
							Double price = trades.getJSONObject(i).getDouble("price");
							Double amount = trades.getJSONObject(i).getDouble("amount");


							Date now = new Date(); 

							String today = sdf.format(now);
							long ctime = Long.parseLong(today + time.replaceAll(":", ""));
							Trade trade = new Trade();
							trade.setCtime(ctime);
							trade.setAmount(amount);
							trade.setPrice(price);
							trade.setType(type);

							tradeMap.add(trade);

						}
					}else{
						logger.warn("返回的交易数据JSONArray为空");
					}
				}

			} catch (JSONException e) {
				logger.error("得到json对象出错" + e.getMessage());
			}
		} catch (IOException e1) {
			logger.error("链接网络取json数据出错" + e1.getMessage());
			//e1.printStackTrace();
		}
		logger.trace("返回一个trades" + tradeMap.size());
		return tradeMap;

	}
	public ArrayList<Trade> getOkCoinTradesByJson(){


		ArrayList<Trade> tradeMap = new ArrayList<Trade>();

		JSONObject json;
		try {
			
			//if(Tid.tid==1L){
			
				//json = tools.getOkCoinJsonObject("https://www.okcoin.com/api/trades.do");
			//}else{
				json = tools.getOkCoinJsonObject("https://www.okcoin.com/api/trades.do?since=" + Tid.tid);
			//}
			logger.trace("开始连接http，请求json格式的交易数据");
			try {
				if(json==null){
					//System.out.println("火币网返回交易数据为空");
					return tradeMap;
				}else{
					JSONArray trades =  json.getJSONArray("trades");
					logger.trace("开始处理json数据");
					if(trades!=null){
						for(int i = 0; i < trades.length(); i++){
							long time = trades.getJSONObject(i).getLong("date");
							long tid = trades.getJSONObject(i).getLong("tid");
							
							String typeS = trades.getJSONObject(i).getString("type");
							int type = 1;
							if(typeS.equals("buy")){
								type = 0;
							}
							Double price = trades.getJSONObject(i).getDouble("price");
							Double amount = trades.getJSONObject(i).getDouble("amount");
							
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							Date date = new Date(time * 1000);
							long actualCloseTime = Long.valueOf(sdf.format(date));
							//System.out.println(actualCloseTime);


							Date now = new Date(); 

							Trade trade = new Trade();
							trade.setCtime(actualCloseTime);
							trade.setAmount(amount);
							trade.setPrice(price);
							trade.setType(type);
							trade.setTid(tid);
							Tid.tid = tid;
							//System.out.println(tid);
							tradeMap.add(trade);

						}
					}else{
						logger.warn("返回的交易数据JSONArray为空");
					}
				}

			} catch (JSONException e) {
				logger.error("得到json对象出错" + e.getMessage());
			}
		} catch (IOException e1) {
			logger.error("链接网络取json数据出错" + e1.getMessage());
			//e1.printStackTrace();
		}
		logger.trace("返回一个trades" + tradeMap.size());
		return tradeMap;

	}

	public HashMap<String, Trade> getHuoBiTrades() throws Exception{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HashMap<String, Trade> tradeMap = new HashMap<String, Trade>(); 

		try {
			HttpPost httpPost = new HttpPost("https://www.huobi.com/trade/index.php?a=history");
			httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");;
			httpPost.setHeader("Accept-Encoding","gzip,deflate,sdch");
			httpPost.setHeader("Accept-Language","zh-CN,zh;q=0.8");
			httpPost.setHeader("Cache-Control","keep-alive");
			httpPost.setHeader("Cookie","HUOBIMEIBISESSID=default_a7f8d721d8187b9246cba7d262dc3162; SS_ID=DE; DS_ID=DE; CNZZDATA5565080=cnzz_eid%3D51849190-1388583972-%26ntime%3D1392966769%26cnzz_a%3D44%26sin%3Dnone%26ltime%3D1392947398225%26rtime%3D8; Hm_lvt_096fb4146d7430c291b5a3f654089c29=1392712028,1392877516,1392886496,1392947398; Hm_lpvt_096fb4146d7430c291b5a3f654089c29=1392966833");
			httpPost.setHeader("Host","www.huobi.com");
			httpPost.setHeader("Referer","https://www.huobi.com/trade/index.php");
			httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
			//httpPost.setHeader("Accept","gzip");

			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("a", "history"));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));

			CloseableHttpResponse response1 = httpclient.execute(httpPost);


			try {
				//System.out.println(response1.getStatusLine());
				HttpEntity entity1 = response1.getEntity();

				InputStream in = entity1.getContent();
				in.skip(15);

				StringBuffer out = new StringBuffer(); 
				byte[] b = new byte[4096]; 
				for(int n; (n = in.read(b))!= -1;) { 
					out.append(new String(b, 0, n,"utf-8")); 
				} 
				String result = out.toString(); 


				Document doc = Jsoup.parse(result.trim());


				Elements elements = doc.select("table.table-hover");
				Element body = elements.get(0);

				Elements trs = body.child(1).children();



				for(int i=0; i < trs.size(); i++){
					Elements tds = trs.get(i).children();

					////                	System.out.println(tds.get(1).text());
					//                	System.out.println(tds.get(2).text());
					//                	System.out.println(tds.get(3).text());
					//                	System.out.println(tds.get(4).text());

					///////////////加入数据库//////////////////////

					long ctime = Long.parseLong(tds.get(0).text().replace("年", "").replace("月", "").replace("日", "").replace(":", "").replace(" ", ""));                	
					int type = tds.get(1).text().equals("买入")?0:1;
					Double price =Double.valueOf(tds.get(2).text().substring(1,tds.get(2).text().length()));
					Double amount =Double.valueOf(tds.get(3).text().substring(1,tds.get(3).text().length()));
					Trade trade = new Trade();
					trade.setCtime(ctime);
					trade.setAmount(amount);
					trade.setPrice(price);
					trade.setType(type);
					tradeMap.put(String.valueOf(ctime) + String.valueOf(amount) + String.valueOf(price) + String.valueOf(type), trade);


				}


			} finally {
				response1.close();

			}

		} finally {
			httpclient.close();
		}
		//System.out.println("返回一个trades" + tradeMap.size());
		return tradeMap;
	}

	public boolean getHuoBiTrade(BlockingQueue<Trade> tradesQueue) throws Exception{
		ArrayList<Trade> tradesNow = this.getHuoBiTradesByJson();

		if(tradesNow.size()==0){
			logger.warn("本次沒有得到交易集合");
			return false;
		}

		int j = 0;
		//System.out.println("开始插入数据库操作");
			for(int i = 0; i < tradesNow.size();i++){

			Trade trade = tradesNow.get(i);
			
			//logger.trace("开始调用插入数据库操作程序");
			
			tradesQueue.add(trade);
			//System.out.println("队列的长度： " + tradesQueue.size());
			
		}



		return true;
	}
	public boolean getOkCoinTrade(BlockingQueue<Trade> tradesQueue) throws Exception{
		ArrayList<Trade> tradesNow = this.getOkCoinTradesByJson();

		if(tradesNow.size()==0){
			logger.warn("本次沒有得到交易集合");
			return false;
		}

		int j = 0;
		//System.out.println("开始插入数据库操作");
			for(int i = 0; i < tradesNow.size();i++){

			Trade trade = tradesNow.get(i);
			
			//logger.trace("开始调用插入数据库操作程序");
			
			tradesQueue.add(trade);
			System.out.println("加入队列： " + trade.getTid());
			
		}



		return true;
	}
	public static void main(String[] args) throws Exception {

//		TradeCatcher catcher = new TradeCatcher(); 
//
//		//while(true){
//			try{
//				if(!catcher.getHuoBiTrade()){
//					logger.error("主runner中的主逻辑調用返回false");
//					//continue;
//				}
//			}catch(Exception e){
//				logger.error("主runner中的主逻辑調用返回false" + e.getMessage());
//				//continue;
//
//			}
//			logger.debug("server开始运转");
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				fatalLoger.fatal("主runner被打断" + e.getMessage());
//			}
			
		}

	//} 
	

}
