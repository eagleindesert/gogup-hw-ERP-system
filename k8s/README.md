# ERP Microservices - Kubernetes Deployment

Kubernetes에 ERP 마이크로서비스를 배포하기 위한 매니페스트 파일들입니다.

## 📁 파일 구조

```
k8s/
├── namespace.yaml                      # 네임스페이스
├── mysql.yaml                          # MySQL 데이터베이스
├── mongodb.yaml                        # MongoDB 데이터베이스
├── zookeeper.yaml                      # Zookeeper
├── kafka.yaml                          # Kafka
├── employee-service.yaml               # 직원 서비스
├── approval-request-service.yaml       # 결재 요청 서비스
├── approval-processing-service.yaml    # 결재 처리 서비스
├── notification-service.yaml           # 알림 서비스
├── deploy-k8s.sh                       # 배포 스크립트 (Bash)
└── deploy-k8s.ps1                      # 배포 스크립트 (PowerShell)
```

## 🚀 배포 방법

### 사전 준비

1. **Docker 이미지 빌드 및 푸시**
   ```powershell
   # PowerShell
   .\scripts\push-to-dockerhub.ps1 -Version "1.0.0"
   
   # Bash
   ./scripts/push-to-dockerhub.sh myusername 1.0.0
   ```

2. **Kubernetes 클러스터 준비**
   - Minikube, Docker Desktop, 또는 클라우드 K8s 클러스터

### 전체 배포

```powershell
# PowerShell
.\k8s\deploy-k8s.ps1

# Bash
chmod +x k8s/deploy-k8s.sh
./k8s/deploy-k8s.sh
```

### 개별 배포

```bash
# 네임스페이스 생성
kubectl apply -f k8s/namespace.yaml

# 인프라 서비스 배포
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/mongodb.yaml
kubectl apply -f k8s/zookeeper.yaml
kubectl apply -f k8s/kafka.yaml

# 애플리케이션 서비스 배포
kubectl apply -f k8s/employee-service.yaml
kubectl apply -f k8s/approval-request-service.yaml
kubectl apply -f k8s/approval-processing-service.yaml
kubectl apply -f k8s/notification-service.yaml
```

## 📊 상태 확인

```bash
# 전체 리소스 확인
kubectl get all -n erp-system

# Pod 상태 확인
kubectl get pods -n erp-system

# Service 확인
kubectl get svc -n erp-system

# 로그 확인
kubectl logs <pod-name> -n erp-system

# Pod 상세 정보
kubectl describe pod <pod-name> -n erp-system
```

## 🔌 서비스 접근

### Port Forwarding

```bash
# Employee Service
kubectl port-forward svc/employee-service 8081:8081 -n erp-system

# Approval Request Service
kubectl port-forward svc/approval-request-service 8082:8082 -n erp-system

# Approval Processing Service
kubectl port-forward svc/approval-processing-service 8083:8083 -n erp-system

# Notification Service
kubectl port-forward svc/notification-service 8084:8084 -n erp-system
```

### LoadBalancer (클라우드 환경)

```bash
# External IP 확인
kubectl get svc -n erp-system

# 예시 출력:
# NAME                        TYPE           EXTERNAL-IP     PORT(S)
# employee-service            LoadBalancer   34.123.45.67    8081:30001/TCP
```

## 🗑️ 삭제

```powershell
# PowerShell
.\k8s\deploy-k8s.ps1 -Action delete

# Bash
./k8s/deploy-k8s.sh delete

# 또는 전체 네임스페이스 삭제
kubectl delete namespace erp-system
```

## ⚙️ 주요 설정

### Replicas
- 각 애플리케이션 서비스: 2개 (고가용성)
- 인프라 서비스 (MySQL, MongoDB, Kafka): 1개

### Resources
- **Requests**: CPU 250m, Memory 512Mi
- **Limits**: CPU 500m, Memory 1Gi

### Health Checks
- **Liveness Probe**: 서비스 정상 동작 확인
- **Readiness Probe**: 트래픽 수신 준비 확인

### Persistent Storage
- MySQL: 5Gi PVC
- MongoDB: 5Gi PVC

## 📝 주의사항

1. **이미지 Pull**: Docker Hub에 이미지가 먼저 푸시되어 있어야 합니다
2. **리소스 요구사항**: 클러스터에 충분한 리소스(CPU, Memory)가 있어야 합니다
3. **LoadBalancer**: 로컬 환경(Minikube, Docker Desktop)에서는 `NodePort`로 변경 필요할 수 있습니다
4. **PersistentVolume**: 클러스터에서 동적 프로비저닝을 지원해야 합니다

## 🔧 트러블슈팅

### Pod이 Pending 상태
```bash
kubectl describe pod <pod-name> -n erp-system
# 리소스 부족 또는 PVC 문제 확인
```

### 이미지 Pull 실패
```bash
# 이미지가 Docker Hub에 있는지 확인
docker pull eagleindesert/employee-service:latest
```

### 서비스 연결 실패
```bash
# DNS 확인
kubectl run -it --rm debug --image=busybox --restart=Never -n erp-system -- nslookup mysql
```
