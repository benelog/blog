# Java auto boxing의 컴파일 방식과 Integer 캐시 글 사실 관계 검증 — 2026-09-26

- 대상: [java-auto-boxing.adoc](../src/content/java-auto-boxing.adoc)
- 검토 기준 커밋: `16d2fddea98e6610b2b0e3f9eba4ffccd24ecd7c` (대상 글의 마지막 변경은 `032a1e0`이다. 작업 트리에는 `.claude/skills/` 아래 미커밋 변경이 있으나 대상 글은 커밋과 같다.)
- 이전 기록: 없음
- 범위: 글 전체. 바이트코드와 CFR 역컴파일 출력, JDK 25 `Integer`·`Long`·`Short`·`Byte`·`Character`·`Boolean`·`Double`·`Float` 소스 발췌와 캐시 범위, CDS·`@Stable` 설명, `-XX:AutoBoxCacheMax`와 `java.lang.Integer.IntegerCache.high`의 관계, JLS 5.1.7 인용(SE 8, SE 25)과 버전별 변화, 'JDK 버전별 변경 이력' 표의 버전·이슈 번호·backport, `javac` 경고 출력, SpotBugs `DM_NUMBER_CTOR` 인용, Eclipse·IntelliJ IDEA 검사 설명, Effective Java 3판 Item 6·61, 반복문 측정 결과를 봤다.
- 방법: Codex 작업(`task`, 글 전체 대상)으로 의심 지점을 모은 뒤, Claude가 1차 출처로 다시 확인했다. JDK 소스는 `jdk-25+36` 태그의 원본 파일을 받아 대조했고, JLS는 SE 6(3판)·7·8·9·13·14·25의 5.1.7절을 curl로 받아 태그를 벗기고 문장을 비교했다. JBS 이슈의 Fix Version과 backport는 JBS REST API로 확인했다. 예제는 로컬 Temurin 25+36에서 다시 실행했다(`javap -c`, CFR 0.152 `--sugarboxing false`/기본값, `Identity`의 기본·`-XX:AutoBoxCacheMax=1000`·`-D` 실행, `javac` 기본·`-Xlint:deprecation`). `javac` 경고는 Temurin 21.0.8과 8.0.462에서도 실행했다. 반복문 측정은 본문과 같은 `boxed()`에 `primitive()`와 `System.nanoTime()` 측정을 붙인 코드로 재현했다.
- 결과: **우선 수정할 사항 3건, 표현을 보완할 사항 4건.** 바이트코드·역컴파일·실행 출력은 모두 본문과 같고, 박싱이 `valueOf()`/`xxxValue()` 호출로 컴파일되며 `==` 결과가 캐시 범위와 실행 옵션에 따라 달라진다는 핵심 결론은 타당하다. 고칠 곳은 JLS 5.1.7이 "SE 8까지 리터럴만 보장했다"는 역사 서술, JDK-6807702를 옵션·프로퍼티의 도입으로 적은 표의 JDK 7 행, 역컴파일 결과의 boolean 표현을 설명한 문장이다.
- 반영 상태: **우선 수정 3건과 표현 보완 4건을 모두 본문에 반영함(반영한 수정은 이 기록과 같은 커밋에 합쳐져 있다).** 우선 수정 1번의 375행 제안 문장은 AsciiDoc에서 백틱 뒤에 한글 조사가 붙어 렌더링이 깨져서, 나열 순서를 바꾸고 "부터 … 까지"로 풀어 반영했다. 본문의 `jbake-last_updated`와 변경이력도 갱신했다. 이 문서의 검증 본문은 작성 당시의 수정 제안을 그대로 둔다.
- 아래 행 번호는 위 커밋 기준이다.

## 우선 수정할 사항

### 1. JLS 5.1.7이 "Java SE 8까지 리터럴에 대해서만" 보장했다는 서술이 틀렸다

**위치: 본문 336–337행(표의 JDK 9 행), 342–343행(표의 JDK 14 행), 375행, 382행.** 375행은 "Java SE 8까지는 참조 동일성을 '리터럴'에 대해서만 보장했습니다"라고 쓰고, 382행은 SE 9의 변화를 "명세가 구현에 맞춰 보장 범위를 넓혔습니다"로 맺는다. 표의 JDK 14 행은 "타입 목록에 `byte` 추가"라고 쓴다.

리터럴 제한은 SE 8에서만 있었다. JLS 3판(Java 5·6)과 SE 7은 리터럴이나 상수식이라는 조건 없이 값과 타입으로 보장했다.

- JLS 3판·SE 7: "If the value p being boxed is true, false, a byte, or a char in the range \u0000 to \u007f, or an int or short number between -128 and 127 (inclusive), then let r1 and r2 be the results of any two boxing conversions of p. It is always the case that r1 == r2."
- SE 8: "an integer literal of type int between -128 and 127 inclusive, or the boolean literal true or false, or a character literal ..." (리터럴로 한정, `byte`·`short` 빠짐)
- SE 9–13: "the result of evaluating a constant expression of type boolean, char, short, int, or long ..." (`long` 처음 포함, `byte`는 여전히 빠짐)
- SE 14–25: 같은 문장의 타입 목록에 `byte`가 다시 들어감

따라서 "SE 8까지 리터럴만"은 SE 8 한 판의 문구를 그 이전 전체로 넓힌 것이다. 382행의 "명세가 구현에 맞춰 보장 범위를 넓혔다"도 SE 8 대비로는 맞지만, SE 7 이전과 비교하면 SE 9의 문구가 오히려 상수식으로 조건을 붙인 것이어서 한 방향으로 넓어진 이력이 아니다. `byte`는 SE 7에 있었으므로 JDK 14 행은 "추가"보다 "다시 포함"이 정확하다. 본문에 인용한 SE 8 문장 자체는 원문과 같다.

수정 제안(375행):

> JLS 5.1.7의 문구도 버전마다 달랐습니다. JLS 3판(Java 5·6)과 Java SE 7은 리터럴 여부와 관계없이 -128~127의 `int`·`short`, `byte`, `'\u0000'`~`'\u007f'`의 `char`, `true`·`false`를 박싱한 결과가 같은 참조라고 규정했습니다. Java SE 8은 이 조건을 '리터럴'로 좁혀 적었습니다.

수정 제안(382행):

> Java SE 9는 조건을 상수식의 평가 결과로 바꾸고 `long` 을 대상 타입에 넣었습니다. `Integer x = 100 + 27;` 처럼 리터럴이 아닌 상수식도 SE 8과 달리 보장 대상이 됩니다. JDK 구현은 JDK 5부터 ``valueOf()``에서 -128~127을 캐시해 왔으므로, 어느 판의 문구에서도 실제 동작은 같았습니다.

수정 제안(표):

> |JDK 9 +
> |wrapper 클래스의 생성자에 `@Deprecated(since="9")` 부여. JLS 5.1.7의 참조 동일성 보장 조건이 SE 8의 '리터럴'에서 '상수식의 평가 결과'로 바뀌고, 대상 타입에 `long` 포함
>
> |JDK 14 +
> |JLS 5.1.7의 참조 동일성 보장 대상 타입 목록에 `byte` 를 다시 포함 (SE 7에는 있었고 SE 8–13에는 빠져 있었음)

근거: [JLS 3판 5.1.7](https://docs.oracle.com/javase/specs/jls/se6/html/conversions.html#5.1.7), [JLS SE 7 5.1.7](https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.7), [JLS SE 8 5.1.7](https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.7), [JLS SE 9 5.1.7](https://docs.oracle.com/javase/specs/jls/se9/html/jls-5.html#jls-5.1.7), [JLS SE 13 5.1.7](https://docs.oracle.com/javase/specs/jls/se13/html/jls-5.html#jls-5.1.7), [JLS SE 14 5.1.7](https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.7).

원문 대조 판정. Codex 지적 1번을 Claude가 확인했고, `long`이 SE 9에서 처음 들어간 점과 `byte`가 SE 7에 있었던 점은 Claude가 추가로 찾았다.

**반영 완료.**

### 2. 표의 JDK 7 행이 JDK-6807702를 옵션·프로퍼티의 도입으로 적었다

**위치: 본문 333–334행, 355행.** JDK 7 행은 "`IntegerCache` 의 상한이 설정 가능해짐. `-XX:AutoBoxCacheMax` 옵션과 `java.lang.Integer.IntegerCache.high` 프로퍼티 도입 (JDK-6807702)"이라고 쓴다.

JDK-6807702 커밋(`a2b46bd`, 2009-03-24)이 바꾼 파일은 `Integer.java`, `Long.java`, `System.java`와 테스트뿐이다. 이 커밋의 부모(`7d94fdb`)에 이미 HotSpot의 `AutoBoxCacheMax` product 플래그(C2 전용, `c2_globals.hpp`)가 있었고, `arguments.cpp`는 `AggressiveOpts`나 `AutoBoxCacheMax`가 지정되면 `java.lang.Integer.IntegerCache.high` 프로퍼티를 넣고 있었다. 이슈 설명도 "HotSpot already has the ability to configure a larger cache if supported by the Integer implementation"이라고 쓴다. 즉 이 변경은 옵션과 프로퍼티를 새로 만든 것이 아니라, 클래스 라이브러리가 그 값을 읽어 캐시 상한을 정하도록 한 것이다.

또 JBS에서 JDK-6807702 본 레코드의 Fix Version은 `6u14`이고, JDK 7은 backport 레코드(JDK-2174041)로 기록되어 있다. 355행은 "OpenJDK mainline 최초 반영 기준"이라고 밝혔으므로 JDK 7 표기는 그 기준에 맞지만, 다른 backport(11.0.6, 21.0.2)처럼 6u14도 적는 편이 일관된다.

수정 제안(표):

> |JDK 7 +
> |HotSpot의 `-XX:AutoBoxCacheMax` 옵션이 넘기는 `java.lang.Integer.IntegerCache.high` 값을 `IntegerCache` 가 읽어 캐시 상한을 정하도록 변경 (JDK-6807702)

수정 제안(355행 뒤 문장):

> ``IntegerCache``의 상한 설정(JDK-6807702)은 6u14에, CDS 아카이브 반영(JDK-8209120)은 11.0.6에, 박스 캐시의 `@Stable` 변경(JDK-8295555)은 21.0.2에 backport되었습니다.

근거: [JDK-6807702](https://bugs.openjdk.org/browse/JDK-6807702) (Fix Version 6u14, backport JDK-2174041 Fix Version 7), [JDK-6807702 커밋](https://github.com/openjdk/jdk/commit/a2b46bd320752bffdec24c95c775dd5609233d03), [직전 커밋의 arguments.cpp](https://github.com/openjdk/jdk/blob/7d94fdb066380613add05e6b0874660a112d2680/hotspot/src/share/vm/runtime/arguments.cpp#L1351), [직전 커밋의 c2_globals.hpp](https://github.com/openjdk/jdk/blob/7d94fdb066380613add05e6b0874660a112d2680/hotspot/src/share/vm/opto/c2_globals.hpp#L376).

소스 대조 판정. Codex 지적 2번을 Claude가 확인했고, 6u14 Fix Version은 Claude가 JBS에서 추가로 찾았다. HotSpot 플래그가 처음 들어간 버전은 확인하지 못했다(한계 참고).

**반영 완료.**

### 3. 역컴파일 결과의 boolean 표현을 "바이트코드에 boolean 연산이 따로 없고 분기 명령만 있어서"로 설명했다

**위치: 본문 76행.** "바이트코드에는 boolean 연산이 따로 없고 분기 명령만 있어서 이런 모양이 됩니다."

JVMS 2.3.4는 boolean 값만을 위한 전용 명령이 없고 boolean 식은 `int` 명령으로 컴파일된다고 설명한다. `&`, `|`, `^` 같은 boolean 연산은 `iand`, `ior`, `ixor`로 처리되므로 "분기 명령만 있다"는 틀렸다. 이 예제에서 `(... ? 1 : 0) != 0` 모양이 나온 이유는 `==` 비교 결과를 값으로 쓰려면 `if_icmpne` 분기와 `iconst_1`/`iconst_0`로 0·1 정수를 만들기 때문이다.

수정 제안:

> 바이트코드에는 boolean 값만 다루는 명령이 없어서, `javac`은 비교 결과를 `if_icmpne` 분기와 0·1 정수로 만듭니다. 역컴파일러는 그 흐름을 그대로 옮겨 이런 모양으로 보여 줍니다.

근거: [JVMS SE 25 2.3.4 The boolean Type](https://docs.oracle.com/javase/specs/jvms/se25/html/jvms-2.html#jvms-2.3.4), [JVMS SE 25 2.11.1 Types and the Java Virtual Machine](https://docs.oracle.com/javase/specs/jvms/se25/html/jvms-2.html#jvms-2.11.1).

사양 대조 판정. Codex 지적 3번과 같다. 로컬 `javap -c` 출력에서도 두 `println` 인자가 `if_icmpne`와 `iconst_1`/`iconst_0`로 만들어지는 것을 확인했다.

**반영 완료.**

## 표현을 보완할 사항

### 1. JDK 9 이전에는 "정적 분석기를 따로 붙여야" 찾을 수 있었다는 일반화

**위치: 본문 186행, 389행.** 186행은 "그전에는 ``javac``이 이 코드를 경고하지 않아서 FindBugs 같은 정적 분석기를 따로 붙여야 찾을 수 있었습니다"라고 쓰고, 389행은 "예전에는 FindBugs 같은 정적 분석기를 따로 붙여야 알 수 있었지만, 지금은 ``javac``과 Eclipse, IntelliJ IDEA가 모두 경고합니다"라고 정리한다.

JDK 9 이전 `javac`이 경고하지 않았다는 것은 맞다(JDK 8 `javac -Xlint:all`로 `OldStyle.java`를 컴파일하면 경고 없음). 그러나 IntelliJ IDEA의 `CachedNumberConstructorCall` 검사는 JDK 9 출시(2017) 전에 이미 있었다. 2016-08-08 커밋의 소스에 저작권 표기 "2003-2014"와 함께 들어 있다. 따라서 "따로 붙여야"는 IntelliJ 사용자에게는 맞지 않는다. 또 389행의 "`javac`이 경고합니다"는 JDK 25 기본 설정에서 `Note:` 두 줄만 출력한다는 194행·370–372행과 어긋난다.

수정 제안(186행):

> 그전에는 ``javac``이 이 코드를 경고하지 않았습니다. https://findbugs.sourceforge.net/[FindBugs] 같은 정적 분석기나 IntelliJ IDEA의 인스펙션을 써야 찾을 수 있었습니다.

수정 제안(389행):

> * wrapper 클래스의 생성자는 JDK 9부터 deprecated 상태입니다. JDK 8까지는 ``javac``이 경고하지 않아 FindBugs나 IDE 검사에 기대야 했지만, 지금은 ``javac``이 deprecation을 알리고 Eclipse와 IntelliJ IDEA도 기본 설정으로 경고합니다.

근거: [IntelliJ Community `CachedNumberConstructorCallInspection.java` (2016-08-08 커밋)](https://github.com/JetBrains/intellij-community/blob/78d1492242539eb0d5e36016f9dc1f9b55d76f94/plugins/InspectionGadgets/InspectionGadgetsAnalysis/src/com/siyeh/ig/numeric/CachedNumberConstructorCallInspection.java).

소스 대조와 실행 확인. Codex 지적 4번을 Claude가 확인했다. 당시 이 검사가 기본으로 켜져 있었는지는 확인하지 못했다.

**반영 완료.**

### 2. IntelliJ IDEA 검사의 기본 옵션

**위치: 본문 236행.** "IntelliJ IDEA에는 deprecation 경고와 별개로 ... 인스펙션이 ... 있습니다. ... 생성자에 primitive 값을 넘기는 코드를 찾아 ``valueOf()``로 바꾸도록 안내합니다."

검사 ID·그룹(Java > Numeric issues)·대상 네 타입은 Inspectopedia와 같다. 다만 현재 문서는 `reportOnlyWhenDeprecated`("Report only when constructor is @Deprecated") 옵션의 기본값을 Selected로 적는다. 따라서 기본 설정에서는 생성자가 deprecated일 때(JDK 9 이상)만 보고하므로 "deprecation 경고와 별개로"는 옵션을 바꿨을 때의 이야기다.

수정 제안:

> IntelliJ IDEA에는 'Number constructor call with primitive argument'(`CachedNumberConstructorCall`) 인스펙션이 Java > Numeric issues 그룹에 있습니다. `Long`, `Integer`, `Short`, `Byte` 생성자에 primitive 값을 넘기는 코드를 찾아 ``valueOf()``로 바꾸도록 안내합니다. 기본값으로는 'Report only when constructor is @Deprecated' 옵션이 켜져 있어서, 생성자가 deprecated된 JDK 9 이상에서만 보고합니다.

근거: [Inspectopedia: Number constructor call with primitive argument](https://www.jetbrains.com/help/inspectopedia/CachedNumberConstructorCall.html) (Inspectopedia 2026.2, Inspection options).

문서 대조 판정. Codex가 표에서 덧붙인 내용을 Claude가 확인했다.

**반영 완료.**

### 3. `-XX:AutoBoxCacheMax`와 `-D` 프로퍼티의 관계를 보완할 수 있다

**위치: 본문 165행, 182행.** 165행은 "공개 VM 옵션인 `-XX:AutoBoxCacheMax=<size>`"라고 쓰고, 182행은 `-Djava.lang.Integer.IntegerCache.high=<size>`도 같은 효과가 난다고 쓴다.

본문 설명은 맞다. `-D`로 지정해도 `System.getProperty("java.lang.Integer.IntegerCache.high")`는 `null`이고 캐시는 늘어났다(로컬 실행). 보완할 점은 세 가지다.

- JDK 25 `arguments.cpp`는 `AutoBoxCacheMax`가 기본값이 아니면 `java.lang.Integer.IntegerCache.high=<값>` 프로퍼티를 넣는다. 그래서 두 값을 함께 주면 순서와 관계없이 `-XX` 값이 이긴다(`-D...=127 -XX:AutoBoxCacheMax=1000`은 `c == d : true`, `-XX:AutoBoxCacheMax=127 -D...=1000`은 `false`로 확인).
- 값은 캐시 원소 개수가 아니라 포함되는 최댓값이다. HotSpot 플래그 설명도 "Sets max value cached by the java.lang.Integer autobox cache"이다. `<size>`는 JDK 소스 주석의 표기를 따른 것이지만, 본문 설명(상한)과 맞추려면 `<max>`가 읽기 쉽다.
- 이 플래그는 `c2_globals.hpp`에 정의되고 `arguments.cpp`에서도 `#ifdef COMPILER2` 안에서 처리되므로, C2 컴파일러가 포함된 HotSpot에서만 쓸 수 있다.

수정 제안(165행):

> ``Integer``는 HotSpot 옵션인 `-XX:AutoBoxCacheMax=<max>` 로 캐시의 상한을 늘릴 수 있습니다. 이 옵션은 C2 컴파일러가 포함된 HotSpot에서 쓸 수 있습니다. 앞의 예제를 같은 클래스 파일로 다시 실행하면 결과가 바뀝니다.

수정 제안(182행 끝에 추가):

> 두 값을 함께 지정하면 VM이 `-XX:AutoBoxCacheMax` 값으로 이 프로퍼티를 덮어쓰므로 `-XX` 옵션이 우선합니다.

근거: [JDK 25 arguments.cpp](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/runtime/arguments.cpp#L1746), [JDK 25 c2_globals.hpp](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/opto/c2_globals.hpp#L515), [JDK 25 Integer.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Integer.java#L915).

소스 대조와 실행 확인. Codex 지적 5번을 Claude가 확인했고, C2 전용이라는 점은 Claude가 추가로 찾았다.

**반영 완료.**

### 4. `Double`에 캐시가 없으므로 "더 주의해야 한다"는 비교 결론

**위치: 본문 320행.** "실수 계열을 다루는 반복문에서는 박싱된 타입을 누적 변수로 쓰지 않도록 더 주의해야 합니다."

`Long` 누적 예제도 누적값이 127을 넘은 뒤에는 매번 새 인스턴스를 반환한다(295행). 반복문의 누적 변수처럼 값이 금방 캐시 범위를 벗어나는 경우에는 `Double`과 `Long`의 차이가 작으므로, 캐시가 없다는 사실만으로 "더" 주의해야 한다는 비교는 뒷받침이 약하다. 차이는 작은 값(-128~127)을 반복해서 박싱할 때 `Long`은 캐시를 쓰고 `Double`은 쓰지 않는다는 데 있다.

수정 제안:

> 따라서 JDK 25의 ``Double.valueOf()``는 값이 작아도 캐시된 인스턴스를 반환하지 않습니다. 정수 계열이라면 캐시되었을 -128~127 범위의 값도 박싱할 때마다 새 객체가 됩니다.

근거: [JDK 25 Double.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Double.java#L950), [JDK 25 Long.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Long.java#L995).

소스 대조 판정. Codex 지적 6번 중 Double 결론 부분을 Claude가 확인했다.

**반영 완료.**

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| `Boxing`의 `javap -c` 출력(`Integer.valueOf`, `intValue`, `if_icmpne`), 56행의 전용 바이트코드 명령이 없다는 설명 | **확인.** Temurin 25+36에서 실행, 명령과 오프셋이 같다(상수 풀 주석의 공백 폭만 다름). JDK 8 `javac`도 같은 호출로 컴파일한다. | 로컬 실행 |
| 58행: `int`와 `Integer` 비교는 언박싱 후 값 비교, 둘 다 `Integer`면 참조 비교 | **확인.** | [JLS SE 25 15.21.1](https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.21.1), [15.21.3](https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.21.3) |
| CFR 0.152의 `--sugarboxing false`·기본값 출력, `SumTest.boxed()` 역컴파일 | **확인.** Maven Central의 `cfr-0.152.jar`로 실행, 세 출력 모두 본문과 같다. | 로컬 실행 |
| JDK 25 `IntegerCache`·`valueOf(int)` 발췌, CDS 아카이브가 없거나 작을 때 부족한 부분만 만드는 동작(125행) | **확인.** 발췌는 원본 932–985행, 1003–1007행과 같다. | [JDK 25 Integer.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Integer.java) |
| 127행: `Integer.valueOf(int)`가 -128~127을 항상 캐시한다는 API 계약 | **확인.** "This method will always cache values in the range -128 to 127, inclusive" | [Integer.valueOf(int) Javadoc](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Integer.html#valueOf(int)) |
| JLS SE 25 5.1.7 인용(131행), SE 8 인용(379행) | **확인.** 절 참조(§15.29 등)를 뺀 것 외에는 원문과 같다. | [SE 25](https://docs.oracle.com/javase/specs/jls/se25/html/jls-5.html#jls-5.1.7), [SE 8](https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.7) |
| `Identity` 실행 결과(기본, `-XX:AutoBoxCacheMax=1000`), 옵션이 `Long`에 영향을 주지 않는다는 180행 | **확인.** 로컬 실행. `-XX:AutoBoxCacheMax=1000`에서도 `Long` 128은 서로 다른 참조였다. | 로컬 실행 |
| 182행: `-D` 값은 `System.getProperty()`로 보이지 않는 saved property | **확인.** `-D`와 `-XX` 모두에서 `System.getProperty()` 결과가 `null`, `IntegerCache`는 `VM.getSavedProperty()`로 읽는다. | 로컬 실행, [JDK 25 Integer.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Integer.java#L944) |
| 161행: `Long`·`Short`·`Byte`는 -128~127, `Character`는 `\u0000`~`\u007f`, `Boolean`은 `TRUE`/`FALSE` 재사용 | **확인.** | JDK 25 [Long](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Long.java)·[Short](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Short.java)·[Byte](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Byte.java)·[Character](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Character.java)·[Boolean](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Boolean.java) 소스 |
| 208–220행: JDK 25 `Integer(int)` 생성자의 `@Deprecated(since="9")`와 Javadoc 문구, `forRemoval` 없음 | **확인.** | [JDK 25 Integer.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Integer.java#L1023) |
| `javac` 출력: JDK 25 `-Xlint:deprecation`(198–205행), JDK 21 removal 경고와 JDK 25 기본 `Note:`(361–372행) | **확인.** Temurin 25+36, 21.0.8에서 실행해 같은 출력을 얻었다. JDK 8은 `-Xlint:all`에서도 경고가 없다. | 로컬 실행 |
| SpotBugs `DM_NUMBER_CTOR` 인용 세 문단(227–231행)과 제목 | **확인.** 원문과 같다. | [SpotBugs Bug descriptions](https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html) |
| 236행: Eclipse 'Deprecated API'의 기본값 Warning | **확인.** | [Eclipse JDT Errors/Warnings](https://help.eclipse.org/latest/topic/org.eclipse.jdt.doc.user/reference/preferences/java/compiler/ref-preferences-errors-warnings.htm) |
| 236행: IntelliJ 검사 ID·이름·그룹·대상 타입 | **확인.** 기본 옵션 문제는 표현 보완 2번. | [Inspectopedia](https://www.jetbrains.com/help/inspectopedia/CachedNumberConstructorCall.html) |
| 240행, 390행: Effective Java 3판 Item 6 "Avoid creating unnecessary objects", Item 61 "Prefer primitive types to boxed primitives"와 예제 | **확인.** Codex 대조 결과를 따른다. 원서 예제는 `i <= Integer.MAX_VALUE`이고 본문은 변형이라고 밝혔다. | [저자의 Item 6 예제 코드](https://github.com/jbloch/effective-java-3e-source-code/blob/master/src/effectivejava/chapter2/item6/Sum.java) |
| 269–293행: `boxed()`의 바이트코드 | **확인.** 로컬 `javap -c` 출력과 명령·오프셋이 같다. | 로컬 실행 |
| 297–304행: `Long`과 `long` 누적의 약 10배 차이 | **확인(재현).** 같은 `boxed()`와 `long` 누적을 한 번씩 재는 코드로 세 번 실행해 `Long: 4618–4638 ms, long: 466–467 ms`(약 9.9배)를 얻었다. 저자의 측정 코드는 본문에 없어 같은 코드로 재현한 것은 아니다. | 로컬 실행 |
| 310–318행: JDK 25 `Double.valueOf()`가 `new Double(d)`를 호출 | **확인.** `Float.valueOf()`도 같다. | [JDK 25 Double.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Double.java#L950) |
| 표 JDK 5 행: `valueOf(int)` 추가 | **확인.** `@since 1.5`. | [JDK 25 Integer.java](https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/java.base/share/classes/java/lang/Integer.java#L1000) |
| 표 JDK 12 행: JDK-8209120, JDK-8213033 | **확인.** 둘 다 Fix Version 12. | [JDK-8209120](https://bugs.openjdk.org/browse/JDK-8209120), [JDK-8213033](https://bugs.openjdk.org/browse/JDK-8213033) |
| 표 JDK 16 행: JEP 390, value-based 지정과 생성자 `forRemoval` | **확인.** JEP 390 Release 16, 요약에 "deprecate their constructors for removal". | [JEP 390](https://openjdk.org/jeps/390) |
| 표 JDK 22 행: JDK-8295555 `@Stable` | **확인.** Fix Version 22, 배열 내용을 상수로 취급할 수 있게 한다는 설명. | [JDK-8295555](https://bugs.openjdk.org/browse/JDK-8295555) |
| 표 JDK 25 행: JDK-8354335 `forRemoval` 해제 | **확인.** Fix Version 25. | [JDK-8354335](https://bugs.openjdk.org/browse/JDK-8354335) |
| 355행: 11.0.6(JDK-8209120), 21.0.2(JDK-8295555) backport | **확인.** backport 레코드 JDK-8230512(11.0.6), JDK-8319215(21.0.2). | JBS REST API |

## 한계

- HotSpot의 `AutoBoxCacheMax` 플래그가 처음 들어간 JDK 버전은 확인하지 못했다. JDK-6807702 커밋 직전의 통합 저장소 소스에 이미 있다는 것까지만 확인했다.
- JDK-6807702의 Fix Version 6u14는 JBS 기록으로만 확인했다. 6u14 배포본의 `Integer` 소스는 대조하지 않았다.
- JDK 5와 JDK 7 `javac`은 로컬에 없어 실행하지 않았다. "JDK 5 이후 변환 방식이 같다"(14, 324행)는 JDK 8·21·25 실행으로만 확인했다.
- JDK 9 이전 IntelliJ IDEA에서 `CachedNumberConstructorCall` 검사가 기본으로 켜져 있었는지는 확인하지 못했다.
- 186행의 FindBugs Eclipse 플러그인 경고 메시지는 2009년 원문 캡처에서 옮긴 것이라 다시 재현하지 않았다.
- 반복문 측정은 이 검증에서 만든 코드로 재현했고, 저자의 원래 측정 코드와 실행 환경(CPU, OS)은 본문에 없어 대조하지 않았다.
