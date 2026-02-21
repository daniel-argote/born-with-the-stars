This repository is a simple Java 2D tile-based game (no build tool). The guidance below helps AI coding agents work productively in this codebase.

- Purpose: Support small, iterative changes to gameplay, tiles, UI, debug helpers, and resource loading. Prioritize minimal, local edits consistent with existing package structure.

- Project layout (important):
  - `src/main` : app entry and game loop (`Main.java`, `GamePanel.java`, `KeyHandler.java`, `CollisionChecker.java`).
  - `src/entity` : `Player.java`, `Entity.java` — player and entity logic.
  - `src/tile` : tile data and drawing (`TileManager.java`, `Tile.java`, `TileData.java`). Tile sheets are sliced in `TileManager.sliceSheet()`.
  - `src/ui` : rendering HUD and minimap (`UI.java`).
  - `res/` and `maps/` : runtime resources referenced via getResourceAsStream (paths like `/maps/world_map.txt`, `/tiles/...`, `/player/cursor_arrow.png`).

- Big-picture architecture and runtime behavior:
  - Single-window Swing app started from `main.Main`. `GamePanel` holds core state (tile manager, player, UI) and runs the game thread.
  - Rendering order: tiles (via `TileManager.draw`) → player → UI. Zoom and camera math are implemented in `GamePanel` + `TileManager.draw()`.
  - Resources are loaded with `getClass().getResourceAsStream("/...")` — resources must be available on the classpath root with the same path.

- Build & run (manual):
  - Compile (from project root):
    - Windows: `javac -d bin -sourcepath src src\main\Main.java src\main\GamePanel.java src\main\KeyHandler.java src\entity\*.java src\tile\*.java src\ui\*.java src\utils\*.java`
  - Run (from project root):
    - Windows: `java -cp bin main.Main`
  - Note: If resources fail to load, copy resource folders into `bin` so classpath lookups like `/maps/world_map.txt` resolve.

- Project-specific patterns & gotchas:
  - Tile indexing: `TileManager.sliceSheet()` maps tiles into numeric ID ranges (e.g., grass 0–63, ocean ranges 800–1119, coastline 1120–1183). Respect those ranges when editing map files.
  - Map files are space-separated rows under `/maps/world_map.txt`. `TileManager.loadMap()` expects `gp.maxWorldCol` columns per row.
  - Zoom: `GamePanel.scale` is applied when drawing tiles — ensure math in `TileManager.draw()` remains consistent if changing tileSize or camera.
  - Input: movement uses WASD, zoom modifier `Z`, minimap toggles `M` (on/off) and `N` (local/full view) in `KeyHandler.java`.

- Debugging tips for AI agents:
  - Prefer small changes and run locally. Use `System.out.println` or existing `System.out` messages (e.g., missing resource prints in `TileManager`) to detect missing assets.
  - Common runtime failure: resource not on classpath. Verify resource presence at `res/maps/world_map.txt`, `res/tiles/`, `res/player/` and copy into `bin` before running.
  - Visual checks: UI rendering occurs last — use temporary overlays or color changes in `UI.draw()` to highlight state.

- When editing code:
  - Keep package structure and public APIs stable. Update only the smallest set of files required to implement a change.
  - If touching tile assets or map layout, include a short note in the PR explaining the ID ranges used and how to regenerate slices.
  - Prioritize clean, abstract design patterns (e.g., generic classes like `OBJ_Environment` or `OBJ_NPC` over specific subclasses) to avoid class explosion and maintain scalability.

- Examples to reference when making changes:
  - Camera & loop: [src/main/GamePanel.java](src/main/GamePanel.java)
  - Tile slicing & map load: [src/tile/TileManager.java](src/tile/TileManager.java)
  - UI & minimap: [src/ui/UI.java](src/ui/UI.java)
  - Entrypoint: [src/main/Main.java](src/main/Main.java)

If anything here is unclear or you'd like the instructions to emphasize other workflows (IDE setup, unit tests, packaging), tell me which areas to expand. 