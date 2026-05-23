# Snake Classic Roadmap

**Goal:** Build Snake Classic into a cross-platform mobile game on Android and iOS with shared leaderboards between both platforms.

**Current status:** Android native Java prototype, public GitHub repo, releases v0.1.0 through v0.6.0 available with APKs.

**Guiding rule:** Every version updates `CHANGELOG.md`. Before pushing/publishing any new version, ask: **Is this version ready for GitHub?**

---

## Product Direction

Snake Classic should become:

- A simple polished Snake game.
- Available on Google Play Store and Apple App Store.
- Playable offline.
- Able to share leaderboard groups between Android and iPhone.
- Simple enough to maintain without a large backend at first.

---

## Architecture Recommendation

### Short term

Keep improving the current Android version because it is already testable and on GitHub.

### Medium term

Add a backend for cross-platform accounts/leaderboards before starting the Apple port. This gives both platforms the same leaderboard model.

Recommended backend:

```text
Supabase
- Postgres database
- Auth later if needed
- REST API / Edge Functions
- Row Level Security
```

Why Supabase:

- Works with Android and iOS.
- Easy leaderboard tables.
- Friend-code groups are straightforward.
- Can start free/cheap.
- Avoids locking leaderboards to only Google Play Games or Apple Game Center.

### Long term

For iOS, either:

1. **Port the game to a cross-platform engine** — recommended before App Store release.
2. Maintain separate native Android and iOS versions — possible, but more duplicate work.

Recommended future app stack:

```text
Godot or Flutter
```

Current recommendation: **Godot** if Snake Classic stays game-focused.

Reason:

- Better game tooling than raw Android/iOS views.
- Can export to Android and iOS.
- Keeps gameplay code shared.
- Easier to add animations, sounds, menus, effects, and themes.

Important iOS note:

```text
iOS App Store builds require macOS + Xcode + Apple Developer account.
```

WSL can help with code/planning, but final iOS signing/submission needs a Mac or cloud macOS build service.

---

## Leaderboard Strategy

### Phase 1 — Local leaderboard

Add a real local leaderboard screen:

- Per difficulty.
- Per wall mode.
- Player display name.
- Share scores using Android share sheet.

Modes:

```text
Easy / Solid walls
Easy / Wrap
Normal / Solid walls
Normal / Wrap
Hard / Solid walls
Hard / Wrap
```

### Phase 2 — Online friend-code leaderboard

Use shared backend tables.

Core features:

- Create leaderboard group.
- Generate friend code.
- Join group by code.
- Submit score.
- View leaderboard by group + difficulty + wall mode.
- Share invite text.
- QR code invite.

Example:

```text
Group: MEXSNAKE
Mode: Normal / Wrap

1. Chris — 42
2. Alex — 39
3. Mike — 31
```

### Phase 3 — Cross-platform leaderboard

Android and iOS use the same backend:

```text
Android app  ─┐
              ├── Supabase leaderboard API ── Postgres
 iOS app     ─┘
```

### Phase 4 — Optional platform-native extras

After the shared leaderboard works:

- Android: Google Play Games achievements/leaderboards.
- iOS: Apple Game Center achievements/leaderboards.

These should be extras, not the primary leaderboard, because Google and Apple leaderboards do not naturally share one unified friend group.

---

## Store Strategy

### Google Play Store

Needed before production:

- Google Play Console developer account.
- Release build as `.aab`.
- App icon.
- Feature graphic.
- Screenshots.
- Privacy policy.
- Data safety form.
- Internal/closed testing first.
- Current target API compliance.

Current Android config already targets SDK 35, which is a good foundation.

### Apple App Store

Needed before production:

- Apple Developer Program membership.
- Mac/Xcode build environment or cloud macOS CI.
- Bundle ID.
- App Store Connect app record.
- App icon.
- Screenshots for required device sizes.
- Privacy nutrition labels.
- Privacy policy.
- TestFlight beta testing.
- App Review submission.

If we add third-party login later, Sign in with Apple may be required on iOS. For now, friend-code groups can avoid that complexity.

---

## Version Roadmap

### v0.7.0 — Local leaderboard screen

Goal: Make the existing high-score system visible.

Tasks:

- Add `Leaderboard` button to main menu.
- Add leaderboard screen showing all mode/difficulty high scores.
- Add player name setting.
- Add `Share Scores` button.
- Update `CHANGELOG.md`.
- Build APK.
- Ask if ready for GitHub.

Acceptance:

- User can see all local high scores in one place.
- Each difficulty + wall mode has its own score.
- User can share a text summary.

### v0.8.0 — Real QR code generation

Goal: Replace QR placeholder with a real generated QR image.

Tasks:

- Generate QR code from friend code/invite string.
- Keep written code below QR.
- Improve Friends screen spacing.
- Add share invite polish.
- Update `CHANGELOG.md`.
- Build APK.
- Ask if ready for GitHub.

Acceptance:

- QR code is scannable by another phone camera.
- Written friend code remains visible.

### v0.9.0 — Online leaderboard backend prototype

Goal: Prove Android can create/join a real online leaderboard group.

Tasks:

- Create Supabase project.
- Add database schema.
- Add score submission API.
- Add group creation/join flow.
- Add basic error handling.
- Add privacy-safe device/player identifier.
- Update privacy notes.
- Update `CHANGELOG.md`.
- Build APK.
- Ask if ready for GitHub.

Acceptance:

- Two Android devices can join the same friend code and see shared scores.

### v0.10.0 — Online leaderboard polish

Goal: Make leaderboard usable for real friends.

Tasks:

- Add player display names.
- Add top scores by group/mode.
- Add loading/error states.
- Add score submission rules.
- Add basic anti-spam/anti-cheat protections.
- Add weekly/all-time filter if simple.
- Update `CHANGELOG.md`.
- Build APK.
- Ask if ready for GitHub.

Acceptance:

- Friend leaderboard feels usable and reliable.

### v0.11.0 — Store-readiness polish

Goal: Prepare Android version for Google Play testing.

Tasks:

- Improve app icon.
- Improve menu/UI polish.
- Add screenshots.
- Add privacy policy draft.
- Create release `.aab` build.
- Add Google Play internal testing checklist.
- Update `CHANGELOG.md`.
- Build APK/AAB.
- Ask if ready for GitHub.

Acceptance:

- Android app is ready for Play Console internal testing.

### v1.0.0 — Android store candidate

Goal: Submit first Android store candidate.

Tasks:

- Finalize Play Store listing.
- Finalize privacy/data safety details.
- Upload `.aab`.
- Run internal/closed test.
- Fix blockers.
- Tag release candidate.
- Ask before GitHub release and store submission.

Acceptance:

- Android app is submitted to Google Play testing/production path.

### v1.1.0 — Cross-platform engine decision/prototype

Goal: Decide the Apple port strategy and start shared code path.

Tasks:

- Compare Godot vs Flutter based on current game needs.
- Prototype Snake gameplay in chosen cross-platform stack.
- Confirm Android export still works.
- Confirm iOS export requirements.
- Document migration plan.
- Update `CHANGELOG.md` if a prototype is committed.
- Ask if ready for GitHub.

Acceptance:

- We know whether to port to Godot/Flutter or maintain native versions.

### v1.2.0 — iOS TestFlight prototype

Goal: Get Snake Classic running on iPhone through TestFlight.

Tasks:

- Set up Apple Developer account assets.
- Configure bundle ID.
- Build iOS version on macOS/Xcode.
- Connect same leaderboard backend.
- Upload to TestFlight.
- Test friend leaderboard between Android and iPhone.
- Ask before GitHub release.

Acceptance:

- Android and iPhone can share the same leaderboard group.

### v1.3.0 — iOS App Store candidate

Goal: Prepare iOS store release.

Tasks:

- Finalize iOS screenshots.
- Finalize App Store privacy labels.
- Submit to App Review.
- Fix review issues.
- Ask before GitHub release and store submission.

Acceptance:

- iOS app is submitted to Apple App Store.

---

## Backend Data Model Draft

Tables:

```text
players
- id
- display_name
- platform: android | ios
- created_at

groups
- id
- code
- name
- owner_player_id
- created_at

group_members
- group_id
- player_id
- joined_at

scores
- id
- group_id
- player_id
- score
- difficulty: easy | normal | hard
- wall_mode: solid | wrap
- app_version
- created_at
```

Leaderboard query:

```text
For each group + difficulty + wall_mode:
show best score per player, ordered descending.
```

---

## Risks / Decisions

### Apple port risk

We need macOS/Xcode for final iOS builds. Decide later whether to use:

- User-owned Mac.
- Borrowed Mac.
- Cloud macOS CI.
- Apple build service through a framework provider.

### Backend risk

Any online leaderboard needs:

- Privacy policy.
- Abuse/spam handling.
- Some anti-cheat protection.
- Database cost monitoring.

### Engine risk

The current Android Java prototype is great for learning and testing, but a cross-platform engine may be better before maintaining both Android and iOS long-term.

---

## Immediate Next Step

Build **v0.7.0 — Local leaderboard screen**.

Why:

- It builds on the current app.
- It makes the leaderboard concept visible.
- It does not require backend or Apple tooling yet.
- It prepares the UI for shared leaderboards later.
