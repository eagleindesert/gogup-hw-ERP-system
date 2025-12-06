#!/bin/bash

# ============================================
# ERP 시스템 Docker 이미지 빌드 및 푸시 스크립트
# ============================================

set -e

echo "=========================================="
echo "Docker Image Build & Push Script"
echo "=========================================="
echo ""

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 레지스트리 설정 (환경변수로 설정 가능)
if [ -z "$REGISTRY" ]; then
    echo -e "${YELLOW}REGISTRY 환경변수가 설정되지 않았습니다.${NC}"
    echo "사용 예: export REGISTRY=your-dockerhub-username"
    echo "또는: export REGISTRY=yourregistry.azurecr.io"
    echo ""
    read -p "Docker Registry를 입력하세요: " REGISTRY
fi

# 버전 태그 설정
if [ -z "$VERSION" ]; then
    VERSION="latest"
fi

echo "Registry: $REGISTRY"
echo "Version: $VERSION"
echo ""
read -p "계속하시겠습니까? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 1
fi

# 함수: 이미지 빌드 및 푸시
build_and_push() {
    local service_name=$1
    local build_context=$2
    local dockerfile=$3
    
    echo -e "${GREEN}Building ${service_name}...${NC}"
    docker build -f ${dockerfile} -t ${REGISTRY}/${service_name}:${VERSION} ${build_context}
    
    echo -e "${GREEN}Pushing ${service_name}...${NC}"
    docker push ${REGISTRY}/${service_name}:${VERSION}
    
    echo -e "${GREEN}✓ ${service_name} completed${NC}"
    echo ""
}

# ============================================
# 1. Employee Service
# ============================================
build_and_push "employee-service" "./employee-service/demo" "./employee-service/demo/Dockerfile"

# ============================================
# 2. Approval Request Service
# ============================================
build_and_push "approval-request-service" "." "./approval-request-service/demo/Dockerfile"

# ============================================
# 3. Approval Processing Service
# ============================================
build_and_push "approval-processing-service" "." "./approval-processing-service/demo/Dockerfile"

# ============================================
# 4. Notification Service
# ============================================
build_and_push "notification-service" "./notification-service/demo" "./notification-service/demo/Dockerfile"

# ============================================
# 완료
# ============================================
echo "=========================================="
echo -e "${GREEN}All images built and pushed successfully!${NC}"
echo "=========================================="
echo ""
echo "Images:"
echo "- ${REGISTRY}/employee-service:${VERSION}"
echo "- ${REGISTRY}/approval-request-service:${VERSION}"
echo "- ${REGISTRY}/approval-processing-service:${VERSION}"
echo "- ${REGISTRY}/notification-service:${VERSION}"
echo ""
echo "다음 단계:"
echo "1. k8s/apps/*.yaml 파일의 image 필드를 위 경로로 업데이트하세요"
echo "2. k8s/deploy.sh 스크립트로 배포하세요"
