# Java 로깅 라이브러리 생태계 사실 관계 검증 — 2026-09-27

- 대상: [java-logging-ecosystem.adoc](../src/content/java-logging-ecosystem.adoc)
- 검토 기준 커밋: `fc7b09ba4b7e272c1752dabdbeb68d9ce52f1389`
- 이전 기록: 없음
- 범위: 글 전체. 버전과 릴리스 날짜, 인용문, GitHub 이슈의 내용, 소스 발췌, ServiceLoader 동작 설명을 중심으로 봤다.
- 방법: Codex 리뷰를 요청하고 Claude가 1차 출처로 검증했다. 문서는 curl로 받아 문장을 검색했고, 소스는 GitHub 태그의 원본과 Maven Central의 POM을 대조했다. ServiceLoader 예제는 JDK 25에서 실행 확인했다.
- 결과: **우선 수정할 사항 6건, 표현을 보완할 사항 13건.** 파사드와 구현체의 구분, 탐색 방식의 변천이라는 글의 핵심 전개는 출처와 맞는다.
- 반영 상태: **본문 반영 완료.** 아래 19건을 모두 반영했고, 본문 수정은 이 기록과 같은 커밋(제목 "로깅 글 사실 검증과 반영")에 들어 있다.
- 아래 행 번호는 위 커밋 기준이다.

Codex는 19건을 지적했다. 그중 7건은 Claude가 따로 찾은 항목과 겹쳤고, 11건은 재검증을 거쳐 새로 받아들였으며, 1건은 기각했다. Codex에서 나온 항목에는 "Codex 지적"이라고 표시했다.

## 우선 수정할 사항

### 1. SLF4J와 Logback의 최신 버전이 바뀜

**위치: 본문 184~192행, 727행.** 표는 SLF4J 2.0.19(2026-09-04), Logback 1.6.3(2026-08-14)을 최신으로 적었다.

SLF4J 2.0.20이 2026-09-22에, Logback 1.6.4가 2026-09-24에 나왔다. 표 제목이 "2026년 9월 기준"이므로 갱신해야 한다.

수정 제안:

> SLF4J 2.0.20, 2026-09-22 / Logback 1.6.4, 2026-09-24

근거: [SLF4J News](https://www.slf4j.org/news.html), [Logback News](https://logback.qos.ch/news.html), Maven Central POM의 last-modified.

반영 완료. 480행의 "SLF4J 2.0.19의 `LoggerFactory`"는 v_2.0.19 태그의 소스로 확인한 서술이므로 그대로 두었다.

### 2. slf4j-jdk-platform-logging의 도입 버전

**위치: 본문 155행.** "SLF4J 2.0.0-alpha5부터 제공합니다"라고 적었다.

모듈은 2.0.0-alpha4(2021-08-12)에서 추가되었다. alpha5(2021-08-30)는 `META-INF/services/java.lang.System$LoggerFinder` 파일의 내용을 바로잡은 버전이다.

수정 제안:

> ``LoggerFinder`` 구현입니다. SLF4J 2.0.0-alpha4에서 추가되었고, ``META-INF/services`` 등록 파일의 오류는 2.0.0-alpha5에서 고쳐졌습니다.

근거: [SLF4J News](https://www.slf4j.org/news.html)의 2.0.0-alpha4, 2.0.0-alpha5 항목. Maven Central에 alpha4 POM이 있다.

반영 완료.

### 3. LogFactory.release()와 메모리 누수 설명의 출처

**위치: 본문 315행.** "메모리 누수가 생기는 문제도 같은 문서가 다룹니다"라고 적어 Tech Guide를 가리킨다.

Tech Guide에는 `LogFactory.release()`도 memory leak도 나오지 않는다. 이 내용은 User Guide에 있다. "같은 문서"는 2026-09-27의 표현 수정에서 Claude가 원문을 확인하지 않고 넣은 오류다. User Guide는 `WeakHashtable` 덕분에 `release()`를 부르지 않아도 클래스로더가 회수될 수 있다고도 설명하므로 "생긴다"보다 "생길 수 있다"가 맞다.

수정 제안:

> 클래스로더 언로드 시 ``LogFactory.release()``를 호출하지 않으면 메모리 누수가 생길 수 있다는 점은 JCL의 사용자 가이드가 다룹니다.

근거: [Commons Logging User Guide](https://commons.apache.org/proper/commons-logging/guide.html), [Tech Guide](https://commons.apache.org/proper/commons-logging/tech.html).

반영 완료. 참고 자료의 User Guide 설명에도 항목을 더했다.

### 4. JCL LogFactory 탐색 순서에서 빠진 단계

**위치: 본문 306~310행, 318~320행.** 탐색 순서를 시스템 프로퍼티, `commons-logging.properties`, 자동 탐지의 세 단계로 적었다.

`LogFactory.getFactory()`의 Javadoc은 네 단계를 든다. 시스템 프로퍼티 다음에 "JDK 1.3 Service Discovery mechanism", 곧 `META-INF/services/org.apache.commons.logging.LogFactory` 파일 조회가 있다. 본문 629행은 `jcl-over-slf4j`와 `log4j-jcl`이 이 파일을 제공한다고 설명하므로 앞의 목록에 없으면 앞뒤가 맞지 않는다. 같은 이유로 320행의 "통째로 대체하는 방법뿐"도 과하다. `log4j-jcl`은 `commons-logging.jar`를 대체하지 않고 서비스 등록 파일로 연결했다.

수정 제안:

> . 시스템 프로퍼티 ``org.apache.commons.logging.LogFactory``
> . 클래스패스의 ``META-INF/services/org.apache.commons.logging.LogFactory`` 파일
> . 클래스패스의 ``commons-logging.properties`` 파일
> . 기본 구현인 ``LogFactoryImpl``의 자동 탐지

> 다른 구현체를 쓰려면 ``Log`` 인터페이스를 구현한 클래스를 설정 파일이나 시스템 프로퍼티로 지정하거나, ``LogFactory``를 구현한 클래스를 ``META-INF/services``에 등록해야 했습니다. Log4j 2의 ``log4j-jcl``이 뒤의 방법을 썼습니다. 클래스로더 탐색까지 피하려면 (…) ``commons-logging.jar``를 통째로 대체해야 했습니다.

근거: [Commons Logging 1.4.0 LogFactory.java](https://github.com/apache/commons-logging/blob/rel/commons-logging-1.4.0/src/main/java/org/apache/commons/logging/LogFactory.java)의 `getFactory()` Javadoc과 `SERVICE_ID`, [spring-framework #20457](https://github.com/spring-projects/spring-framework/issues/20457), [#32459](https://github.com/spring-projects/spring-framework/issues/32459)의 Karwasz 댓글("`log4j-jcl` never used the `org.apache.commons.logging` namespace").

소스 대조 판정. 반영 완료. Codex도 같은 지적을 했다.

### 5. Spring Boot 기본 구성을 설명한 정리 항목 (Codex 지적)

**위치: 본문 715행.** 다른 파사드의 호출을 `jcl-over-slf4j`, `log4j-to-slf4j`, `jul-to-slf4j`로 모으며 Spring Boot의 기본 스타터가 이 구성이라고 적었다.

Spring Boot 4.1.1의 스타터에는 `jcl-over-slf4j`가 없다. 본문 234~247행의 설명과도 어긋난다. 구현체로 Log4j Core를 고르면 `log4j-to-slf4j`를 쓸 수 없으므로 구현체를 가리지 않는 목록으로 적을 수도 없다.

수정 제안:

> Logback을 쓴다면 다른 파사드로 들어오는 호출은 ``log4j-to-slf4j``, ``jul-to-slf4j``로 모으고, JCL 호출은 Commons Logging 1.3 이상이 SLF4J로 연결합니다. Spring Boot의 기본 스타터가 이 구성입니다.

근거: [spring-boot-starter-logging build.gradle](https://github.com/spring-projects/spring-boot/blob/v4.1.1/starter/spring-boot-starter-logging/build.gradle).

소스 대조 판정. 반영 완료.

### 6. slf4j-jcl의 제공 범위 (Codex 지적)

**위치: 본문 120행.** "요즘은 쓸 이유가 거의 없습니다"라고만 적었다.

`slf4j-jcl`은 SLF4J 1.8.0-alpha0(2017-04-07)에서 대체 없이 제거되었다. SLF4J 2.0용으로는 없다.

수정 제안:

> SLF4J 호출을 JCL로 보냅니다. SLF4J 1.8.0-alpha0에서 제거되어 1.7 계열까지만 제공됩니다.

근거: [SLF4J News](https://www.slf4j.org/news.html)의 1.8.0-alpha0 항목.

반영 완료.

## 표현을 보완할 사항

### 1. Tech Guide의 컨텍스트 클래스로더 서술

**위치: 본문 314행.** "탐지가 실패한다고 설명합니다"라고 적었다.

원문은 두 경우 모두 "will cause difficulties"라고만 한다. 탐지 실패라고 단정하지 않는다.

수정 제안:

> (…) 컨텍스트로 지정하면 컨텍스트 클래스로더를 쓰는 코드가 어려움을 겪는다고 설명합니다.

근거: [Tech Guide](https://commons.apache.org/proper/commons-logging/tech.html)의 "Issues with Context ClassLoaders".

반영 완료.

### 2. SLF4J 2.0의 ServiceLoader.load() 호출 형태

**위치: 본문 480행.** `ServiceLoader.load(SLF4JServiceProvider.class)`로 적었다.

실제 코드는 `ServiceLoader.load(SLF4JServiceProvider.class, classLoaderOfLoggerFactory)`다. 인자가 하나인 `load()`는 스레드 컨텍스트 클래스로더를 쓰므로 동작이 다르다. JCL의 TCCL 문제를 다룬 글이므로 구분할 가치가 있다.

수정 제안:

> SLF4J 2.0.19의 ``LoggerFactory``는 ``ServiceLoader.load()``에 ``SLF4JServiceProvider.class``와 ``LoggerFactory``를 로딩한 클래스로더를 넘겨 provider 목록을 얻습니다.

근거: [SLF4J 2.0.19 LoggerFactory.java](https://github.com/qos-ch/slf4j/blob/v_2.0.19/slf4j-api/src/main/java/org/slf4j/LoggerFactory.java) 143행.

소스 대조 판정. 반영 완료. Codex도 같은 지적을 했다.

### 3. StaticLoggerBinder 무시 경고의 조건

**위치: 본문 483행.** "클래스패스에 있으면 목록으로 출력만 하고 무시합니다"라고 적었다.

`reportIgnoredStaticLoggerBinders()`는 `bind()`에서 provider가 하나도 없는 분기에서만 호출된다. provider가 있으면 출력하지 않는다.

수정 제안:

> provider가 하나도 없으면 (…) NOP 구현으로 동작합니다. 이때 1.7용 ``StaticLoggerBinder``가 클래스패스에 있으면 그 위치를 출력만 하고 무시합니다.

근거: 위 LoggerFactory.java 194~212행, [SLF4J codes](https://www.slf4j.org/codes.html#ignoredBindings).

반영 완료.

### 4. Hoeller의 답이 나온 이슈

**위치: 본문 689행.** #21146 오류 메시지 바로 뒤에 "`jcl-over-slf4j`보다 상황이 나쁘지는 않다고 답했습니다"가 온다.

"no worse than"은 2024년 #32459의 댓글이다. #21146에서는 "This is by design"이라고 하고 하나만 두라고 답했다. 두 답의 취지는 같지만 출처가 섞여 읽힌다.

수정 제안:

> Hoeller는 그 이슈에서 이것이 의도한 설계라고 답했습니다. ``spring-jcl``은 ``jcl-over-slf4j``처럼 ``commons-logging``을 대체하는 jar이므로 모듈 경로에는 둘 중 하나만 두어야 한다는 것입니다. #32459에서도 ``jcl-over-slf4j``나 ``log4j-jcl``을 쓸 때보다 상황이 나쁘지는 않다고 답했습니다.

근거: [#21146](https://github.com/spring-projects/spring-framework/issues/21146), [#32459](https://github.com/spring-projects/spring-framework/issues/32459).

반영 완료.

### 5. Logback의 SLF4J 요구 버전과 1.6.x 교체 조건

**위치: 본문 192행.** "1.5.x 이후 SLF4J 2.0.x가 필요합니다", "1.6.x로 바로 교체할 수 있습니다"라고 적었다.

SLF4J 2.x를 대상으로 하는 것은 Logback 1.3부터다. Logback News는 1.6.x가 1.5.x의 drop-in replacement라고 하면서 Janino 조건문을 예외로 든다.

수정 제안:

> 1.3.x부터 SLF4J 2.0.x가 필요합니다. 1.6.x는 JDK 11 이상. 1.5.x를 쓰던 프로젝트는 1.6.x로 바로 교체할 수 있는데, 설정 파일에 Janino 조건문을 쓴 경우는 예외여서 설정 파일을 고쳐야 합니다.

근거: [Logback News](https://logback.qos.ch/news.html)의 1.6.x, 1.5.x 설명, [SLF4J codes](https://www.slf4j.org/codes.html).

반영 완료.

### 6. #37266 수정의 진행 상태

**위치: 본문 708행.** "수정이 7.0.10에 예정되어 있습니다"라고 적었다.

수정 커밋 `b96592e`는 보고된 날(2026-09-10)에 들어갔고 이슈도 닫혔다. 7.0.10은 아직 나오지 않았다(Maven Central 최신은 7.0.9).

수정 제안:

> (…) ``LogAccessor``에 레벨 확인을 넣는 수정이 보고된 날 커밋되어 7.0.10에 들어갈 예정입니다.

근거: [#37266](https://github.com/spring-projects/spring-framework/issues/37266), [커밋 b96592e](https://github.com/spring-projects/spring-framework/commit/b96592e4afebfaeb64903c7f8397fef48abc6e3b).

반영 완료. 7.0.10이 나오면 문장을 다시 고쳐야 한다.

### 7. 그림의 ServiceLoader.load() 표기 (Codex 지적)

**위치: 본문 487행의 `slf4j-binding.png`.** 그림에 `ServiceLoader.load(SLF4JServiceProvider.class)`라고 적혀 있었다.

보완 사항 2와 같은 이유다. SLF4J News에 따르면 2.0.4에서 스레드 컨텍스트 클래스로더 대신 `LoggerFactory`를 로딩한 클래스로더를 쓰도록 바꾸었다.

반영 완료. `slf4j-binding.drawio`의 표기를 `ServiceLoader.load(SLF4JServiceProvider.class, classLoader)`로 고치고 PNG를 다시 내보냈다. 본문에도 2.0.4의 변경을 한 문장으로 넣었다.

근거: [SLF4J News](https://www.slf4j.org/news.html)의 2.0.4 항목.

### 8. Commons Logging 1.4.0 팩토리 선택 순서의 전제 (Codex 지적)

**위치: 본문 500~505행.** "1.4.0의 `LogFactory`는 아래 순서로 팩토리를 고릅니다"라고 적었다.

나열한 네 단계는 `newStandardFactory()`의 순서다. 시스템 프로퍼티, `META-INF/services`, `commons-logging.properties`로 지정한 팩토리가 있으면 그쪽이 먼저다. 그리고 `META-INF/services` 단계는 1.3.0부터 `ServiceLoader`를 쓴다(1.2는 파일을 직접 읽었다). 글의 주제와 직접 닿는 사실이므로 본문에 넣었다.

수정 제안:

> ``META-INF/services``에 등록된 ``LogFactory``를 찾는 단계도 1.3.0부터는 ``ServiceLoader``를 씁니다. 시스템 프로퍼티, ``META-INF/services``, ``commons-logging.properties`` 중 어디에도 팩토리가 지정되지 않았으면 1.4.0의 ``LogFactory``는 아래 순서로 팩토리를 고릅니다.

근거: [LogFactory.java 1.4.0](https://github.com/apache/commons-logging/blob/rel/commons-logging-1.4.0/src/main/java/org/apache/commons/logging/LogFactory.java) 825행, [1.3.0](https://github.com/apache/commons-logging/blob/rel/commons-logging-1.3.0/src/main/java/org/apache/commons/logging/LogFactory.java), [1.2](https://github.com/apache/commons-logging/blob/LOGGING_1_2/src/main/java/org/apache/commons/logging/LogFactory.java).

소스 대조 판정. 반영 완료.

### 9. SLF4J의 ServiceLoader 도입 시점 (Codex 지적)

**위치: 본문 462행.** 1.8.0-beta0에서 `org.slf4j.impl` 패키지를 없애고 `ServiceLoader` 탐색을 시도했다고 적었다.

패키지 제거는 beta0(2017-10-20)이 맞다. `ServiceLoader` 호출은 1.8.0-alpha1(2017-04-13)의 소스에 이미 있다. alpha0의 소스는 확인하지 않았다.

수정 제안:

> SLF4J는 2017년 4월의 1.8.0 알파 버전부터 ``ServiceLoader``로 구현체를 찾기 시작했고, 1.8.0-beta0(2017-10-20)에서 ``org.slf4j.impl`` 패키지를 없앴습니다.

근거: [LoggerFactory.java 1.8.0-alpha1](https://github.com/qos-ch/slf4j/blob/v_1.8.0-alpha1/slf4j-api/src/main/java/org/slf4j/LoggerFactory.java) 104행.

소스 대조 판정. 반영 완료.

### 10. log4j-over-slf4j와 PropertyConfigurator (Codex 지적)

**위치: 본문 135행.** `PropertyConfigurator`를 직접 참조하는 코드는 깨진다고 적었다.

SLF4J 문서는 지금도 그렇게 설명하지만, 2.0.19의 `log4j-over-slf4j`에는 메서드 본문이 빈 `PropertyConfigurator` 클래스가 들어 있다. 참조는 되고 호출이 무시된다.

수정 제안:

> appender나 filter처럼 이 모듈에 없는 클래스를 참조하는 코드는 깨지고, ``PropertyConfigurator`` 호출은 아무 일도 하지 않습니다.

근거: [PropertyConfigurator.java 2.0.19](https://github.com/qos-ch/slf4j/blob/v_2.0.19/log4j-over-slf4j/src/main/java/org/apache/log4j/PropertyConfigurator.java), [Bridging legacy APIs](https://www.slf4j.org/legacy.html).

소스 대조 판정. 반영 완료.

### 11. JUL 순환의 조건과 60배 수치의 범위 (Codex 지적)

**위치: 본문 169행, 175행.**

SLF4J 문서는 `jul-to-slf4j`와 `slf4j-jdk14`의 순환에 "and SLF4JBridgeHandler is installed"라는 조건을 붙였다. 60배는 JUL이 SLF4J 쪽 레벨을 모른 채 `LogRecord`를 만들어 넘기는 호출의 비용이다.

수정 제안:

> * ``jul-to-slf4j`` + ``slf4j-jdk14``: ``SLF4JBridgeHandler``를 설치했을 때

> JUL은 SLF4J 쪽의 레벨 설정을 모르므로 로그 레코드를 일단 만들어서 넘깁니다. SLF4J 문서에 따르면 SLF4J 쪽에서 레벨이 꺼져 있어 기록되지 않는 로그 호출도 (…)

근거: [Bridging legacy APIs](https://www.slf4j.org/legacy.html).

반영 완료.

### 12. 구현체의 공존과 선택의 구분 (Codex 지적)

**위치: 본문 333행, 338~339행, 492행, 714행.**

SLF4J 1.7에서도 바인딩 두 개를 클래스패스에 함께 둘 수 있고, 경고 뒤에 하나가 선택된다. Spring Boot의 예외도 테스트용 구현체가 선택되었을 때 난다. "함께 있을 여지를 없앤다", "추가하는 순간 충돌한다", "공존할 수 없다"는 과하다. 714행의 "성립하지 않는 비교"도 Log4j API와 SLF4J는 비교할 수 있으므로 API와 Core를 구분해야 한다.

수정 제안:

> 둘째, 두 구현체가 클래스패스에 함께 있으면 어느 쪽이 선택될지 정할 방법이 없습니다.

> 이 라이브러리를 테스트 의존성에 추가하면 Logback과 함께 클래스패스에 놓여 둘 중 하나가 선택됩니다. Spring Boot의 테스트에서 이 라이브러리가 선택되면 (…)

> ``ServiceLoader``로 바뀌었다고 해서 구현체 두 개를 함께 쓸 수 있게 된 것은 아닙니다.

> SLF4J와 비교할 대상은 Log4j API이고, Log4j Core는 Logback과 비교해야 합니다.

근거: [SLF4J codes](https://www.slf4j.org/codes.html#multiple_bindings), [LogbackLoggingSystem.java](https://github.com/spring-projects/spring-boot/blob/v4.1.1/core/spring-boot/src/main/java/org/springframework/boot/logging/logback/LogbackLoggingSystem.java), [Log4j 2 Installation](https://logging.apache.org/log4j/2.x/manual/installation.html).

반영 완료. 글쓴이의 의견이 담긴 문장들이므로 어조가 의도와 다르면 되돌린다.

### 13. ServiceLoader 설명의 적용 범위 (Codex 지적)

**위치: 본문 376~380행.** 구성 요소 세 가지를 조건 없이 설명했다.

public 무인자 생성자와 등록 파일은 provider를 클래스패스에 둘 때의 규칙이다. 모듈에서는 `provides … with` 선언을 쓰고 `provider()` 정적 메서드로도 인스턴스를 만들 수 있다.

수정 제안:

> provider를 클래스패스에 둘 때의 구성 요소는 세 가지입니다.

근거: [ServiceLoader Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ServiceLoader.html).

반영 완료. 등록 파일의 이름이 binary name이라는 점, UTF-8 인코딩, 주석 규칙은 예제에 영향이 없어 본문에 넣지 않았다.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| JEP 264는 범용 로깅 인터페이스 정의가 목표가 아니다(45행) | **확인.** Non-Goals 원문 대조 | [JEP 264](https://openjdk.org/jeps/264) |
| `LoggerFinder`가 없으면 JUL, `java.logging`이 없으면 INFO 이상을 `System.err`로(43행) | **확인.** | [System.LoggerFinder Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/System.LoggerFinder.html) |
| 순환을 일으키는 연결 모듈 조합 3쌍(167~169행) | **확인.** | [Bridging legacy APIs](https://www.slf4j.org/legacy.html) |
| `jul-to-slf4j`의 60배, 20%와 `LevelChangePropagator`(175~176행) | **확인.** 원문 "60-fold or 6000%", "20% overall increase" | 위와 같음 |
| Log4j 2.26.1(2026-06-29), 2.25.5(2026-07-01)와 같은 수정 내용 | **확인.** 두 릴리스의 변경 목록이 같다 | [Log4j 2 Release Notes](https://logging.apache.org/log4j/2.x/release-notes.html) |
| Log4j 3.0.0-beta3(2024-11), Java 17 이상 | **확인.** 릴리스 날짜 2024-11-09 | [Log4j 3 Release Notes](https://logging.apache.org/log4j/3.x/release-notes.html) |
| Log4j API는 3.x가 따로 나오지 않는다(202행) | **확인.** Karwasz의 "There will be no Log4j API 3.x for now" | [#32459](https://github.com/spring-projects/spring-framework/issues/32459) |
| Commons Logging 1.4.0(2026-06-11), 1.3.0(2023-11-26), Karwasz의 기여 | **확인.** | [Commons Logging Changes](https://commons.apache.org/proper/commons-logging/changes.html) |
| Log4j 1.2.17(2012), 2015-08-05 지원 종료 | **확인.** | [Log4j 1.x](https://logging.apache.org/log4j/1.x/) |
| SLF4J 1.8.0-beta0, 2.0.0-alpha0, 2.0.0, 2.1.0-alpha1의 날짜 | **확인.** | [SLF4J News](https://www.slf4j.org/news.html) |
| `slf4j.provider` 시스템 프로퍼티는 2.0.9부터(482행) | **확인.** | 위와 같음 |
| Spring Boot 4.1.1 `spring-boot-starter-logging`의 세 의존성 | **확인.** 소스 대조 | [build.gradle](https://github.com/spring-projects/spring-boot/blob/v4.1.1/starter/spring-boot-starter-logging/build.gradle) |
| `LogbackLoggingSystem`의 `Assert.state` 발췌 | **확인.** 소스 대조. "..."로 줄인 뒷부분은 WebLogic 안내다 | [LogbackLoggingSystem.java](https://github.com/spring-projects/spring-boot/blob/v4.1.1/core/spring-boot/src/main/java/org/springframework/boot/logging/logback/LogbackLoggingSystem.java) |
| Logback `AsyncAppender`의 큐 크기 256, 20% 기준 | **확인.** | [Logback AsyncAppender](https://logback.qos.ch/manual/appenders-async-sift.html) |
| Log4j 2 성능 페이지가 든 비동기 로거의 단점 세 가지(269~273행) | **확인.** | [Log4j 2 Performance](https://logging.apache.org/log4j/2.x/manual/performance.html) |
| 위 단점은 Performance 페이지가 아니라 Asynchronous loggers 페이지에 있다는 지적 | **확인.** Codex 지적, 재검증 결과 기각. 두 페이지 모두 같은 Trade-offs 절을 싣고 있다 | 위와 같음 |
| Logback 인용 "not particularly fast. Thus, its use should be avoided…"(288행) | **확인.** `%C`, `%F`, `%L`, `%M` 네 곳에 같은 문장이 있다 | [Logback Layouts](https://logback.qos.ch/manual/layouts.html) |
| Log4j 2 인용 "This is an expensive operation and should be avoided in performance-sensitive setups"(289행) | **확인.** 글자 단위 일치 | [Log4j 2 Layouts](https://logging.apache.org/log4j/2.x/manual/layouts.html) |
| 비동기 로거의 위치 정보가 기본으로 꺼져 있음(292행) | **확인.** | [Log4j 2 Asynchronous Loggers](https://logging.apache.org/log4j/2.x/manual/async.html), Logback AsyncAppender |
| SLF4J 1.7.36의 `getResources()` 탐색과 다중 바인딩 경고(328행) | **확인.** 소스 대조 | [LoggerFactory.java 1.7.36](https://github.com/qos-ch/slf4j/blob/v_1.7.36/slf4j-api/src/main/java/org/slf4j/LoggerFactory.java) |
| 어느 바인딩이 선택될지는 실질적으로 무작위(329행) | **확인.** 원문 "for all practical purposes should be considered random" | [SLF4J codes](https://www.slf4j.org/codes.html) |
| portingle/slf4jtesting README의 static, singleton 서술(355행) | **확인.** | [README](https://github.com/portingle/slf4jtesting) |
| valfirst/slf4j-test가 SLF4J 2.0.x를 지원(490행) | **확인.** README의 호환표가 2.0.20까지 든다 | [valfirst/slf4j-test](https://github.com/valfirst/slf4j-test) |
| ServiceLoader는 JDK 6부터, 등록 파일 형식, 지연 로딩, 순회 순서(374~458행) | **확인.** Javadoc 대조. 예제는 JDK 25에서 실행 확인 | [ServiceLoader Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/ServiceLoader.html) |
| Logback의 등록 파일과 `module-info`의 `provides`, `slf4j-api`의 `uses`(468~477행) | **확인.** `logback-classic` 1.5.38, `slf4j-api` 2.0.18 jar를 풀어 확인 | 로컬 Maven 저장소 |
| provider가 둘 이상이면 "Found provider"를 출력하고 첫 번째를 사용(481행) | **확인.** 소스 대조 | [LoggerFactory.java 2.0.19](https://github.com/qos-ch/slf4j/blob/v_2.0.19/slf4j-api/src/main/java/org/slf4j/LoggerFactory.java) |
| Commons Logging 1.4.0의 팩토리 선택 순서 네 단계(502~505행) | **확인.** `newStandardFactory()` 소스 대조 | [LogFactory.java 1.4.0](https://github.com/apache/commons-logging/blob/rel/commons-logging-1.4.0/src/main/java/org/apache/commons/logging/LogFactory.java) |
| Karwasz의 "same algorithm as spring-jcl"(507행) | **확인.** | [#32459](https://github.com/spring-projects/spring-framework/issues/32459) |
| Spring Framework 4.3.0, 5.0.0, 5.1.0, 5.1.4, 6.0.0, 6.2.0, 7.0.0-M1, 7.0.0의 릴리스 날짜 | **확인.** Maven Central POM의 last-modified | Maven Central |
| 7.0.0의 `commons-logging` 1.3.5 의존(548행) | **확인.** 7.0.0-M1은 1.3.4였다 | spring-core 7.0.0 POM |
| Spring 4.3 레퍼런스의 서술과 "Avoiding Commons Logging" 절(554~556행) | **확인.** | [Spring 4.3 Reference](https://docs.spring.io/spring-framework/docs/4.3.x/spring-framework-reference/html/overview.html) |
| Spring Boot 1.5의 `commons-logging` 제외와 `jcl-over-slf4j`, 2.0.0의 제거(583~585행, 622행) | **확인.** 1.5.22, 2.0.0 POM 대조 | Maven Central |
| #10000, #11273의 시기와 2016년 7월 종료(588~589행) | **확인.** | GitHub 이슈 |
| Hoeller 인용문과 세 가지 이유(593~607행) | **확인.** #19081 본문과 글자 단위 일치 | [#19081](https://github.com/spring-projects/spring-framework/issues/19081) |
| spring-jcl 5.0.0 `LogFactory`의 Javadoc과 탐색 순서(611~620행) | **확인.** 소스 대조 | [LogFactory.java 5.0.0](https://github.com/spring-projects/spring-framework/blob/v5.0.0.RELEASE/spring-jcl/src/main/java/org/apache/commons/logging/LogFactory.java) |
| #20457, #21127, #22118의 내용과 5.1, 5.1.4 반영(626~637행) | **확인.** | GitHub 이슈 |
| spring-jcl 6.2.0 `LogAdapter` 발췌(642~666행) | **확인.** 원본에서 주석 일부를 뺀 것 외에는 같다 | [LogAdapter.java 6.2.0](https://github.com/spring-projects/spring-framework/blob/v6.2.0/spring-jcl/src/main/java/org/apache/commons/logging/LogAdapter.java) |
| #32459의 제안자, 시기, Hoeller의 조건, Karwasz의 제안과 거절 이유(671~698행) | **확인.** | [#32459](https://github.com/spring-projects/spring-framework/issues/32459) |
| 7.0 릴리스 노트의 `spring-jcl` 제거 설명(701행) | **확인.** | [7.0 Release Notes](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes) |
| 두 SLF4J 어댑터의 레벨 확인 차이(706~707행) | **확인.** 소스 대조. Commons Logging 1.3.5는 `String.valueOf(message)`를 바로 호출한다 | [Slf4jLogFactory.java 1.3.5](https://github.com/apache/commons-logging/blob/rel/commons-logging-1.3.5/src/main/java/org/apache/commons/logging/impl/Slf4jLogFactory.java) |

## 한계

- 그림 `facade-and-implementation.png`, `spring-boot-logging.png`의 내용은 Claude가 대조하지 않았다. Codex는 그림 3장을 검토했다고 밝혔고 `slf4j-binding.png`만 지적했다.
- `slf4j-binding.png`의 "logback-classic 1.5+" 표기는 그대로 두었다. SLF4J 2.0용 provider는 Logback 1.3부터 있다.
- 691행의 "모듈 경로에서는 Apache HttpClient 4.5처럼 (…) 제외 설정이 필요했습니다"는 #21146의 Hoeller 답변에서 끌어낸 추론이다. 실행으로 재현하지 않았다.
- 171행의 `log4j-to-slf4j`와 `log4j-slf4j2-impl`, `log4j-to-jul`과 `log4j-jul` 조합 금지는 Log4j 문서의 해당 문장을 찾아 대조하지 않았다. 두 아티팩트가 있다는 것만 확인했다.
- 150행의 `log4j-jul` 설명은 `java.util.logging.manager` 시스템 프로퍼티 방식만 확인했다. Log4j 문서는 설치 방법을 두 가지로 안내한다.
- CVE-2021-44228이 `log4j-core`에만 해당한다는 서술(215~216행)은 Claude가 보안 페이지의 문장을 찾아 대조하지 않았다. Codex는 맞다고 판정했다.
- lidalia slf4j-test가 SLF4J 1.7까지만 지원한다는 서술은 valfirst README의 "prior to version 1.8.X"와 마지막 릴리스(1.2.0, 2015년)로 판단했다.
- `logback-classic`의 등록 파일은 1.5.38로 확인했다. 표의 최신 버전인 1.6.4의 jar는 풀어 보지 않았다.
