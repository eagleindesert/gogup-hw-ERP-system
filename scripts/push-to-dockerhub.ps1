# =============================================================
# Docker Hub Push Script for ERP Microservices (PowerShell)
# =============================================================
# Usage: .\push-to-dockerhub.ps1 -Username "dockerhub-username" -Version "1.0.0"
# Example: .\push-to-dockerhub.ps1 -Username "eagleindesert" -Version "1.0.0"
# =============================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$Username = "eagleindesert",
    
    [Parameter(Mandatory=$false)]
    [string]$Version = "latest"
)

# Configuration
$Services = @(
    "employee-service",
    "approval-request-service",
    "approval-processing-service",
    "notification-service"
)

# Error handling
$ErrorActionPreference = "Stop"

function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

function Write-Header {
    param([string]$Title)
    Write-ColorOutput "======================================" -Color Green
    Write-ColorOutput $Title -Color Green
    Write-ColorOutput "======================================" -Color Green
}

# Main script
Write-Header "Docker Hub Push Script"
Write-ColorOutput "Docker Hub Username: $Username" -Color Yellow
Write-ColorOutput "Version Tag: $Version" -Color Yellow
Write-Host ""

# Check if Docker is running
try {
    docker info | Out-Null
} catch {
    Write-ColorOutput "Error: Docker is not running!" -Color Red
    exit 1
}

# Login to Docker Hub
Write-ColorOutput "Logging in to Docker Hub..." -Color Yellow
docker login
if ($LASTEXITCODE -ne 0) {
    Write-ColorOutput "Error: Docker login failed!" -Color Red
    exit 1
}

# Build and push each service
foreach ($Service in $Services) {
    Write-Host ""
    Write-Header "Processing: $Service"
    
    $ImageName = "$Username/$Service"
    
    # Determine Dockerfile location
    if ($Service -eq "employee-service" -or $Service -eq "notification-service") {
        $DockerfilePath = "./$Service/demo/Dockerfile"
        $BuildContext = "./$Service/demo"
    } else {
        $DockerfilePath = "./$Service/demo/Dockerfile"
        $BuildContext = "."
    }
    
    Write-ColorOutput "Building image: ${ImageName}:${Version}" -Color Yellow
    
    # Build image
    docker build -t "${ImageName}:${Version}" `
                 -t "${ImageName}:latest" `
                 -f $DockerfilePath `
                 $BuildContext
    
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "✓ Build successful" -Color Green
    } else {
        Write-ColorOutput "✗ Build failed for $Service" -Color Red
        exit 1
    }
    
    # Push with version tag
    Write-ColorOutput "Pushing ${ImageName}:${Version}..." -Color Yellow
    docker push "${ImageName}:${Version}"
    
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "✓ Push successful ($Version)" -Color Green
    } else {
        Write-ColorOutput "✗ Push failed for ${Service}:${Version}" -Color Red
        exit 1
    }
    
    # Push latest tag
    Write-ColorOutput "Pushing ${ImageName}:latest..." -Color Yellow
    docker push "${ImageName}:latest"
    
    if ($LASTEXITCODE -eq 0) {
        Write-ColorOutput "✓ Push successful (latest)" -Color Green
    } else {
        Write-ColorOutput "✗ Push failed for ${Service}:latest" -Color Red
        exit 1
    }
}

Write-Host ""
Write-Header "All services pushed successfully!"
Write-Host ""
Write-ColorOutput "Pushed images:" -Color White
foreach ($Service in $Services) {
    Write-ColorOutput "  ✓ $Username/${Service}:$Version" -Color Green
    Write-ColorOutput "  ✓ $Username/${Service}:latest" -Color Green
}
Write-Host ""
Write-ColorOutput "You can now pull these images with:" -Color Yellow
Write-ColorOutput "  docker pull $Username/<service-name>:$Version" -Color White
Write-Host ""
