### 자주 쓰이는 서브에이전트 패턴

* Claude Code 팀은 `build-validator`, `code-architect`, `code-simplifier`, `oncall-guide`, `verify-app`를 체크인한다.
  * `security-reviewer` : injection, auth, secrets, insecure deserialization 점검한다.
  * `test-writer` : 테스트 생성, code-reviewer와 루프를 구성한다.
  * `debugger` : 실패 테스트를 root cause까지 추적한다.
  * `performance-auditor` : flow와 query profiling을 수행한다.
  * `migration-writer` : 프로젝트 관례에 맞는 DB migration 생성한다.
  * `release-notes-writer` : commit history에서 changelog 작성한다.

### Claude Code를 검증 가능한 에이전트로 다루기

* Claude Code의 생산성 차이는 단순한 프롬프트보다 메모리, 커스텀 명령, 병렬 세션, 프로젝트 설정을 어떻게 누적하느냐에서 벌어진다.
* 핵심 원칙은 Claude가 자기 결과를 검증할 수 있게 만드는 것이며, Boris Cherny와 Anthropic 팀은 이 방식만으로도 품질이 2~3배 개선된다고 본다.
* 작업 흐름은 탐색 → 계획 → 구현 순서가 적합하다.
  * 계획 모드 : 읽기 전용 탐색
  * 파일을 읽고 흐름과 데이터 모델에 파악한 뒤 계획을 세우고 실행하는 방식이 권장
  * 여러 파일을 건드리는 작업에는 계획 모드가 유용하고 작은 수정에는 생략 가능
* 계획 모드는 구현 전 검토 가능한 설계 문서로 다룰 수 있다.
  * Claude가 계획을 작성하고 새로운 세션의 두 번째 Claude가 편향 없는 스태프 엔지니어링처럼 검토하게 만들 수 있다.
  * 구현이 어긋나면 계획 모드로 돌아가 검증 단계까지 포함해 다시 계획하는 흐름이 적합하다.
* 모호한 지시보다 정확한 참조가 효과적이다.