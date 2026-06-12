# Partition Backend

공유 주거(하우스 파티션) 관리 앱 **Partition**의 백엔드 서버입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.6 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL (AWS RDS) |
| Auth | JWT + Kakao OAuth |
| Push | Firebase FCM |
| Infra | AWS EC2 (t3.micro), Docker Compose, nginx |
| Monitoring | Prometheus, Grafana |

## 프로젝트 구조

```
src/main/java/com/partition/
├── domain/
│   ├── alarm/          # 알림
│   ├── auth/           # 카카오 소셜 로그인 / JWT 인증
│   ├── calender/       # 월간·일간 캘린더
│   ├── chore/          # 집안일 등록·배정·완료
│   ├── homeshare/      # 귀가 위치 공유
│   ├── household/      # 하우스(그룹) 관리
│   ├── preference/     # 사용자 선호 설정
│   ├── report/         # 집안일 리포트
│   ├── reservation/    # 공용 공간 예약
│   ├── schedule/       # 개인 일정
│   ├── supply/         # 생활용품 구매·정산
│   ├── user/           # 사용자 프로필
│   └── utilitybill/    # 공과금 관리
├── entity/             # JPA 엔티티
└── global/
    ├── config/         # Security, JWT, Swagger, Firebase
    └── exception/      # GlobalExceptionHandler
```

## 주요 기능

- **카카오 소셜 로그인** - JWT 액세스/리프레시 토큰 발급
- **집안일 관리** - 등록, 자동 배정, 완료 처리, FCM 알림
- **귀가 공유** - 위치 기반 귀가 감지 및 룸메이트 알림
- **공과금 관리** - 항목별 납부 내역 기록 및 정산
- **생활용품 관리** - 구매 요청 및 비용 정산
- **공용 공간 예약** - 예약 등록 및 충돌 방지
- **월간 리포트** - 집안일 기여도 통계

## API 문서

서버 실행 후 Swagger UI에서 확인할 수 있습니다.

```
https://api.partition.site/swagger-ui/index.html
```

## 로컬 실행

**사전 요구사항:** Java 21, Docker

**1. 환경변수 설정**

```bash
cp .env.example .env
# .env 파일에 DB, JWT, Kakao, Firebase 정보 입력
```

**2. 빌드 및 실행**

```bash
./gradlew bootJar
docker build -t partition-spring .
docker run -p 8080:8080 --env-file .env partition-spring
```

## 배포

GitHub Actions를 통해 `develop` 브랜치에 병합 시 자동 배포됩니다.

```
develop 병합
  → Docker 이미지 빌드 & Docker Hub 푸시
  → EC2 서버에서 docker compose pull & up
```

## 브랜치 전략

| 브랜치 | 용도 |
|--------|------|
| `develop` | 통합 브랜치 (기본 브랜치) |
| `feat/기능명` | 기능 개발 |
| `fix/버그명` | 버그 수정 |
| `chore/작업명` | 설정, 인프라 작업 |

## 모니터링

Prometheus + Grafana로 서버 지표를 모니터링합니다.

```
https://api.partition.site/grafana/
```

주요 모니터링 지표:
- HTTP 요청 응답시간 (p50 / p95 / p99)
- JVM 힙 메모리 사용량
- HikariCP DB 커넥션 풀 상태
- CPU 사용률
