#!/bin/bash

# ============================================
# ERP 시스템 Kubernetes 배포 스크립트
# ============================================

set -e  # 에러 발생 시 중단

echo "=========================================="
echo "ERP System Kubernetes Deployment"
echo "=========================================="
echo ""

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 함수: 단계 출력
print_step() {
    echo -e "${GREEN}[STEP]${NC} $1"
}

# 함수: 경고 출력
print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 함수: 에러 출력
print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 함수: Pod가 Ready 상태가 될 때까지 대기
wait_for_pods() {
    local label=$1
    local timeout=${2:-120}
    
    print_step "Waiting for pods with label $label to be ready..."
    kubectl wait --for=condition=ready pod -l $label -n erp-system --timeout=${timeout}s || {
        print_error "Timeout waiting for pods with label $label"
        return 1
    }
    echo ""
}

# ============================================
# 1. 사전 확인
# ============================================
print_step "Checking prerequisites..."

# kubectl 확인
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed"
    exit 1
fi

# 클러스터 연결 확인
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster"
    exit 1
fi

echo "✓ kubectl is installed"
echo "✓ Connected to Kubernetes cluster"
echo ""

# ============================================
# 2. Namespace 및 기본 설정
# ============================================
print_step "Creating namespace and base configurations..."

kubectl apply -f k8s/base/namespace.yaml
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secret.yaml

echo "✓ Namespace and configurations created"
echo ""

# ============================================
# 3. 인프라 서비스 배포
# ============================================
print_step "Deploying infrastructure services..."

# Zookeeper
print_step "Deploying Zookeeper..."
kubectl apply -f k8s/infra/zookeeper.yaml
sleep 5
wait_for_pods "app=zookeeper" 120

# Kafka
print_step "Deploying Kafka..."
kubectl apply -f k8s/infra/kafka.yaml
sleep 10
wait_for_pods "app=kafka" 120

# MySQL
print_step "Deploying MySQL..."
kubectl apply -f k8s/infra/mysql.yaml
sleep 5
wait_for_pods "app=mysql" 120

# MongoDB
print_step "Deploying MongoDB..."
kubectl apply -f k8s/infra/mongodb.yaml
sleep 5
wait_for_pods "app=mongodb" 120

echo "✓ Infrastructure services deployed successfully"
echo ""

# ============================================
# 4. 애플리케이션 서비스 배포
# ============================================
print_step "Deploying application services..."

# Employee Service
print_step "Deploying Employee Service..."
kubectl apply -f k8s/apps/employee-service.yaml
sleep 5

# Approval Request Service
print_step "Deploying Approval Request Service..."
kubectl apply -f k8s/apps/approval-request-service.yaml
sleep 5

# Approval Processing Service
print_step "Deploying Approval Processing Service..."
kubectl apply -f k8s/apps/approval-processing-service.yaml
sleep 5

# Notification Service
print_step "Deploying Notification Service..."
kubectl apply -f k8s/apps/notification-service.yaml
sleep 5

echo "✓ Application services deployment initiated"
echo ""

# 애플리케이션이 준비될 때까지 대기
print_step "Waiting for application services to be ready..."
wait_for_pods "app=employee-service" 180
wait_for_pods "app=approval-request-service" 180
wait_for_pods "app=approval-processing-service" 180
wait_for_pods "app=notification-service" 180

echo "✓ All application services are ready"
echo ""

# ============================================
# 5. Ingress 및 HPA 배포
# ============================================
print_step "Deploying Ingress and HPA..."

kubectl apply -f k8s/base/ingress.yaml
kubectl apply -f k8s/base/hpa.yaml

echo "✓ Ingress and HPA deployed"
echo ""

# ============================================
# 6. 배포 상태 확인
# ============================================
print_step "Deployment Summary:"
echo ""

echo "Pods:"
kubectl get pods -n erp-system
echo ""

echo "Services:"
kubectl get svc -n erp-system
echo ""

echo "Ingress:"
kubectl get ingress -n erp-system
echo ""

echo "HPA:"
kubectl get hpa -n erp-system
echo ""

# ============================================
# 완료
# ============================================
echo "=========================================="
echo -e "${GREEN}Deployment completed successfully!${NC}"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Check pod logs: kubectl logs -f deployment/<service-name> -n erp-system"
echo "2. Access services via Ingress: kubectl get ingress -n erp-system"
echo "3. Monitor HPA: kubectl get hpa -n erp-system -w"
echo ""
echo "For more information, see k8s/README.md"
