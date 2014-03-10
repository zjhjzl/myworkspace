package com.btcrobot.runner;

import java.util.concurrent.BlockingQueue;

import javax.servlet.ServletConfig;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.btctools.trade.Trade;
import com.btctools.trade.TradeCatcher;

public class Runner extends Thread{
	
	static Logger logger = LogManager.getLogger(); 
	static Logger fatalLoger = LogManager.getLogger("Fetal");  
	private TradeCatcher catcher;
	private final BlockingQueue<Trade> tradesQueue;
	
	
	public Runner(String threadName, TradeCatcher catcher, BlockingQueue<Trade> queue) {
		
        super(threadName);
        this.catcher = catcher;
        this.tradesQueue = queue;
    }
	
	
 
    public void run() {
    	logger.info("主程序启动......");
   

		while(true){
			

			try{
				if(!catcher.getOkCoinTrade(tradesQueue)){
					//logger.error("主runner中的主逻辑調用返回false");
					continue;
				}
			}catch(Exception e){
				//e.printStackTrace();
				continue;

			}
			logger.debug("server开始运转");
		}
		//System.out.println("runner跳出循环");
    }

}
