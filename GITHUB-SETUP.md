# Living Trust App - GitHub Setup Script

## Quick Setup (Run in PowerShell as Administrator)

```powershell
# 1. Install GitHub CLI
winget install GitHub.cli

# 2. Restart terminal, then authenticate
gh auth login

# 3. Create repo and push (run from project folder)
cd "C:\Users\smaeu\.openclaw\workspace\Projects\Living-Trust-App"

# Create repo on GitHub
gh repo create Living-Trust-App --public --source=. --push

# 4. Create feature branch
git checkout -b feature/initial-setup
git push -u origin feature/initial-setup

# 5. Create PR
gh pr create --title "Initial Living Trust App Setup" --body "Full mobile app with React Native + Node.js backend"

# 6. View PR
gh pr view --web
```

## Current Status

✅ **Local Git:** Ready (2 commits)
- `8bf4983` - Initial commit
- `72ff103` - Fix: Correct import paths
- `f96f9fe` - Fix: TypeScript paths, navigation types

⏳ **GitHub:** Need to install `gh` CLI

---

## What Will Be Pushed

| Branch | Description |
|--------|-------------|
| `main` | Initial project structure |
| `feature/initial-setup` | Full app code |

## After PR Approval

Merge in GitHub, then pull:
```bash
git checkout main
git pull origin main
```
