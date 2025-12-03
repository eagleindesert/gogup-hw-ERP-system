# Docker 환경 설정 가이드

## 📁 파일 구조

```
gogup-hw-1/
├── docker-compose.yml                    # 🌐 전체 서비스 통합 실행
├── employee-service/demo/
│   ├── docker-compose.yml                # 단독 실행용
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties        # 로컬 개발 환경
│       └── application-docker.properties # Docker 환경
├── approval-request-service/demo/
│   ├── docker-compose.yml                # 단독 실행용
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties        # 로컬 개발 환경
│       └── application-docker.properties # Docker 환경
└── approval-processing-service/demo/
    ├── docker-compose.yml                # 단독 실행용
    ├── Dockerfile
    └── src/main/resources/
        ├── application.properties        # 로컬 개발 환경
        └── application-docker.properties # Docker 환경
```

## 🔧 설정 분리 원칙

### application.properties (로컬 개발용)
- `localhost` 기반 연결
- 개발/디버깅에 최적화된 설정
- IDE에서 직접 실행 시 사용

### application-docker.properties (Docker용)
- Docker 네트워크 서비스명 기반 연결 (예: `mysql`, `mongodb`)
- 프로덕션에 가까운 설정
- `SPRING_PROFILES_ACTIVE=docker`로 활성화

## 🚀 실행 방법

### 전체 서비스 실행 (권장)
```bash
# 루트 디렉토리에서
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down
```

### 개별 서비스 실행
```bash
# 네트워크 먼저 생성 (한 번만)
docker network create erp-network

# Employee Service만 실행
cd employee-service/demo
docker-compose up -d

# Approval Request Service만 실행
cd approval-request-service/demo
docker-compose up -d

# Approval Processing Service만 실행
cd approval-processing-service/demo
docker-compose up -d
```

## 🌐 서비스 포트

| Service | REST API | gRPC | Database Port |
|---------|----------|------|---------------|
| Employee Service | 8081 | - | 3307 (MySQL) |
| Approval Request Service | 8082 | 9091 | 27018 (MongoDB) |
| Approval Processing Service | 8083 | 9090 | - (In-Memory) |
| Notification Service | 8084 | - | - (예정) |

## 🔗 Docker 네트워크 연결

모든 서비스는 `erp-network` 네트워크에서 통신합니다.

```
┌─────────────────────────────────────────────────────────────┐
│                     erp-network                              │
│                                                              │
│  ┌──────────┐    ┌──────────────────┐    ┌──────────────┐  │
│  │  MySQL   │───▶│ Employee Service │◀───│   Client     │  │
│  │  :3306   │    │ :8081           │    │              │  │
│  └──────────┘    └──────────────────┘    └──────────────┘  │
│                                                              │
│  ┌──────────┐    ┌────────────────────┐                    │
│  │ MongoDB  │───▶│ Approval Request   │◀──────────┐       │
│  │  :27017  │    │ :8082 (REST)       │           │       │
│  └──────────┘    │ :9091 (gRPC)       │           │       │
│                  └────────────────────┘           │       │
│                           │ gRPC                  │       │
│                           ▼                       │       │
│                  ┌────────────────────┐           │       │
│                  │ Approval Processing │──────────┘       │
│                  │ :8083 (REST)        │ (Pull on startup)│
│                  │ :9090 (gRPC)        │                  │
│                  └────────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
```

## ⚙️ 환경별 설정 비교

### Employee Service

| 설정 | 로컬 (application.properties) | Docker (application-docker.properties) |
|------|-------------------------------|---------------------------------------|
| MySQL URL | `localhost:3306` | `mysql:3306` |
| 사용자 | root | root |

### Approval Request Service

| 설정 | 로컬 | Docker |
|------|------|--------|
| MongoDB | `localhost:27017` | `mongodb:27017` |
| Employee Service | `http://localhost:8081` | `http://employee-service:8081` |
| Processing gRPC | `localhost:9090` | `approval-processing-service:9090` |

### Approval Processing Service

| 설정 | 로컬 | Docker |
|------|------|--------|
| Request gRPC | `localhost:9091` | `approval-request-service:9091` |

## 💡 팁

1. **Profile 변경**: `SPRING_PROFILES_ACTIVE` 환경 변수만 변경하면 됨
2. **설정 오버라이드**: 필요시 docker-compose에서 환경 변수로 개별 설정 덮어쓰기 가능
3. **디버깅**: 로컬 개발 시 IDE의 Run Configuration에서 Profile을 지정하지 않으면 기본 설정 사용
