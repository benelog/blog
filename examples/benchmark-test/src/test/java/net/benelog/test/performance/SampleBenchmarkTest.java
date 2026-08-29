package net.benelog.test.performance;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

/**
 * @author benelog
 */
@BenchmarkMethodChart(filePrefix = "benchmark-sample")
public class SampleBenchmarkTest extends AbstractBenchmark {

	String imageUrl = "http://localhost/screen_shot/123.jpg";
	static HttpClient httpClient;
	static ThreadSafeClientConnManager connManager;
	
	@BeforeClass
	public static void prepareHttpClient(){
		createHttpClient(5000, 5000);
	}

	@AfterClass
	public static void releaseConnectionPool(){
		connManager.shutdown();
	}
	
	@Test
	@BenchmarkOptions(benchmarkRounds = 8, warmupRounds = 0, concurrency=8)
	public void testCreateApk() throws ClientProtocolException, IOException, InterruptedException {

		String requestUrl = "http://localhost/create";
		HttpPost request = new HttpPost(requestUrl);
	
		UrlEncodedFormEntity formEntity = createEntity();
		request.setEntity(formEntity);
		
		HttpResponse response = httpClient.execute(request);

		HttpEntity entity = response.getEntity();
		String message = IOUtils.toString(entity.getContent());
		System.out.println(message);

		int statusCode = response.getStatusLine().getStatusCode();
		assertThat(statusCode, is(200));
	}
	
	private UrlEncodedFormEntity createEntity() throws UnsupportedEncodingException {
		List<BasicNameValuePair> form = new ArrayList<BasicNameValuePair>();

		add(form,"versionCode", "1");
		return new UrlEncodedFormEntity(form, "UTF-8");
	}

	private void add(List<BasicNameValuePair> formParams, String key, String value) {
		formParams.add(new BasicNameValuePair(key, value));
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
