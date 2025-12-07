# =============================================================
# Kubernetes 배포 스크립트
# =============================================================
# Usage: .\deploy-k8s.ps1
# =============================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$Action = "apply"
)

$ErrorActionPreference = "Stop"

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Write-Header {
    param([string]$Title)
    Write-ColorOutput "======================================" -Color Green
    Write-ColorOutput $Title -Color Green
    Write-ColorOutput "======================================" -Color Green
}

Write-Header "ERP Microservices - Kubernetes Deployment"

# Kubernetes 매니페스트 순서
$manifests = @(
    "namespace.yaml",
    "mysql.yaml",
    "mongodb.yaml",
    "zookeeper.yaml",
    "kafka.yaml",
    "employee-service.yaml",
    "approval-request-service.yaml",
    "approval-processing-service.yaml",
    "notification-service.yaml"
)

$k8sPath = ".\k8s"

if ($Action -eq "delete") {
    Write-ColorOutput "Deleting all resources..." -Color Yellow
    [array]::Reverse($manifests)
}

foreach ($manifest in $manifests) {
    $filePath = Join-Path $k8sPath $manifest
    
    if (Test-Path $filePath) {
        Write-ColorOutput "`n$Action $manifest..." -Color Cyan
        kubectl $Action -f $filePath
        
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput "Error: Failed to $Action $manifest" -Color Red
            exit 1
        }
    } else {
        Write-ColorOutput "Warning: $filePath not found" -Color Yellow
    }
}

Write-Host ""
Write-Header "Deployment Complete!"

if ($Action -eq "apply") {
    Write-ColorOutput "`nChecking pod status..." -Color Cyan
    kubectl get pods -n erp-system
    
    Write-Host "`n"
    Write-ColorOutput "Checking services..." -Color Cyan
    kubectl get svc -n erp-system
    
    Write-Host "`n"
    Write-ColorOutput "Useful commands:" -Color Yellow
    Write-ColorOutput "  kubectl get pods -n erp-system" -Color White
    Write-ColorOutput "  kubectl logs <pod-name> -n erp-system" -Color White
    Write-ColorOutput "  kubectl describe pod <pod-name> -n erp-system" -Color White
    Write-ColorOutput "  kubectl port-forward svc/employee-service 8081:8081 -n erp-system" -Color White
}
