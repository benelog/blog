# Call by value vs Call by reference 글 사실 관계 검증 — 2026-09-19

- 대상: [call-by-value-vs-call-by-reference.adoc](../src/content/call-by-value-vs-call-by-reference.adoc)
- 검토 기준 커밋: `ce4bac9b0efa8d9b4460d01f9603757db0a535f8`
- 참고: 이 기록에 적은 검토 기준 커밋과 반영 커밋은 이후 하나의 커밋으로 합쳐져 현재 이력에서는 해시로 찾을 수 없다. 검증 당시 글은 `call-by-value.adoc`이었고, 합칠 때 지금 파일명으로 바꿨다.
- 이전 기록: 없음
- 범위: 위 커밋에서 새로 쓴 C·C++·Go·Rust·Ada 절, Call by sharing 절, 정리 절의 비교 표. 기존 Java·JavaScript 절은 이번 범위가 아니다.
- 방법: Codex 적대적 리뷰(`adversarial-review --base HEAD~1`)로 의심 지점을 모은 뒤, Claude가 언어 사양과 표준 문서 원문을 WebFetch·curl로 받아 대조했다. 예제 코드는 컴파일·실행하지 않았다.
- 결과: **우선 수정할 사항 2건.** Java와 JavaScript가 call by value만 쓴다는 핵심 결론과 각 언어의 전달 기제 설명은 타당하다. 문제는 비교 표와 정리 절에서 swap 예제의 모양을 언어 전체의 규칙으로 확대한 부분이다.
- 반영 상태: **2건 모두 본문에 반영함.** 검증 당일 반영해 커밋 `159109e`에 적용했다. 이 문서의 검증 본문은 작성 당시의 수정 제안을 그대로 둔다. 아래 행 번호는 위 기준 커밋 기준이다.

## 우선 수정할 사항

### 1. 호출 지점의 명시적 표기를 Go·Rust 전체의 조건으로 일반화함

**반영 완료.** Go 절과 Rust 절에 메서드 호출의 자동 주소 취득·대여 문단을 추가하고, 표와 정리 절의 "호출 지점에 명시"를 "넘김"으로 바꾼 뒤 예외를 적었다.

**위치: 본문 286행(Go), 307행(Rust), 352~353행(비교 표), 360행(정리).** "호출한 쪽의 값을 바꾸려면 포인터(가변 참조)를 호출 지점에 명시합니다"라고 서술하고, 정리 절에서는 "호출 지점에 아무 표시 없이 호출자 변수의 별칭을 만드는 언어는 C++과 Ada뿐"이라고 단정한다.

Go 사양 Selectors 절은 `x`가 addressable이고 `&x`의 메서드 집합에 `m`이 있으면 `x.m()`이 `(&x).m()`의 축약 표기라고 규정한다. 포인터 수신자 메서드는 호출식에 `&`가 없어도 원래 변수를 바꾼다. Rust Reference의 메서드 호출 규칙([expr.method.autoref-deref])도 메서드를 찾을 때 수신자를 자동으로 역참조하거나 대여할 수 있다고 규정하며, 후보 수신자 타입에 `&T`와 `&mut T`를 자동으로 추가한다. 따라서 "호출식의 모양으로 호출한 쪽 변수가 바뀌는지 판단할 수 있다"는 뉘앙스는 두 언어의 메서드 호출에서 깨진다. 전달되는 것이 포인터·가변 참조 값이라는 본문의 설명 자체는 맞다.

수정 제안:

> 일반 함수로 호출한 쪽의 값을 바꾸려면 C처럼 `swap(&a, &b)`로 포인터를 넘깁니다. 다만 메서드 호출에서는 호출 지점에 `&`가 없어도 됩니다. Go 사양의 Selectors 절은 `x`가 주소를 구할 수 있는 값이고 `&x`의 메서드 집합에 `m`이 있으면 `x.m()`이 `(&x).m()`의 축약 표기라고 규정합니다.

> (Rust) 메서드 호출에서는 Go처럼 호출 지점에 `&mut`가 없어도 됩니다. Rust Reference의 메서드 호출 규칙은 메서드를 찾을 때 수신자를 자동으로 역참조하거나 대여할 수 있다고 규정합니다.

근거: [Go 사양 Selectors](https://go.dev/ref/spec#Selectors) "If x is addressable and &x's method set contains m, x.m() is shorthand for (&x).m()", [Rust Reference expr.method.autoref-deref](https://doc.rust-lang.org/reference/expressions/method-call-expr.html#r-expr.method.autoref-deref) "the receiver may be automatically dereferenced or borrowed in order to call a method".

Codex 지적을 Claude가 두 사양 원문에서 확인했다. 소스 대조 판정이며 실행하지 않았다.

### 2. Ada의 by-copy 타입이 항상 복사로 전달된다고 단정함

**반영 완료.** Ada 절에 `aliased` 파라미터 예외 문장을 추가하고, 표의 조건을 "실제로 복사로 전달된 파라미터"로 바꿨다.

**위치: 본문 330행(Ada 절), 354행(비교 표).** "elementary type은 by-copy type이라 복사로 전달"하고, 표에서는 "by-copy type은 정상 종료 시 되돌려 쓰는 copy-restore"라고 적는다.

Ada Reference Manual 6.2의 3항은 by-copy type 파라미터를 복사로 전달하되 "unless the formal parameter is explicitly aliased"라는 단서를 달고, 10항은 explicitly aliased 파라미터는 타입과 관계없이 참조로 전달한다고 규정한다. 위 예제의 `X`를 `X : aliased in out Integer`로 선언하면 `Integer`가 elementary type이어도 참조로 전달되고, 도중에 예외가 전파돼도 변경이 실제 인자에 남는다. 글이 강조하는 "중단 시 관찰 결과 차이"를 이 경우에 잘못 예측하게 된다.

수정 제안:

> 예외가 하나 있습니다. 같은 절 3항과 10항에 따라 `aliased`로 선언한 형식 파라미터는 타입과 관계없이 참조로 전달됩니다. 위 예제의 `X`를 `X : aliased in out Integer`로 선언하면 `Integer`가 elementary type이어도 참조로 전달됩니다.

근거: [Ada Reference Manual 6.2](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) 3항 "A parameter of a by-copy type is passed by copy, unless the formal parameter is explicitly aliased", 10항 "A parameter of a by-reference type is passed by reference, as is an explicitly aliased parameter of any type".

Codex 지적을 Claude가 매뉴얼 원문에서 확인했다. 소스 대조 판정이다.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| C11 N1570 6.5.2.2 4항 "In preparing for the call to a function, the arguments are evaluated, and each parameter is assigned the value of the corresponding argument" | **확인.** N1570 HTML 미러를 curl로 받아 인용문과 각주 번호(93)를 대조 | [N1570 PDF](https://www.open-std.org/jtc1/sc22/wg14/www/docs/n1570.pdf), [port70 HTML 미러](https://port70.net/~nsz/c/c11/n1570.html) |
| N1570 각주 93: 함수가 파라미터를 바꿔도 인자에 영향이 없고, 포인터를 넘기면 가리키는 객체를 바꿀 수 있음 | **확인.** 각주 원문 대조 | 위와 같음 |
| Go 사양 Calls 절: 인자 평가 후 함수 변수(파라미터·결과 포함)용 저장 공간을 새로 할당하고 인자를 파라미터에 대입 | **확인.** go.dev/ref/spec을 curl로 받아 원문 대조. 현재 사양은 "passed by value"가 아니라 "assigned to their corresponding function parameters"라고 쓴다 | [Go 사양 Calls](https://go.dev/ref/spec#Calls) |
| Go FAQ "As in all languages in the C family, everything in Go is passed by value" 및 맵·슬라이스가 포인터를 담은 descriptor라는 설명 | **확인.** WebFetch로 원문 대조 | [Go FAQ](https://go.dev/doc/faq#pass_by_value) |
| Rust Book "Passing a variable to a function will move or copy, just as assignment does" | **확인.** WebFetch로 원문 대조 | [Rust Book 4.1](https://doc.rust-lang.org/book/ch04-01-what-is-ownership.html#ownership-and-functions) |
| `std::mem::swap` 시그니처 `fn swap<T>(x: &mut T, y: &mut T)` | **확인.** 문서상 `pub const fn swap<T>(x: &mut T, y: &mut T)`. 본문은 `pub const`를 생략했으나 파라미터 형태는 일치 | [std::mem::swap](https://doc.rust-lang.org/std/mem/fn.swap.html) |
| Ada RM 6.2: elementary type은 by-copy, tagged·task·protected·explicitly limited record는 by-reference, 그 밖은 unspecified | **확인.** WebFetch로 원문 대조. 단, aliased 예외는 수정 사항 2 참고 | [Ada RM 6.2](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) |
| Ada RM 6.4.1 17항: by-copy로 전달된 in out·out 파라미터는 정상 종료 후 실제 인자에 되돌려 대입 | **확인.** WebFetch로 11항과 17항 원문 대조 | [Ada RM 6.4](https://ada-lang.io/docs/arm/AA-6/AA-6.4) |
| 비교 표의 Java·JavaScript·C·C++ 행 | **확인.** 각 절의 본문 설명과 일치. C++ 참조 파라미터만 재할당이 호출한 쪽에 보인다는 구분은 [dcl.ref] 설명과 일치 | 본문 각 절 |
| `C++`를 `{cpp}`로 바꾼 뒤 렌더링 | **확인.** JBake 전체 빌드 결과에서 본문 백틱 노출 0개, `{cpp}` 미치환 0개, H2 11개 순서 확인. Codex는 Asciidoctor 2.0.20 단독 변환으로 같은 내용을 확인했다 | `./gradlew bake` 결과 |

## 한계

- C, C++, Go, Rust, Ada 예제 코드는 컴파일·실행하지 않았다. 문법과 결과 주석은 언어 사양에 비추어 검토한 소스 대조 판정이다. Ada 예제는 선언부를 생략한 발췌라 단독으로 컴파일되지 않는다.
- C++ 표준 인용([dcl.ref], [expr.call], 'passed by reference'가 네 번 나온다는 집계)과 ALGOL 60 보고서 4.7.3절 제목은 이 커밋에서 위치만 옮긴 기존 문장이라 이번에 다시 대조하지 않았다.
- Go 사양 Calls 절은 과거 판본에서 "passed by value"라는 표현을 썼다고 알려져 있으나 어느 판본에서 바뀌었는지는 확인하지 않았다. 본문은 현재 판본 기준으로 서술한다.
