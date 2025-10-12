# Main Branch Backups

This directory contains documentation for backups of the main branch.

## Purpose

Backups are created to preserve important states of the main branch before significant changes or releases.

## Backup Naming Convention

Backups follow the format: `bak-yyyyMMdd`

Example: `bak-20251012` represents a backup created on October 12, 2025.

## Available Backups

| Backup Name | Date | Commit SHA | Notes |
|------------|------|------------|-------|
| bak-20251012 | 2025-10-12 | cfa2c59a31a05d072b4d58097e57ff265169c486 | Initial backup of main branch |

## How to Use Backups

### View Available Backups

```bash
# List all backup tags
git tag -l "bak-*"

# List all backup branches
git branch -a | grep "bak-"
```

### Restore from a Backup

```bash
# Checkout a specific backup
git checkout bak-20251012

# Create a new branch from a backup
git checkout -b my-restore-branch bak-20251012
```

### Create a New Backup

Use the backup script:

```bash
./scripts/backup-main-branch.sh
```

This will:
1. Fetch the latest main branch
2. Create a backup branch with format `bak-yyyyMMdd`
3. Create a backup tag with the same name
4. Generate documentation for the backup

## Backup Strategy

- Backups are created before major releases
- Backups are created before significant refactoring
- Backups are created on request
- Each backup preserves a snapshot of the main branch at a specific point in time

## Notes

- Backups are read-only references to specific commits
- Do not make changes directly to backup branches
- To work with a backup, create a new branch from it
