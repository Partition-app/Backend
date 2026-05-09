# Partition Backend - CLAUDE.md

## 프로젝트 개요
Spring Boot 기반 하우스 파티션(공유 주거) 관리 앱 백엔드.
패키지: `com.partition`

---

## 프로젝트 구조

```
src/main/java/com/partition/
├── domain/                  # 도메인별 비즈니스 로직
│   ├── {domain}/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   └── exception/       # 도메인별 ErrorCode enum
│   └── common/
│       └── dto/response/    # ApiResponse<T> 공통 응답
├── entity/                  # JPA 엔티티 (도메인 분리 없이 flat 구조)
│   └── enums/               # 엔티티에서 사용하는 enum
└── global/
    ├── config/              # Security, JWT, Swagger, Firebase 설정
    └── exception/           # GlobalExceptionHandler, CustomException, BaseErrorCode
```

현재 도메인: `alarm`, `auth`, `calender`, `chore`, `common`, `household`,
`preference`, `report`, `reservation`, `schedule`, `supply`, `user`, `utilitybill`

---

## API 응답 형식

모든 API는 `ApiResponse<T>`를 사용한다. (`domain/common/dto/response/ApiResponse.java`)

### 성공 응답
```java
// 데이터 있을 때
return ResponseEntity.ok(ApiResponse.onSuccess("200", "메시지", result));

// 데이터 없을 때 (생성/삭제)
return ResponseEntity.ok(ApiResponse.onSuccess("200", "메시지"));
```

```json
{
  "isSuccess": true,
  "code": "200",
  "message": "집안일 완료 처리 성공",
  "result": { ... }
}
```

### 실패 응답
```java
throw new CustomException(SomeDomainErrorCode.ERROR_NAME);
// GlobalExceptionHandler가 자동으로 처리
```

```json
{
  "isSuccess": false,
  "code": "CHORE_4001",
  "message": "에러 메시지",
  "error": null
}
```

---

## 예외 처리 패턴

### 1. 도메인별 ErrorCode enum 정의
```java
// domain/{domain}/exception/{Domain}ErrorCode.java
@Getter
@RequiredArgsConstructor
public enum ChoreErrorCode implements BaseErrorCode {
    CHORE_NOT_FOUND(404, "집안일을 찾을 수 없습니다."),
    NOT_ASSIGNED_USER(403, "해당 집안일에 배정된 사용자가 아닙니다.");

    private final int status;
    private final String message;
}
```

### 2. 예외 throw
```java
throw new CustomException(ChoreErrorCode.CHORE_NOT_FOUND);
```

### 3. BaseErrorCode 인터페이스 구현 필수
```java
public interface BaseErrorCode {
    int getStatus();
    String getMessage();
    String name();
}
```

---

## 컨트롤러 패턴

```java
@RestController
@RequestMapping("/api/{domain}s")
@RequiredArgsConstructor
public class {Domain}Controller {

    @Operation(summary = "요약", description = "상세 설명")
    @{HttpMethod}Mapping("/{path}")
    public ResponseEntity<ApiResponse<{ResponseDto}>> methodName(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid {RequestDto} request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        // ...
        return ResponseEntity.ok(ApiResponse.onSuccess("200", "메시지", result));
    }
}
```

- `@AuthenticationPrincipal CustomUserDetails`로 인증 사용자 추출
- `userId = Long.parseLong(userDetails.getUsername())`으로 userId 획득
- Swagger `@Operation` 필수

---

## Git 브랜치 전략

- **메인 브랜치**: `develop` (통합 브랜치)
- **기능 브랜치**: `feat/기능명` (예: `feat/reservation-state-toggle`)
- 모든 작업은 `develop` 기반으로 브랜치 생성 후 PR

### 브랜치 생성
```bash
git checkout develop
git pull origin develop
git checkout -b feat/기능명
```

### rebase (PR 전, 중간)
```bash
git fetch origin
git rebase origin/develop
```