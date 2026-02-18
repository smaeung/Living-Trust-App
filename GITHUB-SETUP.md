# Living Trust App - GitHub Setup

## Quick Setup

```powershell
# 1. Install GitHub CLI
winget install GitHub.cli

# 2. Authenticate
gh auth login

# 3. Clone and setup
git clone https://github.com/smaeung/Living-Trust-App.git
cd Living-Trust-App

# 4. Create feature branch
git checkout -b feature/your-feature

# 5. Push and create PR
git add .
git commit -m "Your changes"
git push -u origin feature/your-feature

# 6. Create PR
gh pr create --title "Your Feature" --body "Description"
```

## Current Status

| Branch | Status | Description |
|--------|--------|-------------|
| `master` | ✅ Pushed | Initial structure |
| `feature/v1.0.0-complete` | ✅ Pushed | Full app code |

## After Merging

```bash
git checkout main
git pull origin main
```
