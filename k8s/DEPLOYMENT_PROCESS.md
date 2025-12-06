# Kubernetes 배포 구조

```
k8s/
├── README.md                    # 상세 배포 가이드
├── DEPLOYMENT_PROCESS.md        # 이 파일 (배포 프로세스 요약)
├── deploy.sh                    # Linux/Mac 배포 스크립트
├── deploy.ps1                   # Windows PowerShell 배포 스크립트
├── build-images.sh              # Linux/Mac 이미지 빌드 스크립트
├── build-images.ps1             # Windows PowerShell 이미지 빌드 스크립트
│
├── base/                        # 기본 설정
│   ├── namespace.yaml          # erp-system 네임스페이스
│   ├── configmap.yaml          # 공통 환경 변수 설정
│   ├── secret.yaml             # 민감 정보 (비밀번호 등)
│   ├── ingress.yaml            # 외부 접근 라우팅
│   └── hpa.yaml                # Auto-scaling 설정
│
├── infra/                       # 인프라 서비스
│   ├── zookeeper.yaml          # Kafka를 위한 Zookeeper
│   ├── kafka.yaml              # 메시지 브로커
│   ├── mysql.yaml              # Employee Service DB
│   └── mongodb.yaml            # Approval Request Service DB
│
└── apps/                        # 애플리케이션 서비스
    ├── employee-service.yaml
    ├── approval-request-service.yaml
    ├── approval-processing-service.yaml
    └── notification-service.yaml
```

## 배포 프로세스 전체 흐름

### Phase 1: 사전 준비
```
1. Kubernetes 클러스터 준비
   - 클라우드 (AKS, EKS, GKE) 또는
   - 온프레미스 (kubeadm) 또는
   - 로컬 (minikube, kind, Docker Desktop)

2. 필수 도구 설치
   - kubectl
   - docker
   - helm (선택사항)

3. Ingress Controller 설치
   - NGINX Ingress Controller 권장
   - 또는 Traefik, HAProxy 등

4. Metrics Server 설치 (HPA용)
```

### Phase 2: 이미지 빌드 및 푸시
```
1. 레지스트리 선택
   Docker Hub     : docker.io/username/
   Azure ACR      : yourregistry.azurecr.io/
   AWS ECR        : 123456789012.dkr.ecr.region.amazonaws.com/
   Google GCR     : gcr.io/project-id/
   Private        : registry.company.com/

2. 레지스트리 로그인
   docker login [registry]

3. 이미지 빌드 및 푸시
   Linux/Mac    : ./k8s/build-images.sh
   Windows      : ./k8s/build-images.ps1

4. 매니페스트 업데이트
   k8s/apps/*.yaml의 image 필드를 실제 레지스트리 경로로 수정
```

### Phase 3: 배포 실행
```
자동 배포:
  Linux/Mac    : ./k8s/deploy.sh
  Windows      : ./k8s/deploy.ps1

또는 수동 배포:

1. 네임스페이스 및 설정
   kubectl apply -f k8s/base/namespace.yaml
   kubectl apply -f k8s/base/configmap.yaml
   kubectl apply -f k8s/base/secret.yaml

2. 인프라 (순서 중요!)
   kubectl apply -f k8s/infra/zookeeper.yaml
   [대기: Pod Ready]
   
   kubectl apply -f k8s/infra/kafka.yaml
   [대기: Pod Ready]
   
   kubectl apply -f k8s/infra/mysql.yaml
   kubectl apply -f k8s/infra/mongodb.yaml
   [대기: Pods Ready]

3. 애플리케이션
   kubectl apply -f k8s/apps/employee-service.yaml
   kubectl apply -f k8s/apps/approval-request-service.yaml
   kubectl apply -f k8s/apps/approval-processing-service.yaml
   kubectl apply -f k8s/apps/notification-service.yaml
   [대기: Pods Ready]

4. Ingress & HPA
   kubectl apply -f k8s/base/ingress.yaml
   kubectl apply -f k8s/base/hpa.yaml
```

### Phase 4: 검증
```
1. Pod 상태 확인
   kubectl get pods -n erp-system

2. 서비스 확인
   kubectl get svc -n erp-system

3. Ingress 확인
   kubectl get ingress -n erp-system
   kubectl describe ingress erp-ingress -n erp-system

4. 로그 확인
   kubectl logs -f deployment/employee-service -n erp-system
   kubectl logs -f deployment/approval-request-service -n erp-system
   kubectl logs -f deployment/approval-processing-service -n erp-system
   kubectl logs -f deployment/notification-service -n erp-system

5. Health Check
   kubectl port-forward -n erp-system svc/employee-service 8081:8081
   curl http://localhost:8081/actuator/health
```

## 서비스별 의존성

```
┌─────────────────────────────────────────────────────────────┐
│                        Ingress                               │
│  (외부 트래픽 → 내부 서비스 라우팅)                          │
└─────────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┬─────────────┐
        │                 │                 │             │
        ▼                 ▼                 ▼             ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Employee    │  │  Approval    │  │  Approval    │  │ Notification │
│  Service     │  │  Request     │  │  Processing  │  │  Service     │
│              │  │  Service     │  │  Service     │  │              │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────────────┘
       │                 │                 │
       ▼                 │                 │
┌──────────────┐         │                 │
│    MySQL     │         │                 │
│  (직원 DB)   │         │                 │
└──────────────┘         │                 │
                         ▼                 ▼
                  ┌──────────────┐  ┌──────────────┐
                  │   MongoDB    │  │    Kafka     │
                  │  (결재 DB)   │  │ (메시지 큐)  │
                  └──────────────┘  └──────┬───────┘
                                           │
                                           ▼
                                    ┌──────────────┐
                                    │  Zookeeper   │
                                    │ (Kafka 조정) │
                                    └──────────────┘
```

## 배포 순서와 이유

### 1단계: Zookeeper
- Kafka가 클러스터 조정을 위해 필요
- 먼저 실행되어야 함

### 2단계: Kafka
- Approval Request ↔ Processing 간 통신에 필요
- Zookeeper에 의존

### 3단계: MySQL & MongoDB
- 병렬 배포 가능
- 각각 Employee Service, Approval Request Service에 필요

### 4단계: 애플리케이션 서비스
- 모든 인프라가 준비된 후 배포
- 순서는 중요하지 않음 (서로 독립적)
- 하지만 의존성 순서 권장:
  1. Employee Service (다른 서비스에서 호출됨)
  2. Notification Service (Approval Request에서 호출)
  3. Approval Request Service
  4. Approval Processing Service

### 5단계: Ingress & HPA
- 모든 서비스가 준비된 후
- 외부 접근 및 자동 확장 설정

## 환경별 설정 변경

### 개발 환경 (Dev)
```yaml
# Replica 수 감소
replicas: 1

# 리소스 제한 완화
resources:
  requests:
    memory: "256Mi"
    cpu: "100m"

# 이미지 태그
image: registry/service:dev
```

### 스테이징 환경 (Staging)
```yaml
replicas: 2

resources:
  requests:
    memory: "512Mi"
    cpu: "250m"

image: registry/service:staging
```

### 프로덕션 환경 (Production)
```yaml
# 고가용성
replicas: 3

# 엄격한 리소스 관리
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"

# 안정 버전
image: registry/service:v1.0.0

# PodDisruptionBudget 추가
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: service-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: service
```

## 주요 설정 포인트

### ConfigMap (k8s/base/configmap.yaml)
- 서비스 간 URL
- 데이터베이스 호스트/포트
- Kafka 설정
- 애플리케이션 프로파일

### Secret (k8s/base/secret.yaml)
- 데이터베이스 비밀번호
- API 키
- 인증서

### Ingress (k8s/base/ingress.yaml)
- 도메인 설정: `erp.example.com` → 실제 도메인
- TLS 인증서 설정
- WebSocket 지원 설정 (Notification Service)

### HPA (k8s/base/hpa.yaml)
- CPU 기반 자동 확장 (70%)
- Memory 기반 확장 (80%)
- 최소/최대 Replica 수

## 트러블슈팅 체크리스트

### Pod이 시작하지 않을 때
```bash
# 상세 정보 확인
kubectl describe pod <pod-name> -n erp-system

# 로그 확인
kubectl logs <pod-name> -n erp-system
kubectl logs <pod-name> -n erp-system --previous

# 이벤트 확인
kubectl get events -n erp-system --sort-by='.lastTimestamp'
```

### 일반적인 문제들

1. **ImagePullBackOff**
   - 이미지 경로 확인
   - 레지스트리 로그인 확인
   - imagePullSecrets 설정

2. **CrashLoopBackOff**
   - 환경변수 확인
   - 의존 서비스 (DB, Kafka) 연결 확인
   - 로그에서 에러 메시지 확인

3. **Pending**
   - 리소스 부족 확인
   - PVC 바인딩 확인
   - Node selector/affinity 확인

4. **Service 연결 실패**
   - Service selector 확인
   - Pod labels 확인
   - Network Policy 확인

## 모니터링 및 관리

### 로그 모니터링
```bash
# 실시간 로그
kubectl logs -f deployment/service-name -n erp-system

# 여러 Pod 로그 (stern 사용)
stern -n erp-system service-name
```

### 메트릭 확인
```bash
# CPU/Memory 사용량
kubectl top pods -n erp-system
kubectl top nodes

# HPA 상태
kubectl get hpa -n erp-system -w
```

### 배포 업데이트
```bash
# 새 이미지로 업데이트
kubectl set image deployment/service-name service-name=registry/service:new-version -n erp-system

# 롤아웃 상태 확인
kubectl rollout status deployment/service-name -n erp-system

# 롤백
kubectl rollout undo deployment/service-name -n erp-system
```

## 보안 고려사항

1. **Network Policy**: Pod 간 통신 제한
2. **RBAC**: 최소 권한 원칙
3. **Secret 관리**: 외부 Secret Manager 사용 (AWS Secrets Manager, Azure Key Vault)
4. **Image 스캔**: 보안 취약점 검사
5. **TLS/SSL**: Ingress에 인증서 적용
6. **Pod Security Standards**: 컨테이너 보안 정책

## 추가 개선 사항

1. **Service Mesh** (Istio, Linkerd)
   - 서비스 간 통신 보안
   - Traffic 관리
   - Observability

2. **GitOps** (ArgoCD, Flux)
   - Git을 통한 배포 자동화
   - 버전 관리 및 롤백

3. **Monitoring Stack**
   - Prometheus + Grafana
   - ELK Stack (Elasticsearch, Logstash, Kibana)
   - Jaeger (분산 추적)

4. **Backup & Disaster Recovery**
   - Velero를 통한 백업
   - Multi-region 배포
