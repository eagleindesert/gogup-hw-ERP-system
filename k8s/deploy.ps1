# ============================================
# ERP 시스템 Kubernetes 배포 스크립트 (PowerShell)
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "ERP System Kubernetes Deployment" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 함수: 단계 출력
function Print-Step {
    param($Message)
    Write-Host "[STEP] $Message" -ForegroundColor Green
}

# 함수: 경고 출력
function Print-Warning {
    param($Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

# 함수: 에러 출력
function Print-Error {
    param($Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# 함수: Pod가 Ready 상태가 될 때까지 대기
function Wait-ForPods {
    param(
        [string]$Label,
        [int]$Timeout = 120
    )
    
    Print-Step "Waiting for pods with label $Label to be ready..."
    kubectl wait --for=condition=ready pod -l $Label -n erp-system --timeout="${Timeout}s"
    if ($LASTEXITCODE -ne 0) {
        Print-Error "Timeout waiting for pods with label $Label"
        throw "Pod wait failed"
    }
    Write-Host ""
}

# ============================================
# 1. 사전 확인
# ============================================
Print-Step "Checking prerequisites..."

# kubectl 확인
if (!(Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Print-Error "kubectl is not installed"
    exit 1
}

# 클러스터 연결 확인
kubectl cluster-info | Out-Null
if ($LASTEXITCODE -ne 0) {
    Print-Error "Cannot connect to Kubernetes cluster"
    exit 1
}

Write-Host "✓ kubectl is installed" -ForegroundColor Green
Write-Host "✓ Connected to Kubernetes cluster" -ForegroundColor Green
Write-Host ""

# ============================================
# 2. Namespace 및 기본 설정
# ============================================
Print-Step "Creating namespace and base configurations..."

kubectl apply -f k8s/base/namespace.yaml
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secret.yaml

Write-Host "✓ Namespace and configurations created" -ForegroundColor Green
Write-Host ""

# ============================================
# 3. 인프라 서비스 배포
# ============================================
Print-Step "Deploying infrastructure services..."

# Zookeeper
Print-Step "Deploying Zookeeper..."
kubectl apply -f k8s/infra/zookeeper.yaml
Start-Sleep -Seconds 5
Wait-ForPods -Label "app=zookeeper" -Timeout 120

# Kafka
Print-Step "Deploying Kafka..."
kubectl apply -f k8s/infra/kafka.yaml
Start-Sleep -Seconds 10
Wait-ForPods -Label "app=kafka" -Timeout 120

# MySQL
Print-Step "Deploying MySQL..."
kubectl apply -f k8s/infra/mysql.yaml
Start-Sleep -Seconds 5
Wait-ForPods -Label "app=mysql" -Timeout 120

# MongoDB
Print-Step "Deploying MongoDB..."
kubectl apply -f k8s/infra/mongodb.yaml
Start-Sleep -Seconds 5
Wait-ForPods -Label "app=mongodb" -Timeout 120

Write-Host "✓ Infrastructure services deployed successfully" -ForegroundColor Green
Write-Host ""

# ============================================
# 4. 애플리케이션 서비스 배포
# ============================================
Print-Step "Deploying application services..."

# Employee Service
Print-Step "Deploying Employee Service..."
kubectl apply -f k8s/apps/employee-service.yaml
Start-Sleep -Seconds 5

# Approval Request Service
Print-Step "Deploying Approval Request Service..."
kubectl apply -f k8s/apps/approval-request-service.yaml
Start-Sleep -Seconds 5

# Approval Processing Service
Print-Step "Deploying Approval Processing Service..."
kubectl apply -f k8s/apps/approval-processing-service.yaml
Start-Sleep -Seconds 5

# Notification Service
Print-Step "Deploying Notification Service..."
kubectl apply -f k8s/apps/notification-service.yaml
Start-Sleep -Seconds 5

Write-Host "✓ Application services deployment initiated" -ForegroundColor Green
Write-Host ""

# 애플리케이션이 준비될 때까지 대기
Print-Step "Waiting for application services to be ready..."
Wait-ForPods -Label "app=employee-service" -Timeout 180
Wait-ForPods -Label "app=approval-request-service" -Timeout 180
Wait-ForPods -Label "app=approval-processing-service" -Timeout 180
Wait-ForPods -Label "app=notification-service" -Timeout 180

Write-Host "✓ All application services are ready" -ForegroundColor Green
Write-Host ""

# ============================================
# 5. Ingress 및 HPA 배포
# ============================================
Print-Step "Deploying Ingress and HPA..."

kubectl apply -f k8s/base/ingress.yaml
kubectl apply -f k8s/base/hpa.yaml

Write-Host "✓ Ingress and HPA deployed" -ForegroundColor Green
Write-Host ""

# ============================================
# 6. 배포 상태 확인
# ============================================
Print-Step "Deployment Summary:"
Write-Host ""

Write-Host "Pods:" -ForegroundColor Cyan
kubectl get pods -n erp-system
Write-Host ""

Write-Host "Services:" -ForegroundColor Cyan
kubectl get svc -n erp-system
Write-Host ""

Write-Host "Ingress:" -ForegroundColor Cyan
kubectl get ingress -n erp-system
Write-Host ""

Write-Host "HPA:" -ForegroundColor Cyan
kubectl get hpa -n erp-system
Write-Host ""

# ============================================
# 완료
# ============================================
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Deployment completed successfully!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:"
Write-Host "1. Check pod logs: kubectl logs -f deployment/<service-name> -n erp-system"
Write-Host "2. Access services via Ingress: kubectl get ingress -n erp-system"
Write-Host "3. Monitor HPA: kubectl get hpa -n erp-system -w"
Write-Host ""
Write-Host "For more information, see k8s/README.md"
