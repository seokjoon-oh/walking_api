# Walking Trail API - Project Report

## 1. 프로젝트 목표

산책로 공공데이터를 단순 조회하는 수준을 넘어 사용자가 거리·난이도·편의시설·태그를 기준으로 산책로를 검색하고, 즐겨찾기와 후기/평점을 남길 수 있는 REST API 서버를 구현했습니다.

기존 `walk1`, `walk2`, `walk3` 테이블은 유지하고 사용자 기능을 위해 `favorites`, `reviews`, `walk_tags` 세 테이블을 추가했습니다.

## 2. 개발 환경

- Language: Java 17
- Framework: Spring Boot
- DB Access: Spring JDBC / `DataSource` / `PreparedStatement`
- Database: MySQL
- Cloud DB: Aiven MySQL
- Deployment: Railway
- API Test: Browser, Postman
- IDE: IntelliJ IDEA

## 3. 시스템 구성

```text
사용자 요청
   ↓
Spring Boot Controller
   ↓
DataSource
   ↓
MySQL
   ↓
SQL 실행
   ↓
ResultSet 처리
   ↓
DTO 또는 Map 생성
   ↓
JSON 응답
```

Controller에서 JDBC를 직접 사용해 SQL을 실행했고, DB 리소스는 try-with-resources로 관리했습니다. 파라미터가 필요한 쿼리는 `PreparedStatement`를 사용했습니다.

## 4. 데이터베이스 설계

### 기존 테이블

`walk1`, `walk2`, `walk3`를 `id` 기준으로 JOIN해 하나의 산책로 응답을 구성합니다.

- `walk1`: 산책로 기본 정보
- `walk2`: 거리, 소요시간, 난이도, 편의시설
- `walk3`: 주소 및 좌표

### 추가 테이블

#### favorites

사용자별 즐겨찾기를 저장합니다.

- `favorite_id`
- `user_id`
- `walk_id`
- `created_at`
- `UNIQUE(user_id, walk_id)`

#### reviews

사용자 후기와 평점을 저장합니다.

- `review_id`
- `walk_id`
- `user_id`
- `rating`
- `content`
- `created_at`
- `UNIQUE(user_id, walk_id)`
- `CHECK (rating BETWEEN 1 AND 5)`

#### walk_tags

산책로별 검색 태그를 저장합니다.

- `tag_id`
- `walk_id`
- `tag_name`
- `UNIQUE(walk_id, tag_name)`

## 5. Trigger

`trg_review_default_content` Trigger를 작성했습니다. 후기 INSERT 전에 `content`가 `NULL` 또는 빈 문자열인지 확인하고, 비어 있으면 `후기 미작성`으로 변경합니다.

```text
Review INSERT
   ↓
BEFORE INSERT Trigger
   ↓
content NULL / 빈 문자열 확인
   ↓
"후기 미작성"으로 변경
   ↓
reviews 저장
```

## 6. API 구성

총 15개의 API를 구현했습니다.

### WalkingController - 7개

| Method | Endpoint | 내용 |
| --- | --- | --- |
| GET | `/api/walks` | 산책로 목록 |
| GET | `/api/walks/{walkId}` | 상세 조회 |
| GET | `/api/walks/short?maxKm=5` | 거리 필터 |
| GET | `/api/walks/difficulty?level=쉬움` | 난이도 필터 |
| GET | `/api/walks/facility?toilet=yes` | 화장실 여부 |
| GET | `/api/walks/water?water=yes` | 식수대 여부 |
| GET | `/api/walks/tags?tag=초보추천` | 태그 검색 |

### FavoriteController - 4개

| Method | Endpoint | 내용 |
| --- | --- | --- |
| POST | `/api/favorites` | 즐겨찾기 추가 |
| GET | `/api/favorites/user/{userId}` | 사용자별 즐겨찾기 |
| GET | `/api/favorites/popular` | 즐겨찾기 수 기준 인기 산책로 |
| DELETE | `/api/favorites/{favoriteId}` | 즐겨찾기 삭제 |

### ReviewController - 4개

| Method | Endpoint | 내용 |
| --- | --- | --- |
| POST | `/api/reviews` | 후기 작성 |
| GET | `/api/reviews/walk/{walkId}` | 산책로별 후기 |
| GET | `/api/reviews/top-rated` | 평균 평점순 조회 |
| DELETE | `/api/reviews/{reviewId}` | 후기 삭제 |

## 7. SQL 활용

산책로 정보 조회에서는 기본적으로 세 테이블을 JOIN합니다.

```sql
FROM walk1 w1
JOIN walk2 w2 ON w1.id = w2.id
JOIN walk3 w3 ON w1.id = w3.id
```

후기 평점 순위는 `AVG`, 후기 수는 `COUNT`, 산책로 단위 집계는 `GROUP BY`를 이용합니다. 즐겨찾기 인기 순위도 `COUNT`와 `GROUP BY`를 사용합니다.

테이블 구조와 재현용 샘플 데이터는 `database/walking.sql`에 포함했습니다.

## 8. 테스트 및 배포

GET 요청은 브라우저에서도 확인했고, POST/DELETE 요청은 Postman을 이용해 테스트했습니다.

프로젝트 당시 Aiven MySQL을 외부 DB로 사용하고 Railway에 Spring Boot 서버를 배포했습니다. 설명 영상에서는 로컬 API 호출과 Railway 배포 URL에서 JSON 응답이 반환되는 과정을 확인했습니다.

## 9. 문제 해결

### DB 접속 정보가 GitHub Push Protection에 감지됨

초기에는 DB 접속 정보를 `application.properties`에 직접 작성했지만 Push Protection에 의해 업로드가 차단되었습니다. 이후 다음과 같이 환경변수로 분리했습니다.

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

### Aiven URI와 JDBC URL 형식 차이

Aiven의 `mysql://` 형식 URI를 그대로 사용하면 JDBC Driver가 인식하지 못했습니다. Spring Boot에서는 `jdbc:mysql://...` 형식으로 변경해 연결했습니다.

### 로컬 환경변수 누락

Railway에는 Variables가 설정되어 있었지만 로컬 IntelliJ에서는 환경변수가 없어 연결 오류가 발생했습니다. Run/Debug Configurations의 Environment variables에 DB 접속 정보를 등록해 해결했습니다.

### HTTP Method 혼동

`/api/reviews`는 POST 전용인데 브라우저 주소창에서 GET으로 요청하면서 `405 Method Not Allowed`가 발생했습니다. 후기 조회는 `/api/reviews/walk/{walkId}` 또는 `/api/reviews/top-rated`를 사용하도록 구분했습니다.

## 10. 구현 확인 화면

### 산책로 목록

![산책로 목록](images/walk-list.png)

### 태그 필터

![태그 필터](images/tag-filter.png)

### 즐겨찾기 등록

![즐겨찾기 등록](images/favorite-post.png)

### 후기 등록

![후기 등록](images/review-post.png)

### Railway 배포 결과

![Railway 배포 결과](images/railway-favorites.png)
