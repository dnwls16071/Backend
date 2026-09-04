### 대규모 프로젝트 컨텍스트 엔지니어링: `.claude/rules/` 조건부 규칙

* 결론부터 말하자면 핵심 원칙을 "구체적으로 짧게" 지정해야 한다.
* [How I Organized My CLAUDE.md in a Monorepo with Too Many Contexts](https://dev.to/anvodev/how-i-organized-my-claudemd-in-a-monorepo-with-too-many-contexts-37k7)

#### 전략 1 : 프론트매터 조건으로 자동 로드

* 파일 맨 뒤에 조건을 설정한다. 예를 들어, "이 규칙은 이런 파일에만 적용해줘"와 같다.
* 단순히 쪼개는 것이 아니라 불필요한 규칙이 Claude의 주의를 뺏지 않게 하는 것이다. 중요한 규칙에 온전히 집중한다.
* 프로젝트의 `.claude/rules/` 디렉터리에 마크다운 파일을 배치한다. 각 파일은 `testing.md` 또는 `api-design.md`와 같은 설명적인 파일명으로 한 가지 주제를 다루어야 한다.
* 모든 `.md` 파일은 재귀적으로 발견되므로 `frontend/` 또는 `backend/`와 같은 하위 디렉토리로 규칙을 구성할 수 있다.

```
your-project/
├── .claude/
│   ├── CLAUDE.md           # 주 프로젝트 지침
│   └── rules/
│       ├── code-style.md   # 코드 스타일 가이드라인
│       ├── testing.md      # 테스트 규칙
│       └── security.md     # 보안 요구사항
```
* `paths` frontmatter가 없는 규칙은 `.claude/CLAUDE.md`와 동일한 우선순위로 시작 시 로드된다.
* 경로별 규칙 지정도 가능하다. 규칙은 `paths` 필드가 있는 YAML frontmatter를 사용하여 특정 파일로 범위를 지정할 수 있다.
* 이러한 조건부 규칙은 Claude가 지정된 패턴과 일치하는 파일로 작업할 때만 적용된다.

```
---
paths:
  - "src/api/**/*.ts"
---

# API 개발 규칙

- 모든 API 엔드포인트는 입력 검증을 포함해야 합니다
- 표준 오류 응답 형식을 사용합니다
- OpenAPI 문서 주석을 포함합니다
```
* `paths` 필드가 없는 규칙은 무조건 로드되며 모든 파일에 적용된다.
* 경로 범위 규칙은 모든 도구 사용 시가 아니라 Claude가 패턴과 일치하는 파일을 읽을 때 트리거된다.
* v2.1.198 이상에서는 예를 들어 프로젝트 디렉토리에 대한 심볼릭 링크된 경로를 통해 Claude가 파일에 도달할 때도 일치가 작동한다.

#### 전략 2 : 하위 CLAUDE.md

* 결론부터 말하자면 건드리기 전까지는 읽지 않는다는 것으로 컨텍스트를 아끼는 핵심 전략이 된다.

| `.claude/rules/` | 하위 CLAUDE.md(부서별 메뉴얼) |
|:---:|:---:|
| 특정 파일 종류 작업 시 | 특정 폴더에 접근 시 |
| 테스트 규칙, CSS 규칙, 보안 규칙 | API 폴더, 결제 기능 폴더, DB 폴더 |
| 한 곳에 모아서 관리 | 각 폴더에 나눠서 관리 |
| 파일 종류로 나눌 때 | 기능 / 폴더별로 나눌 때 |

