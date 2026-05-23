# Changelog

All notable changes to Snake Classic will be tracked here.

## v0.7.0 — 2026-05-22

### Added
- Local Leaderboard screen from the main menu.
- Leaderboard rows for all six difficulty + wall mode combinations.
- Share Scores button using the Android share sheet.
- Player name selector for shared score text.
- Test-covered local leaderboard row/share text model.

### Changed
- Main menu now includes a dedicated Leaderboard button.

## v0.6.0 — 2026-05-22

### Added
- Wall mode option with **Solid walls** and **Wrap** gameplay.
- Wrap mode lets the snake exit one wall and re-enter from the opposite side.
- Separate local high scores per difficulty and wall mode combination:
  - Easy / Solid walls
  - Easy / Wrap
  - Normal / Solid walls
  - Normal / Wrap
  - Hard / Solid walls
  - Hard / Wrap

### Fixed
- Fixed compact layout issue where food could appear outside the playable map after board-affecting option changes.
- Added board-boundary guard before drawing food.

### Changed
- Board-affecting settings now reset the active game from pause: difficulty, layout, wall mode.
- Controls, theme, sound, and ads can be changed from pause without resetting the active game.

## v0.5.0 — 2026-05-22

### Fixed
- Shifted Friends screen controls lower to reduce overlap.

### Changed
- Only difficulty changes reset a paused game, instead of every option change.
- Added warning text when opening Options from a paused game.

## v0.4.0 — 2026-05-22

### Added
- In-game Pause button.
- Pause menu with Resume, Settings, and Menu actions.
- QR scanner placeholder button in Friends screen.

### Changed
- Settings can be opened from pause.
- Friends QR/code placement adjusted.

## v0.3.0 — 2026-05-22

### Added
- Friends screen.
- Generated 6-character friend/leaderboard code.
- Android share sheet invite text.
- QR-style placeholder visual.
- Join-code placeholder.

## v0.2.0 — 2026-05-22

### Added
- Main menu and Options menu.
- Control options: Swipe, on-screen Buttons, Tap-turn.
- Difficulty options: Easy, Normal, Hard.
- Theme options: Neon, Classic, Ocean.
- Layout options: Tall, Compact.
- Sound effects toggle.
- Ad placeholder toggle.

## v0.1.0 — 2026-05-22

### Added
- Initial Android Snake prototype.
- Classic grid-based Snake gameplay.
- Swipe controls.
- Score counter.
- Local high score.
- Game over and restart flow.
