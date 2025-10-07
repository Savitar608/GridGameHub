# CS611-Assignment 2
## Dots and Boxes
---------------------------------------------------------------------------
- Names: Adithya Lnu, Archit Kiran Kumar  and Samahitha Chakkodbail Madhavaprasad 
- Student IDs: U41860632, U15354769, U77295190
- Emails: <adithyav@bu.edu>, <architkk@bu.edu>, <mahicm13@bu.edu>

This repository contains a small Java console project that implements two grid-based games:
- Sliding Puzzle
- Dots & Boxes

## Files
- `GameHub.java` / `GameHubApp.java` — application launcher / menu that starts games.
- `GridGame.java` — abstract base that defines the lifecycle for grid-based games (setSize, initializeGame, displayGrid, processUserInput, makeMove, checkWinCondition, displayWinMessage).
- `GameController.java` — orchestrates the game loop for any `GridGame` implementation.
- Sliding Puzzle
  - `SlidingPuzzle.java`, `SlidingPuzzleGame.java`, `SlidingPuzzlePiece.java` — sliding puzzle game implementation and pieces.
- Dots & Boxes
  - `DotsAndBoxes.java`, `DotsAndBoxesGame.java`, `DotsAndBoxesMove.java`, `DotsAndBoxesPiece.java` — Dots & Boxes implementation.
- Core & utilities
  - `Grid.java`, `Tile.java`, `GamePiece.java`, `Player.java`, `Scoreboard.java` — core data structures and player/score handling.
- IO abstractions
  - `InputService.java`, `OutputService.java`, `ConsoleInputService.java`, `ConsoleOutputService.java` — input/output are abstracted to make testing easier.
- Additional features
  - `HintSystem.java`, `GreedyBoxStrategy.java`, `MoveStrategy.java`, `TurnTimer.java`, `UndoStateManager.java`, `BoardDump.java`, `DifficultyManager.java`, `Scoreboard.java`.

---

## Notes

---

## How to compile and run
From the project root (where the .java files live), run:

```shell
# compile all sources
javac -d out -encoding UTF-8 *.java

# run the game hub (menu that launches both games)
java -cp out GameHub

# run a single game directly
java -cp out SlidingPuzzle
java -cp out DotsAndBoxes
```

Keep your working directory at the source root when compiling. If you change package names or file locations, update the classpath accordingly.

---

## Gameplay overview

Sliding Puzzle
- Goal: arrange numbered tiles in ascending order; one empty tile (value 0) represents the gap.
- Controls: enter the number of an adjacent tile to slide it into the empty space.
- Grid sizes: n x m supported (default/limits enforced in UI). Difficulty levels control shuffle complexity.

Dots & Boxes
- Grid is represented as a lattice where coordinates map to piece types:
  - even/even => DOT
  - even/odd  => H_EDGE (horizontal edge)
  - odd/even  => V_EDGE (vertical edge)
  - odd/odd   => BOX
- Players select edges by entering coordinates; completing a box awards a point and an extra turn.

For both games, the console UI is implemented via `ConsoleInputService` and `ConsoleOutputService`. The project includes prompting, validation, and a canonical `quit` token to exit.

---

## Developer notes & conventions

- Quit token: the keyword `quit` is used across the project to request exit. The helper `GridGame.isQuitCommand(String)` and `GridGame.requestExit()` are used — do not change the token without updating all call sites.
- Grid indexing: 0-based for rows and columns. Dots & Boxes exposes 0-based coordinates for edges and boxes.
- Persistence: player top scores are stored in `Player.topScores` as `Map<Integer, Map<String,Integer>>`, where the inner key is a grid key from `toGridKey(rows,cols)` in the format `"{rows}x{cols}"`.
- Adding difficulty levels: call `addDifficultyLevel(int, String)` from a `GridGame` subclass (examples are in the game constructors).
- IO abstractions: prefer injecting `InputService`/`OutputService` (constructors accept these interfaces) so tests can supply deterministic input.
- Game loop: `GameController` drives the flow — it calls `setSize()`, `initializeGame()`, `displayGrid()` and loops on `processUserInput()` until `checkWinCondition()` or `requestExit()`.

When extending or refactoring, avoid changing the signatures of `InputService`/`OutputService`, `GridGame` lifecycle hooks, or the `toGridKey` format unless you update all usages.

---

## Extending the project

- To add a new grid-based game: extend `GridGame<T>` and implement the required lifecycle methods (`setSize`, `initializeGame`, `displayGrid`, `processUserInput`, `makeMove`, `checkWinCondition`, `displayWinMessage`).
- To change Dots & Boxes rendering, edit `DotsAndBoxesPiece.getDisplayToken()` or coloring helpers in `DotsAndBoxesGame`.

---


## Input/Output Example
### Start screen
```
============== GAME HUB ==============
1) Sliding Puzzle
2) Dots & Boxes
3) Quit
Choose an option (1-3):
```

### Sliding Puzzle
#### Welcome screen
```
Choose an option (1-3): 1

--- Launching Sliding Puzzle ---

=========================================
    WELCOME TO THE SLIDING PUZZLE GAME!
=========================================
Type 'quit' at any prompt to exit the game.

--- How to Play ---
1. Objective: Arrange the numbers in ascending order, from left to right, top to bottom.
   The empty space should be in the bottom-right corner when solved.

   For a 3x3 puzzle, the solved state looks like this:
   +--+--+--+
   | 1| 2| 3|
   +--+--+--+
   | 4| 5| 6|
   +--+--+--+
   | 7| 8|  |
   +--+--+--+

2. Your Move: To move a tile, enter the number of the tile you wish to slide
   into the empty space. You can only move tiles that are adjacent
   (up, down, left, or right) to the empty space.

Good luck and have fun! 🧩
-----------------------------------------

Enter player name (type 'quit' to exit):
```
#### Main menu
```
Enter player name (type 'quit' to exit): Adithya
Enter grid size (rows x cols) (Min 3, Max 20) or type 'quit' to exit: 3x3
Hey Adithya, choose your difficulty level: 1 (Easy), 2 (Medium), 3 (Hard), 4 (Expert), 5 (Master), 6 (Legendary)
Note: The difficulty level increases exponentially with grid size
Type 'quit' at any prompt to exit the game.
3
Difficulty set to: Hard
No previous score for this difficulty level.
Warning: Hard mode can be quite challenging!
Tip: Plan your moves ahead and try to visualize the solution.
+--+--+--+
| 4|  | 7|
+--+--+--+
| 2| 8| 6|
+--+--+--+
| 5| 1| 3|
+--+--+--+


Choose an option:
  [1] Start the game
  [2] Regenerate the board
  [3] View top scores
  [q] Quit
>
```
#### Quitting the game
```
Choose an option:
  [1] Start the game
  [2] Regenerate the board
  [3] View top scores
  [q] Quit
> q
Thanks for playing the Sliding Puzzle Game. Goodbye!

[Scoreboard]
(no results yet)

(Returning to Game Hub...)

============== GAME HUB ==============
1) Sliding Puzzle
2) Dots & Boxes
3) Quit
Choose an option (1-3):
Input closed. Goodbye!
```

### Dots & Boxes

### Quit
```
============== GAME HUB ==============
1) Sliding Puzzle
2) Dots & Boxes
3) Quit
Choose an option (1-3): 3
Goodbye!
```