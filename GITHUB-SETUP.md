# Living Trust App - GitHub Setup

## Quick Setup

```powershell
# 1. Install GitHub CLI
winget install GitHub.cli

# 2. Authenticate
gh auth login

# 3. Clone the repo
git clone https://github.com/<username>/Living-Trust-App.git
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

## After Merging

```bash
git checkout main
git pull origin main
```
