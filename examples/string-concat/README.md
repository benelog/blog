# string-concat

Benchmark code for [How Java Optimizes String Concatenation](https://tech.benelog.net/java-string-concat.html) ([Korean version](https://blog.benelog.net/java-string-concat.html)).

It measures the performance difference between `+` concatenation and using `StringBuilder` directly when a string is accumulated in a loop.

How to run (JDK 11 or later):

```bash
java StringConcatBench.java
```

Sample results on JDK 25 (Temurin 25+36):

| Iterations | `+` concatenation | `StringBuilder` used directly | Ratio |
|---|---|---|---|
| 100 | 0.008ms | 0.001ms | about 10x |
| 1,000 | 0.53ms | 0.009ms | about 56x |
| 10,000 | 10.6ms | 0.023ms | about 471x |
| 100,000 | 1,485ms | 0.22ms | about 6,667x |
