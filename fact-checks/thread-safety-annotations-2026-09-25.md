# 스레드 안전성의 문서화와 검증 글 사실 관계 검증 — 2026-09-25

- 대상: [thread-safety-annotations.adoc](../src/content/thread-safety-annotations.adoc)
- 검토 기준: 2026-09-25 07:46 커밋(`7dbbcb6`) 시점의 글. 이 커밋은 이후 사실 검증 반영과 함께 한 커밋으로 합쳐져 이력에 남아 있지 않다.
- 이전 기록: 없음
- 범위: 글 전체. Effective Java 아이템 82의 분류와 예시, JDK 25와 Spring Batch Javadoc 인용, HttpClient `@Contract`/`ThreadingBehavior`와 4.x 애너테이션 이력, JCIP 라이선스와 복제 패키지, SpotBugs 4.10.4·Error Prone 2.50.0·IntelliJ IDEA(소스 `826413b22cfe`)·SonarJava(소스 `9bf31b6e0037`)의 검사 범위, ArchUnit 규칙 설명과 두 예제 저장소를 봤다.
- 방법: Codex 작업(`task`, 글 전체 대상)으로 의심 지점을 모은 뒤, Claude가 1차 출처로 다시 확인했다. Javadoc은 curl로 받아 태그를 벗기고 문장을 대조했다. SpotBugs(4.10.4 태그)와 Error Prone(v2.50.0 태그)은 소스를 받아 직접 읽었고, HttpComponents·IntelliJ·SonarJava·OpenJDK는 지정 태그나 커밋의 원본 파일을 받아 대조했다. 애너테이션 retention은 Maven Central jar를 `javap`로 확인했다. 예제는 JDK 25에서 `examples/thread-safety-static-analysis`(SpotBugs 기본·`-PreportLow`, Error Prone 기본·`-PthreadSafeCheck`)와 `examples/thread-safety-archunit`(`test`)를 다시 실행했다. 추가로 예제 복사본에서 `options.errorprone.error("ThreadSafe")`를 켜 보고, `Counter`에서 `@GuardedBy`를 뺀 뒤 SpotBugs를 실행했다.
- 결과: **우선 수정할 사항 3건, 표현을 보완할 사항 10건.** 예제 출력은 모두 본문과 같고, 문서화 → 애너테이션 → 도구별 부분 검사 → 프로젝트 규칙 검사로 이어지는 전개와 결론은 타당하다. 고칠 곳은 Effective Java 2판의 예시를 3판의 것으로 쓴 곳, SpotBugs가 `@GuardedBy("this")`로 경고 이름만 바꾼다고 한 설명, JCIP 계열 애너테이션이 모두 RUNTIME retention이라고 읽히는 문장이다.
- 반영 상태: **우선 수정 3건과 표현 보완 10건을 모두 제안 문장대로 본문에 반영함(반영한 수정은 이 기록과 같은 커밋에 합쳐져 있다).** 한계에 적은 예제 정합성 두 건도 함께 고쳤다(`Counter.java` 주석, 본문의 SpotBugs Gradle 발췌). 이 문서의 검증 본문은 작성 당시의 수정 제안을 그대로 둔다.
- 아래 행 번호는 위 검토 기준 시점의 글 기준이다.

## 우선 수정할 사항

### 1. 스레드 적대적 예시와 설명이 3판이 아니라 2판의 내용이다

**위치: 본문 86–87행.** 71행에서 "Effective Java 3판의 아이템 82"를 기준으로 삼고, 스레드 적대적 항목에 "다행히 Java 라이브러리에는 거의 없습니다"와 "예: ``System.runFinalizersOnExit()``. 책이 든 이 예는 JDK 11에서 제거되었습니다"라고 쓴다.

"Luckily, there are very few thread-hostile classes or methods in the Java libraries. The System.runFinalizersOnExit method is thread-hostile and has been deprecated."는 2판 아이템 70의 문장이다(1판 아이템 52에도 같은 취지의 문장이 있다). 3판 아이템 82는 이 두 문장을 "When a class or method is found to be thread-hostile, it is typically fixed or deprecated. The generateSerialNumber method in Item 78 would be thread-hostile in the absence of internal synchronization"으로 바꿨다. 따라서 3판 기준 글에서 "책이 든 이 예"는 맞지 않는다. `runFinalizersOnExit`가 JDK 11에서 제거되었다는 사실 자체는 맞다(JDK-8198249, Fix Version 11).

수정 제안:

> ** 외부에서 동기화해도 멀티스레드에서 쓸 수 없습니다. 주로 동기화 없이 static 데이터를 수정하는 클래스가 여기에 해당합니다. 스레드 적대적인 클래스나 메서드는 발견되면 대개 고쳐지거나 폐기(deprecated)됩니다.
> ** 예: 3판은 아이템 78의 `generateSerialNumber` 메서드가 내부 동기화 없이 static 필드를 증가시킨다면 스레드 적대적이라고 설명합니다. 2판이 예로 든 ``System.runFinalizersOnExit()``는 JDK 11에서 제거되었습니다.

근거: Effective Java 3판 아이템 82 원문 전사본([clxering/Effective-Java-3rd-edition-Chinese-English-bilingual](https://github.com/clxering/Effective-Java-3rd-edition-Chinese-English-bilingual/blob/dev/Chapter-11/Chapter-11-Item-82-Document-thread-safety.md)), 2판 아이템 70 원문 발췌([Java Techies Zone: Item 70](http://jtechies.blogspot.com/2012/07/item-70-document-thread-safety.html)), [JDK-8198249](https://bugs.openjdk.org/browse/JDK-8198249).

원문 대조 판정이지만 출판사 원본이 아니라 제3자의 전사본으로 확인했다(한계 참고). Codex 지적 4번과 Claude의 독자 확인이 일치한다.

**반영 완료.**

### 2. SpotBugs가 `@GuardedBy("this")`를 보고 경고 이름만 바꾼다고 서술함

**위치: 본문 380행, 435행.** 380행은 IS2_INCONSISTENT_SYNC 탐지기가 "경고하기로 한 필드에 ``@GuardedBy("this")``가 붙어 있으면 버그 패턴 이름만 IS_FIELD_NOT_GUARDED로 바꿔서 보고합니다"라고 쓰고, 435행은 "`@GuardedBy` 위반은 lock을 잡은 접근 비율로 짐작했을 때 동기화 누락이라고 판단한 경우에만 보고됩니다"라고 정리한다.

SpotBugs 4.10.4의 `FindInconsistentSync2`에서 `@GuardedBy("this")`는 이름 말고도 두 가지를 바꾼다.

- lock을 잡은 접근이 0회인 필드는 원래 후보에서 빠지는데(`if (!guardedByThis && locked == 0 ...) continue;`), `@GuardedBy("this")` 필드는 빠지지 않는다.
- `ANNOTATED_AS_GUARDED_BY_THIS` 속성(`RAISE_PRIORITY_TO_AT_LEAST_NORMAL`)을 붙인다. `WarningPropertySet.computePriority()`는 오탐 속성(`BELOW_MIN_SYNC_PERCENT` 등)이 있을 때 이 속성이 없으면 경고를 버리고(`EXP_PRIORITY + 1`), 있으면 낮은 우선순위로 남긴다.

그래서 본문의 Counter(lock 비율 33%)가 `-PreportLow`에서 낮은 우선순위로 나타나는 것은 `@GuardedBy("this")` 덕분이다. 예제 복사본에서 Counter의 `@GuardedBy("this")`를 지우고 `-PreportLow`로 다시 실행하면 Counter 경고는 아예 나오지 않았다. 즉 애너테이션은 탐지기가 이미 경고하기로 한 필드의 이름만 바꾸는 것이 아니라, 경고 여부와 우선순위에도 영향을 준다. 33%·60% 예제의 출력 자체는 본문과 같다.

수정 제안(380행 둘째·셋째 문장):

> 그런데 이 패턴은 애너테이션을 보고 위반을 찾는 것이 아닙니다. https://spotbugs.readthedocs.io/en/latest/bugDescriptions.html#is-inconsistent-synchronization-is2-inconsistent-sync[IS2_INCONSISTENT_SYNC] 탐지기는 필드 접근 중 lock을 잡은 비율로 동기화 누락을 추정합니다. 필드에 ``@GuardedBy("this")``가 붙어 있으면 이 탐지기는 버그 패턴 이름을 IS_FIELD_NOT_GUARDED로 바꾸고, lock을 잡은 접근이 없는 필드도 후보에 남기며, 경고 우선순위를 올립니다.

수정 제안(435행 둘째 문장):

> `@GuardedBy` 위반도 결국 lock을 잡은 접근 비율에 기댄 추정으로 보고되며, 애너테이션은 그 추정의 후보와 우선순위를 바꿀 뿐입니다.

근거: [FindInconsistentSync2.java 439–580행](https://github.com/spotbugs/spotbugs/blob/4.10.4/spotbugs/src/main/java/edu/umd/cs/findbugs/detect/FindInconsistentSync2.java#L439), [InconsistentSyncWarningProperty.java](https://github.com/spotbugs/spotbugs/blob/4.10.4/spotbugs/src/main/java/edu/umd/cs/findbugs/detect/InconsistentSyncWarningProperty.java), [WarningPropertySet.java](https://github.com/spotbugs/spotbugs/blob/4.10.4/spotbugs/src/main/java/edu/umd/cs/findbugs/props/WarningPropertySet.java).

소스 대조와 실행 확인. Codex 지적 2번을 Claude가 재검증해 채택했다.

**반영 완료.**

### 3. JCIP 계열 애너테이션이 모두 RUNTIME retention인 것처럼 서술함

**위치: 본문 758행.** "ArchUnit은 바이트코드에서 애너테이션을 읽으므로 JCIP 계열의 RUNTIME retention이든 HttpClient ``@Contract``의 CLASS retention이든 모두 검사할 수 있습니다"라고 쓴다.

Maven Central jar를 `javap -v`로 보면 RUNTIME인 것은 `net.jcip.annotations`(원본 1.0과 stephenc 1.0-1)뿐이다. JSR-305 구현(`com.google.code.findbugs:jsr305:3.0.2`)의 `javax.annotation.concurrent` 네 애너테이션은 CLASS이고, Error Prone의 `@GuardedBy`도 CLASS다(Error Prone의 `@Immutable`·`@ThreadSafe`는 RUNTIME). HttpCore 4.4.4의 `org.apache.http.annotation.ThreadSafe`도 소스 주석에 "The original version used RUNTIME"이라고 적고 CLASS로 바꿨다. 결론(ArchUnit은 두 retention을 모두 읽는다)은 맞지만 "JCIP 계열 = RUNTIME"은 틀리다.

또 같은 문단은 ArchUnit의 능력을 설명하지만, 예제 규칙이 실제로 보는 것은 `net.jcip.annotations.NotThreadSafe` 하나다. JSR-305의 동명 애너테이션이나 `@Contract(threading = UNSAFE)`는 별도 조건을 추가해야 잡힌다.

수정 제안:

> ArchUnit은 바이트코드에서 애너테이션을 읽으므로 원본 JCIP 애너테이션 같은 RUNTIME retention이든 JSR-305 애너테이션이나 HttpClient ``@Contract`` 같은 CLASS retention이든 모두 검사할 수 있습니다. 다만 예제 규칙은 ``net.jcip.annotations.NotThreadSafe``만 보므로, 다른 패키지의 애너테이션이나 ``@Contract``의 `threading` 값을 검사하려면 조건을 추가해야 합니다.

근거: Maven Central의 [jcip-annotations 1.0](https://repo1.maven.org/maven2/net/jcip/jcip-annotations/1.0/), [jsr305 3.0.2](https://repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/), [error_prone_annotations 2.50.0](https://repo1.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.50.0/) jar의 `javap -v` 결과, [HttpCore 4.4.4 ThreadSafe.java](https://github.com/apache/httpcomponents-core/blob/rel/v4.4.4/httpcore/src/main/java/org/apache/http/annotation/ThreadSafe.java), [예제 ThreadSafetyArchTest.java](../examples/thread-safety-archunit/src/test/java/net/benelog/ThreadSafetyArchTest.java).

실행(`javap`) 확인. retention 부분은 Claude가 찾았고, 예제 규칙의 범위는 Codex 지적 9번을 재검증해 합쳤다.

**반영 완료.**

## 표현을 보완할 사항

### 1. `HashMap` 공유 조건과 `Hashtable`의 동기화 범위를 넓게 서술함

**위치: 본문 65행.** "``HashMap``은 생성이 끝난 뒤 다른 스레드에 공개되고 더 이상 변경되지 않는다면 여러 스레드가 ``get()``을 호출해도 문제가 없습니다"와 "모든 메서드가 ``synchronized``인 ``Hashtable``"이라고 쓴다.

생성 후 변경하지 않는 것만으로는 부족하고, 생성한 스레드의 쓰기와 다른 스레드의 읽기 사이에 happens-before 관계가 있도록 안전하게 공개해야 한다(`final` 필드, `volatile` 참조, 같은 lock, 클래스 초기화 등). JDK 25의 `Hashtable`은 `containsValue()`, `keySet()`, `entrySet()`, `values()` 등이 선언에 `synchronized`가 없다(내부에서 동기화된 뷰나 메서드에 위임한다). 또 '키가 없으면 넣는다'는 `Hashtable.putIfAbsent()`와 `Collections.synchronizedMap()`의 `putIfAbsent()`가 이미 원자적으로 처리하므로, 복합 동작의 예로는 두 호출을 조합하는 형태가 정확하다.

수정 제안:

> 예를 들어 ``HashMap``은 생성이 끝난 뒤 안전하게 공개되고(예: `final` 필드나 `volatile` 변수로 전달) 더 이상 변경되지 않는다면 여러 스레드가 ``get()``을 호출해도 문제가 없습니다. … 메서드 대부분이 ``synchronized``인 ``Hashtable``이나 ``Collections.synchronizedMap()``으로 감싼 ``Map``도, ``containsKey()``로 확인한 뒤 ``put()``하는 것처럼 호출 두 개를 조합하면서 외부에서 lock을 잡지 않으면 경쟁 조건이 생깁니다.

근거: [JLS 25 §17.4.5 Happens-before Order](https://docs.oracle.com/javase/specs/jls/se25/html/jls-17.html#jls-17.4.5), [OpenJDK 25 Hashtable.java](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/java.base/share/classes/java/util/Hashtable.java).

소스 대조 판정. Codex 지적 1번을 Claude가 재검증해 채택했다.

**반영 완료.**

### 2. `synchronizedList` 순회 규칙 위반의 결과를 "정의되지 않는다"로 옮김

**위치: 본문 81행.** "그렇지 않으면 순회 결과가 정의되지 않습니다."

`Collections.synchronizedList()` Javadoc의 원문은 "Failure to follow this advice may result in non-deterministic behavior."다. '정의되지 않음(undefined)'은 명세가 결과를 정하지 않았다는 뜻이고, 원문은 실행할 때마다 결과가 달라질 수 있다는 뜻이다.

수정 제안:

> 그렇지 않으면 동작을 예측할 수 없습니다(non-deterministic behavior).

근거: [JDK 25 Collections.synchronizedList Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Collections.html#synchronizedList(java.util.List)).

원문 대조 판정. Claude가 찾았다.

**반영 완료.**

### 3. 불변 예시 중 책에 없는 예시가 책의 분류 요약에 섞여 있음

**위치: 본문 75행.** "예: `String`, `Long`, `BigDecimal`, `java.time.LocalDate`"

3판(2판도 같음)의 불변 예시는 `String`, `Long`, `BigInteger`다. `BigDecimal`과 `LocalDate`도 불변 클래스라 틀린 예는 아니지만, 이 목록은 책의 분류를 요약하는 자리여서 책의 예시로 읽힌다. 나머지 네 단계의 예시(`AtomicLong`, `ConcurrentHashMap`, synchronized 래퍼, `ArrayList`, `HashMap`)는 책과 같다.

수정 제안:

> ** 예: `String`, `Long`, `BigInteger`(책의 예), `java.time.LocalDate`

근거: 우선 수정 1번과 같은 3판·2판 원문 전사본.

원문 대조 판정(전사본). Codex 지적 4번 일부를 재검증해 채택했다.

**반영 완료.**

### 4. `java.time` 클래스 전체가 같은 문장을 쓴다고 일반화함

**위치: 본문 105행.** "JDK 8에서 추가된 `java.time` 패키지의 클래스들은 Javadoc의 `@implSpec` 태그로 같은 문장을 같은 자리에 적습니다."

JDK 25 소스에서 `LocalDate`, `Instant`, `Duration`, `Period`, `Year`, `ZonedDateTime`은 `@implSpec`에 "This class is immutable and thread-safe."를 적는다. 그러나 `ZoneId`는 "both of which are immutable and thread-safe"처럼 다른 문장이고, `Clock`은 구현체에 스레드 안전성을 요구하는 설명이며, `DateTimeFormatterBuilder`는 가변 builder다. 자리(`@implSpec`)는 대체로 같지만 문장까지 같다고 하면 과하다.

수정 제안:

> JDK 8에서 추가된 `java.time` 패키지의 불변 클래스들은 대부분 Javadoc의 `@implSpec` 태그로 같은 문장을 같은 자리에 적습니다.

근거: OpenJDK jdk-25-ga의 [LocalDate.java](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/java.base/share/classes/java/time/LocalDate.java), [ZoneId.java](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/java.base/share/classes/java/time/ZoneId.java), [Clock.java](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/java.base/share/classes/java/time/Clock.java), [DateTimeFormatterBuilder Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/format/DateTimeFormatterBuilder.html).

소스 대조 판정. Codex 지적 5번과 Claude의 독자 확인이 일치한다.

**반영 완료.**

### 5. Error Prone `@GuardedBy` 검사가 모든 위반 접근을 잡는 것처럼 서술함

**위치: 본문 443행.** "접근 비율에 따라 보고 여부가 달라지는 SpotBugs와 달리, 위반하는 접근 하나하나를 그대로 잡습니다."

Error Prone 2.50.0의 `GuardedByChecker`는 생성자, 필드 초기화, 인스턴스·클래스 초기화 블록의 접근을 검사하지 않는다(초기화 중에는 객체가 스레드에 한정된다고 본다). guard가 `ReadWriteLock`이면 `checkGuardedAccess()`가 바로 `NO_MATCH`를 돌려준다. 비율에 기대지 않고 접근마다 판정한다는 대비는 맞지만, 분석 범위의 예외를 밝히는 편이 정확하다.

수정 제안:

> 접근 비율에 따라 보고 여부가 달라지는 SpotBugs와 달리, 분석 범위 안의 접근마다 lock을 잡았는지 판정합니다. 다만 생성자와 초기화 블록 안의 접근, `ReadWriteLock`으로 보호하는 필드는 검사하지 않습니다.

근거: [GuardedByChecker.java](https://github.com/google/error-prone/blob/v2.50.0/core/src/main/java/com/google/errorprone/bugpatterns/threadsafety/GuardedByChecker.java) 68–70행, 112행, 115–130행.

소스 대조 판정. Codex 지적 3번을 Claude가 재검증해 채택했다.

**반영 완료.**

### 6. `ThreadSafeChecker`의 통과 조건에서 예외를 뺌

**위치: 본문 674행.** "이 검사를 통과하려면 필드가 ``final``이면서 선언 타입이 Error Prone이 스레드 안전하다고 아는 타입이거나, ``@GuardedBy``가 붙어 있어야 합니다."

`ThreadSafeAnalysis`의 필드 판정은 `static` 필드를 검사하지 않고, `@LazyInit`이 붙은 필드는 `final`이 아니어도 통과시킨 뒤 타입만 본다. 예제의 세 필드 판정은 본문 설명대로다.

수정 제안:

> 이 검사를 통과하려면 인스턴스 필드는 ``final``이면서 선언 타입이 Error Prone이 스레드 안전하다고 아는 타입이거나, ``@GuardedBy``가 붙어 있어야 합니다(`static` 필드는 검사하지 않고, `@LazyInit` 필드는 `final` 조건에서 제외합니다).

근거: [ThreadSafeAnalysis.java](https://github.com/google/error-prone/blob/v2.50.0/core/src/main/java/com/google/errorprone/bugpatterns/threadsafety/ThreadSafeAnalysis.java) 231–250행.

소스 대조 판정. Codex 지적 8번을 Claude가 재검증해 채택했다.

**반영 완료.**

### 7. IntelliJ와 SpotBugs의 `@Immutable` 검사 범위를 같다고 서술함

**위치: 본문 686행, 692행.** 686행은 "검사 범위는 Error Prone이 아니라 SpotBugs와 같습니다. 원본 JCIP와 JSR-305 패키지에 더해 Error Prone의 ``com.google.errorprone.annotations.Immutable``도 인식합니다"라고 쓰고, 692행은 "``@Immutable``은 SpotBugs와 같은 범위로 보지만"이라고 정리한다.

둘 다 필드 타입의 불변성은 보지 않는다는 점은 맞다. 그러나 SpotBugs는 `transient`·`volatile` 필드를 제외하는 반면(본문 301행), IntelliJ의 `NonFinalFieldInImmutableInspection`은 `final` 여부만 보므로 두 필드도 경고한다. 인식 대상도 더 넓다. `ConcurrencyAnnotationsManager`의 기본 목록은 `org.apache.http.annotation`과 `com.android.annotations.concurrency`의 `Immutable`, `com.google.auto.value.AutoValue`를 포함하고, `JCiPUtil.isImmutable()`은 Javadoc의 `@Immutable` 태그도 인정한다.

수정 제안(686행):

> 필드 타입이 가변인지는 보지 않으므로 검사 깊이는 Error Prone이 아니라 SpotBugs에 가깝습니다. 다만 SpotBugs와 달리 `transient`나 `volatile` 필드도 경고합니다. 원본 JCIP와 JSR-305 패키지에 더해 Error Prone의 ``com.google.errorprone.annotations.Immutable``, AutoValue의 ``@AutoValue``, Javadoc의 `@Immutable` 태그도 인식합니다.

근거: [NonFinalFieldInImmutableInspection.java](https://github.com/JetBrains/intellij-community/blob/826413b22cfe5b5c573662f5d2442f454dbc0b23/java/java-analysis-impl/src/com/intellij/codeInspection/concurrencyAnnotations/NonFinalFieldInImmutableInspection.java), [JCiPUtil.java](https://github.com/JetBrains/intellij-community/blob/826413b22cfe5b5c573662f5d2442f454dbc0b23/java/java-analysis-impl/src/com/intellij/codeInspection/concurrencyAnnotations/JCiPUtil.java), [ConcurrencyAnnotationsManager.java](https://github.com/JetBrains/intellij-community/blob/826413b22cfe5b5c573662f5d2442f454dbc0b23/java/java-psi-api/src/com/intellij/codeInsight/ConcurrencyAnnotationsManager.java), [SpotBugs CheckImmutableAnnotation.java](https://github.com/spotbugs/spotbugs/blob/4.10.4/spotbugs/src/main/java/edu/umd/cs/findbugs/detect/CheckImmutableAnnotation.java).

소스 대조 판정. `transient`·`volatile` 차이는 Codex 지적 6번, 인식 대상 범위는 Claude가 찾았다.

**반영 완료.**

### 8. IntelliJ 인스펙션이 "편집기 경고에 그친다"고 정리함

**위치: 본문 692행.** "인스펙션이 기본으로 꺼져 있고 편집기 경고에 그칩니다."

690행은 "Qodana 같은 별도 실행 환경"이라고 써서 다른 방법을 열어 두었지만, 692행의 정리는 IntelliJ 검사를 편집기 안으로 한정한다. IntelliJ IDEA에는 명령행 검사 기능(`inspect.sh`/`idea.sh inspect`)도 있어서 CI에서 결과를 판정할 수 있다. `javac` 컴파일을 막지 않는다는 점은 맞다.

수정 제안:

> 인스펙션이 기본으로 꺼져 있고 `javac` 컴파일에는 연동되지 않으므로, 강제하려면 Qodana나 명령행 검사 같은 별도 실행 환경이 필요합니다.

근거: [IntelliJ IDEA: Run code inspections from the command line](https://www.jetbrains.com/help/idea/command-line-code-inspector.html).

문서 대조 판정. Codex 지적 7번을 재검증해 채택했다.

**반영 완료.**

### 9. ArchUnit 규칙의 한계에 상속 필드가 빠짐

**위치: 본문 833행.** 한계로 raw 선언 타입, 상위 타입의 애너테이션, 외부 lock, scope, JDK 타입 목록을 든다.

예제 규칙은 `fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)`이므로, `@RestController`가 없는 상위 클래스에 선언된 필드를 컨트롤러가 상속하면 검사하지 않는다. 한계 목록에 한 문장 더할 만하다. 다른 패키지의 애너테이션을 보지 않는 점은 우선 수정 3번에서 다뤘다.

수정 제안(833행 끝에 추가):

> 규칙은 ``@RestController``가 붙은 클래스에 직접 선언된 필드만 보므로, 애너테이션이 없는 상위 클래스에서 상속한 필드도 놓칩니다.

근거: [예제 ThreadSafetyArchTest.java](../examples/thread-safety-archunit/src/test/java/net/benelog/ThreadSafetyArchTest.java), [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html).

코드 대조 판정. Codex 지적 9번 일부를 재검증해 채택했다.

**반영 완료.**

### 10. SonarQube로 넘길 SpotBugs 보고서 형식이 빠짐

**위치: 본문 289행.** "SonarQube에서 이 결과를 보려면 SpotBugs를 빌드에서 돌려 보고서를 넘겨야 합니다."

SonarJava의 `SpotBugsXmlReportReader`는 루트가 `BugCollection`인 XML만 읽고, 경로는 `sonar.java.spotbugs.reportPaths`로 지정한다. 예제는 텍스트 보고서(`main.txt`)만 만들므로 그대로는 넘길 수 없다.

수정 제안:

> 따라서 SonarQube에서 이 결과를 보려면 SpotBugs를 빌드에서 돌려 XML 보고서를 만들고 `sonar.java.spotbugs.reportPaths`로 넘겨야 합니다.

근거: [SonarQube: External analyzer reports](https://docs.sonarsource.com/sonarqube-server/analyzing-source-code/importing-external-issues/external-analyzer-reports), [SpotBugsXmlReportReader.java](https://github.com/SonarSource/sonar-java/blob/9bf31b6e003782152b920fac0b1cfa5ba55e05a1/external-reports/src/main/java/org/sonar/java/externalreport/SpotBugsXmlReportReader.java).

소스·문서 대조 판정. Codex 보완 지적을 재검증해 채택했다.

**반영 완료.**

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| Effective Java 3판 아이템 82가 `synchronized`를 구현 세부 사항으로 보고 Javadoc이 출력하지 않는 것을 옹호함(36행) | **확인.** "The presence of the synchronized modifier in a method declaration is an implementation detail, not a part of its API." (전사본) | [3판 아이템 82 전사본](https://github.com/clxering/Effective-Java-3rd-edition-Chinese-English-bilingual/blob/dev/Chapter-11/Chapter-11-Item-82-Document-thread-safety.md) |
| 다섯 단계 분류와 무조건·조건부·안전하지 않음 예시(76–84행) | **확인.** 3판 원문과 같다(전사본). | 위와 같음 |
| Effective Java(2판부터)가 분류를 JCIP 애너테이션과 대략 대응시킴(206행), 조건부·무조건부 모두 `@ThreadSafe`(표) | **확인.** 2판·3판 모두 "correspond roughly to the thread safety annotations"라고 쓰고, 3판은 두 분류가 모두 `@ThreadSafe`에 해당한다고 밝힌다. | [2판 아이템 70 발췌](http://jtechies.blogspot.com/2012/07/item-70-document-thread-safety.html), 3판 전사본 |
| `LinkedList` Javadoc 세 번째 문단의 굵은 'not synchronized'(95행) | **확인.** 세 번째 `<p>`가 `<strong>Note that this implementation is not synchronized.</strong>`로 시작한다. | [JDK 25 LinkedList](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/LinkedList.html) |
| `SimpleDateFormat`의 Synchronization 절과 API Note(101행) | **확인.** "Date formats are not synchronized.", "API Note: Consider using DateTimeFormatter as an immutable and thread-safe alternative." | [JDK 25 SimpleDateFormat](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/text/SimpleDateFormat.html) |
| `DateTimeFormatter`·`LocalDate`의 Implementation Requirements 문장(105행) | **확인.** 두 문서 모두 "This class is immutable and thread-safe." | [DateTimeFormatter](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/format/DateTimeFormatter.html), [LocalDate](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/LocalDate.html) |
| Spring Batch 5.2.6 `FlatFileItemWriter`의 'The implementation is *not* thread-safe.'(109행) | **확인.** 문서 제목이 5.2.6이고 원문이 `The implementation is <b>not</b> thread-safe.` | [FlatFileItemWriter](https://docs.spring.io/spring-batch/docs/current/api/org/springframework/batch/item/file/FlatFileItemWriter.html) |
| `ThreadingBehavior` 여섯 값의 의미와 기본값 `UNSAFE`, `@Contract`의 `@Documented`(124–145, 165행) | **확인.** HttpCore 5.3 소스와 같다. | [ThreadingBehavior.java](https://github.com/apache/httpcomponents-core/blob/rel/v5.3/httpcore5/src/main/java/org/apache/hc/core5/annotation/ThreadingBehavior.java), [Contract.java](https://github.com/apache/httpcomponents-core/blob/rel/v5.3/httpcore5/src/main/java/org/apache/hc/core5/annotation/Contract.java) |
| HttpClient 5.5의 세 클래스 선언과 'fully thread-safe' 설명(149–169행) | **확인.** 선언이 본문 코드와 같다. | [HttpClient 5.5 소스](https://github.com/apache/httpcomponents-client/tree/rel/v5.5/httpclient5/src/main/java/org/apache/hc/client5/http/impl) |
| HttpClient 4.5.2의 네 클래스 선언과 JCIP 유래 설명(171–190행) | **확인.** 4.5.2 선언이 본문과 같고, HttpCore 4.4.4 `ThreadSafe.java`에 "concepts published in 'Java Concurrency in Practice'"가 있다. | [HttpClient 4.5.2 소스](https://github.com/apache/httpcomponents-client/tree/rel/v4.5.2/httpclient/src/main/java/org/apache/http), [HttpCore 4.4.4 ThreadSafe.java](https://github.com/apache/httpcomponents-core/blob/rel/v4.4.4/httpcore/src/main/java/org/apache/http/annotation/ThreadSafe.java) |
| HttpCore 4.4.5가 네 애너테이션을 제거하고 HttpClient 4.5.3이 `@Contract`로 바뀜(190, 248행) | **확인.** 4.4.5 릴리스 노트가 네 애너테이션 제거를 명시하고, 4.4.5 패키지에 `Contract`·`ThreadingBehavior`가 생겼다. HTTPCLIENT-1743의 Fix Version은 4.5.3·5.0 Alpha2다. 단, 4.5.3의 `HttpGet`은 `@Contract`로 바뀌지 않고 애너테이션만 없어졌다(Codex 보완 지적, 본문 결론에는 영향 없음). | [HttpCore 4.4.x RELEASE_NOTES](https://github.com/apache/httpcomponents-core/blob/4.4.x/RELEASE_NOTES.txt), [HTTPCLIENT-1743](https://issues.apache.org/jira/browse/HTTPCLIENT-1743), [4.5.3 HttpGet.java](https://github.com/apache/httpcomponents-client/blob/rel/v4.5.3/httpclient/src/main/java/org/apache/http/client/methods/HttpGet.java) |
| 원본 JCIP의 CC Attribution 라이선스, CC 재단의 소프트웨어 비권장, stephenc의 Apache 2.0 재구현(241행) | **확인.** stephenc README가 원본을 CC BY 2.5라고 밝히고 clean-room 재구현이라고 쓴다. CC FAQ: "We recommend against using Creative Commons licenses for software." | [stephenc/jcip-annotations](https://github.com/stephenc/jcip-annotations), [CC FAQ](https://creativecommons.org/faq/#can-i-apply-a-creative-commons-license-to-software) |
| Guava가 `error_prone_annotations`를 의존성으로 가짐(255행) | **확인.** Guava 33.5.0-jre POM에 의존성이 있다. | [guava-33.5.0-jre.pom](https://repo1.maven.org/maven2/com/google/guava/guava/33.5.0-jre/guava-33.5.0-jre.pom) |
| SonarJava S3077이 JSR-305 `@Immutable`·`@ThreadSafe`를 예외로 보고, 외부 규칙 목록에 세 SpotBugs 패턴이 있음(289행) | **확인.** 구현 102–103행과 `spotbugs-rules.json`. "애너테이션 참조 구현은 S3077뿐"이라는 저장소 전체 검색은 재현하지 않았다(한계). | [VolatileNonPrimitiveFieldCheck.java](https://github.com/SonarSource/sonar-java/blob/9bf31b6e003782152b920fac0b1cfa5ba55e05a1/java-checks/src/main/java/org/sonar/java/checks/VolatileNonPrimitiveFieldCheck.java) |
| SpotBugs가 세 패키지를 인식하고, Jakarta Annotations 3.0.0에 `concurrent` 패키지가 없음(295–299행) | **확인.** `NoteJCIPAnnotation`의 세 접두어, `jakarta.annotation-api-3.0.0.jar`에 `concurrent` 항목 없음. | [NoteJCIPAnnotation.java](https://github.com/spotbugs/spotbugs/blob/4.10.4/spotbugs/src/main/java/edu/umd/cs/findbugs/detect/NoteJCIPAnnotation.java) |
| `JCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS`가 `transient`·`volatile`을 제외(301행) | **확인.** `!obj.isFinal() && !obj.isTransient() && !obj.isVolatile()` | [CheckImmutableAnnotation.java](https://github.com/spotbugs/spotbugs/blob/4.10.4/spotbugs/src/main/java/edu/umd/cs/findbugs/detect/CheckImmutableAnnotation.java) |
| SpotBugs 예제 출력(Memo, Counter 33% Low, LockedCounter 60% High) | **확인.** 실행 결과가 본문 출력과 글자 단위로 같다. | 로컬 실행 |
| `@NotThreadSafe` 필드 제외, `@ThreadSafe`는 우선순위 상향(433행), 50% 미만은 오탐으로 취급(380행) | **확인.** `continue`와 `ANNOTATED_AS_THREAD_SAFE`(`RAISE_PRIORITY`), `MIN_SYNC_PERCENT = 50`과 `BELOW_MIN_SYNC_PERCENT`(`FALSE_POSITIVE`). | [FindInconsistentSync2.java](https://github.com/spotbugs/spotbugs/blob/4.10.4/spotbugs/src/main/java/edu/umd/cs/findbugs/detect/FindInconsistentSync2.java) |
| Error Prone `@GuardedBy`가 JSR-305·androidx·Error Prone 패키지를 인식하고 원본 JCIP는 인식하지 않음(445행) | **확인.** `GuardedByUtils`의 목록에 `net.jcip`가 없다. 실행에서도 JcipCounter만 통과했다. | [GuardedByUtils.java](https://github.com/google/error-prone/blob/v2.50.0/core/src/main/java/com/google/errorprone/bugpatterns/threadsafety/GuardedByUtils.java) |
| Error Prone `@Immutable` 검사가 자체 패키지만 검사한다고 문서에 명시(445, 569행) | **확인.** "Other versions of the annotation, such as `javax.annotation.concurrent.Immutable`, are currently *not* enforced." 실행에서도 JsrMemo는 통과했다. | [Immutable.md](https://github.com/google/error-prone/blob/v2.50.0/docs/bugpattern/Immutable.md) |
| Error Prone 예제 출력(JsrCounter, ErrorProneCounter, ErrorProneMemo 두 필드, `-PthreadSafeCheck`의 ErrorProneRegistry 두 필드) | **확인.** 실행 결과가 본문과 같다. | 로컬 실행 |
| `ThreadSafe`를 켜면 'ThreadSafe is not a valid checker name'으로 실패하고, 2.50.0·2.45.0·2.20.0·2.3.0의 `BuiltInCheckerSuppliers`에 `ThreadSafeChecker`가 없음(573행) | **확인.** 예제 복사본에 `error("ThreadSafe")`를 넣어 같은 오류를 재현했고, 네 태그의 파일에서 `ThreadSafeChecker` 참조가 0건이다(2.3.0과 2.45.0 사이 모든 버전은 보지 않음). | [BuiltInCheckerSuppliers.java](https://github.com/google/error-prone/blob/v2.50.0/core/src/main/java/com/google/errorprone/scanner/BuiltInCheckerSuppliers.java), 로컬 실행 |
| `ThreadSafeChecker` 생성자가 패키지 전용(619행) | **확인.** 82행 `ThreadSafeChecker(`에 접근 제한자가 없다. | [ThreadSafeChecker.java](https://github.com/google/error-prone/blob/v2.50.0/core/src/main/java/com/google/errorprone/bugpatterns/threadsafety/ThreadSafeChecker.java) |
| IntelliJ 'Concurrency annotation issues' 여섯 인스펙션(`@GuardedBy` 다섯, `@Immutable` 하나)이 모두 `enabledByDefault="false"`, `WARNING`(680, 690행) | **확인.** `UnknownGuard`, `StaticGuardedByInstance`, `NonFinalGuard`, `InstanceGuardedByStatic`, `FieldAccessNotGuarded`, `NonFinalFieldInImmutable` | [Inspections.xml](https://github.com/JetBrains/intellij-community/blob/826413b22cfe5b5c573662f5d2442f454dbc0b23/java/java-backend/resources/META-INF/Inspections.xml) |
| `@GuardedBy` 인식 패키지와 `@ThreadSafe` 기본 목록, Error Prone `@ThreadSafe` 미포함(684, 688행) | **확인.** `FRAMEWORKS`·`ANDROID_FRAMEWORKS`(`AnyThread`)와 Error Prone `GuardedBy`만 추가하는 생성자. | [ConcurrencyAnnotationsManager.java](https://github.com/JetBrains/intellij-community/blob/826413b22cfe5b5c573662f5d2442f454dbc0b23/java/java-psi-api/src/com/intellij/codeInsight/ConcurrencyAnnotationsManager.java) |
| Access to static field locked on instance가 `final` 필드의 선언 타입 애너테이션을 확인(688행) | **확인.** 상수가 아닌 `static` 필드만 보고, `final`이면 `getThreadSafeList()`로 확인한다. | [AccessToStaticFieldLockedOnInstanceInspection.java](https://github.com/JetBrains/intellij-community/blob/826413b22cfe5b5c573662f5d2442f454dbc0b23/java/java-impl-inspections/src/com/siyeh/ig/threading/AccessToStaticFieldLockedOnInstanceInspection.java) |
| ArchUnit 예제 두 규칙이 실패하고 본문 메시지를 냄(805–814행), 의존성 버전(805행) | **확인.** `./gradlew test` 결과 2개 중 2개 실패, 위반 필드가 본문과 같다. `build.gradle.kts`의 ArchUnit 1.5.0, Spring Web 7.0.9, JUnit 6.1.3. | 로컬 실행 |
| `BasicHttpClientConnectionManager`는 한 번에 한 실행 스레드가 쓰도록 권함 | **확인.** Codex 보완 제안, 재검증 결과 채택하지 않음. 본문의 인용('fully thread-safe')은 정확하고, 추가 설명은 글의 범위를 넓히는 선택 사항이다. | [BasicHttpClientConnectionManager.java](https://github.com/apache/httpcomponents-client/blob/rel/v5.5/httpclient5/src/main/java/org/apache/hc/client5/http/impl/io/BasicHttpClientConnectionManager.java) |

## 한계

- Effective Java 원문은 출판사 판본을 직접 보지 못했다. 3판은 GitHub의 영중 대역 전사본, 2판은 블로그 발췌, 1판은 studfile의 미리보기 검색 결과로 확인했다. 세 출처의 문장이 서로 맞고 판본 간 차이도 일관되지만, 쪽 번호는 확인하지 않았다.
- SonarJava 저장소 전체를 `ThreadSafe`, `GuardedBy`, `javax.annotation.concurrent`로 검색한 결과(289행)는 재현하지 않았다. S3077 구현과 외부 규칙 목록만 지정 커밋에서 대조했다.
- Error Prone에서 `ThreadSafeChecker`가 없다는 이전 버전 범위(2.3.0–2.45.0)는 2.3.0, 2.20.0, 2.45.0 세 태그만 표본으로 봤다.
- 예제 저장소와의 정합성(본문의 사실 관계와는 별개):
  - `examples/thread-safety-static-analysis/spotbugs/src/main/java/net/benelog/Counter.java`의 Javadoc은 "SpotBugs는 이 위반을 보고하지 않는다"라고 쓰지만, 실제로는 `-PreportLow`에서 낮은 우선순위로 보고된다. 본문 382행의 설명이 정확하다. **반영 완료.** 주석을 기본 설정과 `-PreportLow`의 차이로 고쳤다(`count++`의 행 번호가 바뀌지 않게 두 줄로 유지).
  - 본문 332–369행의 `spotbugs/build.gradle.kts` 발췌에는 실제 파일 23–26행의 `reportLow` 처리가 빠져 있다. 382행에서 `-PreportLow`를 쓰므로, 발췌를 그대로 따라 하면 이 옵션이 동작하지 않는다. Error Prone 쪽은 645–655행에서 생략 사실을 밝혔지만 SpotBugs 쪽은 밝히지 않았다. **반영 완료.** 본문 발췌에 `reportLow` 블록을 넣어 실제 파일과 같게 했다.
