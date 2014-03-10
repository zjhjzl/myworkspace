package com.btcrobot.runner;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.btctools.trade.Trade;
import com.btctools.trade.TradeCatcher;

public class StartUp extends HttpServlet{

	private static final long serialVersionUID = 1L;
	static Logger logger = LogManager.getLogger(); 
	static Logger fatalLoger = LogManager.getLogger("Fetal");  
	TradeCatcher catcher;
	@Override
	public void init(ServletConfig servletConfig) throws ServletException{
		
		BlockingQueue<Trade> tradesQueue = new ArrayBlockingQueue<Trade>(10000);
		
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletConfig.getServletContext());  
		this.catcher = ctx.getBean(TradeCatcher.class);

		new Runner("A",catcher,tradesQueue).start();
		
        System.out.println("启动线程A.....");
		
		//new Runner("B",catcher,tradesQueue).start();
		//System.out.println("启动线程B.....");

		
		//new Runner("C",catcher,tradesQueue).start();
		//System.out.println("启动线程C.....");
		
		
		//new Runner("D",catcher,tradesQueue).start();
		//System.out.println("启动线程D.....");
		
		TradeConsumer tradeConsumer = ctx.getBean(TradeConsumer.class);
		
		tradeConsumer.setTradesQueue(tradesQueue);
		tradeConsumer.start();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fatalLoger.fatal("主runner被打断" + e.getMessage());
		}

		//new Runner("B",servletConfig,tradesQueue).start();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			fatalLoger.fatal("主runner被打断" + e.getMessage());
		}

		//new Runner("C",servletConfig,tradesQueue).start();
		//		logger.info("主程序启动......");
		//		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletConfig.getServletContext());  
		//		this.catcher = ctx.getBean(TradeCatcher.class);
		//
		//		while(true){
		//			try{
		//				if(!catcher.getHuoBiTrade()){
		//					logger.error("主runner中的主逻辑調用返回false");
		//					continue;
		//				}
		//			}catch(Exception e){
		//				logger.error("主runner中的主逻辑調用返回false" + e.getMessage());
		//				continue;
		//
		//			}
		//			logger.debug("server开始运转");
		////			try {
		////				Thread.sleep(500);
		////			} catch (InterruptedException e) {
		////				fatalLoger.fatal("主runner被打断" + e.getMessage());
		////			}
		//
		//		}

	} 

}
