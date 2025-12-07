#!/bin/bash

# =============================================================
# Docker Hub Push Script for ERP Microservices
# =============================================================
# Usage: ./push-to-dockerhub.sh [dockerhub-username] [version]
# Example: ./push-to-dockerhub.sh myusername 1.0.0
# =============================================================

set -e  # Exit on error

# Configuration
DOCKERHUB_USERNAME="${1:-eagleindesert}"
VERSION="${2:-latest}"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Services to build and push
SERVICES=(
  "employee-service"
  "approval-request-service"
  "approval-processing-service"
  "notification-service"
)

echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}Docker Hub Push Script${NC}"
echo -e "${GREEN}======================================${NC}"
echo -e "Docker Hub Username: ${YELLOW}${DOCKERHUB_USERNAME}${NC}"
echo -e "Version Tag: ${YELLOW}${VERSION}${NC}"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running!${NC}"
    exit 1
fi

# Login to Docker Hub
echo -e "${YELLOW}Logging in to Docker Hub...${NC}"
docker login

# Build and push each service
for SERVICE in "${SERVICES[@]}"; do
    echo ""
    echo -e "${GREEN}======================================${NC}"
    echo -e "${GREEN}Processing: ${SERVICE}${NC}"
    echo -e "${GREEN}======================================${NC}"
    
    IMAGE_NAME="${DOCKERHUB_USERNAME}/${SERVICE}"
    
    # Determine Dockerfile location
    if [ "$SERVICE" == "employee-service" ] || [ "$SERVICE" == "notification-service" ]; then
        DOCKERFILE_PATH="./${SERVICE}/demo/Dockerfile"
        BUILD_CONTEXT="./${SERVICE}/demo"
    else
        DOCKERFILE_PATH="./${SERVICE}/demo/Dockerfile"
        BUILD_CONTEXT="."
    fi
    
    echo -e "${YELLOW}Building image: ${IMAGE_NAME}:${VERSION}${NC}"
    
    # Build image
    docker build -t "${IMAGE_NAME}:${VERSION}" \
                 -t "${IMAGE_NAME}:latest" \
                 -f "${DOCKERFILE_PATH}" \
                 "${BUILD_CONTEXT}"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Build successful${NC}"
    else
        echo -e "${RED}✗ Build failed for ${SERVICE}${NC}"
        exit 1
    fi
    
    # Push with version tag
    echo -e "${YELLOW}Pushing ${IMAGE_NAME}:${VERSION}...${NC}"
    docker push "${IMAGE_NAME}:${VERSION}"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Push successful (${VERSION})${NC}"
    else
        echo -e "${RED}✗ Push failed for ${SERVICE}:${VERSION}${NC}"
        exit 1
    fi
    
    # Push latest tag
    echo -e "${YELLOW}Pushing ${IMAGE_NAME}:latest...${NC}"
    docker push "${IMAGE_NAME}:latest"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Push successful (latest)${NC}"
    else
        echo -e "${RED}✗ Push failed for ${SERVICE}:latest${NC}"
        exit 1
    fi
done

echo ""
echo -e "${GREEN}======================================${NC}"
echo -e "${GREEN}All services pushed successfully!${NC}"
echo -e "${GREEN}======================================${NC}"
echo ""
echo -e "Pushed images:"
for SERVICE in "${SERVICES[@]}"; do
    echo -e "  ${GREEN}✓${NC} ${DOCKERHUB_USERNAME}/${SERVICE}:${VERSION}"
    echo -e "  ${GREEN}✓${NC} ${DOCKERHUB_USERNAME}/${SERVICE}:latest"
done
echo ""
echo -e "${YELLOW}You can now pull these images with:${NC}"
echo -e "  docker pull ${DOCKERHUB_USERNAME}/<service-name>:${VERSION}"
echo ""
