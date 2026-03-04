# TODO LIST (제출 기준)

## 1) 과제 분석
- [x] 과제 원문 요구사항 정리
- [x] UI 규칙(타입별 줄수/가격/찜 아이콘) 체크포인트 정의
- [x] API 스펙(`sections`, `section/products`) 정리
- [x] 완료 체크 기준과 검증 방법 수립

## 2) 아키텍처/모듈
- [x] 멀티 모듈 구조 확정 (`app`, `core`, `feature`, `mockserver`)
- [x] domain 계약(Repository, UseCase, Policy) 정의
- [x] data 구현(DTO, API, RepositoryImpl, local store) 완료
- [x] presentation(ViewModel + UiState + Compose Screen) 완료
- [x] 의존성 방향 점검 (presentation -> domain -> data)

## 3) 기능 구현
- [x] 섹션 목록 로드 및 페이지네이션
- [x] 섹션 타입별 UI 렌더링 (vertical/horizontal/grid)
- [x] 상품 가격 표시 규칙 반영 (할인율, 할인가, 원가 취소선)
- [x] 제목 줄수/ellipsis 규칙 반영
- [x] 그리드 3x2(최대 6개) 표시
- [x] 찜 토글 및 전역 동기화
- [x] 찜 상태 로컬 저장(DataStore) 및 복원
- [x] pull-to-refresh 동작
- [x] 에러/재시도 처리

## 4) 테스트/품질
- [x] 도메인 테스트 (UseCase, Policy)
- [x] 데이터 매핑 테스트 (DTO -> Model)
- [x] ViewModel 테스트 (이벤트/상태 전이)
- [x] 포맷터 테스트 (가격 문자열)
- [x] 코드리뷰 에이전트 반복 점검 (요구사항 누락/회귀 관점)

## 5) 제출 산출물
- [x] README 아키텍처/실행법 정리
- [x] AI Assist 프롬프트 기록 문서 작성 (`docs/ai-prompts.md`)
- [x] 결과물 검증 (테스트/빌드 성공)
- [x] 제출용 완료 문구 준비

## 6) 컨텍스트 핸드오프 규칙
- [x] 컨텍스트 부족 시 즉시 이 파일 체크 상태 갱신
- [x] 다음 컨텍스트에서 시작할 작업 1개 명시
- [x] 핸드오프 프롬프트 템플릿 유지

### 핸드오프 템플릿
```text
[Handoff]
- 완료: ...
- 진행중: ...
- 다음 1스텝: ...
- 실행 커맨드: ...
```
