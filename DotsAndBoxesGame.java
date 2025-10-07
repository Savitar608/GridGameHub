/**
 * Dots & Boxes (console) built on GridGame framework.
 * This version:
 *  - Input order: Player1 name → Player2 name → Box size (rows cols)
 *  - Max board size: 20x20 (min 2x2)
 *  - Colored edges supported (decided by GameHub when it constructs the game)
 *  - Supports "h row c1 c2" and "v col r1 r2" input formats (adjacent endpoints),
 *    as well as the older "h r c" / "v r c" formats and legacy raw grid coords.
 *  - Includes per-turn timer, 2-hint-per-player system, and one undo per player.
 */
/**
 * Console implementation of Dots & Boxes built on the GridGame framework.
 * Handles board initialization, parsing user input, move execution, scoring,
 * and rendering. Inline comments explain coordinate mappings between dot/box
 * coordinates and internal grid indices.
 */
public final class DotsAndBoxesGame extends GridGame<DotsAndBoxesPiece> {

    // ANSI colors
    private static final String RESET = "\u001B[0m";
    private static final String RED   = "\u001B[31m";
    private static final String BLUE  = "\u001B[34m";

    // ASCII drawing (safe on Windows terminals)
    private static final String DOT   = ".";
    private static final String HLINE = "---";
    private static final String VLINE = "|";

    private final boolean coloredEdges;

    // Box rows/cols (boxes, not dots). Default 3x3; user can set up to 20x20.
    private int boxRows = 3, boxCols = 3;

    // Players & scores
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private char p1Mark = 'A', p2Mark = 'B';
    private int p1Score = 0, p2Score = 0, currentPlayer = 1;
    private int totalBoxes = 0;

    // Timing
    private long p1TotalMillis = 0L, p2TotalMillis = 0L;
    private int  p1Moves = 0, p2Moves = 0;
    private double lastMoveSeconds = 0.0;

    public DotsAndBoxesGame() { this(true); }
    public DotsAndBoxesGame(boolean coloredEdges) {
        super(DotsAndBoxesPiece.class, 1, 1); // actual grid set in setSize/initializeGame
        this.coloredEdges = coloredEdges;
        // NOTE: we intentionally do NOT add difficulty levels for this game.
    }
    
    public DotsAndBoxesGame(boolean coloredEdges, InputService inputService, OutputService outputService) {
        super(DotsAndBoxesPiece.class, 1, 1, inputService, outputService); // actual grid set in setSize/initializeGame
        this.coloredEdges = coloredEdges;
        // NOTE: we intentionally do NOT add difficulty levels for this game.
    }

    // -------- Hints (2 per player) --------
    private final HintSystem<Grid<DotsAndBoxesPiece>, String, DotsAndBoxesMove> hintSystem =
        new HintSystem<>(new GreedyBoxStrategy(), name -> name);
    // -------- Timer (common) --------
    private final TurnTimer<String> turnTimer = new TurnTimer<>();
    // -------- Undo (one per player) --------
    private final UndoStateManager<DbxSnapshot, String> undoMgr =
        new UndoStateManager<>(name -> name);

    // Snapshot for undo
    private static final class DbxSnapshot {
        int rows, cols;
        char[][] owners;
        int p1Score, p2Score, currentPlayer;
    }
    private DbxSnapshot snapshotNow() {
        DbxSnapshot s = new DbxSnapshot();
        s.rows = getRows();
        s.cols = getCols();
        s.owners = new char[s.rows][s.cols];
        // Capture owner char for every cell (edges/boxes/dots) so we can fully restore state
        for (int r = 0; r < s.rows; r++) {
            for (int c = 0; c < s.cols; c++) {
                s.owners[r][c] = gameGrid.getPiece(r, c).getOwner();
            }
        }
        s.p1Score = p1Score;
        s.p2Score = p2Score;
        s.currentPlayer = currentPlayer;
        return s;
    }
    private void restoreFrom(DbxSnapshot s) {
        if (s == null) return;
        // Restore piece owners for the whole grid to revert to a previous move
        for (int r = 0; r < s.rows; r++) {
            for (int c = 0; c < s.cols; c++) {
                gameGrid.getPiece(r, c).setOwner(s.owners[r][c]);
            }
        }
        p1Score = s.p1Score;
        p2Score = s.p2Score;
        currentPlayer = s.currentPlayer;
    }

    // ===== GridGame required overrides =====
    @Override
    protected void validateSize() {
        if (boxRows < 2 || boxCols < 2 || boxRows > 20 || boxCols > 20) {
            throw new IllegalArgumentException("Boxes must be between 2x2 and 20x20.");
        }
    }

    @Override
    protected void setSize() {
        // Non-interactive; initializeGame will ask for size and resize again.
        validateSize();
        int gridR = 2 * boxRows + 1;
        int gridC = 2 * boxCols + 1;
        gameGrid.resize(gridR, gridC);
        totalBoxes = boxRows * boxCols;
    }

    @Override
    protected void displayWelcomeMessage() {
        OutputService out = getOutputService();
        out.println("=======================================");
        out.println("         Dots & Boxes (2 players)      ");
        out.println("=======================================");
        out.println("Input (recommended):");
        out.println(" - h row c1 c2  : edge on row between columns c1..c2 (adjacent)");
        out.println(" - v col r1 r2  : edge on column between rows r1..r2 (adjacent)");
        out.println("Type 'quit' to exit.");
        if (coloredEdges) out.println("Colors: P1=BLUE, P2=RED");
        out.println("");
    }

    @Override
    protected void initializeGame() {
        OutputService out = getOutputService();
        InputService in  = getInputService();

        // Player 1 - get from the configured player
        String p1 = getPlayer().getName();
        if (p1 != null && !p1.trim().isEmpty()) {
            player1Name = p1.trim();
        }

        // Board size after names
        out.println("");
        out.println("Choose board size in boxes (R C). Example: 3 3");
        out.print("Enter rows cols (2..20), or press Enter for default 3 3 (or 'quit'): ");
        out.flush();
        String szLine = in.readLine();
        if (szLine != null && !szLine.trim().isEmpty()) {
            if (isQuitCommand(szLine)) { requestExit(); return; }
            String[] sz = szLine.trim().split("\\s+");
            if (sz.length >= 2) {
                try { boxRows = Integer.parseInt(sz[0]); boxCols = Integer.parseInt(sz[1]); }
                catch (NumberFormatException ignore) {}
            }
        }
        validateSize();
        int gridR = 2 * boxRows + 1;
        int gridC = 2 * boxCols + 1;
        gameGrid.resize(gridR, gridC);
        totalBoxes = boxRows * boxCols;

        // Marks
        p1Mark = firstMarkFrom(player1Name, 'A');
        p2Mark = firstMarkFrom(player2Name, 'B');
        if (Character.toUpperCase(p1Mark) == Character.toUpperCase(p2Mark)) {
            p2Mark = (p2Mark == 'Z') ? 'Y' : (char)(Character.toUpperCase(p2Mark) + 1);
        }

        // Populate grid kinds
        int R = getRows(), C = getCols();
        for (int r = 0; r < R; r++) {
            for (int c = 0; c < C; c++) {
                DotsAndBoxesPiece.Kind kind;
                if (r % 2 == 0 && c % 2 == 0)      kind = DotsAndBoxesPiece.Kind.DOT;
                else if (r % 2 == 0 && c % 2 == 1) kind = DotsAndBoxesPiece.Kind.H_EDGE;
                else if (r % 2 == 1 && c % 2 == 0) kind = DotsAndBoxesPiece.Kind.V_EDGE;
                else                                kind = DotsAndBoxesPiece.Kind.BOX;
                gameGrid.setPiece(r, c, new DotsAndBoxesPiece(kind));
            }
        }

        // Reset stats
        p1Score = p2Score = 0;
        p1TotalMillis = p2TotalMillis = 0;
        p1Moves = p2Moves = 0;
        lastMoveSeconds = 0.0;
        currentPlayer = 1;

        hintSystem.resetForNewGame();
        undoMgr.resetForNewGame();
        undoMgr.setSnapshotFunctions(this::snapshotNow, this::restoreFrom);
        turnTimer.reset();
    }

    @Override
    protected void displayGrid() {
        OutputService out = getOutputService();
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("Board ").append(boxRows).append("x").append(boxCols)
          .append(" | ").append(player1Name).append("[").append(p1Mark).append("]=").append(p1Score)
          .append(" (avg ").append(String.format("%.2f", getP1AvgSeconds())).append("s)")
          .append(" vs ").append(player2Name).append("[").append(p2Mark).append("]=").append(p2Score)
          .append(" (avg ").append(String.format("%.2f", getP2AvgSeconds())).append("s)")
          .append(" | Turn: ").append(currentPlayer == 1 ? player1Name : player2Name)
          .append(" (").append(currentPlayer == 1 ? p1Mark : p2Mark).append(")")
          .append(" | last ⏱ ").append(String.format("%.2f", lastMoveSeconds)).append("s\n");

        // Top indices
        sb.append("    ");
        for (int dc = 0; dc <= boxCols; dc++) {
            sb.append(dc);
            if (dc < boxCols) sb.append("   ");
        }
        sb.append("\n");

    final int R = getRows();
    // Render rows: even rows are dot rows, odd rows contain vertical edges and boxes
    for (int r = 0; r < R; r++) {
            StringBuilder line = new StringBuilder();

            if (r % 2 == 0) line.append(String.format("%2d  ", r / 2));
            else            line.append("    ");

            if (r % 2 == 0) {
                // dot row: print a DOT then optional horizontal edge to the right
                for (int b = 0; b <= boxCols; b++) {
                    line.append(DOT);
                    if (b < boxCols) {
                        int eCol = 2 * b + 1;
                        DotsAndBoxesPiece edge = gameGrid.getPiece(r, eCol);
                        if (edge.isEmpty()) line.append("   ");
                        else line.append(colorize(edge, HLINE));
                    }
                }
            } else {
                // vertical edge / box row: print vertical edge then the box content
                for (int b = 0; b <= boxCols; b++) {
                    int vCol = 2 * b;
                    DotsAndBoxesPiece vedge = gameGrid.getPiece(r, vCol);
                    line.append(vedge.isEmpty() ? " " : colorize(vedge, VLINE));
                    if (b < boxCols) {
                        int bc = 2 * b + 1;
                        DotsAndBoxesPiece box = gameGrid.getPiece(r, bc);
                        if (box.getOwner() == 0) line.append("   ");
                        else line.append(colorize(box, " " + box.getOwner() + " "));
                    }
                }
            }
            sb.append(line).append("\n");
        }

        // Bottom indices
        sb.append("    ");
        for (int dc = 0; dc <= boxCols; dc++) {
            sb.append(dc);
            if (dc < boxCols) sb.append("   ");
        }
        sb.append("\n");

        out.println(sb.toString());
    }

    @Override
    protected void processUserInput() {
        if (isGameOver()) return;
        OutputService out = getOutputService();
        InputService in  = getInputService();

        out.print("Move (h row c1 c2 / v col r1 r2) or type 'quit' or 'hint' or 'undo': ");
        out.flush();

        // start timing for the current player BEFORE reading input
        String whoNow = (currentPlayer == 1 ? player1Name : player2Name);
        turnTimer.start(whoNow);

        String line = in.readLine();

        if (line == null) { turnTimer.cancel(); return; }
        if (isQuitCommand(line)) { turnTimer.cancel(); requestExit(); return; }
        line = line.trim();
        if (line.isEmpty()) { turnTimer.cancel(); displayInvalidInputMessage(); return; }

        if (line.equalsIgnoreCase("hint")) {
            turnTimer.cancel();
            String who = (currentPlayer == 1 ? player1Name : player2Name);
            int left = hintSystem.remainingFor(who);
            if (left <= 0) {
                out.println("No hints left for " + who + ".");
            } else {
                try {
                    DotsAndBoxesMove hint = hintSystem.suggest(gameGrid, who);
                    out.println("Hint (" + who + ", left=" + hintSystem.remainingFor(who) + "): try " + hint);
                } catch (IllegalStateException ex) {
                    out.println(ex.getMessage());
                }
            }
            return;
        }

        if (line.equalsIgnoreCase("undo")) {
            turnTimer.cancel();
            String who = (currentPlayer == 1 ? player1Name : player2Name);
            boolean ok = undoMgr.undoLastFor(who, msg -> out.println(msg));
            if (ok) displayGrid();
            return;
        }

        String[] parts = line.split("\\s+");
        try {
            int gridRow, gridCol;

            if (parts.length >= 3 && (parts[0].equalsIgnoreCase("h") || parts[0].equalsIgnoreCase("v"))) {
                boolean horiz = parts[0].equalsIgnoreCase("h");

                if (parts.length == 4) {
                    if (horiz) {
                        int row = Integer.parseInt(parts[1]);     // 0..boxRows
                        int c1  = Integer.parseInt(parts[2]);     // 0..boxCols
                        int c2  = Integer.parseInt(parts[3]);     // 0..boxCols
                        if (row < 0 || row > boxRows || c1 < 0 || c1 > boxCols || c2 < 0 || c2 > boxCols) {
                            out.println("Out of range. h row c1 c2 : row=0.."+boxRows+", cols=0.."+boxCols+".");
                            turnTimer.cancel(); return;
                        }
                        if (Math.abs(c1 - c2) != 1) {
                            out.println("Columns must be adjacent for horizontal edge (e.g., 0 1).");
                            turnTimer.cancel(); return;
                        }
                        int leftCol = Math.min(c1, c2);
                        gridRow = 2 * row;
                        gridCol = 2 * leftCol + 1;
                    } else {
                        int col = Integer.parseInt(parts[1]);     // 0..boxCols
                        int r1  = Integer.parseInt(parts[2]);     // 0..boxRows
                        int r2  = Integer.parseInt(parts[3]);     // 0..boxRows
                        if (col < 0 || col > boxCols || r1 < 0 || r1 > boxRows || r2 < 0 || r2 > boxRows) {
                            out.println("Out of range. v col r1 r2 : col=0.."+boxCols+", rows=0.."+boxRows+".");
                            turnTimer.cancel(); return;
                        }
                        if (Math.abs(r1 - r2) != 1) {
                            out.println("Rows must be adjacent for vertical edge (e.g., 2 3).");
                            turnTimer.cancel(); return;
                        }
                        int topRow = Math.min(r1, r2);
                        gridRow = 2 * topRow + 1;
                        gridCol = 2 * col;
                    }
                } else if (parts.length == 3) {
                    int a = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    if (horiz) {
                        if (a < 0 || a > boxRows || b < 1 || b > boxCols) {
                            out.println("Out of range. h r c : r=0.."+boxRows+", c=1.."+boxCols+" (c is RIGHT dot).");
                            turnTimer.cancel(); return;
                        }
                        gridRow = 2 * a;
                        gridCol = 2 * (b - 1) + 1;
                    } else {
                        if (a < 1 || a > boxRows || b < 0 || b > boxCols) {
                            out.println("Out of range. v r c : r=1.."+boxRows+" (BOTTOM), c=0.."+boxCols+".");
                            turnTimer.cancel(); return;
                        }
                        gridRow = 2 * (a - 1) + 1;
                        gridCol = 2 * b;
                    }
                } else {
                    turnTimer.cancel();
                    displayInvalidInputMessage();
                    return;
                }
            } else if (parts.length == 2) {
                gridRow = Integer.parseInt(parts[0]);
                gridCol = Integer.parseInt(parts[1]);
            } else {
                turnTimer.cancel();
                displayInvalidInputMessage();
                return;
            }

            // Snapshot then move
            String who = (currentPlayer == 1 ? player1Name : player2Name);
            undoMgr.recordBeforeMove(who);

            int prePlayer = currentPlayer;
            makeMove(gridRow, gridCol);

            long elapsedMs = turnTimer.stop((prePlayer == 1) ? player1Name : player2Name);
            lastMoveSeconds = elapsedMs / 1000.0;
            if (prePlayer == 1) { p1TotalMillis += elapsedMs; p1Moves++; }
            else { p2TotalMillis += elapsedMs; p2Moves++; }
            out.println(String.format("⏱ Move time: %.2fs", lastMoveSeconds));

        } catch (NumberFormatException nfe) {
            turnTimer.cancel();
            displayInvalidInputMessage();
        }
    }

    @Override
    protected void makeMove(int row, int col) {
        if (!isWithinGrid(row, col)) { displayInvalidInputMessage(); return; }

        boolean isHEdge = (row % 2 == 0) && (col % 2 == 1);
        boolean isVEdge = (row % 2 == 1) && (col % 2 == 0);
        if (!isHEdge && !isVEdge) {
            getOutputService().println("Pick an EDGE cell: use 'h r c' or 'v r c' (see help).");
            return;
        }

        DotsAndBoxesPiece edge = gameGrid.getPiece(row, col);
        if (!edge.isEmpty()) { getOutputService().println("That edge is already claimed."); return; }

        char mark = (currentPlayer == 1 ? p1Mark : p2Mark);
        edge.setOwner(mark);

        int completed = 0;
        if (isHEdge) {
            if (row - 1 >= 1)              completed += tryCompleteBox(row - 1, col, mark);
            if (row + 1 <= getRows() - 2)  completed += tryCompleteBox(row + 1, col, mark);
        } else {
            if (col - 1 >= 1)              completed += tryCompleteBox(row, col - 1, mark);
            if (col + 1 <= getCols() - 2)  completed += tryCompleteBox(row, col + 1, mark);
        }

        if (completed == 0) currentPlayer = (currentPlayer == 1 ? 2 : 1);
        else { if (currentPlayer == 1) p1Score += completed; else p2Score += completed; }
    }

    @Override
    protected void displayInvalidInputMessage() {
        getOutputService().println("Use: h r c  (r=0..R, c=1..C)  or  v r c  (r=1..R, c=0..C)\n"
                + "   or NEW forms: h row c1 c2  /  v col r1 r2  (adjacent endpoints).");
    }

    @Override
    protected boolean checkWinCondition() {
        if (p1Score + p2Score >= totalBoxes) return true;
        int R = getRows(), C = getCols();
        for (int r = 0; r < R; r++) for (int c = 0; c < C; c++) {
            if ((r + c) % 2 == 1 && gameGrid.getPiece(r, c).isEmpty()) return false;
        }
        return true;
    }

    @Override
    protected void displayWinMessage() {
        OutputService out = getOutputService();
        out.println("");
        out.println("===== Game Over =====");
        out.println(player1Name + " [" + p1Mark + "] = " + p1Score + " (avg " + String.format("%.2f", getP1AvgSeconds()) + "s)");
        out.println(player2Name + " [" + p2Mark + "] = " + p2Score + " (avg " + String.format("%.2f", getP2AvgSeconds()) + "s)");
        if (p1Score > p2Score) out.println("Winner: " + player1Name + " 🎉");
        else if (p2Score > p1Score) out.println("Winner: " + player2Name + " 🎉");
        else out.println("It's a draw!");
    }

    // ---- helpers ----
    private boolean isWithinGrid(int r, int c) { return r >= 0 && r < getRows() && c >= 0 && c < getCols(); }

    private int tryCompleteBox(int br, int bc, char mark) {
        if (br % 2 != 1 || bc % 2 != 1) return 0;
        DotsAndBoxesPiece box = gameGrid.getPiece(br, bc);
        if (box.getOwner() != 0) return 0;

        DotsAndBoxesPiece up = gameGrid.getPiece(br - 1, bc);
        DotsAndBoxesPiece dn = gameGrid.getPiece(br + 1, bc);
        DotsAndBoxesPiece lf = gameGrid.getPiece(br, bc - 1);
        DotsAndBoxesPiece rt = gameGrid.getPiece(br, bc + 1);
        if (up.isEmpty() || dn.isEmpty() || lf.isEmpty() || rt.isEmpty()) return 0;

        box.setOwner(mark);
        return 1;
    }

    private char firstMarkFrom(String name, char fallback) {
        if (name == null || name.trim().isEmpty()) return fallback;
        char c = name.trim().charAt(0);
        if (Character.isLetterOrDigit(c)) return Character.toUpperCase(c);
        return fallback;
    }

    private String colorize(DotsAndBoxesPiece piece, String s) {
        if (!coloredEdges || piece.getOwner() == 0) return s;
        String color = (Character.toUpperCase(piece.getOwner()) == Character.toUpperCase(p1Mark)) ? BLUE : RED;
        return color + s + RESET;
    }

    // getters and setters for the hub/scoreboard
    public String getPlayer1Name()  { return player1Name; }
    public String getPlayer2Name()  { return player2Name; }
    public void setPlayer2Name(String name) { 
        if (name != null && !name.trim().isEmpty()) {
            player2Name = name.trim();
        }
    }
    public int    getP1Score()      { return p1Score; }
    public int    getP2Score()      { return p2Score; }
    public String getWinnerName()   {
        if (p1Score > p2Score) return player1Name;
        if (p2Score > p1Score) return player2Name;
        return "draw";
    }
    public double getP1AvgSeconds() { return p1Moves == 0 ? 0.0 : (p1TotalMillis / 1000.0) / p1Moves; }
    public double getP2AvgSeconds() { return p2Moves == 0 ? 0.0 : (p2TotalMillis / 1000.0) / p2Moves; }
}
