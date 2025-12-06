# ERP 시스템 Kubernetes 배포 가이드

## 📋 목차
1. [사전 준비](#사전-준비)
2. [이미지 빌드 및 푸시](#이미지-빌드-및-푸시)
3. [배포 순서](#배포-순서)
4. [검증](#검증)
5. [관리 명령어](#관리-명령어)
6. [트러블슈팅](#트러블슈팅)

---

## 🔧 사전 준비

### 필수 요구사항
- Kubernetes 클러스터 (v1.24+)
- kubectl CLI 설치 및 설정
- Container Registry 접근 권한 (Docker Hub, ACR, ECR, GCR 등)
- Ingress Controller 설치 (nginx-ingress 권장)
- Metrics Server 설치 (HPA를 위해)

### 클러스터 확인
```bash
kubectl cluster-info
kubectl get nodes
```

### Ingress Controller 설치 (필요시)
```bash
# NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

### Metrics Server 설치 (필요시)
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

---

## 🐳 이미지 빌드 및 푸시

### 1. 레지스트리 설정
```bash
# Docker Hub 예시
export REGISTRY="your-dockerhub-username"

# ACR 예시
# export REGISTRY="yourregistry.azurecr.io"

# ECR 예시
# export REGISTRY="123456789012.dkr.ecr.us-east-1.amazonaws.com"
```

### 2. 레지스트리 로그인
```bash
# Docker Hub
docker login

# Azure Container Registry
# az acr login --name yourregistry

# AWS ECR
# aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $REGISTRY
```

### 3. 이미지 빌드 및 푸시
```bash
# Employee Service
cd employee-service/demo
docker build -t ${REGISTRY}/employee-service:v1.0.0 .
docker push ${REGISTRY}/employee-service:v1.0.0
cd ../..

# Approval Request Service
docker build -f approval-request-service/demo/Dockerfile -t ${REGISTRY}/approval-request-service:v1.0.0 .
docker push ${REGISTRY}/approval-request-service:v1.0.0

# Approval Processing Service
docker build -f approval-processing-service/demo/Dockerfile -t ${REGISTRY}/approval-processing-service:v1.0.0 .
docker push ${REGISTRY}/approval-processing-service:v1.0.0

# Notification Service
cd notification-service/demo
docker build -t ${REGISTRY}/notification-service:v1.0.0 .
docker push ${REGISTRY}/notification-service:v1.0.0
cd ../..
```

### 4. K8s 매니페스트 이미지 경로 업데이트
```bash
# k8s/apps/*.yaml 파일들의 image 필드를 실제 레지스트리 경로로 수정
# 예: your-registry/employee-service:latest → ${REGISTRY}/employee-service:v1.0.0
```

---

## 🚀 배포 순서

### 1단계: Namespace 및 기본 설정
```bash
# Namespace 생성
kubectl apply -f k8s/base/namespace.yaml

# ConfigMap 및 Secret 생성
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secret.yaml
```

### 2단계: 인프라 서비스 배포
```bash
# Zookeeper 배포 (Kafka 의존성)
kubectl apply -f k8s/infra/zookeeper.yaml

# Zookeeper가 Ready 상태가 될 때까지 대기
kubectl wait --for=condition=ready pod -l app=zookeeper -n erp-system --timeout=120s

# Kafka 배포
kubectl apply -f k8s/infra/kafka.yaml

# Kafka가 Ready 상태가 될 때까지 대기
kubectl wait --for=condition=ready pod -l app=kafka -n erp-system --timeout=120s

# MySQL 배포
kubectl apply -f k8s/infra/mysql.yaml

# MySQL이 Ready 상태가 될 때까지 대기
kubectl wait --for=condition=ready pod -l app=mysql -n erp-system --timeout=120s

# MongoDB 배포
kubectl apply -f k8s/infra/mongodb.yaml

# MongoDB가 Ready 상태가 될 때까지 대기
kubectl wait --for=condition=ready pod -l app=mongodb -n erp-system --timeout=120s
```

### 3단계: 애플리케이션 서비스 배포
```bash
# Employee Service 배포
kubectl apply -f k8s/apps/employee-service.yaml

# Approval Request Service 배포
kubectl apply -f k8s/apps/approval-request-service.yaml

# Approval Processing Service 배포
kubectl apply -f k8s/apps/approval-processing-service.yaml

# Notification Service 배포
kubectl apply -f k8s/apps/notification-service.yaml
```

### 4단계: Ingress 및 HPA 설정
```bash
# Ingress 배포
kubectl apply -f k8s/base/ingress.yaml

# HPA 배포
kubectl apply -f k8s/base/hpa.yaml
```

### 전체 배포 (한번에)
```bash
# 순서대로 배포하는 스크립트 사용
./k8s/deploy.sh
```

---

## ✅ 검증

### Pod 상태 확인
```bash
kubectl get pods -n erp-system
kubectl get pods -n erp-system -w  # 실시간 모니터링
```

### Service 확인
```bash
kubectl get svc -n erp-system
```

### Ingress 확인
```bash
kubectl get ingress -n erp-system
kubectl describe ingress erp-ingress -n erp-system
```

### 로그 확인
```bash
# 특정 서비스 로그
kubectl logs -f deployment/employee-service -n erp-system
kubectl logs -f deployment/approval-request-service -n erp-system
kubectl logs -f deployment/approval-processing-service -n erp-system
kubectl logs -f deployment/notification-service -n erp-system

# 모든 Pod 로그 (Stern 사용 권장)
# stern -n erp-system '.*'
```

### Health Check
```bash
# Port-forward를 통한 직접 확인
kubectl port-forward -n erp-system svc/employee-service 8081:8081
curl http://localhost:8081/actuator/health

kubectl port-forward -n erp-system svc/approval-request-service 8082:8082
curl http://localhost:8082/actuator/health

kubectl port-forward -n erp-system svc/approval-processing-service 8083:8083
curl http://localhost:8083/actuator/health

kubectl port-forward -n erp-system svc/notification-service 8084:8084
curl http://localhost:8084/actuator/health
```

### Kafka 토픽 확인
```bash
# Kafka Pod 내부에서 토픽 확인
kubectl exec -it -n erp-system kafka-0 -- kafka-topics --bootstrap-server localhost:9092 --list
```

---

## 🛠 관리 명령어

### 스케일링
```bash
# 수동 스케일링
kubectl scale deployment employee-service -n erp-system --replicas=5

# HPA 상태 확인
kubectl get hpa -n erp-system
```

### 업데이트
```bash
# 이미지 업데이트 (롤링 업데이트)
kubectl set image deployment/employee-service employee-service=${REGISTRY}/employee-service:v1.0.1 -n erp-system

# 롤아웃 상태 확인
kubectl rollout status deployment/employee-service -n erp-system

# 롤백
kubectl rollout undo deployment/employee-service -n erp-system
```

### 재시작
```bash
# Deployment 재시작
kubectl rollout restart deployment/employee-service -n erp-system
```

### 삭제
```bash
# 전체 삭제
kubectl delete namespace erp-system

# 개별 서비스 삭제
kubectl delete -f k8s/apps/employee-service.yaml
```

---

## 🔍 트러블슈팅

### Pod이 Pending 상태
```bash
# 이벤트 확인
kubectl describe pod <pod-name> -n erp-system

# 일반적인 원인:
# - 리소스 부족
# - PVC 바인딩 실패
# - ImagePullBackOff
```

### ImagePullBackOff 에러
```bash
# 이미지 경로 확인
kubectl describe pod <pod-name> -n erp-system

# ImagePullSecret 필요 시 생성
kubectl create secret docker-registry regcred \
  --docker-server=<your-registry-server> \
  --docker-username=<your-name> \
  --docker-password=<your-password> \
  --docker-email=<your-email> \
  -n erp-system

# Deployment에 imagePullSecrets 추가
# spec.template.spec.imagePullSecrets:
# - name: regcred
```

### CrashLoopBackOff
```bash
# 로그 확인
kubectl logs <pod-name> -n erp-system
kubectl logs <pod-name> -n erp-system --previous  # 이전 컨테이너 로그

# 일반적인 원인:
# - 환경변수 설정 오류
# - DB 연결 실패
# - Kafka 연결 실패
```

### 데이터베이스 연결 실패
```bash
# MySQL 연결 테스트
kubectl exec -it -n erp-system mysql-0 -- mysql -u root -proot -e "SHOW DATABASES;"

# MongoDB 연결 테스트
kubectl exec -it -n erp-system mongodb-0 -- mongosh --eval "show dbs"
```

### Kafka 연결 문제
```bash
# Kafka 상태 확인
kubectl exec -it -n erp-system kafka-0 -- kafka-broker-api-versions --bootstrap-server localhost:9092

# 토픽 생성 (수동)
kubectl exec -it -n erp-system kafka-0 -- kafka-topics --create --topic approval-request --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kubectl exec -it -n erp-system kafka-0 -- kafka-topics --create --topic approval-result --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

---

## 📊 모니터링 (선택사항)

### Prometheus & Grafana 설치
```bash
# Helm으로 설치
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
```

### Dashboard 접근
```bash
# Grafana 포트포워딩
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

# 브라우저에서 http://localhost:3000 접속
# 기본 계정: admin / prom-operator
```

---

## 🔐 보안 권장사항

1. **Secret 관리**: Kubernetes Secret 대신 외부 Secret Manager 사용 권장 (AWS Secrets Manager, Azure Key Vault 등)
2. **Network Policy**: Pod 간 통신 제한
3. **RBAC**: 최소 권한 원칙 적용
4. **이미지 스캔**: 배포 전 보안 취약점 스캔
5. **TLS/SSL**: Ingress에 TLS 인증서 적용

---

## 📚 참고 자료

- [Kubernetes 공식 문서](https://kubernetes.io/docs/)
- [Spring Boot on Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Horizontal Pod Autoscaling](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/)
