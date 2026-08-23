# string-concat

[Java 문자열 더하기 연산의 최적화](https://blog.benelog.net/java-string-concat.html) 글에서 쓴 측정 코드입니다.

반복문 안에서 문자열을 누적할 때 `+` 결합과 `StringBuilder` 직접 사용의 성능 차이를 잽니다.

실행 방법 (JDK 11 이상):

```bash
java StringConcatBench.java
```

JDK 25 (Temurin 25+36) 에서 측정한 결과 예:

| 반복 횟수 | `+` 결합 | `StringBuilder` 직접 사용 | 배율 |
|---|---|---|---|
| 100 | 0.008ms | 0.001ms | 약 10배 |
| 1,000 | 0.53ms | 0.009ms | 약 56배 |
| 10,000 | 10.6ms | 0.023ms | 약 471배 |
| 100,000 | 1,485ms | 0.22ms | 약 6,667배 |
