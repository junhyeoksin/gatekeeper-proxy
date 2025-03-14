# Gatekeeper Middle Proxy

<!-- ![Gatekeeper 아키텍처](https://via.placeholder.com/800x400?text=Gatekeeper+Architecture) -->

## 프로젝트 개요

Gatekeeper Middle Proxy는 마이크로서비스 아키텍처에서 API 게이트웨이 역할을 수행하는 프록시 서버입니다. Spring Cloud Gateway를 기반으로 하며, Keycloak을 통한 인증/인가 시스템을 통합하여 안전한 API 접근을 제공합니다.

## 프로젝트 구조

```
gatekeeper-proxy/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── gatekeeper/
│   │   │           └── proxy/
│   │   │               ├── GatekeeperProxyApplication.java
│   │   │               ├── config/
│   │   │               │   ├── KeycloakConfig.java
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── KeycloakUserController.java
│   │   │               │   └── UserController.java
│   │   │               └── security/
│   │   │                   └── KeycloakLogoutHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
├── build.gradle
├── settings.gradle
├── Dockerfile
└── README.md  
```
## 주요 기능

* **통합 인증/인가 시스템**
  * Keycloak 기반 인증/인가 시스템
  * OAuth2/OIDC 프로토콜 지원
  * 역할 기반 접근 제어(RBAC)
  * 싱글 사인온(SSO) 지원

* **API 게이트웨이**
  * 마이크로서비스 라우팅 및 로드 밸런싱
  * 경로 기반 필터링 및 요청/응답 변환
  * 회로 차단기(Circuit Breaker) 패턴 구현
  * 요청 속도 제한(Rate Limiting)

* **보안 기능**
  * 토큰 기반 인증
  * 세션 관리 (Redis 기반)
  * 안전한 로그아웃 처리
  * 사용자 정보 관리 API

* **모니터링 및 로깅**
  * 분산 추적 시스템 (Zipkin)
  * 상세 로깅 및 모니터링
  * 실시간 메트릭 수집 및 분석
  * 상태 모니터링 엔드포인트

## 기술 스택

* **프레임워크 및 라이브러리**
  * Spring Boot 3.2.0
  * Spring Cloud Gateway
  * Spring Security
  * Spring WebFlux (리액티브 프로그래밍)
  * Spring Cloud Circuit Breaker (Resilience4j)
  * Spring Boot Actuator

* **인증/인가**
  * Keycloak 23.0.0
  * Spring Security OAuth2
  * JWT/OIDC

* **데이터 관리**
  * Redis (세션 저장소)
  * Spring Session Data Redis

* **모니터링 및 로깅**
  * Micrometer
  * Zipkin
  * Brave (분산 추적)

* **API 문서화**
  * SpringDoc OpenAPI 3.0
  * Swagger UI

* **빌드 및 배포**
  * Gradle 7.x
  * Docker
  * JDK 17

## 시작하기

### 필수 조건

* JDK 17 이상
* Gradle 7.x 이상
* Docker (선택 사항)
* Keycloak 서버
* Redis 서버

### 설치 및 실행

1. 프로젝트 클론
   ```bash
   git clone https://github.com/junhyeoksin/gatekeeper-proxy.git
   ```

2. 애플리케이션 빌드
   ```bash
   ./gradlew clean build
   ```

3. 애플리케이션 실행
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

4. Docker로 실행 (선택 사항)
   ```bash
   ./gradlew docker
   docker run -p 8080:8080 gatekeeper/gatekeeper-proxy:0.1.0
   ```

## 설정

### application.yml

기본 설정 파일입니다. 공통 설정을 포함합니다.

### application-dev.yml

개발 환경 설정 파일입니다. 로컬 개발 시 사용됩니다.

## API 문서

API 문서는 Swagger UI를 통해 제공됩니다.
* 접속 URL: http://localhost:8080/swagger-ui.html

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요. 

 