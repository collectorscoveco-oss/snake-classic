# GitHub Workflow for Snake Classic

## Local workflow

1. Build/test a new APK locally.
2. Update `CHANGELOG.md` with the new version.
3. Ask: **Is this version ready for GitHub?**
4. If yes, commit the version.
5. If a GitHub remote exists, push it.
6. Optionally create a GitHub release and attach the APK.

## Recommended release pattern

- Git tags: `v0.6.0`, `v0.7.0`, etc.
- GitHub releases: one release per tested version.
- Attach the tested APK to each release.

## First-time GitHub setup options

### Option A — Create remote with GitHub CLI
Requires `gh` installed and authenticated:

```bash
gh repo create snake-classic --private --source . --push
```

### Option B — Create remote manually
1. Create a new GitHub repository in the browser.
2. Copy its HTTPS URL.
3. Run:

```bash
git remote add origin https://github.com/YOUR_USERNAME/snake-classic.git
git branch -M main
git push -u origin main
```

## Current preference

Ask before every GitHub push/release so only approved versions go up.
