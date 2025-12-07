#!/bin/bash

# =============================================================
# Kubernetes 배포 스크립트
# =============================================================
# Usage: ./deploy-k8s.sh [apply|delete]
# =============================================================

set -e

ACTION="${1:-apply}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

function print_header() {
    echo -e "${GREEN}======================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}======================================${NC}"
}

print_header "ERP Microservices - Kubernetes Deployment"

# Kubernetes 매니페스트 순서
MANIFESTS=(
    "namespace.yaml"
    "mysql.yaml"
    "mongodb.yaml"
    "zookeeper.yaml"
    "kafka.yaml"
    "employee-service.yaml"
    "approval-request-service.yaml"
    "approval-processing-service.yaml"
    "notification-service.yaml"
)

K8S_PATH="./k8s"

if [ "$ACTION" == "delete" ]; then
    echo -e "${YELLOW}Deleting all resources...${NC}"
    # Reverse order for deletion
    for ((i=${#MANIFESTS[@]}-1; i>=0; i--)); do
        MANIFEST="${MANIFESTS[$i]}"
        FILE_PATH="$K8S_PATH/$MANIFEST"
        
        if [ -f "$FILE_PATH" ]; then
            echo -e "\n${CYAN}$ACTION $MANIFEST...${NC}"
            kubectl $ACTION -f "$FILE_PATH"
        else
            echo -e "${YELLOW}Warning: $FILE_PATH not found${NC}"
        fi
    done
else
    for MANIFEST in "${MANIFESTS[@]}"; do
        FILE_PATH="$K8S_PATH/$MANIFEST"
        
        if [ -f "$FILE_PATH" ]; then
            echo -e "\n${CYAN}$ACTION $MANIFEST...${NC}"
            kubectl $ACTION -f "$FILE_PATH"
        else
            echo -e "${YELLOW}Warning: $FILE_PATH not found${NC}"
        fi
    done
fi

echo ""
print_header "Deployment Complete!"

if [ "$ACTION" == "apply" ]; then
    echo -e "\n${CYAN}Checking pod status...${NC}"
    kubectl get pods -n erp-system
    
    echo -e "\n${CYAN}Checking services...${NC}"
    kubectl get svc -n erp-system
    
    echo -e "\n${YELLOW}Useful commands:${NC}"
    echo -e "  kubectl get pods -n erp-system"
    echo -e "  kubectl logs <pod-name> -n erp-system"
    echo -e "  kubectl describe pod <pod-name> -n erp-system"
    echo -e "  kubectl port-forward svc/employee-service 8081:8081 -n erp-system"
fi
