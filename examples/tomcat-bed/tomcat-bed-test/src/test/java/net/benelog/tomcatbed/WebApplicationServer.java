package net.benelog.tomcatbed;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Embedded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * references : -
 * http://stackoverflow.com/questions/6349472/embedded-tomcat-not-
 * serving-static-content -
 * http://stackoverflow.com/questions/640022/howto-embed-tomcat-6
 * 
 * @author sanghyuk.jung
 */
public class WebApplicationServer {
	private static final int DEFAULT_SERVLET_PRIORTY = 1;
	private static final int JSP_SERVLET_PRIORTY = 2;
	private static final int PORT_CHECK_TIMEOUT = 100;
	private static final Logger log = LoggerFactory.getLogger(WebApplicationServer.class);

	private final Embedded container;
	private final int port;
	private ServletContext servletContext;
	private StandardContext appContext;
	private boolean running;

	public WebApplicationServer(String contextPath, int port, String appBase) {
		this.port = port;
		if (!contextPath.startsWith("/")) {
			contextPath = "/" + contextPath;
		}
		this.container = new Embedded();
		this.container.setName("TomcatEmbeddedServer");
		this.appContext = createAppContext(contextPath);
		Host localHost = createHost(appBase, appContext);
		Engine engine = createEngine(localHost);
		this.container.addEngine(engine);

		Connector connector = this.container.createConnector(
				localHost.getName(), port, false);
		this.container.addConnector(connector);
	}

	@PostConstruct
	public void start() throws IOException, LifecycleException {
		log.info("Starting Tomcat... (port={})", port);

		if (isEmptyPort()) {
			this.container.start();
			this.running = true;
			this.servletContext = appContext.getServletContext();
		} else {
			log.info("The port({}) is already occupied.", port);
		}
	}

	private boolean isEmptyPort() throws MalformedURLException {
		URL localhost = new URL("http://localhost:" + port);
		boolean emptyPort = false;
		try {
			HttpURLConnection con = (HttpURLConnection) localhost.openConnection();
			con.setConnectTimeout(PORT_CHECK_TIMEOUT);
			con.connect();
			con.disconnect();
		} catch (IOException e) {
			emptyPort = true;
		}
		return emptyPort;
	}

	@PreDestroy
	public void stop() throws LifecycleException {
		if (running) {
			log.info("Stopping Tomcat...");
			this.container.stop();
		}
	}

	private Host createHost(String appBase, StandardContext rootContext) {
		Host localHost = container.createHost("localhost", appBase);
		localHost.setAutoDeploy(false);
		localHost.addChild(rootContext);
		return localHost;
	}

	private StandardContext createAppContext(String contextPath) {
		StandardContext appContext = (StandardContext) container.createContext(
				contextPath, "webapp");
		appContext.setDefaultWebXml("web.xml");
		addDefaultServlet(appContext);
		addJspServlet(appContext);
		appContext.setTldNamespaceAware(true);
		appContext.setTldScanTime(60000);
		return appContext;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	private Engine createEngine(Host localHost) {
		Engine engine = container.createEngine();
		engine.setDefaultHost(localHost.getName());
		engine.setName("TomcatEngine");
		engine.addChild(localHost);
		return engine;
	}

	private void addDefaultServlet(StandardContext appContext) {
		Wrapper servlet = appContext.createWrapper();
		servlet.setName("default");
		servlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
		servlet.addInitParameter("debug", "0");
		servlet.addInitParameter("listings", "false");
		servlet.setLoadOnStartup(DEFAULT_SERVLET_PRIORTY);
		appContext.addChild(servlet);
		appContext.addServletMapping("/", "default");
	}

	private void addJspServlet(StandardContext appContext) {
		Wrapper servlet = appContext.createWrapper();
		servlet.setName("jsp");
		servlet.setServletClass("org.apache.jasper.servlet.JspServlet");
		servlet.addInitParameter("fork", "false");
		servlet.addInitParameter("xpoweredBy", "false");
		servlet.setLoadOnStartup(JSP_SERVLET_PRIORTY);
		appContext.addChild(servlet);
		appContext.addServletMapping("*.jsp", "jsp");
	}
}