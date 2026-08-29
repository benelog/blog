package net.benelog.test.performance;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author benelog
 */
public class SimpleRequestTest  {

	static HttpClient httpClient;
	static AtomicInteger widgetIdSeq = new AtomicInteger();
	static ThreadSafeClientConnManager connManager;
	String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<bang type=\"yaml\">--- !ruby/object:Time {}\n" + 
			"</bang>";
	
	@BeforeClass
	public static void prepareHttpClient(){
		createHttpClient(5000, 5000);
	}

	@AfterClass
	public static void releaseConnectionPool(){
		connManager.shutdown();
	}
	
	@Test
	public void query() throws ClientProtocolException, IOException, InterruptedException {

		String requestUrl = "http://localhost";
		HttpGet request = new HttpGet(requestUrl);
	
		BasicHttpParams params = new BasicHttpParams();
		params.setParameter("query", content);
		request.setParams(params);

		
		HttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String message = IOUtils.toString(entity.getContent());
		System.out.println(message);

		int statusCode = response.getStatusLine().getStatusCode();
		assertThat(statusCode, is(200));
	}


	private static void createHttpClient(int connectTimeoutMilsec, int readTimeoutMilsec){
		connManager = new ThreadSafeClientConnManager();
		connManager.setMaxTotal(9);
		connManager.setDefaultMaxPerRoute(9);
		
		HttpClient client = new DefaultHttpClient(connManager);
		client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeoutMilsec);
		client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeoutMilsec);
		httpClient = client;
	}
}
