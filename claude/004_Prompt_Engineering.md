### 클로드에게 역할 부여하기 (Give Claude a role)

* 각 사용 사례별 성능을 높이기 위해 Claude에게 전문가와 같은 특정한 역할을 부여한다.
    * 역할 프롬프팅이 작동하는 방식 : 클로드에게 구체적인 역할을 부여함으로써, 그가 특정 상황이나 문제를 더 정확하게 이해하고 대응할 수 있도록 한다.
    * 예를 들어, 클로드에게 수학 문제를 해결하는 '수학 전문가' 역할을 부여하면 더 나은 성능을 발휘할 수 있다.
* 역할 프롬프팅을 사용할 때 : 특히 기술적이거나 복잡한 작업을 수행할 때, 또는 특정 커뮤니케이션 스타일이 필요할 때 역할 프롬프팅을 사용하면 좋다.

예를 들어,

```
There are two ducks in front of a duck, two ducks behind a duck and a duck in the middle. How many ducks are there?
```

과 같은 문제를 풀어야 할 때는 다음과 같은 방식을 사용해볼 수 있다.

|             Role              |                                                                                                                                                                            Content                                                                                                                                                                            |
|:-----------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|             User              |                                                                      You are a master logic bot designed to answer complex logic problems. Solve this logic puzzle. There are two ducks in front of a duck, two ducks behind a duck and a duck in the middle. How many ducks are there?                                                                       |
| Assistant (Claude's response) | [Automatically outputting thinking step by step as part of its training...] Without additional clarification or assumptions, the logic puzzle does not provide enough information to determine the exact number of ducks. The answer could range from a minimum of 3 ducks to a maximum of 5 ducks, depending on the interpretation of the given information. |

* 역할 프롬프팅 예시 : 다양한 청중에게 '왜 하늘이 파란색인지' 설명하도록 클로드에게 각기 다른 역할 (유치원 선생님, 대학교수)을 부여하는 예시가 있다. 각 역할에 따라 클로드 설명 스타일이 달라진다.

|             Role              |                                                                                                                                                                                                                          Content                                                                                                                                                                                                                          |
|:-----------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|             User              |                                                                                                                                                                                 You are a kindergarten teacher. Succinctly explain why the sky is blue to your students.                                                                                                                                                                                  |
| Assistant (Claude's response) | Have you ever wondered why the sky looks so pretty and blue? The reason is because of the sunlight! When the sun's rays shine down through the Earth's atmosphere, they hit lots of tiny gas molecules in the air. The blue colors in the sunlight get scattered and bounced all around by these molecules. And that's what makes the sky look blue to our eyes! Isn't that neat? The bright blue sky is like nature's big colorful painting just for us. |

|             Role              |                                                                                                                                                                                                                                                                                                                                                                                                                                     Content                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|:-----------------------------:|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|             User              |                                                                                                                                                                                                                                                                                                                                                                                          You are a graduate physics professor. Succinctly explain why the sky is blue to your students.                                                                                                                                                                                                                                                                                                                                                                                          |
| Assistant (Claude's response) | The sky appears blue due to the phenomenon of Rayleigh scattering of sunlight by the molecules in the Earth's atmosphere. Sunlight, which is composed of a spectrum of wavelengths, interacts with the gaseous molecules in the atmosphere. The shorter wavelengths of visible light, corresponding to the blue and violet colors, are scattered more efficiently by these molecules than the longer wavelengths.<br /><br />This selective scattering of the shorter blue wavelengths in all directions by the atmospheric molecules is what causes the sky to appear blue when viewed from the Earth's surface. The longer wavelengths of red and orange, being less scattered, tend to pass through the atmosphere more directly, giving rise to the reddish hues we see during sunrise and sunset when the sunlight has to travel through a greater depth of the atmosphere. |

* 효과적인 역할 프롬프팅 팁 : 클로드에게 역할을 부여할 때는 구체적인 맥락과 역할 정보를 제공하는 것이 중요하다. 다양한 역할과 프롬프트를 실험하여 최적의 접근 방식을 찾아야 한다.

### 오류 수정과 명확한 프롬프트 엔지니어링

* 효과적인 프롬프트의 7가지 원칙

**① 맥락 제공**

* AI는 사용자의 프로젝트 배경을 모르므로 언어, 프레임워크, 라이브러리, 오류 메시지, 목적 등을 명시해야 한다.
* 예: “Node.js + Express + Mongoose 환경에서 user fetch 중 TypeError 발생”처럼 기술적 배경을 포함한다.

**② 목표 명확화**

* "코드가 안돼요"는 도움이 안 된다. “예상 결과는 ○○인데 실제는 △△임. 왜 그런가?” 식으로 정확히 묻는다.

**③ 복잡한 작업 쪼개기**

* 기능 전체를 한 번에 묻기보다 단계별로 요청하는 게 더 효과적이다.

**④ 입출력 예시 포함**

* 원하는 출력 예시를 보여주면 AI의 의도 파악 능력이 높아진다. (e.g. [3,1,4] → [1,3,4])

**⑤ 역할 부여**

* “시니어 리액트 개발자처럼 코드를 리뷰해줘” 식의 역할 설정은 응답의 깊이와 품질을 향상시킨다.

**⑥ 대화형 반복 개선**

* 첫 응답이 완벽할 필요는 없다. 피드백을 주면 AI는 그 흐름을 이어서 점점 정교한 결과를 낸다.

**⑦ 코드 일관성 유지**

* 함수명, 포맷, 주석 등 코드 자체가 일관성 있게 작성되어 있으면, AI도 그 흐름을 유지해 품질이 높아진다.

### 디버깅을 위한 프롬프트 전략

**① 오류 명시와 기대 동작 비교**

* 에러 메시지, 문제 증상, 기대 결과, 입력값을 함께 제시하면 AI는 정확한 진단을 한다.

**② 라인 단위 추적 요청**

* “이 변수 값이 어디서 이상해졌는지 단계별로 추적해줘” 식의 요청은 복잡한 논리 버그에 효과적이다.

**③ 최소 재현 코드 제공**

* 전체 코드 대신 문제가 발생하는 핵심 코드만 주면, AI가 더 정확하게 원인을 분석할 수 있다.

**④ 명확한 후속 질문**

* “왜 이런 결과가 나오죠?”보다 “이 부분에서 어떤 조건이 잘못된 걸까요?”처럼 직접적인 질문이 좋다.

**⑤ 예시 비교: 나쁜 질문 vs. 좋은 질문**

* 단순히 “코드 안됨”이라고 하면 추측성 답변만 나오지만, 에러 메시지와 코드를 함께 주면 정확한 해결책을 받게 된다.

### 리팩토링과 최적화를 위한 프롬프트 전략

**① 리팩토링 목표 명확화**

* 단순히 “리팩토링 해줘”보다는 “가독성 향상, 성능 개선, API 최신화” 등 구체적 개선 목표를 제시해야 한다.
* 목표가 모호하면 AI는 무작위 개선을 시도하거나 원하지 않는 방향으로 바꿀 수 있다.

**② 언어/환경 맥락 제공**

* “React 클래스형 → 함수형 전환”, “Node.js 14 환경” 등 프로젝트 스타일이나 기술 제약을 알려주면 적합한 변환이 가능하다.

**③ 설명도 함께 요청**

* 리팩토링된 코드와 함께 “왜 이렇게 바꿨는지” 설명을 요청하면, 코드 품질 검토와 학습 효과 모두 얻을 수 있다.

**④ 역할 기반 요청으로 수준 향상**

* “시니어 타입스크립트 개발자처럼 리팩토링해줘” 같은 요청은 더 현대적이고 깊이 있는 개선안을 이끌어낸다.

### 새 기능 구현을 위한 프롬프트 전략

**① 기능을 단계로 나눠 요청**

* 복잡한 기능도 “기능 구조 설계 → UI 생성 → 로직 연결” 순으로 나누어 요청하면 더 안정적인 결과를 얻을 수 있다.

**② 기존 코드 스타일 제공**

* 유사한 컴포넌트나 내부 컨벤션을 제시하면, 프로젝트 일관성에 맞는 코드가 생성된다. 예: “UserList 기반으로 ProductList 생성”

**③ 주석/TODO로 의도 전달**

* IDE에서 “// TODO: 요청 유효성 검증 구현”처럼 자연어 주석을 달면 Copilot이 그에 맞는 코드 블록을 자동 생성한다.

**④ 입출력 예시 제시**

* 입력값과 기대 출력 예를 포함하면 AI는 이를 충족시키려 노력하며 정확도가 높아진다.

**⑤ 피드백 기반 반복 개선**

* 첫 결과가 기대에 못 미쳐도 “filter 대신 map 사용해주세요”처럼 피드백을 주면 AI가 바로 반영하며 진화한다.

### 실패하는 프롬프트의 7가지 패턴 (Anti-patterns)

**① 모호한 요청**

* “이 코드 왜 안돼요?” 같은 질문은 의미 없는 일반론적 답변만 이끌어낸다. 오류 메시지, 코드, 기대 결과를 포함시킨다.

**② 요구 과잉**

* “앱 전체 생성 + 인증 기능 추가 + 배포 스크립트 포함” 등 복합 요청은 누락이나 혼란을 야기하므로 단계별 분리가 필요하다.

**③ 질문 없음**

* 코드만 던지고 요청이 없으면 AI는 요약을 하거나 무관한 결과를 내놓기 쉬우므로, 질문 목적을 명확히 해야 한다.

**④ 성공 기준 불명확**

* “빠르게 해줘”, “더 좋게 바꿔줘”는 기준이 모호하다. 예: “O (n) 시간 복잡도로 개선”처럼 측정 가능한 기준을 제시해야 한다.

**⑤ AI의 질문 무시**

* AI가 “이게 함수형인가요 클래스형인가요?”라고 묻는다면 그에 답해야 최적화된 출력을 받을 수 있다.

**⑥ 일관성 부족**

* 스타일, 문법, 용어가 계속 바뀌면 AI도 혼란을 겪는다. 하나의 스타일을 유지해야 응답 품질이 향상된다.

**⑦ “위 코드” 같은 모호한 참조**

* 대화가 길어질수록 “위 코드”는 불명확해진다. 가능한 한 코드를 다시 제시하거나 명시적으로 함수명을 언급하도록 한다.