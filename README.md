# Quest (Kurly Android 과제)

Kurly 과제 사양을 구현한 Android 멀티 모듈 앱입니다.

## 모듈 구조
- `:app`
  - 앱 진입점( `QuestApplication`, `MainActivity` )
  - Compose 화면을 실행하는 호스트 앱
- `:core:model`
  - 공통 모델 (`Product`, `Section`, `SectionsPage`, `SectionType`)
- `:core:domain`
  - 도메인 인터페이스 (`MainRepository`)
  - UseCase 및 정책, 도메인 데이터 전환 모델 (`LoadSectionsPageUseCase`, `ToggleFavoriteUseCase`, `FavoriteStore`, `SectionProductsPage`, 할인 정책)
- `:core:data`
  - 네트워크/로컬 데이터 계층 (`KurlyApi`, DTO, `MainRepositoryImpl`, `DataStoreFavoriteStore`, Hilt 바인딩)
- `:feature:main`
  - 메인 화면 UI(`MainScreen`), 상태관리(`MainViewModel`)
  - UI 모델/상태 및 화면 라우팅
  - 유즈케이스는 `:core:domain`에서 주입받아 사용
- `:mockserver`
  - Mock 인터셉터 + 자산(JSON) 기반의 API 시뮬레이션

## 아스키 아키텍처 (ASCII)

```text
               +-------------------------+
               |         :app            |
               | MainActivity / Hilt APP  |
               +-------------------------+
                        |
                        v
               +-------------------------+
               |      :feature:main      |
               |  MainScreen / MainVM     |
               +-------------------------+
                  |                 |
                  |                 v
                  |            +---------------------+
                  |            |      :core:model     |
                  |            |  Product / Section.. |
                  |            +---------------------+
                  v
               +-------------------------+
               |      :core:data         |
               |  KurlyApi / DTO / Repo  |
               +-------------------------+
                  |            |
                  v            v
            +-------------+   +------------------+
            | :core:domain|   |   :mockserver    |
            | MainRepository|   | Mock 인터셉터    |
            +-------------+   +------------------+
                                  |
                                  v
                             assets/*.json
```

```text
요청 흐름
사용자 입력 -> MainScreen -> MainViewModel -> UseCase -> MainRepository
                                      -> FavoriteStore(DataStore)

MainRepository -> KurlyApi(OKHttp+Retrofit) -> /sections | /section/products
           -> DTO 파싱/매핑 -> Section/Page 모델 -> UI 상태 반영
```

## 핵심 기능
- 섹션 목록 로딩 + 페이지네이션
  - `/sections` 응답의 `next_page` 기준으로 다음 페이지 조회
- 섹션 타입별 화면 처리
  - `vertical`, `horizontal`, `grid`
- 상품 조회
  - 섹션별 `/section/products` 호출
- UI 규칙
  - 수평/그리드: 제목 2줄, ellipsis
  - 수직: 제목 1줄, ellipsis
  - 할인가 존재 시 할인률/할인가 표시 + 원가 취소선
  - 그리드는 3열, 최대 2줄(총 6개) 노출
- 로컬 상태
  - 상품 좋아요 토글 및 `productId` 기준 글로벌 반영
  - `DataStore`에 저장해 앱 재시작 후에도 유지
- 에러/새로고침
  - 초기 로드/페이징/재시도
  - Pull-to-refresh 지원

## 테스트
- `MainScreen` 상태 흐름/로직: `feature:main` 단위 테스트
- 할인 계산/유즈케이스 로직 테스트
- DTO 매핑 단위 테스트(`core:data`)

## 실행/빌드
- debug APK 생성
```bash
./gradlew :app:assembleDebug
```

- 단위 테스트
```bash
./gradlew :feature:main:testDebugUnitTest :core:data:testDebugUnitTest :core:domain:testDebugUnitTest
```

## 참고
- 네트워크 베이스 URL: `https://kurly.com/`
- Mock 응답은 `:mockserver`에서 가로챈 뒤 JSON 자산으로 반환되며, 약 1~2초 응답 지연이 포함됩니다.
