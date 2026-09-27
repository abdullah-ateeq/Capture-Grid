# Capture Grid

<p align="center">
  <b>A polished local two-player Android strategy board game inspired by Go/Weiqi rules.</b>
</p>

---

## 📖 Overview

**Capture Grid** is a modern Android board game built with **Jetpack Compose** and **Material 3**. Two players take turns placing pieces on a 7×7 grid, aiming to surround opposing groups, capture pieces, and control the board.

Designed for offline play with no ads, no trackers, and no account requirements.

---

## ✨ Features

- ⚔️ **Local 2-Player Match**: Play head-to-head on the same device (Player 1 Diamond vs. Player 2 Circle).
- 🧠 **Deep Tactical Gameplay**: Features orthogonal group connectivity, liberty calculation, and group capture mechanics inspired by Go.
- 🎨 **Board Themes**: Switch between Classic and Midnight board visual themes.
- 🔊 **Audio & Haptics**: Procedurally generated dynamic sound effects and tactile feedback on placements and captures.
- 📊 **Game Statistics**: Tracks total games played, wins per player, draws, and most captures in a single match using Jetpack DataStore.
- 📖 **Interactive How-to-Play Guide**: Visual board diagrams demonstrating group liberties, captures, and self-capture rules.
- 🌓 **Light & Dark Mode**: Full system/manual dark mode support built on Material 3 design tokens.
- 📴 **100% Offline & Private**: Zero advertisements, tracking, or network requests required.

---

## 📜 Rules of the Game

1. **Turns**: Players alternate placing one piece on any empty grid cell. Player 1 (Diamond) goes first.
2. **Groups**: Pieces directly adjacent up, down, left, or right form a single connected group. Diagonal pieces do not connect.
3. **Liberties**: Empty cells adjacent to a group are its liberties.
4. **Captures**: When a group has **0 liberties remaining**, it is captured and removed from the board. Each captured piece awards 1 point.
5. **Self-Capture (Suicide) Rule**: You cannot place a piece that leaves your own group with 0 liberties unless that move captures enemy pieces first.
6. **Passing**: Players may pass their turn. Two consecutive passes immediately end the game.
7. **Winning**: The match ends when the board is full or both players pass. The player with the **most captures** wins. If scores are tied, the player with **more pieces remaining on the board** wins.

---

## 🏗️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Navigation**: Jetpack Navigation Compose
- **Architecture**: Unidirectional Data Flow (UDF) with `ViewModel` & Kotlin `StateFlow`
- **Data Persistence**: Jetpack DataStore Preferences
- **Audio**: SoundPool API for minimal-latency sound feedback
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 37 (Android 15+)

### Directory Structure

```
app/src/main/java/com/example/capturegrid/
├── data/           # Repositories & DataStore settings management
├── feedback/       # Audio (SoundPool) and haptic feedback system
├── game/           # Core game engine, board rules, and state models
├── navigation/     # Jetpack Navigation Host setup
├── ui/
│   ├── components/ # Reusable UI components (MiniBoard, PlayerToken, etc.)
│   ├── screens/    # Game, Home, Settings, HowToPlay, Result, and About screens
│   └── theme/      # Material 3 Color palette, Typography, and Theme definitions
```

---

## 🛠️ Getting Started

### Prerequisites

- **Android Studio** (Ladybug or newer recommended)
- **JDK**: 11 or 17
- **Android SDK**: API Level 37

### Build and Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Capture-Grid.git
   cd Capture-Grid
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
