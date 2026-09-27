# IEEE 754 부동소수점 오차와 Java의 대안 글 사실 관계 검증 — 2026-09-27

- 대상: [floating-point-java.adoc](../src/content/floating-point-java.adoc)
- 검토 기준 커밋: `bf74e6704ea4faada03b283eefe1ef0fa594a64d`
- 이전 기록: 없음
- 범위: 글 전체. jshell 예제와 출력, binary64 비트 구조 설명, NEIS와 패트리어트 사례의 수치와 인용, `BigDecimal`·`float` 정밀도 설명, Lawrey 벤치마크, Stripe 인용, JOL 측정값, Java Money·Joda-Money·JSR 385·`Duration` 설명.
- 방법: Codex 작업(`task`, 글 전체 대상)으로 의심 지점을 모은 뒤, Claude가 1차 출처로 다시 확인했다. 코드 예제와 계산 수치는 Temurin JDK 25 jshell로 모두 다시 실행했다. Moneta 1.4.5와 JOL 0.17은 Gradle 임시 프로젝트로 실행했다. 기사와 문서는 curl로 받아 본문 문장을 검색해 대조했고, GAO 보고서는 Wayback Machine의 PDF를 `pdftotext`로 읽었다.
- 결과: **우선 수정할 사항 3건, 표현을 보완할 사항 11건.** jshell 출력과 인용 수치는 본문과 같고, 2진 표현의 반올림 오차가 비교와 누적 계산에서 장애로 이어진다는 핵심 결론은 타당하다. 고칠 곳은 오차가 생기는 금액 범위, 스커드 속도에서 687m를 끌어낸 계산, 도메인 라이브러리의 내부 표현을 일반화한 문장이다.
- 반영 상태: **우선 수정 3건과 표현 보완 11건을 모두 본문에 반영함(반영한 수정은 이 기록과 같은 커밋에 들어 있다).** 제안 문장과 다르게 반영한 곳은 항목마다 적었다.
- 아래 행 번호는 위 커밋 기준이다.

## 우선 수정할 사항

### 1. `calculate()`의 오차는 금액이 2^24를 넘기 전에도 생긴다

**위치: 본문 287행.** "금액 파라미터와 리턴 타입이 정수여도 금액이 16,777,216원(2^24^)을 넘으면 오차가 생길 수 있습니다"라고 서술한다.

2^24는 `long` 금액을 `float`로 바꿀 때 값이 달라지기 시작하는 경계다. 그런데 `amount * rate`도 `float` 곱셈이어서, 금액이 2^24보다 작아도 곱한 결과가 24비트 정밀도를 넘으면 값이 어긋난다. 뒤에서 `* 0.01`을 하며 `double`로 바뀌어도 잃은 정밀도는 돌아오지 않는다.

| 호출 | `calculate()` 결과 | 정확한 값 |
|---|---|---|
| `calculate(671_089L, 50f, HALF_UP)` | 335544 | 335545 |
| `calculate(1_000_001L, 50f, HALF_UP)` | 500000 | 500001 |
| `calculate(10_737_419L, 100f, HALF_UP)` | 10737420 | 10737419 |

비율 50%에서는 671,089원이, 100%에서는 10,737,419원이 1원부터 차례로 넣었을 때 처음으로 틀리는 금액이다. 291~298행의 `16_777_217L` 예제와 24비트 정밀도 설명은 맞다.

수정 제안:

> `long` 값이 ``float``로 변환되고 곱셈도 ``float``로 계산되면서 정밀도를 잃기 때문에, 금액 파라미터와 리턴 타입이 정수여도 오차가 생길 수 있습니다.
> 금액이 16,777,216(2^24^)을 넘으면 ``float``로 바꾸는 순간 값이 달라집니다.
> 금액이 그보다 작아도 비율을 곱한 값이 2^24^을 넘으면 어긋나서, 비율이 50%일 때는 671,089원에서 이미 결과가 1원 틀립니다.

근거: [JLS 5.6 Numeric Contexts](https://docs.oracle.com/javase/specs/jls/se25/html/jls-5.html#jls-5.6), [Spock으로 테스트코드를 짜보자](https://techblog.woowahan.com/2560/)의 원래 `calculate()` 코드.

실행 확인. Codex도 같은 지적을 했고 `10_737_419L` 반례를 제시했다. 50% 반례는 Claude가 1원부터 차례로 넣어 찾았다.

**반영 완료.** 제안 문장의 첫 문장만 287행에 넣고, 2^24보다 작은 금액의 오차는 문장 대신 jshell 예제 `calculate(671_089L, 50f, RoundingMode.HALF_UP)`(결과 335544)와 그 풀이로 보여 주었다. 곱한 값 33,554,450이 `float`에 33,554,448로 저장되는 것은 `new BigDecimal(671_089L * 50f)`로 확인했다.

### 2. 스커드 속도 초속 2km에서 687m를 계산한 문장은 GAO 보고서와 맞지 않는다

**위치: 본문 206행.** "초속 2km로 날아오는 스커드 미사일이라면 0.3433초 동안 687m를 이동합니다"라고 서술한다.

GAO 보고서는 스커드의 속도를 "approximately MACH 5 (3750 mph)"로 적었다. 초속으로는 약 1,676m이고, 이 속도로 0.3433초 동안 가는 거리는 약 575m다. 687m는 보고서 부록 II의 표에 있는 값으로, 100시간 가동했을 때 추적 구간(range gate)이 어긋나는 거리다. 보고서는 이 값을 속도에 시간 오차를 곱해 구했다고 설명하지 않는다. 초속 2km는 687m를 0.3433초로 나눠 거꾸로 얻은 값으로 보이며, 보고서에는 없는 수치다.

수정 제안:

> GAO 보고서의 계산으로는 이 시간 오차 때문에 목표물을 찾는 구간이 687m 어긋납니다.
> 추적 레이더는 실제 미사일 위치에서 그만큼 벗어난 구간을 탐색했습니다.

206행과 207행을 위 두 문장으로 바꾼다.

근거: [GAO/IMTEC-92-26](https://www.gao.gov/products/imtec-92-26) 본문과 부록 II, [The Patriot Missile Failure](https://www-users.cse.umn.edu/~arnold/disasters/patriot.html).

소스 대조 판정. 책 '역사 속의 소프트웨어 오류'가 초속 2km라고 적었는지는 확인하지 못했다.

**반영 완료.** 제안 문장대로 고쳤다.

### 3. 도메인 특화 라이브러리가 모두 `BigDecimal`이나 정수로 수를 담는 것은 아니다

**위치: 본문 425행.** "이런 라이브러리도 안에서는 결국 ``BigDecimal``이나 정수형 최소 단위 중 하나로 수를 담습니다"라고 서술한다.

Java Money(`Money`, `FastMoney`), Joda-Money, `Duration`에는 맞는 말이다. 같은 절에서 소개하는 JSR 385는 다르다. `Quantity.getValue()`의 리턴 타입은 `Number`이고, Indriya의 `NumberQuantity`도 값을 `Number` 필드에 담는다. `double` 값으로도 물리량을 만들 수 있으므로, 단위가 맞지 않는 계산을 막는 것과 십진수를 정확하게 계산하는 것은 별개다.

수정 제안:

> 금액을 다루는 라이브러리는 안에서 ``BigDecimal``이나 정수형 최소 단위 중 하나로 수를 담습니다.

480행 앞에는 다음 문장을 덧붙인다.

> 단위 오류를 막는 것과 수치를 정확하게 계산하는 것은 별개입니다.
> Units of Measurement API는 값을 `Number`로 받으므로 ``double``로 만든 물리량에는 부동소수점 오차가 그대로 남습니다.

근거: [Quantity.java](https://github.com/unitsofmeasurement/unit-api/blob/master/src/main/java/javax/measure/Quantity.java), [NumberQuantity.java](https://github.com/unitsofmeasurement/indriya/blob/master/src/main/java/tech/units/indriya/quantity/NumberQuantity.java).

소스 대조 판정. Codex 지적을 소스에서 확인했다.

**반영 완료.** 425행은 제안 문장대로 고쳤다. 덧붙이는 두 문장은 480행 앞이 아니라 JSR 385를 소개하는 476행 항목 끝에 넣었다. 그 자리가 `Number`를 설명하기에 더 가깝다.

## 표현을 보완할 사항

### 1. DB2의 DOUBLE이 binary64라는 서술은 플랫폼에 따라 다르다

**위치: 본문 189행.** "DB2의 DOUBLE 타입은 IEEE 754 binary64를 따르므로"라고 서술한다.

Db2 for Linux, UNIX, Windows의 문서는 배정밀도 부동소수점을 64비트 근삿값으로 설명하고, 범위를 2.2250738585072014e-308 ~ 1.7976931348623158e+308로 적는다. binary64의 정규 수 범위와 같다. 다만 이 문서는 해당 항목에서 IEEE 754를 언급하지 않는다. Db2 for z/OS의 부동소수점은 IBM 16진 부동소수점이고 범위가 약 5.4E-79 ~ 7.2E+75다. 나이스가 어느 플랫폼의 DB2를 썼는지는 인용한 기사에 없다.

수정 제안:

> Linux, UNIX, Windows용 Db2의 DOUBLE 타입은 64비트이고 값의 범위가 IEEE 754 binary64와 같아서, 자바의 ``double``과 같은 방식으로 오차가 생깁니다.

근거: [Db2 11.5 Numbers](https://www.ibm.com/docs/en/db2/11.5?topic=list-numbers), [IBM hexadecimal floating-point](https://en.wikipedia.org/wiki/IBM_hexadecimal_floating-point).

소스 대조 판정. z/OS 쪽은 IBM 문서가 접근을 막아 검색 결과와 위키백과로만 확인했다.

**반영 완료.** 제안 문장대로 고치고 Db2 11.5 문서 링크를 달았다.

### 2. "시계가 뒤처진다"는 표현

**위치: 본문 205행.** 패트리어트의 내부 시계는 0.1초 단위의 횟수를 정수로 셌고, 이 값은 정확했다. 오차는 횟수를 초로 바꾸는 곱셈에서 생겼다. "시계가 뒤처집니다"는 시계 자체가 느려진 것으로 읽힌다. GAO 표에서 100시간의 행에는 포대가 약 100시간 연속 가동했다는 주석이 있고, 본문은 "over 100 hours"라고 적는다.

수정 제안:

> 연속 가동 20시간이면 계산한 시각이 실제보다 0.0687초, 사건 당시 포대처럼 약 100시간이면 0.3433초 작아집니다.

근거: [GAO/IMTEC-92-26](https://www.gao.gov/products/imtec-92-26) 부록 II.

**반영 완료.** 제안 문장대로 고쳤다.

### 3. 부상자 수 97명

**위치: 본문 199행.** GAO 보고서에는 사망 28명만 있고 부상자 수는 없다. 출처마다 수가 다르다.

| 출처 | 부상자 수 |
|---|---|
| 미 육군 DVIDS, 위키백과 14th Quartermaster Detachment | 99명 |
| Douglas Arnold의 글 | 약 100명 |
| 위키백과 Al-Husayn (missile) | 110명 |

Codex는 미 육군 병참박물관 페이지가 97명으로 적었다고 보고했으나, 그 페이지(`qmmuseum.army.mil`)는 DNS 조회가 되지 않아 Claude가 확인하지 못했다.

수정 제안:

> 미군 28명이 사망하고 100명 가까이 다쳤습니다.

**반영 완료.** 글쓴이의 결정에 따라 제안 문장 대신 미 육군 자료의 수치인 99명으로 고치고, 문장 끝과 참고 자료에 [DVIDS 기사](https://www.dvidshub.net/news/525747/scud-alert-after-blast) 링크를 넣었다. 기사 원문은 "killing 28 U.S. reserve component soldiers and wounding 99"다.

### 4. `BigDecimal.valueOf()`는 이미 생긴 계산 오차를 없애지 못한다

**위치: 본문 234행.** 문자열 생성자와 `valueOf()`를 나란히 권한다. `BigDecimal.valueOf(0.1)`은 0.1이지만 `BigDecimal.valueOf(0.1 + 0.2)`는 0.30000000000000004다. `valueOf()`는 `Double.toString()`의 결과를 쓰는 변환이어서, `double`로 계산하다 생긴 오차는 그대로 옮긴다.

수정 제안(235행 뒤에 추가):

> 다만 ``valueOf()``는 ``double``로 이미 계산한 값의 오차까지 없애지는 못합니다.
> ``BigDecimal.valueOf(0.1 + 0.2)``는 0.30000000000000004가 됩니다.

근거: [BigDecimal.valueOf(double)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#valueOf(double)). 실행 확인.

**반영 완료.** 제안 문장대로 추가했다.

### 5. 고친 `calculate()`의 `longValue()`는 범위를 넘는 값을 경고 없이 자른다

**위치: 본문 310행, 321행.** 고친 버전에 `calculate(Long.MAX_VALUE, new BigDecimal("200"), RoundingMode.HALF_UP)`을 넣으면 -2가 나온다. `longValue()`는 값이 `long` 범위를 넘으면 하위 64비트만 돌려준다. 321행의 "모든 중간값을 정수나 ``BigDecimal``로 유지해야 합니다"는 오차를 없애는 조건의 하나이고, 오버플로는 따로 막아야 한다.

수정 제안: 310행의 `.longValue()`를 `.longValueExact()`로 바꾼다. 범위를 넘으면 `ArithmeticException`이 난다.

근거: [BigDecimal.longValueExact()](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#longValueExact()). 실행 확인. 실제 금액에서는 일어나기 어려운 경우여서 우선 수정으로 올리지 않았다.

**반영 완료.** `longValueExact()`로 바꾸고, `longValue()`와의 차이를 설명하는 두 문장을 코드 아래에 넣었다. 바꾼 메서드에 같은 인자를 넣으면 `ArithmeticException: Overflow`가 나는 것을 실행으로 확인했다.

### 6. IEEE 754 전체와 binary64 정규 수의 설명을 구분

**위치: 본문 93행, 119행, 138행.**

- 93행 "IEEE 754는 실수를 2진수 과학적 표기법으로 바꾼 다음"은 91행에서 소개한 10진 형식에는 맞지 않는다. "IEEE 754의 2진 형식은"으로 한정한다.
- 119행 "52비트 뒤에 이어지는 비트가 1로 시작하므로 올림이 일어나"는 0.1에서는 맞다. 일반 규칙은 가장 가까운 값으로 보내고, 정확히 중간이면 마지막 비트가 0인 쪽을 고르는 것이다. 0.1은 잘리는 부분이 중간값보다 커서 올림이 된다.
- 138행 "분모가 2의 거듭제곱인 소수는 … 정확하게 저장됩니다"에는 유효 비트가 53개를 넘지 않는다는 조건이 필요하다.

수정 제안(138행):

> 0.5(1/2), 0.25(1/4), 0.75(3/4)처럼 분모가 2의 거듭제곱인 소수는 유한한 2진 소수가 되므로, 유효 비트가 53개를 넘지 않으면 정확하게 저장됩니다.

근거: [JLS 4.2.3 Floating-Point Types and Values](https://docs.oracle.com/javase/specs/jls/se25/html/jls-4.html#jls-4.2.3).

**반영 완료.** 93행은 "IEEE 754의 2진 형식은"으로 한정했다. 119행은 "52비트 뒤에 이어지는 비트는 1001…이어서, 버려지는 부분이 마지막 비트 크기의 절반보다 큽니다. 그래서 올림이 일어나"로 고쳤다. 138행은 제안 문장대로 고쳤다.

### 7. 비교 표의 세 항목

**위치: 본문 389행, 390행, 392행.**

- 389행 "자릿수 제한 없이": `BigDecimal`의 스케일은 32비트 정수여서 한도가 있다. "자릿수를 필요한 만큼 늘려"가 정확하다.
- 390행 "최소 단위를 따로 정하지 않아도 됨": 정수로 환산할 자릿수를 정하지 않아도 된다는 뜻이다. 통화가 무엇인지, 몇 자리에서 반올림할지는 `BigDecimal`에서도 정해야 한다.
- 392행 "객체 하나에 40바이트 이상": 369행의 측정 조건(JDK 25 HotSpot 64비트 기본 설정)을 표에는 적지 않아 JVM 설정과 무관한 값처럼 읽힌다.

수정 제안:

> * 자릿수를 필요한 만큼 늘려 십진수를 정확하게 표현
> * 정수로 환산할 자릿수를 따로 정하지 않아도 됨
> * 메모리 사용량 증가(앞의 측정에서는 객체 하나에 40바이트 이상)

근거: [BigDecimal.java](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/java.base/share/classes/java/math/BigDecimal.java).

**반영 완료.** 세 항목을 제안 문장대로 고쳤다.

### 8. 벤치마크 표의 "오차" 칸 이름

**위치: 본문 347행.** 원문의 칸 이름은 "Score error"로, 처리량 측정값의 통계적 오차다. 이 글은 "오차"를 계산 결과의 오차라는 뜻으로 쓰고 있어 혼동할 수 있다. "측정 오차"로 바꾼다. 또 이 벤치마크는 `double`과 `BigDecimal`을 비교한 것이어서 `long`이 빠르다는 직접 근거는 아니다.

**반영 완료.** 칸 이름을 "측정 오차"로 바꾸고, 표 아래에 "``double``과 비교한 결과이고, ``long``과 직접 비교한 것은 아닙니다."를 넣었다.

### 9. JSR 385의 컴파일 시점 검사 범위

**위치: 본문 476행.** "단위가 맞지 않는 계산을 걸러내는데, 이쪽은 컴파일 시점에 막습니다"라고 서술한다. 컴파일러가 막는 것은 길이와 질량처럼 물리량의 종류가 다른 경우다. 미터와 킬로미터처럼 같은 물리량의 다른 단위는 더할 수 있다. `Unit.asType()`처럼 실행 시점에 검사하는 경로도 있다.

수정 제안:

> 금액에서 통화가 다르면 예외가 나는 것처럼 물리량의 종류가 맞지 않는 계산을 걸러내는데, 제네릭 타입이 정해져 있으면 컴파일 시점에 막습니다.

근거: [Quantity.java](https://github.com/unitsofmeasurement/unit-api/blob/master/src/main/java/javax/measure/Quantity.java)의 `Quantity<Q> add(Quantity<Q> addend)`.

**반영 완료.** 제안 문장대로 고쳤다.

### 10. 90.00000000000001이 설명용 값임을 표시

**위치: 본문 160행.** 영남일보 기사가 든 예는 총점이 90.05점과 90.04점으로 계산된 경우다. 90.00000000000001은 기사에 없는 값이므로 "예를 들어"를 붙여 가정한 값임을 밝힌다. 162행의 울산광역시교육청 안내문은 동점자 판별과 석차 분류의 착오를 뒷받침하고, 16자리 표시는 154행에서 인용한 영남일보 기사가 출처다.

**반영 완료.** 160행을 "예를 들어 두 학생의 총점이 90과 90.00000000000001로 계산되면 두 값은 같지 않으므로"로 시작하게 고쳤다.

### 11. DZone 글의 제목

**위치: 본문 220행, 487행.** 검색 결과에는 이 글의 제목이 "Why You Should Never Use Float and Double for Monetary Calculations"로 나온다. 본문의 링크 문구에는 "Why You Should"가 없다. 페이지가 접근을 막아 현재 제목을 직접 보지 못했으므로, 브라우저로 확인한 뒤 맞춘다.

**반영 완료.** Wayback Machine의 2020년 11월과 2024년 11월 사본에서 `<title>`과 `og:title`이 "Why You Should Never Use Float and Double for Monetary Calculations"임을 확인하고 두 곳의 링크 문구를 고쳤다. 2024년 사본의 본문에는 "If precision is one of your requirements, use BigDecimal instead."가 있어 220행의 서술과 맞다.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| 27~60행 jshell 출력(0.30000000000000004, false, 0.6100000000000001, 0.9999999999999999, `new BigDecimal(0.1)`) | **확인.** JDK 25 jshell에서 실행. | 실행 확인 |
| 60행 0.1과의 차이가 약 5.55 × 10^-18 | **확인.** `new BigDecimal(0.1).subtract(new BigDecimal("0.1"))`이 5.5511…E-18. | 실행 확인 |
| 22행 jshell은 JDK 9부터 포함 | **확인.** JEP 222의 Release가 9. | [JEP 222](https://openjdk.org/jeps/222) |
| 63행 JavaScript의 Number는 binary64 | **확인.** 6.1.6.1절이 "IEEE 754-2019 binary64 values"라고 정의. | [ECMAScript 6.1.6.1](https://tc39.es/ecma262/multipage/ecmascript-data-types-and-values.html) |
| 78~91행 binary32·binary64의 비트 구성, 1985년 명칭 single·double, 2008년 개명, 현행 2019판, 그 밖의 형식 | **확인.** 표준 원문은 유료여서 위키백과로 대조. | [IEEE 754](https://en.wikipedia.org/wiki/IEEE_754), [Double-precision floating-point format](https://en.wikipedia.org/wiki/Double-precision_floating-point_format) |
| 113~135행 0.1 = 1.6 × 2^-4, 지수부 1019, 가수 끝 1010, `3fb999999999999a` | **확인.** `0x3fb`가 1019. | 실행 확인 |
| 150행·183행 2011년 7월, 전국 823개 고교 2만 9,007명, 350개교 2,416명 | **확인.** | [제주일보](https://www.jejunews.com/news/articleView.html?idxno=957280) |
| 154~155행 45가지 방법, 지필고사 65점·수행평가 25점 예 | **확인.** 기사는 "45개 방법"으로 적음. | [영남일보](https://m.yeongnam.com/view.php?key=20110726.010061333590001) |
| 158행 소수점 이하 16자리 표시, 불규칙하게 나타나는 '1' | **확인.** "소수점 이하 16개자리까지 표시하도록 프로그램이 짜여 있는데". | [영남일보](https://m.yeongnam.com/view.php?key=20110726.010061333590001) |
| 161~162행 동점자 판별과 석차 분류의 착오 | **확인.** | [울산광역시교육청](https://use.go.kr/news/user/bbs/BD_selectBbs.do?q_bbsSn=1005&q_bbsDocNo=13244) |
| 163행·184행 계산 오차를 보정하지 않음, 오류 정정 프로그램, 성적표 재발송 | **확인.** | [경향신문](https://www.khan.co.kr/article/201107222144165) |
| 187행 특별점검단 인용문 | **확인.** 글자 단위로 일치. | [전자신문](https://www.etnews.com/201109030016) |
| 190행 '오류 보정 코드'를 일부 프로그램에 적용하지 않음 | **확인.** | [연합뉴스](https://www.yna.co.kr/view/AKR20110902079600004) |
| 192행 이경문 씨 인용과 두 가지 대안 | **확인.** 원문은 "BigFloat와 같이 무한대의 정밀도를 지원하는 클래스". | [데일리시큐](https://www.dailysecu.com/news/articleView.html?idxno=319) |
| 199~200행 1991년 2월 25일, 다란, 28명 사망, 추적과 요격 실패 | **확인.** | [GAO/IMTEC-92-26](https://www.gao.gov/products/imtec-92-26) |
| 203~204행 0.1초 단위 계수, 24비트 레지스터, 0.000000095 | **확인.** 0.1을 소수점 아래 23비트에서 자르면 오차가 9.5367e-08. | [The Patriot Missile Failure](https://www-users.cse.umn.edu/~arnold/disasters/patriot.html), 계산 확인 |
| 205~207행 20시간 0.0687초, 100시간 0.3433초, 687m | **확인.** 부록 II의 표와 일치. | [GAO/IMTEC-92-26](https://www.gao.gov/products/imtec-92-26) |
| 202행 책 1장 제목 | **확인.** 출판사 목차와 일치. | [에이콘출판](https://acornpub.co.kr/product/%EC%97%AD%EC%82%AC-%EC%86%8D%EC%9D%98-%EC%86%8C%ED%94%84%ED%8A%B8%EC%9B%A8%EC%96%B4-%EC%98%A4%EB%A5%98/4533/) |
| 225~247행 `BigDecimal` 연산, 나눗셈 예외 메시지 | **확인.** | 실행 확인 |
| 235행 `valueOf()`는 `Double.toString()`의 문자열을 사용 | **확인.** | [BigDecimal](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#valueOf(double)) |
| 250행 2.5와 3.5의 `HALF_UP`·`HALF_EVEN`·`DOWN` 결과 | **확인.** | 실행 확인 |
| 261~267행 Stripe `amount`의 타입과 인용문 | **확인.** 글자 단위로 일치. | [Stripe API Reference](https://docs.stripe.com/api/charges/object) |
| 277~281행 우아한형제들 글의 `calculate()` 코드 | **확인.** 공백만 다름. | [Spock으로 테스트코드를 짜보자](https://techblog.woowahan.com/2560/) |
| 291~298행 `calculate(16_777_217L, 100f, …)`가 16777216 | **확인.** | 실행 확인 |
| 314~319행 고친 버전이 16777217 | **확인.** | 실행 확인 |
| 342~358행 Lawrey 벤치마크 수치, 1,024개 원소, 약 5.2배 | **확인.** 123208.083 ÷ 23638.568 = 5.212. | [Lawrey의 글](https://blog.vanillajava.blog/2014/07/if-bigdecimal-is-answer-it-must-have.html) |
| 361~364행 Lawrey의 결론과 댓글 | **확인.** Codex는 댓글을 확인하지 못했으나 Claude가 원문 페이지에서 "trading and financial systems predate the use of BigDecimal. Many systems use C++ without decimal."을 찾음. | [Lawrey의 글](https://blog.vanillajava.blog/2014/07/if-bigdecimal-is-answer-it-must-have.html) |
| 369~371행 `BigDecimal` 40바이트, 112바이트 | **확인.** JOL 0.17, Temurin 25, 압축 참조, 8바이트 정렬. | 실행 확인 |
| 375행 `10 / 3`은 3 | **확인.** | 실행 확인 |
| 430~433행 JSR 354, Java SE 9 포함 검토 | **확인.** "it is intended that this JSR can be considered for inclusion in Java SE 9". | [JSR 354](https://jcp.org/en/jsr/detail?id=354) |
| 438~455행 Moneta 1.4.5, `USD 0.61`, `MonetaryException: Currency mismatch: USD/KRW` | **확인.** | 실행 확인 |
| 462행 `FastMoney`의 스케일 5, 약 92조, 10~15배 | **확인.** `SCALE = 5`, "10-15 times faster compared to Money". | [FastMoney.java](https://github.com/JavaMoney/jsr354-ri/blob/master/moneta-core/src/main/java/org/javamoney/moneta/FastMoney.java) |
| 475행 Joda-Money가 Java Money보다 먼저, `Money`와 `BigMoney` | **확인.** Joda-Money 0.5는 2009년 11월, JSR 354 최종판은 2015년 5월. | [Joda-Money](https://www.joda.org/joda-money/), [Money.java](https://github.com/JodaOrg/joda-money/blob/main/src/main/java/org/joda/money/Money.java) |
| 476행 JSR 385의 패키지와 참조 구현 | **확인.** | [Units of Measurement](https://unitsofmeasurement.github.io/), [Indriya](https://unitsofmeasurement.github.io/indriya/) |
| 477행 `Duration`의 초(`long`)와 나노초(`int`) | **확인.** | [Duration](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/Duration.html) |

## 한계

- 반영하면서 사실 검증 범위 밖의 렌더링 오류 하나를 함께 고쳤다. 274행의 `` `amount`와 ``는 백틱 바로 뒤에 한글 조사가 붙어 백틱이 그대로 보였고, 이중 백틱으로 바꿨다.

- '역사 속의 소프트웨어 오류'는 본문을 볼 수 없어 25~35쪽이라는 쪽 번호와 책의 서술 내용을 확인하지 못했다. 이 저장소의 인용 검증 원칙에 따르면 쪽 번호는 빼고 1장 제목만 남기는 편이 맞다.
- 'Effective Java' 3판 아이템 60은 책 원문을 보지 못했다. 항목 제목과 자릿수 기준 문장("nine decimal digits", "eighteen digits")은 2차 출처로만 확인했다.
- DZone, Baeldung, GeeksforGeeks 글은 페이지가 접근을 막거나 본문을 대조하지 않았다. 링크가 살아 있는지만 부분적으로 확인했다.
- IEEE 754 표준 원문은 유료여서 형식 이름과 개정 이력을 위키백과로 대조했다.
- 65행의 Chrome 콘솔 캡처 이미지와 110행의 비트 구조 그림은 이미지 안의 값을 대조하지 않았다.
- 나이스가 쓴 DB2의 플랫폼과 실제 성적 계산식은 공개된 자료에서 찾지 못했다.
