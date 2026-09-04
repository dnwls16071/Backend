### CLAUDE.md 작동 원리

* CLAUDE.md 파일을 찾아서 통째로 읽는다. 매 세션마다.
* 한 번 써두면 매번 자동 적용된다.
* Context(맥락, 참고사항) != Enforcement(강제, 무조건 따름)
* CLAUDE.md는 시스템 프롬프트의 일부가 아니라 사용자 메시지로 전달된다. Claude가 읽고 따르려고 하지만, 모호하거나 충돌하는 지시에 대해서는 엄격하게 따른다는 보장이 없다.

| CLAUDE.md | Auto Memory |
|:---:|:---:|
| 내가 직접 작성 | Claude가 알아서 저장 |
| 지침과 규칙 | 학습한 패턴 |
| 프로젝트 루트 | ~/.claude/projects/ |
| 매 세션 전체 로드 | 처음 200줄만 로드 |
| 업무 매뉴얼 | 업무 일지 |

### 나쁜 패턴 vs 좋은 원칙

* Avoid common failure patterns
  * The over-specified CLAUDE.md : CLAUDE.md가 너무 길면 중요한 규칙이 노이즈에 묻혀서 Claude가 절반을 무시한다. 가차 없이 잘라내어야 한다.
  * Ambiguous instructions vs Specific instructions
    * 모호한 지시 예 : "깔끔한 코드를 작성하세요", "테스트를 잘 작성하세요", "파일을 잘 정리하세요"
    * 구체적 지시 예 : "함수는 30줄 이하로 작성하세요", "새 파일 만들기 전에 나한테 확인하세요", "API 관련 파일은 src/api/ 폴더에 만드세요"
    * 검증 가능한 구체적인 지시를 내려야 한다는 것이 중요하다. 프로젝트가 커질수록 규칙도 늘어나기에 모듈화가 필수다.
  * How do I use it?
    * 실수를 발견할 때마다 CLAUDE.md에 한 줄 추가한다. 처음부터 완벽하게 쓰려고 하지 말자.
* Develop your intuition
  * 검증 방법을 알려줘야 한다.
  ```
  ## 빌드 & 테스트
  * 빌드 : ~~~
  * 테스트 (단일) : ~~~
  * 린트 : ~~~

  ## 워크플로우
  * 코드 수정 & 반드시 타입체크 실행
  * 전체 테스트가 아닌 관련 파일만 테스트(성능)
  * 구현 후 린트 통과 확인
  ```
  * 도메인 용어를 정의하라.
    * 안 좋은 예 : "주문 금액 계산해줘" → 주문항목 하나의 가격만 리턴. "주문"이 뭔지 몰라서 생기는 실수
    * 좋은 예
      * 주문(Order) : 고객이 한 번에 결제하는 주문 묶음(배달비, 총액 포함)
      * 주문항목(OrderItem) : 주문 안의 개별 메뉴(짜장면 1개, 탕수육 1개 등)
      * 가게(Store) : 실제 주문을 받는 개별 매장(메뉴, 영업 시간 보유)
  * `.claude/rules/`로 규칙을 분리하라.
    * `testing.md` : 테스트 관련 규칙
    * `api-design.md` : API 설계 규칙
    * `security.md` : 보안 체크리스트

> * [65줄 텍스트가 AI 코딩을 바꿨다? 하루 400 스타 받은 파일의 정체](https://news.hada.io/topic?id=26655)
> * [andrej-karpathy-skills](https://github.com/multica-ai/andrej-karpathy-skills/blob/main/CLAUDE.md)