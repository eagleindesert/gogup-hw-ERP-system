# ERP 시스템 Kubernetes 배포 - 완료!

## ✅ 생성된 파일 목록

### 📁 디렉터리 구조
```
k8s/
├── 📖 문서
│   ├── README.md                    # 상세 배포 가이드 (완전한 설명서)
│   ├── DEPLOYMENT_PROCESS.md        # 배포 프로세스 상세 설명
│   ├── QUICK_START.md               # 빠른 시작 가이드
│   └── INDEX.md                     # 이 파일 (전체 인덱스)
│
├── 🔧 스크립트
│   ├── deploy.sh                    # Linux/Mac 배포 자동화
│   ├── deploy.ps1                   # Windows PowerShell 배포 자동화
│   ├── build-images.sh              # Linux/Mac 이미지 빌드
│   └── build-images.ps1             # Windows PowerShell 이미지 빌드
│
├── 📦 base/ (기본 설정)
│   ├── namespace.yaml              # erp-system 네임스페이스
│   ├── configmap.yaml              # 공통 환경 변수
│   ├── secret.yaml                 # 민감 정보 (DB 비밀번호 등)
│   ├── ingress.yaml                # 외부 트래픽 라우팅
│   └── hpa.yaml                    # Horizontal Pod Autoscaler (4개 서비스)
│
├── 🗄️ infra/ (인프라 서비스)
│   ├── zookeeper.yaml              # Kafka Coordination
│   ├── kafka.yaml                  # Message Broker
│   ├── mysql.yaml                  # Employee Service Database
│   └── mongodb.yaml                # Approval Request Database
│
└── 🚀 apps/ (애플리케이션 서비스)
    ├── employee-service.yaml
    ├── approval-request-service.yaml
    ├── approval-processing-service.yaml
    └── notification-service.yaml
```

---

## 🎯 배포 프로세스 요약

### 1️⃣ 이미지 빌드 & 푸시
```bash
# 레지스트리 설정
export REGISTRY="your-registry"
export VERSION="v1.0.0"

# 빌드 & 푸시
./k8s/build-images.sh      # Linux/Mac
./k8s/build-images.ps1     # Windows
```

### 2️⃣ 매니페스트 수정
- `k8s/apps/*.yaml`의 `image:` 필드를 실제 레지스트리 경로로 변경
- `k8s/base/ingress.yaml`의 도메인 설정 변경
- `k8s/base/secret.yaml`의 비밀번호 수정 (프로덕션용)

### 3️⃣ 배포 실행
```bash
./k8s/deploy.sh            # Linux/Mac
./k8s/deploy.ps1           # Windows
```

### 4️⃣ 검증
```bash
# Pod 상태
kubectl get pods -n erp-system

# 서비스
kubectl get svc -n erp-system

# Ingress
kubectl get ingress -n erp-system
```

---

## 📚 문서 가이드

### 🔰 처음 시작하는 경우
👉 **[QUICK_START.md](./QUICK_START.md)**
- 빠른 배포를 위한 핵심 명령어
- 체크리스트
- 주요 트러블슈팅

### 📖 상세한 설명이 필요한 경우
👉 **[README.md](./README.md)**
- 완전한 배포 가이드
- 사전 준비사항
- 각 단계별 상세 설명
- 트러블슈팅
- 모니터링 & 보안

### 🏗️ 배포 프로세스 이해
👉 **[DEPLOYMENT_PROCESS.md](./DEPLOYMENT_PROCESS.md)**
- 전체 아키텍처
- 배포 순서와 이유
- 서비스 간 의존성
- 환경별 설정
- 고급 주제

---

## 🔑 핵심 개념

### Namespace (네임스페이스)
모든 리소스를 `erp-system` 네임스페이스에 격리하여 관리

### ConfigMap & Secret
- **ConfigMap**: DB 호스트, Kafka 서버 등 일반 설정
- **Secret**: 비밀번호, 토큰 등 민감 정보 (Base64 인코딩)

### StatefulSet vs Deployment
- **StatefulSet**: DB, Kafka 등 상태를 가진 서비스
- **Deployment**: 무상태 애플리케이션 서비스

### Service
Pod 그룹에 대한 안정적인 네트워크 엔드포인트

### Ingress
외부 → 내부 서비스로의 HTTP/HTTPS 라우팅

### HPA (Horizontal Pod Autoscaler)
CPU/Memory 사용률 기반 자동 스케일링

---

## 🔄 배포 의존성 순서

```
1️⃣ Zookeeper
     ↓
2️⃣ Kafka
     ↓
3️⃣ MySQL & MongoDB (병렬)
     ↓
4️⃣ 애플리케이션 서비스들 (병렬)
     ↓
5️⃣ Ingress & HPA
```

**왜 이 순서인가?**
- Kafka는 Zookeeper 필요
- 애플리케이션은 DB와 Kafka 필요
- Ingress는 애플리케이션이 준비된 후

---

## 🛠️ 주요 설정 파일

### 1. ConfigMap (`k8s/base/configmap.yaml`)
```yaml
MYSQL_HOST: "mysql-service"
KAFKA_BOOTSTRAP_SERVERS: "kafka-service:9092"
```
👉 서비스 간 연결 정보 중앙 관리

### 2. Secret (`k8s/base/secret.yaml`)
```yaml
MYSQL_ROOT_PASSWORD: "root"
MYSQL_PASSWORD: "erp_password"
```
👉 민감 정보 암호화 저장

### 3. Ingress (`k8s/base/ingress.yaml`)
```yaml
host: erp.example.com  # ← 실제 도메인으로 변경
paths:
  /api/employees → employee-service
  /api/approval-requests → approval-request-service
  /ws → notification-service (WebSocket)
```
👉 외부 접근 라우팅 규칙

### 4. HPA (`k8s/base/hpa.yaml`)
```yaml
minReplicas: 2
maxReplicas: 10
targetCPUUtilization: 70%
```
👉 부하에 따라 2~10개 Pod 자동 조절

---

## 🌐 서비스 아키텍처

```
                    Internet
                       │
                       ▼
              ┌────────────────┐
              │     Ingress    │
              │  (nginx/traefik)│
              └────────┬───────┘
                       │
        ┌──────────────┼──────────────┬─────────────┐
        │              │              │             │
        ▼              ▼              ▼             ▼
  ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
  │Employee │   │Approval │   │Approval │   │Notifica-│
  │Service  │   │Request  │   │Process  │   │tion Svc │
  │(2 pods) │   │(2 pods) │   │(2 pods) │   │(2 pods) │
  └────┬────┘   └────┬────┘   └────┬────┘   └─────────┘
       │             │              │
       ▼             ▼              ▼
   ┌──────┐     ┌────────┐    ┌────────┐
   │MySQL │     │MongoDB │    │ Kafka  │
   │(1pod)│     │(1 pod) │    │(1 pod) │
   └──────┘     └────────┘    └───┬────┘
                                   │
                                   ▼
                              ┌─────────┐
                              │Zookeeper│
                              │(1 pod)  │
                              └─────────┘
```

---

## 📊 리소스 할당

### 애플리케이션 서비스 (각각)
- **Replicas**: 2개 (기본) → 최대 10개 (HPA)
- **CPU**: 250m (요청) / 500m (제한)
- **Memory**: 512Mi (요청) / 1Gi (제한)

### 인프라 서비스
| 서비스      | CPU   | Memory |
|------------|-------|--------|
| Kafka      | 250m  | 512Mi  |
| Zookeeper  | 100m  | 256Mi  |
| MySQL      | 250m  | 512Mi  |
| MongoDB    | 250m  | 512Mi  |

### 총 리소스 (최소)
- **CPU**: ~3.5 cores
- **Memory**: ~8 GB
- **Storage**: ~30 GB (PVC)

---

## 🔐 보안 체크리스트

### 배포 전
- [ ] Secret의 기본 비밀번호 변경
- [ ] Ingress TLS 인증서 준비
- [ ] Network Policy 적용 검토
- [ ] RBAC 권한 검토

### 배포 후
- [ ] Pod Security Standards 적용
- [ ] Image 취약점 스캔
- [ ] 불필요한 Port 노출 확인
- [ ] Logging 및 Monitoring 설정

---

## 🚨 일반적인 문제와 해결

### 1. Pod이 Pending
**원인**: 리소스 부족, PVC 바인딩 실패
```bash
kubectl describe pod <pod> -n erp-system
```

### 2. ImagePullBackOff
**원인**: 이미지 경로 오류, 인증 실패
```bash
# 이미지 경로 확인
kubectl get pod <pod> -n erp-system -o yaml | grep image

# Secret 생성
kubectl create secret docker-registry regcred ...
```

### 3. CrashLoopBackOff
**원인**: 애플리케이션 에러, 환경변수 오류
```bash
kubectl logs <pod> -n erp-system
kubectl logs <pod> -n erp-system --previous
```

### 4. Kafka 연결 실패
**원인**: Kafka/Zookeeper 미준비
```bash
# Kafka Pod 확인
kubectl get pods -l app=kafka -n erp-system

# 토픽 확인
kubectl exec -it kafka-0 -n erp-system -- \
  kafka-topics --list --bootstrap-server localhost:9092
```

---

## 📈 모니터링 & 관리

### 로그 확인
```bash
# 실시간 로그
kubectl logs -f deployment/employee-service -n erp-system

# 모든 서비스 로그 (stern 도구 사용)
stern -n erp-system '.*'
```

### 메트릭 확인
```bash
# Pod 리소스 사용량
kubectl top pods -n erp-system

# HPA 상태
kubectl get hpa -n erp-system
```

### 업데이트
```bash
# 롤링 업데이트
kubectl set image deployment/employee-service \
  employee-service=registry/employee-service:v1.0.1 \
  -n erp-system

# 롤백
kubectl rollout undo deployment/employee-service -n erp-system
```

---

## 🎓 다음 학습 단계

### 기본 → 중급
1. ✅ 기본 배포 완료
2. 📊 Prometheus + Grafana 모니터링
3. 📝 ELK Stack 로깅
4. 🔒 Network Policy 적용

### 중급 → 고급
5. 🔐 External Secrets Operator
6. 🚀 GitOps (ArgoCD, Flux)
7. 🌐 Service Mesh (Istio, Linkerd)
8. 🔄 Multi-cluster 배포

---

## 🔗 유용한 링크

### 공식 문서
- [Kubernetes 공식](https://kubernetes.io/docs/)
- [Spring Boot on K8s](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [NGINX Ingress](https://kubernetes.github.io/ingress-nginx/)

### 도구
- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- [k9s](https://k9scli.io/) - Terminal UI
- [stern](https://github.com/stern/stern) - Multi-pod log tailing
- [kubectx/kubens](https://github.com/ahmetb/kubectx) - Context switching

### 학습 리소스
- [Kubernetes Patterns](https://www.redhat.com/en/resources/kubernetes-patterns-e-book)
- [12 Factor App](https://12factor.net/)

---

## 📞 지원

### 문제가 발생하면?
1. 📖 README.md 트러블슈팅 섹션 확인
2. 🔍 Pod 로그 및 이벤트 확인
3. 💬 Kubernetes 커뮤니티 질문

### 피드백
이 배포 가이드에 대한 개선 제안은 환영합니다!

---

**🎉 Kubernetes 배포 준비 완료!**

다음 단계로 실제 클러스터에 배포해보세요!
