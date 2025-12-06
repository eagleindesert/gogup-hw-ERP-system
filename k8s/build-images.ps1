# ============================================
# ERP 시스템 Docker 이미지 빌드 및 푸시 스크립트 (PowerShell)
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Docker Image Build & Push Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 레지스트리 설정 (환경변수로 설정 가능)
if (-not $env:REGISTRY) {
    Write-Host "REGISTRY 환경변수가 설정되지 않았습니다." -ForegroundColor Yellow
    Write-Host "사용 예: `$env:REGISTRY='your-dockerhub-username'"
    Write-Host "또는: `$env:REGISTRY='yourregistry.azurecr.io'"
    Write-Host ""
    $env:REGISTRY = Read-Host "Docker Registry를 입력하세요"
}

# 버전 태그 설정
if (-not $env:VERSION) {
    $env:VERSION = "latest"
}

Write-Host "Registry: $env:REGISTRY" -ForegroundColor Cyan
Write-Host "Version: $env:VERSION" -ForegroundColor Cyan
Write-Host ""
$confirm = Read-Host "계속하시겠습니까? (y/n)"
if ($confirm -ne 'y' -and $confirm -ne 'Y') {
    exit 1
}

# 함수: 이미지 빌드 및 푸시
function Build-AndPush {
    param(
        [string]$ServiceName,
        [string]$BuildContext,
        [string]$Dockerfile
    )
    
    Write-Host "Building $ServiceName..." -ForegroundColor Green
    docker build -f $Dockerfile -t "$env:REGISTRY/${ServiceName}:$env:VERSION" $BuildContext
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to build $ServiceName" -ForegroundColor Red
        throw "Build failed"
    }
    
    Write-Host "Pushing $ServiceName..." -ForegroundColor Green
    docker push "$env:REGISTRY/${ServiceName}:$env:VERSION"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to push $ServiceName" -ForegroundColor Red
        throw "Push failed"
    }
    
    Write-Host "✓ $ServiceName completed" -ForegroundColor Green
    Write-Host ""
}

# ============================================
# 1. Employee Service
# ============================================
Build-AndPush -ServiceName "employee-service" -BuildContext "./employee-service/demo" -Dockerfile "./employee-service/demo/Dockerfile"

# ============================================
# 2. Approval Request Service
# ============================================
Build-AndPush -ServiceName "approval-request-service" -BuildContext "." -Dockerfile "./approval-request-service/demo/Dockerfile"

# ============================================
# 3. Approval Processing Service
# ============================================
Build-AndPush -ServiceName "approval-processing-service" -BuildContext "." -Dockerfile "./approval-processing-service/demo/Dockerfile"

# ============================================
# 4. Notification Service
# ============================================
Build-AndPush -ServiceName "notification-service" -BuildContext "./notification-service/demo" -Dockerfile "./notification-service/demo/Dockerfile"

# ============================================
# 완료
# ============================================
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "All images built and pushed successfully!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Images:" -ForegroundColor Cyan
Write-Host "- $env:REGISTRY/employee-service:$env:VERSION"
Write-Host "- $env:REGISTRY/approval-request-service:$env:VERSION"
Write-Host "- $env:REGISTRY/approval-processing-service:$env:VERSION"
Write-Host "- $env:REGISTRY/notification-service:$env:VERSION"
Write-Host ""
Write-Host "다음 단계:"
Write-Host "1. k8s/apps/*.yaml 파일의 image 필드를 위 경로로 업데이트하세요"
Write-Host "2. k8s/deploy.ps1 스크립트로 배포하세요"
