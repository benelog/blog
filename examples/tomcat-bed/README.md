# Tomcat-bed : 통합 테스트를 위한 Embeded-Tomcat

## 특징
### 장점
- 전통적인 UI테스트 방식과 비교하면
    - 별도의 배포작업이 필요없다.
    - Test Coverage 측정이 쉽다. Eclemma로 Eclipse에서 바로 확인도 가능.
- Spring-mvc-test과 비교하면
    - JSP 내용도 검증할 수 있다. JwebUnit을 활용가능하다.
- Spring integration test와 같이 사용하면
    - net.benelog.tomcatbed.WebApplicationServer를 bean으로 등록하면 여러 개의 테스트를 같이 실행시켜도 한번만 WAS를 시작한다.
    - 테스트할 서버의 주소, 포트를 Spring의 properties관리로 지정하면 개발서버, Staging서버 등 여러 환경에서 실행가능.
- 지정된 포트가 사용중이면 Embeded Tomcat를 실행하지 않는다.
    - PC에서 개발용 WAS가 떠있어도 테스트는 똑같이 동작한다.

### 단점
- 실제로  Tomcat이 실행되므로 Spring-mvc-test과 비교하면 시작속도가 느리다.
- 태그라이브러리 선언파일(*.tld)를 수동으로 등록해야 인식된다.

## Spring 프레임워크와 통합 사용법
#### 1. (선택) test서버를 정의한 속성파일 정의. Localhost가 아닌곳에서도 같은 테스트를 실행할 수 있다.

    contextPath=/
    port=8080
    baseUrl=http://localhost:8080

#### 2. net.benelog.tomcatbed.WebApplicationServer를 bean등록 

    <util:properties id="testServer" location="classpath:testServer.properties" />
    
    <bean id="was" class="net.benelog.tomcatbed.WebApplicationServer"
    	c:contextPath="#{testServer.contextPath}"
    	c:port="#{testServer.port}"
    	c:appBase="src/main"/>

#### 3. 2번 파일을 @ContextConfiguration로 지정해서  테스트 실행

    import static net.sourceforge.jwebunit.junit.JWebUnit.*;
    import static org.hamcrest.CoreMatchers.*;
    import static org.junit.Assert.*;
    import net.benelog.tomcatbed.domain.Image;
    import org.junit.Before;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.test.context.ContextConfiguration;
    import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
    import org.springframework.web.client.RestTemplate;
    
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(locations = { "/applicationContext-ui-test.xml"})
    public class HomePageTest {
    	@Value("#{testServer.baseUrl}")
    	String baseUrl;
        
    	@Before
        public void prepare() {
    		setBaseUrl(baseUrl);
    		setScriptingEnabled(false);
        }
    
    	@Test
    	public void home() {
    		beginAt("/"); 
    		assertResponseCode(200);
    		
    		assertButtonPresent("computer");
    		assertButtonPresent("cloud");
    		assertButtonPresent("code");
    	}
    }