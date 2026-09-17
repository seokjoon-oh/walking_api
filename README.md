# Walking Trail REST API

공공 산책로 데이터를 기반으로 **산책로 조회·조건 검색·태그 검색·즐겨찾기·후기/평점 기능**을 구현한 Spring Boot REST API 프로젝트입니다.

기존 `walk1`, `walk2`, `walk3` 데이터를 `id` 기준으로 JOIN해 하나의 산책로 정보로 제공하고, 사용자 기능을 위해 `favorites`, `reviews`, `walk_tags` 테이블을 추가했습니다. JDBC `PreparedStatement`를 사용해 SQL을 실행하며 결과는 DTO 또는 `Map`으로 가공해 JSON으로 반환합니다.

## 핵심 구현

- REST API **15개** 구현
  - 산책로 조회/검색 7개
  - 즐겨찾기 4개
  - 후기/평점 4개
- `walk1` + `walk2` + `walk3` **3개 테이블 JOIN**
- 거리, 난이도, 화장실, 식수대, 태그 기준 검색
- `AVG`, `COUNT`, `GROUP BY`를 활용한 평점순·즐겨찾기순 조회
- `UNIQUE` 제약조건으로 중복 즐겨찾기/중복 후기 방지
- `CHECK (rating BETWEEN 1 AND 5)`로 평점 범위 제한
- Trigger를 사용해 빈 후기 내용을 `후기 미작성`으로 자동 처리
- DB 접속 정보는 환경변수(`DB_URL`, `DB_USER`, `DB_PASSWORD`)로 분리
- 프로젝트 당시 Aiven MySQL과 Railway를 이용해 외부 배포 및 API 호출 검증

## 기술 스택

`Java 17` · `Spring Boot` · `Spring JDBC` · `MySQL` · `Maven` · `Postman` · `Aiven MySQL` · `Railway`

## 처리 흐름

```text
Client (Browser / Postman)
        ↓
Spring Boot Controller
        ↓
DataSource / JDBC
        ↓
PreparedStatement
        ↓
MySQL
        ↓
ResultSet
        ↓
DTO / Map
        ↓
JSON Response
```

## API

### 산책로

| Method | Endpoint | 기능 |
| --- | --- | --- |
| `GET` | `/api/walks` | 산책로 목록 조회 (현재 코드 기준 최대 50건) |
| `GET` | `/api/walks/{walkId}` | 산책로 상세 조회 |
| `GET` | `/api/walks/short?maxKm=5` | 최대 거리 기준 검색 |
| `GET` | `/api/walks/difficulty?level=쉬움` | 난이도 기준 검색 |
| `GET` | `/api/walks/facility?toilet=yes` | 화장실 여부 기준 검색 |
| `GET` | `/api/walks/water?water=yes` | 식수대 여부 기준 검색 |
| `GET` | `/api/walks/tags?tag=초보추천` | 태그 기준 검색 |

### 즐겨찾기

| Method | Endpoint | 기능 |
| --- | --- | --- |
| `POST` | `/api/favorites` | 즐겨찾기 추가 |
| `GET` | `/api/favorites/user/{userId}` | 사용자별 즐겨찾기 조회 |
| `GET` | `/api/favorites/popular` | 즐겨찾기 수가 많은 산책로 조회 |
| `DELETE` | `/api/favorites/{favoriteId}` | 즐겨찾기 삭제 |

요청 예시:

```json
{
  "userId": "user01",
  "walkId": "K001"
}
```

### 후기 / 평점

| Method | Endpoint | 기능 |
| --- | --- | --- |
| `POST` | `/api/reviews` | 후기 작성 |
| `GET` | `/api/reviews/walk/{walkId}` | 산책로별 후기 조회 |
| `GET` | `/api/reviews/top-rated` | 평균 평점이 높은 산책로 조회 |
| `DELETE` | `/api/reviews/{reviewId}` | 후기 삭제 |

요청 예시:

```json
{
  "walkId": "K001",
  "userId": "user01",
  "rating": 5,
  "content": "산책하기 좋았습니다."
}
```

## Database

기존 산책로 데이터는 세 테이블로 분리되어 있습니다.

| Table | 역할 |
| --- | --- |
| `walk1` | 산책로명, 코스명, 설명, 지역 등 기본 정보 |
| `walk2` | 거리, 소요시간, 난이도, 식수대·화장실 여부 |
| `walk3` | 주소, 위도, 경도 |

추가 기능을 위해 아래 테이블을 설계했습니다.

| Table | 역할 | 주요 제약 |
| --- | --- | --- |
| `favorites` | 사용자별 즐겨찾기 | `UNIQUE(user_id, walk_id)` |
| `reviews` | 후기 및 평점 | `UNIQUE(user_id, walk_id)`, 평점 1~5 `CHECK` |
| `walk_tags` | 산책로별 태그 | `UNIQUE(walk_id, tag_name)` |

`reviews`에는 `trg_review_default_content` Trigger를 적용했습니다. 후기 등록 시 `content`가 `NULL` 또는 빈 문자열이면 `후기 미작성`으로 저장됩니다.

테이블 구조와 재현용 샘플 데이터는 [`database/walking.sql`](database/walking.sql)에서 확인할 수 있습니다.

## 구현 화면

### 산책로 목록 조회

![산책로 목록 조회](docs/images/walk-list.png)

### 태그 검색

![태그 검색](docs/images/tag-filter.png)

### 즐겨찾기 POST 테스트

![즐겨찾기 추가](docs/images/favorite-post.png)

### 후기 POST 테스트

![후기 등록](docs/images/review-post.png)

### Railway 배포 환경에서 즐겨찾기 조회

![Railway 배포 결과](docs/images/railway-favorites.png)

## 프로젝트 구조

```text
walking_api/
├── database/
│   └── walking.sql
├── docs/
│   ├── project-report.md
│   └── images/
│       ├── favorite-post.png
│       ├── railway-favorites.png
│       ├── review-post.png
│       ├── tag-filter.png
│       └── walk-list.png
├── src/
│   ├── main/java/com/example/walkingservice/
│   │   ├── controller/
│   │   │   ├── FavoriteController.java
│   │   │   ├── ReviewController.java
│   │   │   └── WalkingController.java
│   │   └── dto/
│   │       ├── FavoriteDto.java
│   │       ├── ReviewDto.java
│   │       └── WalkingDto.java
│   └── main/resources/
│       └── application.properties
├── pom.xml
├── mvnw
└── README.md
```

## 실행 방법

### 1. MySQL 준비

MySQL에 사용할 데이터베이스를 만든 뒤 `database/walking.sql`을 실행합니다.

### 2. 환경변수 설정

현재 `application.properties`는 DB 접속 정보를 코드에 직접 저장하지 않고 환경변수로 받습니다.

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
server.port=${PORT:8080}
```

로컬 실행 예시:

```text
DB_URL=jdbc:mysql://localhost:3306/walking?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USER=root
DB_PASSWORD=<your-password>
```

### 3. 서버 실행

macOS / Linux:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

기본 포트는 `8080`입니다.

## 배포 및 테스트

프로젝트에서는 브라우저와 Postman으로 GET/POST/DELETE 요청을 테스트했습니다. 이후 Aiven MySQL을 연결하고 Railway에 배포해 외부 환경에서도 API 응답을 확인했습니다.

프로젝트 보고서와 설명 영상에서 확인한 주요 테스트 흐름은 산책로 목록/조건 검색 → 즐겨찾기 추가·조회 → 후기 등록·조회 → 평균 평점순 조회 순서입니다.

당시 사용한 Railway 주소는 아래와 같습니다. 서비스 상태에 따라 현재는 접속되지 않을 수 있습니다.

```text
https://walkingapi-production-dd9a.up.railway.app
```

## 문제 해결 경험

프로젝트 진행 중 실제 배포 환경에서 다음 문제를 확인하고 해결했습니다.

- GitHub Push Protection이 DB 비밀번호가 포함된 커밋을 차단 → DB 접속 정보를 환경변수로 분리
- Aiven Service URI의 `mysql://` 형식을 Spring JDBC가 인식하지 못함 → `jdbc:mysql://...` 형식으로 변경
- Railway에서는 동작하지만 로컬 IDE에서 환경변수가 없어 DB 연결 실패 → Run/Debug 환경변수 설정
- 브라우저에서 POST 전용 `/api/reviews`에 GET으로 접근해 `405 Method Not Allowed` 발생 → HTTP Method별 API 사용 방식 확인

현재 저장소의 `application.properties`에는 실제 DB 비밀번호를 포함하지 않습니다.

더 자세한 구현 내용은 [`docs/project-report.md`](docs/project-report.md)에서 확인할 수 있습니다.
