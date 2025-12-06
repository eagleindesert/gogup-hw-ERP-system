# 🚀 Kubernetes 배포 빠른 시작 가이드

## 1분 요약

### 사전 준비
- ✅ Kubernetes 클러스터
- ✅ kubectl 설치
- ✅ Docker 레지스트리 접근

### 빠른 배포 (3단계)

#### 1️⃣ 이미지 빌드 & 푸시
```bash
# 레지스트리 설정
export REGISTRY="your-dockerhub-username"  # 또는 ACR/ECR/GCR
export VERSION="v1.0.0"

# 빌드 & 푸시
./k8s/build-images.sh   # Linux/Mac
# 또는
./k8s/build-images.ps1  # Windows
```

#### 2️⃣ 매니페스트 이미지 경로 수정
`k8s/apps/*.yaml` 파일들에서:
```yaml
# 변경 전
image: your-registry/employee-service:latest

# 변경 후
image: dockerhub-username/employee-service:v1.0.0
```

#### 3️⃣ 배포 실행
```bash
./k8s/deploy.sh   # Linux/Mac
# 또는
./k8s/deploy.ps1  # Windows
```

---

## 주요 명령어 모음

### 배포
```bash
# 전체 배포
kubectl apply -f k8s/

# 특정 서비스만
kubectl apply -f k8s/apps/employee-service.yaml
```

### 상태 확인
```bash
# 모든 리소스
kubectl get all -n erp-system

# Pod 상태
kubectl get pods -n erp-system

# 로그
kubectl logs -f deployment/employee-service -n erp-system
```

### 접근
```bash
# Port Forward
kubectl port-forward -n erp-system svc/employee-service 8081:8081

# Ingress 주소
kubectl get ingress -n erp-system
```

### 삭제
```bash
# 전체 삭제
kubectl delete namespace erp-system

# 특정 서비스
kubectl delete -f k8s/apps/employee-service.yaml
```

---

## 배포 순서

```
1. Zookeeper     → Kafka를 위해
2. Kafka         → 메시지 브로커
3. MySQL         → Employee Service DB
4. MongoDB       → Approval Request DB
5. 애플리케이션  → 4개 서비스
6. Ingress       → 외부 접근
7. HPA           → 자동 스케일링
```

---

## 서비스 포트

| 서비스                    | 포트 | 용도             |
|--------------------------|------|------------------|
| Employee Service         | 8081 | REST API         |
| Approval Request         | 8082 | REST API         |
| Approval Processing      | 8083 | REST API         |
| Notification Service     | 8084 | REST + WebSocket |
| MySQL                    | 3306 | Database         |
| MongoDB                  | 27017| Database         |
| Kafka                    | 9092 | Message Broker   |
| Zookeeper                | 2181 | Coordination     |

---

## 트러블슈팅

### Pod이 Pending
```bash
kubectl describe pod <pod-name> -n erp-system
# → 리소스 부족, PVC 문제, Node 문제 확인
```

### ImagePullBackOff
```bash
# 이미지 경로 확인
kubectl get pod <pod-name> -n erp-system -o yaml | grep image

# Secret 추가 필요 시
kubectl create secret docker-registry regcred \
  --docker-server=<registry> \
  --docker-username=<username> \
  --docker-password=<password> \
  -n erp-system
```

### CrashLoopBackOff
```bash
# 로그 확인
kubectl logs <pod-name> -n erp-system
kubectl logs <pod-name> -n erp-system --previous

# 일반 원인: DB 연결 실패, 환경변수 오류, Kafka 연결 실패
```

---

## 환경별 레지스트리 설정

### Docker Hub
```bash
export REGISTRY="your-username"
docker login
```

### Azure Container Registry (ACR)
```bash
export REGISTRY="yourregistry.azurecr.io"
az acr login --name yourregistry
```

### AWS Elastic Container Registry (ECR)
```bash
export REGISTRY="123456789012.dkr.ecr.region.amazonaws.com"
aws ecr get-login-password --region region | \
  docker login --username AWS --password-stdin $REGISTRY
```

### Google Container Registry (GCR)
```bash
export REGISTRY="gcr.io/project-id"
gcloud auth configure-docker
```

---

## 파일 구조

```
k8s/
├── README.md                    # 상세 가이드
├── DEPLOYMENT_PROCESS.md        # 배포 프로세스 상세
├── QUICK_START.md               # 이 파일
├── deploy.sh / deploy.ps1       # 배포 스크립트
├── build-images.sh/ps1          # 이미지 빌드 스크립트
│
├── base/
│   ├── namespace.yaml          # 네임스페이스
│   ├── configmap.yaml          # 환경 변수
│   ├── secret.yaml             # 비밀 정보
│   ├── ingress.yaml            # 외부 접근
│   └── hpa.yaml                # Auto-scaling
│
├── infra/
│   ├── zookeeper.yaml
│   ├── kafka.yaml
│   ├── mysql.yaml
│   └── mongodb.yaml
│
└── apps/
    ├── employee-service.yaml
    ├── approval-request-service.yaml
    ├── approval-processing-service.yaml
    └── notification-service.yaml
```

---

## 체크리스트

### 배포 전
- [ ] Kubernetes 클러스터 준비
- [ ] kubectl 설치 및 클러스터 연결
- [ ] Docker 레지스트리 준비
- [ ] Ingress Controller 설치
- [ ] Metrics Server 설치 (HPA용)

### 이미지 빌드
- [ ] 레지스트리 설정 ($REGISTRY)
- [ ] 버전 태그 설정 ($VERSION)
- [ ] Docker 로그인
- [ ] 이미지 빌드 & 푸시 스크립트 실행
- [ ] k8s/apps/*.yaml 이미지 경로 수정

### 배포
- [ ] 배포 스크립트 실행 또는 수동 배포
- [ ] Pod 상태 확인 (모두 Running)
- [ ] Service 확인
- [ ] Ingress 확인
- [ ] 로그 확인 (에러 없음)

### 검증
- [ ] Health Check 통과
- [ ] API 호출 테스트
- [ ] Kafka 메시지 흐름 확인
- [ ] WebSocket 연결 테스트
- [ ] HPA 동작 확인

---

## 다음 단계

1. **도메인 설정**: Ingress에 실제 도메인 적용
2. **TLS 인증서**: HTTPS 설정
3. **모니터링**: Prometheus + Grafana 설치
4. **로깅**: ELK Stack 또는 Loki 설치
5. **CI/CD**: GitHub Actions, GitLab CI, Jenkins 연동
6. **백업**: Velero 설정

---

## 유용한 링크

- 📖 [상세 배포 가이드](./README.md)
- 📖 [배포 프로세스 상세](./DEPLOYMENT_PROCESS.md)
- 🌐 [Kubernetes 공식 문서](https://kubernetes.io/docs/)
- 🌐 [Spring Boot on K8s](https://spring.io/guides/gs/spring-boot-kubernetes/)
