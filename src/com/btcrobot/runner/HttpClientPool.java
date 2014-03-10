package com.btcrobot.runner;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;

import org.apache.http.conn.routing.HttpRoute;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


public class HttpClientPool {
	static PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
	public static synchronized CloseableHttpClient getHttpClient() {
	

	        	connManager.setMaxTotal(500);
	        	connManager.setDefaultMaxPerRoute(50);
	        	//connManager.setMaxPerRoute(new HttpRoute(new HttpHost("somehost", 80)), 20);
	                      // 超时设置
	        	CloseableHttpClient customerHttpClient = HttpClients.custom().setConnectionManager(connManager).build();
	            // 从连接池中
	        	//connManager.
	        return customerHttpClient;
	}

}
