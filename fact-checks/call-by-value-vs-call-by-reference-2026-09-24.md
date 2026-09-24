# Call by value vs Call by reference 글 사실 관계 검증 — 2026-09-24

- 대상: [call-by-value-vs-call-by-reference.adoc](../src/content/call-by-value-vs-call-by-reference.adoc)
- 검토 기준 커밋: `551d9c8b43cd4989c23e5540067eba3a5a3b16f6` (미커밋 변경 포함)
- 이전 기록: [2026-09-20 검증](call-by-value-vs-call-by-reference-2026-09-20.md)과 그 안에서 링크한 2026-09-19 1~4차 기록.
- 범위: 2026-09-24에 바꾼 부분. 기준 커밋이 `a2d8cd5`에서 바꾼 내용과 작업 트리의 미커밋 변경이다. Dragon Book 2판 1.6.6절과 Strachey 3.4.2절의 인용과 번역, 두 문헌의 소개 문장과 배치 순서, Java 절에 추가한 Dragon Book의 Java 서술 인용과 해설, 판별 기준의 `targetTv` 대입 예시, 책 표지 두 개의 캡션, 인용 뒤에 이어지는 'reference' 용어 문단을 봤다.
- 방법: Codex 적대적 리뷰(`adversarial-review --base a2d8cd5`)를 돌린 뒤, Claude가 Strachey 논문 PDF와 Dragon Book 2판 PDF를 pdftotext로 풀어 인용한 영어 원문 다섯 단락을 공백을 무시하고 글자 단위로 대조했다. 표지의 판차는 Open Library의 ISBN API로 확인했다. 실행으로 재현할 항목은 없다. 모두 소스 대조 판정이다.
- 결과: **우선 수정할 사항 1건, 표현을 보완할 사항 2건.** 인용문과 번역, 표지 판차는 모두 원문과 맞는다. 고칠 곳은 Strachey를 call by value/reference 구분의 기원처럼 소개한 문장과, 오해의 원인을 두고 앞 절과 어긋나게 읽히는 문단이다. Codex는 "No material findings"로 승인했고 아래 세 건은 Claude가 재검증 중에 찾았다.
- 반영 상태: **우선 수정 1건과 표현 보완 2건을 모두 제안 문장대로 본문에 반영함(아직 커밋 전).** 커밋하면 이 줄에 반영 커밋 해시를 적는다. 이 문서의 검증 본문은 작성 당시의 수정 제안을 그대로 둔다.
- 아래 행 번호는 위 기준 커밋에 미커밋 변경을 더한 작업 트리 기준이다.

## 우선 수정할 사항

### 1. Strachey의 1967년 강의를 두 방식 구분의 기원처럼 서술함

**위치: 본문 44행.** "이 구분은 Christopher Strachey의 1967년 강의로 거슬러 올라갑니다."라고 쓴다.

call by value와 call by reference의 구분은 1967년보다 앞선다. 본문 61행이 스스로 밝히듯 `call by value`라는 말은 1960년 ALGOL 60 보고서에 이미 나온다. 인용한 3.4.2절의 바로 다음 문단에서 Strachey도 "FORTRAN calls all its parameters by reference", "The ALGOL call by value corresponds to call by R-value as above"라고 써서 기존 언어의 방식을 L-value/R-value로 다시 설명하고 있다. Strachey가 한 일은 이 구분을 L-value와 R-value라는 개념으로 정리한 것이지 구분 자체를 처음 만든 것이 아니다. 지금 문장은 61행과 어긋난다.

수정 제안:

> Christopher Strachey는 1967년 강의를 정리한 https://www.cs.cmu.edu/~crary/819-f09/Strachey67.pdf[Fundamental Concepts in Programming Languages] 3.4.2절에서 이 구분을 L-value와 R-value로 설명했습니다. 파라미터에 표현식의 R-value를 넘기는 방식을 call by value, L-value를 넘기는 방식을 call by reference라고 불렀습니다.

**반영 완료.** 44행을 제안 문장으로 바꿨다.

근거: [Strachey, Fundamental Concepts in Programming Languages 3.4.2절 둘째 문단](https://www.cs.cmu.edu/~crary/819-f09/Strachey67.pdf), [Revised Report on the Algorithmic Language Algol 60 4.7.3절](https://www.masswerk.at/algol60/report.htm).

소스 대조 판정.

## 표현을 보완할 사항

### 1. 오해의 원인을 'reference' 용어로만 돌려 앞 절과 어긋나게 읽힘

**위치: 본문 215행.** "Java가 객체를 call by reference로 전달한다는 오해는 'reference'라는 용어에서 비롯된 것으로 보입니다."라고 쓴다.

같은 오해를 두고 144행의 절 제목은 "오해의 근원인 객체 상태 변경"이라 하고, 171행은 호출자 변수로 바뀐 상태가 보이니 "객체는 call by reference로 전달된다"는 설명이 그럴듯하게 들린다고 설명한다. 57행도 "뒤에서 볼 오해는 이 둘을 구분하지 못하는 것에서 생깁니다"라고 예고한다. 이번에 주어를 "이런 혼란은"에서 "Java가 객체를 call by reference로 전달한다는 오해는"으로 구체화하면서, 그 오해의 원인이 용어 하나뿐인 것처럼 읽히게 됐다. 바로 앞의 Dragon Book 인용도 용어가 아니라 동작의 효과 때문에 call by reference라는 말을 빌려 쓴 사례다. 용어는 원인을 하나 더 보태는 요인으로 쓰는 편이 글 전체와 맞는다.

수정 제안:

> 'reference'라는 용어도 Java가 객체를 call by reference로 전달한다는 오해를 키운 것으로 보입니다. Java에서는 객체를 가리키는 값을 'reference'라고 부르는데, 이 단어가 'call by reference'의 reference와 겹치기 때문입니다.

**반영 완료.** 215행의 첫 문장을 제안 문장으로 바꿨다. 둘째 문장은 그대로 두었다.

근거: 본문 57행, 144행, 171행의 서술. 논리 일관성 판정.

### 2. "behaves as if" 해설이 원문의 복사 비용 맥락을 빠뜨림

**위치: 본문 213행.** "'call by reference를 쓰는 것처럼 동작한다(behaves as if)'는 호출된 메서드에서 바꾼 객체 상태가 호출한 쪽에서도 보인다는 효과를 가리킵니다."라고 쓴다.

Dragon Book에서 이 문장은 call by reference 항목의 셋째 문단 끝에 있다. 그 문단은 큰 객체를 strict call by value로 넘기면 통째로 복사해야 해서 비용이 크다는 이야기로 시작하고, Java는 참조만 복사해서 이 문제를 푼다고 한 뒤 "The effect is that…"으로 이어진다. 원문이 말하는 효과에는 객체를 통째로 복사하지 않는다는 점이 먼저 들어 있다. 호출된 쪽에서 객체를 바꿀 수 있다는 점은 call by value 항목의 "Thus, the called procedure is able to affect the value of the object itself."에 있다. 본문의 해설이 틀린 것은 아니지만 인용한 문장 자체의 맥락을 반쯤만 옮긴다.

수정 제안:

> 'call by reference를 쓰는 것처럼 동작한다(behaves as if)'는 객체를 통째로 복사하지 않고, 호출된 메서드에서 바꾼 객체 상태가 호출한 쪽에서도 보인다는 효과를 가리킵니다.

**반영 완료.** 213행의 첫 문장을 제안 문장으로 바꿨다.

근거: Aho, Lam, Sethi, Ullman, Compilers: Principles, Techniques, and Tools 2판 1.6.6절 Call-by-Reference 항목 셋째 문단(인쇄 쪽 34~35쪽). 소스 대조 판정.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| Dragon Book 2판 1.6.6절 call by value 인용(30행): "In call-by-value, the actual parameter is evaluated … as well as in most other languages." | **확인.** PDF 텍스트와 공백을 무시하고 글자 단위로 일치. 원문 `C + +`는 추출 과정의 공백이다 | Compilers 2판 1.6.6 Call-by-Value, 인쇄 쪽 34쪽 |
| Dragon Book 2판 1.6.6절 call by reference 인용(32행) | **확인.** 글자 단위 일치 | Compilers 2판 1.6.6 Call-by-Reference, 인쇄 쪽 34쪽 |
| Dragon Book의 Java 서술 인용 두 단락(204행, 206행) | **확인.** 글자 단위 일치. 첫 단락은 Call-by-Value 항목 셋째 문단, 둘째 단락은 Call-by-Reference 항목 셋째 문단의 끝부분이며 사이를 `...`로 표시했다 | Compilers 2판 1.6.6, 인쇄 쪽 34~35쪽 |
| Strachey 3.4.2절 인용(48행) | **확인.** 3.4.2절 첫 문단 전체와 글자 단위 일치 | [Strachey 1967/2000](https://www.cs.cmu.edu/~crary/819-f09/Strachey67.pdf) |
| 네 인용문의 한국어 번역(34·36행, 208·210행, 50행) | **확인.** actual/formal parameter, bound variable, referentially transparent, "will also be known as"를 원문 뜻대로 옮겼다. 생략 표시 위치도 영어 원문과 같다 | 위 두 출처 |
| 인용 뒤 설명: actual parameter는 이 글의 인자, formal parameter는 이 글의 파라미터에 해당(39행) | **확인.** 1.6.6절 첫 문단이 actual parameter를 "the parameters used in the call of a procedure", formal parameter를 "those used in the procedure definition"으로 정의 | Compilers 2판 1.6.6 도입 문단 |
| Strachey가 R-value 전달을 call by value, L-value 전달을 call by reference라고 불렀음(44행) | **확인.** "calling a parameter by value (R-value) or reference (L-value)"와 다음 문단의 "call by reference" 대조 | [Strachey 1967/2000](https://www.cs.cmu.edu/~crary/819-f09/Strachey67.pdf) |
| Strachey 강의가 1967년이고 뒤에 정리됨(44행) | **확인.** 초록이 "a course of lectures given at the International Summer School in Computer Programming at Copenhagen in August, 1967"이라 밝히고 Higher-Order and Symbolic Computation 13권(2000) 11–49쪽에 실림 | [Strachey 1967/2000](https://www.cs.cmu.edu/~crary/819-f09/Strachey67.pdf) |
| Dragon Book 인용 앞 문장: 같은 절에서 Java는 call by value만 쓴다고 밝히고, call by reference 항목에서는 call by reference처럼 동작한다고 적음(200행) | **확인.** "Even though Java uses call-by-value exclusively"는 Call-by-Value 항목, "behaves as if it used call-by-reference"는 Call-by-Reference 항목에 있다 | Compilers 2판 1.6.6 |
| 213행 둘째 문장: 이 책이 앞에서 밝혔듯이 실제 전달 방식은 call by reference가 아니라는 전제를 깐 표현 | **확인.** 해당 문단이 "As we noted when discussing call-by-value"로 시작해 앞 항목의 "call-by-value exclusively"를 전제로 한다 | Compilers 2판 1.6.6 |
| `targetTv = new Tv()`는 파라미터 자체에 대입, `targetTv.setChannel("Rock")`은 객체 상태 변경(57행) | **확인.** 본문의 `reassign`(134행), `changeState` 예제의 코드와 같고 `Tv` 클래스의 `channel` 필드는 `private`이라 setter 호출 형태가 맞다 | 본문 예제 코드 |
| Dragon Book 표지 캡션: 2판, ISBN 9780321486813(41행) | **확인.** Open Library 레코드가 "Compilers", "2 edition", 2006-08-31, Addison Wesley. 표지 이미지에 "Second Edition"과 저자 네 명이 보인다 | [Open Library ISBN 9780321486813](https://openlibrary.org/isbn/9780321486813) |
| The Java Programming Language 표지 캡션: 4판, ISBN 9780321349804(188행) | **확인.** Open Library 레코드가 "The Java Programming Language (4th Edition)", "4 edition". 표지 이미지에 "Fourth Edition"이 보이고, 인용 출처 표기와 참고 자료 링크의 ISBN도 같다 | [Open Library ISBN 9780321349804](https://openlibrary.org/isbn/9780321349804) |
| Dragon Book의 전신이 1977년 **Principles of Compiler Design**이고 표지 그림 때문에 Dragon Book으로 불림(39행) | **확인.** Codex가 든 저자 서지를 Claude가 받아 대조했다. Aho와 Ullman의 Principles of Compiler Design이 Addison-Wesley 1977년, 2판이 2007년으로 적혀 있다. Wikipedia 항목은 이 책이 "Dragon Book"으로 알려졌다고 적고, 2판 표지 캡션에서 기사와 용이 그려져 있다고 설명한다. 이번 변경은 문장을 옮기고 나눈 것이며 사실 내용은 바뀌지 않았다 | [Alfred V. Aho의 저서 목록](https://www.cs.columbia.edu/~aho/books.html), [Wikipedia: Compilers: Principles, Techniques, and Tools](https://en.wikipedia.org/wiki/Compilers:_Principles,_Techniques,_and_Tools) |

## 한계

- Dragon Book 2판은 출판사 정식본이 아니라 웹에 공개된 PDF 사본(dbscience.org)으로 대조했다. 인쇄 쪽 번호(34~35쪽)와 본문은 2006년 초판 인쇄본 기준이며, 이후 쇄에서 문구가 바뀌었는지는 확인하지 못했다. 2026-09-19 3차 기록도 PDF 34~35쪽으로 같은 결과를 냈다.
- 53행의 "이 강의록은 L-value와 R-value, first-class 객체 같은 용어의 출처로 지금도 인용되는" 문장은 이번에 위치만 옮겼고 문구는 2026-09-20 커밋 `65e0b60`에서 들어왔다. 이 강의록 2.2절이 "we shall give these two values the neutral names: L-value … and R-value"라고 이름을 붙이는 것은 확인했지만, 그보다 앞선 CPL 문서에 같은 용어가 있었는지는 이번 범위에서 확인하지 않았다. 문장이 "출처로 인용되는"이라고 써서 최초 사용을 단정하지는 않는다.
- 이 검증을 시작할 때 Codex CLI가 플랫폼 바이너리(`@openai/codex-linux-x64`) 누락으로 실행되지 않아, 설치된 버전과 같은 `@openai/codex@0.156.1`을 다시 설치한 뒤 리뷰를 돌렸다.
