# Born with the Stars

**An experiment in AI-assisted game development.**

> *"This project is as much about exploring the use of AI in software engineering as it is about making an actual game. It is a 'for fun' prototype built from scratch."*

## About the Project
**Born with the Stars** is a 2D top-down survival/exploration game currently built on a custom Java 2D engine. The code, assets, and design decisions have been heavily iterated upon using AI coding assistants (Gemini).

It is currently in a **Pre-Alpha / Prototype** state. It's not quite a full game yet, but it has a beating heart!

## Current Features
Everything here was built from scratch using standard Java libraries (`javax.swing`, `java.awt`):

*   **Custom Engine**: A tile-based rendering engine with zoom support and camera tracking.
*   **World Generation**: Procedural generation for terrain textures (Grass, Water, Sand) and object placement.
*   **Day/Night Cycle**: A full 10-minute cycle with visual darkness filters and lighting effects.
*   **Survival Systems**:
    *   **Stats**: Health, Energy, and Spirit.
    *   **Inventory**: Grid-based inventory with hotbar support.
    *   **Crafting**: Combine resources to make tools (Pickaxe, Fire Pit, Arrows).
*   **Spirit Mechanics**:
    *   **Dreams**: Sleeping near a fire can trigger dream sequences that grant buffs.
    *   **Companions**: Summon a Spirit Animal to follow you.
*   **Save/Load**: Full persistence of player state, inventory, and world objects to local files.

## How to Play
### Requirements
*   Java Development Kit (JDK) 8 or higher installed.

### Running the Game
**Windows:**
Simply double-click the `PlayGame.bat` file in the root folder. It will automatically compile the latest code and launch the game window.

**Manual Compilation:**
If you prefer the command line:
```bash
javac -d bin -sourcepath src src\main\Main.java src\main\GamePanel.java src\main\KeyHandler.java src\entity\*.java src\tile\*.java src\ui\*.java
java -cp bin main.Main
```

## Controls
*   **WASD**: Move
*   **ENTER**: Attack / Interact
*   **C**: Open Crafting Menu
*   **F**: Sleep (when near a fire at night)
*   **M / N**: Toggle Mini-Map / View Mode
*   **Z**: Hold to Zoom
*   **X**: Summon Spirit Companion

## Future Plans
We are planning a major refactor to migrate the codebase to **LITIENGINE** to take advantage of a more robust 2D game framework, allowing us to focus more on gameplay and less on rendering logic.
