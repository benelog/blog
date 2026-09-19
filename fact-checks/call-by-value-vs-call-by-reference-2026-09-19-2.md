# Call by value vs Call by reference 글 사실 관계 검증 — 2026-09-19 (2차)

- 대상: [call-by-value-vs-call-by-reference.adoc](../src/content/call-by-value-vs-call-by-reference.adoc)
- 검토 기준 커밋: `0e528009787fab1d30eac12af154029eb6679f36`
- 참고: 이 기록에 적은 검토 기준 커밋과 반영 커밋은 이후 하나의 커밋으로 합쳐져 현재 이력에서는 해시로 찾을 수 없다. 검증 당시 글은 `call-by-value.adoc`이었고, 합칠 때 지금 파일명으로 바꿨다.
- 이전 기록: [2026-09-19 1차 검증](call-by-value-vs-call-by-reference-2026-09-19.md). 같은 날 앞선 커밋 `ce4bac9`를 검증한 기록이다.
- 범위: 위 커밋에서 추가한 Python 절과 Ruby 절, Rust 절의 첫 문장, 'Call by reference 지원' 열 중심으로 재구성한 정리 표. 1차 기록에서 확인한 C·C++·Go·Ada 절의 인용은 다시 대조하지 않았다.
- 방법: Codex 적대적 리뷰(`adversarial-review --base HEAD~1`)로 의심 지점을 모은 뒤, Claude가 Python 공식 튜토리얼과 FAQ, Ruby 공식 FAQ, Ada Reference Manual 원문을 WebFetch로 받아 대조했다. Python·Ruby 예제는 로컬에서 실행했다.
- 결과: **우선 수정할 사항 1건.** Python·Ruby 절의 인용과 예제, Rust가 call by reference를 지원하지 않는다는 서술은 타당하다. 문제는 정리 표의 Ada 행이 참조 전달 여부를 파라미터 모드가 정하는 것처럼 요약한 부분이다.
- 반영 상태: **1건 본문에 반영함.** 커밋 `ec98564`에 적용했다. 검증 직후 저자 판단으로 Ruby 절과 정리 표의 Ruby 행, Ruby FAQ 참고 링크는 본문에서 삭제했다. 아래 Ruby 관련 확인 항목은 기준 커밋 시점의 기록으로 남겨 둔다.
- 아래 행 번호는 위 기준 커밋 기준이다.

## 우선 수정할 사항

### 1. 정리 표의 Ada 행이 call by reference 지원 조건을 파라미터 모드로 요약함

**반영 완료.** 정리 표의 Ada 행을 수정 제안 문구대로 바꿨다(커밋 `ec98564`).

**위치: 본문 441행(정리 표 Ada 행).** 'Call by reference 지원' 열에 "예. `in out`, `out` 모드. 단, 복사로 전달된 파라미터는 정상 종료 시 되돌려 쓰는 copy-restore"라고 적었다.

Ada에서 전달 기제를 정하는 것은 모드가 아니라 타입과 `aliased` 여부다. Ada Reference Manual 6.2는 by-copy 타입 파라미터를 explicitly aliased가 아닌 한 복사로 전달하고(3/3항), by-reference 타입 파라미터와 explicitly aliased 파라미터는 참조로 전달하며(10항), 그 밖의 타입은 미지정(11항)이라고 규정한다. 13항의 NOTE는 모드가 "the direction of information transfer", 즉 정보 전달 방향을 기술한다고 밝힌다. 따라서 `aliased`가 아닌 `in out Integer`는 모드가 `in out`이어도 반드시 복사로 전달되고, tagged 타입은 `in` 모드여도 참조로 전달된다. 표의 문구는 모드가 참조 전달을 결정하는 것으로 읽혀 본문 415~417행의 설명과 어긋난다. 뒤에 붙인 copy-restore 단서만으로는 이 오독을 막지 못한다.

다만 이 글의 call by reference 기준은 "파라미터에 대입했을 때 호출한 쪽 변수가 바뀌는가"이고, `in` 모드 파라미터는 상수 뷰라 대입할 수 없다(6.2 14항). 그래서 지원 열에는 모드 조건과 기제 조건을 함께 적어야 한다. Codex는 모드를 지원 열에서 완전히 빼자고 제안했지만, 이 글의 기준에서는 `in out`·`out`이라는 조건이 여전히 필요하다.

수정 제안(441행):

> |Ada|부분적으로. `in out`, `out` 파라미터 중 by-reference 타입이거나 `aliased`로 선언한 것만 call by reference. by-copy 타입은 정상 종료 시 되돌려 쓰는 copy-restore, 그 밖의 타입은 unspecified|타입에 따라 by-copy 또는 by-reference. `aliased` 파라미터는 항상 by-reference|`in out`, `out` 모드 선언

근거: [Ada Reference Manual 6.2](https://ada-lang.io/docs/arm/AA-6/AA-6.2/) 3/3항 "A parameter of a by-copy type is passed by copy, unless the formal parameter is explicitly aliased", 10항 "A parameter of a by-reference type is passed by reference, as is an explicitly aliased parameter of any type", 13항 NOTE 1 "The mode of a formal parameter describes the direction of information transfer", 14항 "A formal parameter of mode in is a constant view; it cannot be updated".

Codex 지적을 Claude가 매뉴얼 원문에서 확인했다. 지적의 근거는 타당하고, 제안 문구는 이 글의 판별 기준에 맞게 조정했다. 소스 대조 판정이다.

## 확인한 사항

| 주장 | 판정 | 근거 |
|---|---|---|
| Python 튜토리얼 4.8 Defining Functions: "arguments are passed using call by value (where the value is always an object reference, not the value of the object)" | **확인.** WebFetch로 원문과 절 번호 대조 | [Python Tutorial 4.8](https://docs.python.org/3/tutorial/controlflow.html#defining-functions) |
| 같은 문장의 각주: "call by object reference would be a better description, since if a mutable object is passed, the caller will see any changes the callee makes to it" | **확인.** 각주 원문 대조 | 위와 같음 |
| Python FAQ: "arguments are passed by assignment in Python. Since assignment just creates references to objects, there's no alias between an argument name in the caller and callee, and consequently no call-by-reference" | **확인.** 항목 제목 "How do I write a function with output parameters (call by reference)?"와 첫 문단 대조 | [Python FAQ](https://docs.python.org/3/faq/programming.html#how-do-i-write-a-function-with-output-parameters-call-by-reference) |
| Ruby 공식 FAQ "How are arguments passed?": 실제 인자가 형식 인자에 대입되며 "There is no equivalent of other language's pass-by-reference semantics" | **확인.** ruby-lang.org FAQ 4부 원문 대조 | [Ruby FAQ 4](https://www.ruby-lang.org/en/documentation/faq/4/) |
| Ruby 공식 FAQ "Does assignment to a formal argument influence the actual argument?": 형식 인자는 지역 변수이며 대입하면 다른 객체를 가리키게 될 뿐 | **확인.** 원문 대조. ruby-doc.org의 FAQ 묶음에서는 4.6 항목 | 위와 같음, [ruby-doc.org FAQ](https://ruby-doc.org/docs/ruby-doc-bundle/FAQ/FAQ.html) |
| Python 예제 출력 `1`, `News`, `Sports` | **확인.** Python 3.12.3으로 실행 | 로컬 실행 |
| Ruby 예제 출력 `1`, `News`, `Sports` | **확인.** Ruby 3.2.3으로 실행. `Tv = Struct.new(:channel)` 정의 포함 | 로컬 실행 |
| Rust는 call by reference를 지원하지 않으며 `&mut T` 참조도 값으로 전달된다(375행) | **확인.** 1차 기록에서 대조한 Rust Book의 "will move or copy, just as assignment does"와 Rust Reference의 `&mut T`가 `Copy`가 아니라는 설명에 부합. 파라미터가 호출자 변수의 별칭이 되는 규정은 없다 | [Rust Book 4.1](https://doc.rust-lang.org/book/ch04-01-what-is-ownership.html#ownership-and-functions), [Rust Reference](https://doc.rust-lang.org/reference/types/pointer.html#mutable-references-mut) |
| 정리 표의 Java·JavaScript·C·Python·Ruby·Go·Rust 행 "아니오"와 C++ 행 "예. 참조 파라미터에 한함" | **확인.** 각 절의 본문 설명과 일치 | 본문 각 절 |
| Call by sharing 절의 "Python 튜토리얼이 쓴 call by object reference도 같은 뜻" | **확인.** 튜토리얼 각주의 설명(변경 가능한 객체를 넘기면 호출자에게 변경이 보임)이 CLU 문서의 call by sharing 정의와 같은 동작을 가리킨다. 용어 대응은 저자의 해석이다 | 위 튜토리얼 각주 |

## 한계

- Ruby FAQ 항목의 번호는 ruby-lang.org 판과 ruby-doc.org 판이 다를 수 있어 본문에서는 항목 제목만 인용했다.
- 1차 기록에서 확인한 C·C++·Go·Ada 절의 인용은 이번에 다시 대조하지 않았다.
