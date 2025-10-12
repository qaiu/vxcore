#!/bin/bash

###############################################################################
# Main Branch Backup Script
# 
# This script creates a backup of the main branch with format bak-yyyyMMdd
###############################################################################

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Main Branch Backup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Get current date in yyyyMMdd format
BACKUP_DATE=$(date +%Y%m%d)
BACKUP_NAME="bak-${BACKUP_DATE}"

echo -e "${YELLOW}Backup name: ${BACKUP_NAME}${NC}"
echo ""

# Fetch latest main branch
echo -e "${YELLOW}Fetching latest main branch...${NC}"
git fetch origin main

# Create backup branch from main
echo -e "${YELLOW}Creating backup branch from main...${NC}"
if git show-ref --verify --quiet "refs/heads/${BACKUP_NAME}"; then
    echo -e "${YELLOW}Backup branch ${BACKUP_NAME} already exists${NC}"
else
    git branch "${BACKUP_NAME}" origin/main
    echo -e "${GREEN}✓ Created backup branch: ${BACKUP_NAME}${NC}"
fi

# Create backup tag from main
echo -e "${YELLOW}Creating backup tag from main...${NC}"
if git tag -l "${BACKUP_NAME}" | grep -q "${BACKUP_NAME}"; then
    echo -e "${YELLOW}Backup tag ${BACKUP_NAME} already exists${NC}"
else
    git tag "${BACKUP_NAME}" origin/main
    echo -e "${GREEN}✓ Created backup tag: ${BACKUP_NAME}${NC}"
fi

# Get commit information
COMMIT_SHA=$(git rev-parse origin/main)
COMMIT_MSG=$(git log origin/main --oneline -1)

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Backup Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Backup Name: ${GREEN}${BACKUP_NAME}${NC}"
echo -e "Source: ${GREEN}origin/main${NC}"
echo -e "Commit: ${GREEN}${COMMIT_MSG}${NC}"
echo ""

# Create or update backup documentation
BACKUP_DOC_DIR="docs/backups"
mkdir -p "${BACKUP_DOC_DIR}"

BACKUP_DOC="${BACKUP_DOC_DIR}/${BACKUP_NAME}.md"

cat > "${BACKUP_DOC}" << EOF
# Main Branch Backup - $(date +%Y-%m-%d)

## Backup Information

- **Backup Date**: $(date +%Y-%m-%d)
- **Backup Name**: ${BACKUP_NAME}
- **Source Branch**: main
- **Commit SHA**: ${COMMIT_SHA}
- **Commit Message**: ${COMMIT_MSG}

## Backup Details

This backup preserves the state of the main branch as of $(date +%Y-%m-%d).

### How to Restore from Backup

If you need to restore or reference this backup:

\`\`\`bash
# View the backup tag
git tag -l ${BACKUP_NAME}

# View the backup branch (if created)
git branch -a | grep ${BACKUP_NAME}

# Checkout the backup
git checkout ${BACKUP_NAME}

# Or create a new branch from the backup
git checkout -b restore-from-${BACKUP_NAME} ${BACKUP_NAME}
\`\`\`

### Backup Created By

Automated backup script on $(date +%Y-%m-%d)

## Notes

This backup was created to preserve the main branch state before any potential changes or updates.
EOF

echo -e "${GREEN}✓ Created backup documentation: ${BACKUP_DOC}${NC}"
echo ""

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  Next Steps${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo -e "The backup has been created locally. To push to remote:"
echo ""
echo -e "${BLUE}1. Push the backup branch:${NC}"
echo -e "   git push origin ${BACKUP_NAME}"
echo ""
echo -e "${BLUE}2. Push the backup tag:${NC}"
echo -e "   git push origin ${BACKUP_NAME}"
echo ""
echo -e "${BLUE}3. Commit the backup documentation:${NC}"
echo -e "   git add ${BACKUP_DOC}"
echo -e "   git commit -m 'docs: add backup documentation for ${BACKUP_NAME}'"
echo -e "   git push"
echo ""
