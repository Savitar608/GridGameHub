
/**
 * File: DotsAndBoxesGame.java
 * Description: Console implementation of the classic Dots and Boxes game built on the
 *              shared GridGame framework with support for teams, scoring, and colored edges.
 */

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Console implementation of the classic Dots and Boxes game leveraging the
 * existing {@link GridGame} framework. Supports head-to-head play as well as
 * teams of two, providing flexible participant configuration and scoreboard
 * tracking.
 */
public final class DotsAndBoxesGame extends GridGame<DotsAndBoxesCell> {
    // Game configuration constants
    private static final int MIN_SIZE = 2;
    private static final int MAX_SIZE = 20;
    private static final int DEFAULT_ROWS = 3;
    private static final int DEFAULT_COLS = 3;
    private static final int TEAM_SIZE = 2;
    private static final String DOT = "•";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String[] COLOR_OPTIONS = { "red", "blue", "green", "yellow", "magenta", "cyan", "white" };

    // Game state tracking
    private final Map<Player, Integer> playerScores = new LinkedHashMap<>();
    private final Map<Team, Integer> teamScores = new LinkedHashMap<>();
    private final List<Team> matchTeams = new ArrayList<>();
    private final Map<Player, String> playerColors = new LinkedHashMap<>();
    private final Map<Team, String> teamColorCodes = new LinkedHashMap<>();
    private final Set<String> usedColorKeys = new HashSet<>();

    // Game flow tracking
    private boolean teamMode;
    private int claimedBoxes;

    /**
     * Creates a new Dots and Boxes game instance with default input and output
     * services.
     */
    public DotsAndBoxesGame() {
        super(DotsAndBoxesCell.class, DEFAULT_ROWS, DEFAULT_COLS);
        initializeBoard();
    }

    /**
     * Creates a new Dots and Boxes game instance with the specified input and
     * output services.
     * 
     * @param inputService  the input service for reading user input
     * @param outputService the output service for displaying prompts and messages
     */
    public DotsAndBoxesGame(InputService inputService, OutputService outputService) {
        super(DotsAndBoxesCell.class, DEFAULT_ROWS, DEFAULT_COLS, inputService, outputService);
        initializeBoard();
    }

    @Override
    protected void initializeDefaultDifficultyLevels() {
        // Override to disable difficulty presets for Dots and Boxes.
        getDifficultyManager().clear();
    }

    @Override
    public boolean supportsDifficultySelection() {
        return false;
    }

    @Override
    public boolean supportsTopScores() {
        return false;
    }

    @Override
    public boolean supportsBoardRegeneration() {
        return false;
    }

    /**
     * Guides the player setup process, allowing the user to choose between
     * head-to-head and team play modes, and initializes the roster accordingly.
     *
     * @param inputService  shared input service for prompts
     * @param outputService shared output service for instructions
     * @return {@code true} when configuration succeeds and play can continue,
     *         otherwise {@code false}
     */
    @Override
    protected boolean configurePlayers(InputService inputService, OutputService outputService) {
        clearPlayers();
        playerScores.clear();
        teamScores.clear();
        matchTeams.clear();
        playerColors.clear();
        teamColorCodes.clear();
        usedColorKeys.clear();
        teamMode = false;

        outputService.println("=== Player Setup ===");
        outputService.println("Choose game mode: [1] Head-to-head (1v1) | [2] Teams of two | 'quit' to exit");
        String modeInput = readLineTrimmed(inputService);
        if (modeInput == null || isQuitCommand(modeInput)) {
            requestExit();
            return false;
        }

        switch (modeInput) {
            case "2":
            case "teams":
            case "team":
                teamMode = true;
                if (!configureTeamMode(inputService, outputService)) {
                    return false;
                }
                break;
            default:
                if (!configureHeadToHead(inputService, outputService)) {
                    return false;
                }
        }

        try {
            setActivePlayerIndex(0);
        } catch (IllegalArgumentException ignored) {
            // Should never happen because at least one player is registered.
        }

        outputService.println("Players ready! Let's begin.\n");
        return true;
    }

    /**
     * Configures a standard head-to-head match between two individual players.
     *
     * @param inputService  the input service for reading user input
     * @param outputService the output service for displaying prompts and messages
     * @return {@code true} if head-to-head mode was successfully configured,
     *         {@code false} otherwise
     */
    /**
     * Prompts for two individual players, assigns colors, and registers them for
     * a head-to-head match.
     *
     * @param inputService  input service used for player prompts
     * @param outputService output service for feedback
     * @return {@code true} if configuration succeeds, {@code false} if the user quits
     */
    private boolean configureHeadToHead(InputService inputService, OutputService outputService) {
        for (int i = 1; i <= 2; i++) {
            String prompt = String.format(Locale.ROOT, "Enter name for Player %d (or 'quit'): ", i);
            String name = promptForRequiredValue(prompt, inputService, outputService);
            if (name == null) {
                requestExit();
                return false;
            }
            Player player = new Player();
            player.setName(name);
            player.setDifficultyLevel(1);
            addPlayer(player);
            playerScores.put(player, 0);

            ColorChoice colorChoice = promptForColor(
                    String.format(Locale.ROOT,
                            "Choose a color for %s (%s): ",
                            player.getName(), formatColorOptions()),
                    true, inputService, outputService);
            if (colorChoice == null) {
                requestExit();
                return false;
            }
            playerColors.put(player, colorChoice.ansiCode);
        }
        return true;
    }

    /**
     * Configures the team mode settings, allowing for team-based play.
     *
     * @param inputService  the input service for reading user input
     * @param outputService the output service for displaying prompts and messages
     * @return {@code true} if team mode was successfully configured, {@code false}
     *         otherwise
     */
    /**
     * Collects information for each team, including name, tag, color assignments,
     * and member details, then prepares the players for play.
     *
     * @param inputService  input service used for prompts
     * @param outputService output service for instructions and validation feedback
     * @return {@code true} when teams are configured successfully, {@code false} if
     *         the process is cancelled
     */
    private boolean configureTeamMode(InputService inputService, OutputService outputService) {
        List<List<Player>> teamPlayers = new ArrayList<>();
        for (int teamIndex = 1; teamIndex <= 2; teamIndex++) {
            outputService.println(String.format(Locale.ROOT, "-- Team %d Configuration --", teamIndex));
            String teamName = promptForRequiredValue("Team name: ", inputService, outputService);
            if (teamName == null) {
                requestExit();
                return false;
            }
            String teamTag = promptForRequiredValue("Team tag (short code): ", inputService, outputService);
            if (teamTag == null) {
                requestExit();
                return false;
            }
            ColorChoice teamColor = promptForColor(
                    "Team color (" + formatColorOptions() + "): ",
                    true, inputService, outputService);
            if (teamColor == null) {
                requestExit();
                return false;
            }

            Team team = new Team(teamName, teamTag, teamColor.displayName);
            matchTeams.add(team);
            teamScores.put(team, 0);
            teamColorCodes.put(team, teamColor.ansiCode);

            List<Player> members = new ArrayList<>();

            for (int member = 1; member <= TEAM_SIZE; member++) {
                String prompt = String.format(Locale.ROOT, "Player %d name for %s: ", member, team.getName());
                String playerName = promptForRequiredValue(prompt, inputService, outputService);
                if (playerName == null) {
                    requestExit();
                    return false;
                }
                Player player = new Player();
                player.setName(playerName);
                player.setDifficultyLevel(1);
                player.joinTeam(team);
                members.add(player);
                playerColors.put(player, teamColor.ansiCode);
            }

            teamPlayers.add(members);
        }

        registerAlternatingTeamPlayers(teamPlayers);
        return true;
    }

    /**
     * Registers players for alternating team play.
     *
     * @param teamPlayers the list of teams and their players
     */
    /**
     * Registers players in alternating team order so that teammates never take
     * consecutive turns.
     *
     * @param teamPlayers ordered lists of players for each team
     */
    private void registerAlternatingTeamPlayers(List<List<Player>> teamPlayers) {
        players.clear();

        int maxMembers = 0;
        for (List<Player> members : teamPlayers) {
            maxMembers = Math.max(maxMembers, members.size());
        }

        for (int slot = 0; slot < maxMembers; slot++) {
            for (List<Player> members : teamPlayers) {
                if (slot < members.size()) {
                    Player player = members.get(slot);
                    addPlayer(player);
                    playerScores.put(player, 0);
                }
            }
        }
    }

    /**
     * Ensures the configured board dimensions fall within the supported range.
     */
    @Override
    protected void validateSize() {
        if (getRows() < MIN_SIZE || getCols() < MIN_SIZE || getRows() > MAX_SIZE || getCols() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "Board size must be between " + MIN_SIZE + "x" + MIN_SIZE + " and " + MAX_SIZE + "x" + MAX_SIZE);
        }
    }

    /**
     * Prompts the user for custom board dimensions, enforcing valid bounds and
     * falling back to defaults when necessary.
     */
    @Override
    protected void setSize() {
        OutputService outputService = getOutputService();
        InputService inputService = getInputService();

        outputService.print(String.format(Locale.ROOT,
                "Enter board size (rows x cols) between %d and %d, or 'quit': ", MIN_SIZE, MAX_SIZE));
        String input = readLineTrimmed(inputService);
        if (input == null || isQuitCommand(input)) {
            requestExit();
            return;
        }

        String[] parts = input.split("\\s*x\\s*");
        int rows = DEFAULT_ROWS;
        int cols = DEFAULT_COLS;
        boolean customSize = parts.length == 2;
        if (customSize) {
            try {
                rows = Integer.parseInt(parts[0]);
                cols = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                outputService.println("Invalid size. Using default 3x3 board.");
                rows = DEFAULT_ROWS;
                cols = DEFAULT_COLS;
            }
        } else {
            outputService.println("Invalid input. Using default 3x3 board.");
        }

        if (rows < MIN_SIZE || rows > MAX_SIZE || cols < MIN_SIZE || cols > MAX_SIZE) {
            outputService.println(String.format(Locale.ROOT,
                    "Board size out of range. Using default %dx%d.", DEFAULT_ROWS, DEFAULT_COLS));
            rows = DEFAULT_ROWS;
            cols = DEFAULT_COLS;
        }

        gameGrid.resize(rows, cols);
        initializeBoard();
        validateSize();
    }

    /**
     * Prints introductory instructions and rules for the Dots and Boxes game,
     * summarizing gameplay flow and team-specific behavior.
     */
    @Override
    protected void displayWelcomeMessage() {
        OutputService outputService = getOutputService();
        outputService.println("=======================================");
        outputService.println("        WELCOME TO DOTS AND BOXES       ");
        outputService.println("=======================================");
        outputService.println("Rules:");
        outputService.println("1. Players take turns drawing a single edge between two dots.");
        outputService.println("2. Completing the fourth edge around a box claims it and awards a point.");
        outputService.println("3. Completing a box grants an extra turn. Otherwise, play passes to the next player.");
        outputService.println("4. In team mode, turns alternate between teams—teammates never play consecutively.");
        outputService.println("5. The player or team with the most boxes when the board is full wins.");
        outputService.println("Type 'quit' at any prompt to exit the game.\n");
    }

    /**
     * Resets scoreboard state, reinitializes the board, and sets the starting
     * player before a new match begins.
     */
    @Override
    protected void initializeGame() {
        initializeBoard();
        claimedBoxes = 0;
        for (Player player : getPlayers()) {
            playerScores.put(player, 0);
        }
        if (teamMode) {
            for (Team team : matchTeams) {
                teamScores.put(team, 0);
            }
        } else {
            teamScores.clear();
            matchTeams.clear();
        }
        try {
            setActivePlayerIndex(0);
        } catch (IllegalArgumentException ignored) {
            // Should never happen because at least one player is registered.
        }
    }

    /**
     * Renders the current board state, including colored edges and claimed boxes,
     * and outputs the scoreboard after the visual representation.
     */
    @Override
    protected void displayGrid() {
        OutputService outputService = getOutputService();
        int rows = gameGrid.getRows();
        int cols = gameGrid.getCols();

        for (int r = 0; r < rows; r++) {
            StringBuilder topLine = new StringBuilder();
            topLine.append(DOT);
            for (int c = 0; c < cols; c++) {
                DotsAndBoxesCell cell = gameGrid.getPiece(r, c);
                topLine.append(formatHorizontalEdge(cell, DotsAndBoxesEdge.TOP));
                topLine.append(DOT);
            }
            outputService.println(topLine.toString());

            StringBuilder middleLine = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                DotsAndBoxesCell cell = gameGrid.getPiece(r, c);
                middleLine.append(formatVerticalEdge(cell, DotsAndBoxesEdge.LEFT));

                String token = centerToken(cell.getDisplayToken());
                Player owner = cell.getOwner();
                if (owner != null) {
                    token = applyColor(token, getColorForPlayer(owner));
                }
                middleLine.append(token);
            }
            DotsAndBoxesCell lastCell = gameGrid.getPiece(r, cols - 1);
            middleLine.append(formatVerticalEdge(lastCell, DotsAndBoxesEdge.RIGHT));
            outputService.println(middleLine.toString());
        }

        // Bottom border
        StringBuilder bottomLine = new StringBuilder();
        bottomLine.append(DOT);
        for (int c = 0; c < gameGrid.getCols(); c++) {
            DotsAndBoxesCell cell = gameGrid.getPiece(gameGrid.getRows() - 1, c);
            bottomLine.append(formatHorizontalEdge(cell, DotsAndBoxesEdge.BOTTOM));
            bottomLine.append(DOT);
        }
        outputService.println(bottomLine.toString());

        displayScores(outputService);
    }

    /**
     * Reads and validates the active player's move, applying it when valid or
     * reporting errors and turn outcomes.
     */
    @Override
    protected void processUserInput() {
        OutputService outputService = getOutputService();
        InputService inputService = getInputService();
        Player currentPlayer = getActivePlayer();

        outputService.print(String.format(Locale.ROOT,
                "Note: Board is %dx%d. Rows and columns are 1-indexed.\n1-based indexing means the top-left corner is (1, 1).\n",
                gameGrid.getRows(), gameGrid.getCols()));

        outputService.print(String.format(Locale.ROOT,
                "%s, enter your move as 'row col side' (side = T/B/L/R) or 'quit': ",
                currentPlayer.getName()));
        String input = readLineTrimmed(inputService);
        if (input == null || isQuitCommand(input)) {
            requestExit();
            return;
        }

        String[] parts = input.split("\\s+");
        if (parts.length != 3) {
            displayInvalidInputMessage();
            return;
        }

        Integer row = parseInteger(parts[0]);
        Integer col = parseInteger(parts[1]);
        DotsAndBoxesEdge edge = parseEdge(parts[2]);
        if (row == null || col == null || edge == null) {
            displayInvalidInputMessage();
            return;
        }

        row -= 1;
        col -= 1;
        if (!gameGrid.isValidPosition(row, col)) {
            outputService.println("Coordinates out of range. Try again.");
            return;
        }

        int boxesCompleted = applyMove(row, col, edge, currentPlayer);
        if (boxesCompleted < 0) {
            outputService.println("That edge is already drawn. Choose a different move.");
            return;
        }

        if (boxesCompleted == 0) {
            advanceToNextPlayer();
        } else {
            outputService.println(String.format(Locale.ROOT,
                    "Great! You completed %d box%s.", boxesCompleted, boxesCompleted == 1 ? "" : "es"));
        }

        if (claimedBoxes == getTotalBoxes()) {
            setGameOver(true);
        }
    }

    /**
     * Unsupported for Dots and Boxes because moves are edge-based rather than
     * cell-based; invocation results in an exception.
     */
    @Override
    protected void makeMove(int row, int col) {
        throw new UnsupportedOperationException("Dots and Boxes uses edge-based moves processed elsewhere.");
    }

    /**
     * Provides guidance when an entered move cannot be parsed or is out of range.
     */
    @Override
    protected void displayInvalidInputMessage() {
        getOutputService().println("Invalid move. Use the format 'row col side' where side is T, B, L, or R.");
    }

    /**
     * Determines whether all boxes on the board have been claimed, signalling the
     * end of the match.
     */
    @Override
    protected boolean checkWinCondition() {
        return claimedBoxes == getTotalBoxes();
    }

    /**
     * Announces the final results, displaying box counts and identifying winning
     * players or teams after the board is complete.
     */
    @Override
    protected void displayWinMessage() {
        OutputService outputService = getOutputService();
        outputService.println("\n=== FINAL RESULTS ===");
        displayScores(outputService);

        if (teamMode && !teamScores.isEmpty()) {
            int bestScore = teamScores.values().stream().max(Integer::compareTo).orElse(0);
            List<Team> winners = new ArrayList<>();
            for (Team team : matchTeams) {
                if (teamScores.getOrDefault(team, 0) == bestScore) {
                    winners.add(team);
                }
            }
            if (winners.size() == 1) {
                Team winner = winners.get(0);
                outputService.println(String.format(Locale.ROOT, "Team %s (%s) wins with %d boxes!",
                        winner.getName(), winner.getTag(), bestScore));
            } else {
                outputService.println("It's a tie between teams:");
                for (Team team : winners) {
                    outputService.println(String.format(Locale.ROOT, "  %s (%s)", team.getName(), team.getTag()));
                }
            }
        } else {
            int bestScore = playerScores.values().stream().max(Integer::compareTo).orElse(0);
            List<Player> winners = new ArrayList<>();
            for (Player player : getPlayers()) {
                if (playerScores.getOrDefault(player, 0) == bestScore) {
                    winners.add(player);
                }
            }
            if (winners.size() == 1) {
                outputService.println(String.format(Locale.ROOT, "%s wins with %d boxes!",
                        winners.get(0).getName(), bestScore));
            } else {
                outputService.println("It's a tie between:");
                for (Player player : winners) {
                    outputService.println("  " + player.getName());
                }
            }
        }
    }

    /**
     * Initializes or resets the game board to an empty state.
     */
    /**
     * Fills the grid with fresh cells and resets box counters prior to gameplay.
     */
    private void initializeBoard() {
        for (int r = 0; r < gameGrid.getRows(); r++) {
            for (int c = 0; c < gameGrid.getCols(); c++) {
                gameGrid.setPiece(r, c, new DotsAndBoxesCell());
            }
        }
        claimedBoxes = 0;
    }

    /**
     * Applies a move to the game board.
     *
     * @param row    the row of the cell
     * @param col    the column of the cell
     * @param edge   the edge being claimed
     * @param player the player making the move
     * @return the number of boxes completed by this move, or -1 if the move is
     *         invalid
     */
    /**
     * Applies the requested edge to the board, checks for completed boxes, and
     * updates scores while mirroring the edge for adjacent cells.
     *
     * @param row    zero-based row index of the targeted cell
     * @param col    zero-based column index of the targeted cell
     * @param edge   edge to draw
     * @param player player performing the move
     * @return number of boxes completed, or {@code -1} if the edge already exists
     */
    private int applyMove(int row, int col, DotsAndBoxesEdge edge, Player player) {
        DotsAndBoxesCell primary = gameGrid.getPiece(row, col);
        String edgeColor = getColorForPlayer(player);
        if (!primary.addEdge(edge, edgeColor)) {
            return -1;
        }

        int boxesCompleted = 0;
        if (primary.isCompleted() && primary.claim(player)) {
            boxesCompleted++;
            incrementScore(player, 1);
        }

        int neighborRow = row;
        int neighborCol = col;
        switch (edge) {
            case TOP:
                neighborRow = row - 1;
                break;
            case BOTTOM:
                neighborRow = row + 1;
                break;
            case LEFT:
                neighborCol = col - 1;
                break;
            case RIGHT:
                neighborCol = col + 1;
                break;
        }

        if (gameGrid.isValidPosition(neighborRow, neighborCol)) {
            DotsAndBoxesCell neighbor = gameGrid.getPiece(neighborRow, neighborCol);
            neighbor.addEdge(edge.opposite(), edgeColor);
            if (neighbor.isCompleted() && neighbor.claim(player)) {
                boxesCompleted++;
                incrementScore(player, 1);
            }
        }

        claimedBoxes += boxesCompleted;
        return boxesCompleted;
    }

    /**
     * Increments the score for the specified player and their team if applicable.
     *
     * @param player the player whose score to increment
     * @param delta  the amount to increment the score by
     */
    /**
     * Adds the specified delta to the player's score and, when applicable, the
     * associated team's total.
     *
     * @param player scoring player
     * @param delta  number of boxes claimed
     */
    private void incrementScore(Player player, int delta) {
        playerScores.merge(player, delta, Integer::sum);
        player.getTeam().ifPresent(team -> {
            if (teamMode) {
                teamScores.merge(team, delta, Integer::sum);
            }
        });
    }

    /**
     * Displays the current scores for all players and teams.
     * 
     * @param outputService
     */
    /**
     * Prints the per-player and per-team score breakdown.
     *
     * @param outputService destination for score output
     */
    private void displayScores(OutputService outputService) {
        outputService.println("Scores:");
        for (Player player : getPlayers()) {
            int score = playerScores.getOrDefault(player, 0);
            String label = applyColor(player.getName(), getColorForPlayer(player));
            outputService.println(String.format(Locale.ROOT, "  %s: %d", label, score));
        }
        if (teamMode && !teamScores.isEmpty()) {
            outputService.println("Team totals:");
            for (Team team : matchTeams) {
                int score = teamScores.getOrDefault(team, 0);
                String label = String.format(Locale.ROOT, "%s (%s)", team.getName(), team.getTag());
                outputService.println(String.format(Locale.ROOT, "  %s: %d",
                        applyColor(label, teamColorCodes.get(team)), score));
            }
        }
        outputService.println("");
    }

    /**
     * Gets the ANSI color code for the specified player.
     * 
     * @param player the player whose color to retrieve
     * @return the ANSI color code, or reset code if none assigned
     */
    /**
     * Resolves the ANSI color code to use when rendering output for the given
     * player.
     *
     * @param player player whose color should be retrieved
     * @return ANSI color code string
     */
    private String getColorForPlayer(Player player) {
        if (player == null) {
            return ANSI_RESET;
        }
        String color = playerColors.get(player);
        if (color != null) {
            return color;
        }
        Team team = player.getTeam().orElse(null);
        if (team != null) {
            String teamCode = teamColorCodes.get(team);
            if (teamCode != null) {
                return teamCode;
            }
            ColorChoice fallback = resolveColorChoice(team.getColor());
            if (fallback != null) {
                teamColorCodes.put(team, fallback.ansiCode);
                return fallback.ansiCode;
            }
        }
        return ANSI_RESET;
    }

    /**
     * Formats a horizontal edge for display.
     * 
     * @param cell the cell containing the edge
     * @param edge the edge to format
     * @return the formatted edge string
     */
    /**
     * Produces the string representation for a horizontal edge, applying color
     * when present.
     *
     * @param cell cell containing the edge
     * @param edge edge being rendered
     * @return colored edge segment or whitespace when absent
     */
    private String formatHorizontalEdge(DotsAndBoxesCell cell, DotsAndBoxesEdge edge) {
        if (cell != null && cell.hasEdge(edge)) {
            return applyColor("───", cell.getEdgeColor(edge));
        }
        return "   ";
    }

    /**
     * Formats a vertical edge for display.
     * 
     * @param cell the cell containing the edge
     * @param edge the edge to format
     * @return the formatted edge string
     */
    /**
     * Produces the string representation for a vertical edge, applying color when
     * present.
     *
     * @param cell cell containing the edge
     * @param edge edge being rendered
     * @return colored edge glyph or a space when absent
     */
    private String formatVerticalEdge(DotsAndBoxesCell cell, DotsAndBoxesEdge edge) {
        if (cell != null && cell.hasEdge(edge)) {
            return applyColor("│", cell.getEdgeColor(edge));
        }
        return " ";
    }

    /**
     * Applies ANSI color codes to the given text.
     * 
     * @param text      the text to color
     * @param colorCode the ANSI color code to apply
     * @return the colored text
     */
    /**
     * Wraps the provided text in ANSI color codes if one is supplied.
     *
     * @param text      text to colorize
     * @param colorCode ANSI color code (may be {@code null})
     * @return colorized or original text depending on availability
     */
    private String applyColor(String text, String colorCode) {
        if (text == null) {
            return null;
        }
        if (colorCode == null || colorCode.isEmpty() || ANSI_RESET.equals(colorCode)) {
            return text;
        }
        return colorCode + text + ANSI_RESET;
    }

    /**
     * Formats the available color options for display.
     * 
     * @return a comma-separated string of color options
     */
    /**
     * Builds a human-readable list of selectable colors, indicating which have
     * already been claimed.
     *
     * @return formatted color options string
     */
    private String formatColorOptions() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < COLOR_OPTIONS.length; i++) {
            ColorChoice choice = resolveColorChoice(COLOR_OPTIONS[i]);
            String label = choice != null ? applyColor(choice.displayName, choice.ansiCode)
                    : capitalize(COLOR_OPTIONS[i]);
            if (choice != null && usedColorKeys.contains(choice.key)) {
                label += " (taken)";
            }
            builder.append(label);
            if (i < COLOR_OPTIONS.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Prompts the user to select a color.
     * 
     * @param prompt        the prompt message
     * @param requireUnique whether the color must be unique among players
     * @param inputService  the input service for reading user input
     * @param outputService the output service for displaying prompts and messages
     * @return the selected ColorChoice, or null if the user chose to quit
     */
    /**
     * Prompts the user for a color choice, optionally requiring a unique
     * selection, and returns a resolved color descriptor.
     *
     * @param prompt        message to display
     * @param requireUnique whether the selection must be unused
     * @param inputService  input provider
     * @param outputService output service for feedback
     * @return color choice or {@code null} if the user opts to quit
     */
    private ColorChoice promptForColor(String prompt, boolean requireUnique, InputService inputService,
        OutputService outputService) {
        while (true) {
            outputService.print(prompt);
            String response = readLineTrimmed(inputService);
            if (response == null || isQuitCommand(response)) {
                return null;
            }
            ColorChoice choice = resolveColorChoice(response);
            if (choice == null) {
                outputService.println("Unknown color. Available options: " + formatColorOptions());
                continue;
            }
            if (requireUnique && usedColorKeys.contains(choice.key)) {
                outputService.println("That color is already taken. Available options: " + formatColorOptions());
                continue;
            }
            if (requireUnique) {
                usedColorKeys.add(choice.key);
            }
            return choice;
        }
    }


    /**
     * Resolves a color choice from user input.
     * 
     * @param input the user input
     * @return the corresponding ColorChoice, or null if unrecognized
     */
    /**
     * Normalizes user input into a predefined color choice.
     *
     * @param input user-supplied color name or abbreviation
     * @return matching color option when found, otherwise {@code null}
     */
    private ColorChoice resolveColorChoice(String input) {
        if (input == null) {
            return null;
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        switch (normalized) {
            case "r":
            case "red":
                return newColorChoice("red", "\u001B[31m");
            case "b":
            case "blue":
                return newColorChoice("blue", "\u001B[34m");
            case "g":
            case "green":
                return newColorChoice("green", "\u001B[32m");
            case "y":
            case "yellow":
                return newColorChoice("yellow", "\u001B[33m");
            case "m":
            case "magenta":
            case "purple":
                return newColorChoice("magenta", "\u001B[35m");
            case "c":
            case "cyan":
                return newColorChoice("cyan", "\u001B[36m");
            case "w":
            case "white":
                return newColorChoice("white", "\u001B[37m");
            default:
                return null;
        }
    }


    /**
     * Creates a new ColorChoice instance.
     * 
     * @param key      the key for the color choice
     * @param ansiCode the ANSI code for the color
     * @return a new ColorChoice instance
     */
    /**
     * Creates a standardized {@link ColorChoice} instance from the supplied key
     * and ANSI code.
     *
     * @param key      normalized identifier
     * @param ansiCode ANSI escape for rendering
     * @return constructed color choice record
     */
    private ColorChoice newColorChoice(String key, String ansiCode) {
        return new ColorChoice(key, capitalize(key), ansiCode);
    }


    /**
     * Capitalizes the first letter of the given string.
     * 
     * @param value the string to capitalize
     * @return the capitalized string, or an empty string if the input is null or empty
     */
    /**
     * Capitalizes the provided string while lower-casing remaining characters.
     *
     * @param value input string
     * @return capitalized representation or empty string when {@code null}
     */
    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }


    /**
     * Gets the total number of boxes on the board.
     * 
     * @return the total number of boxes
     */
    /**
     * @return total number of boxes on the current board.
     */
    private int getTotalBoxes() {
        return gameGrid.getRows() * gameGrid.getCols();
    }


    /**
     * Centers a token string within a 3-character width.
     * 
     * @param token the token string
     * @return the centered token string
     */
    /**
     * Centers a token string within a three-character cell for board display.
     *
     * @param token token to center
     * @return padded representation suitable for rendering
     */
    private String centerToken(String token) {
        String trimmed = token == null ? "" : token.trim();
        if (trimmed.length() >= 3) {
            return trimmed.substring(0, 3);
        }
        int padding = 3 - trimmed.length();
        int padStart = padding / 2;
        int padEnd = padding - padStart;
        return repeat(' ', padStart) + trimmed + repeat(' ', padEnd);
    }


    /**
     * Reads a line of input and trims whitespace.
     * 
     * @param inputService the input service to read from
     * @return the trimmed input line, or null if end of input
     */
    /**
     * Reads a line from the provided input service and trims whitespace.
     *
     * @param inputService input source
     * @return trimmed string or {@code null} if no input is available
     */
    private String readLineTrimmed(InputService inputService) {
        String line = inputService.readLine();
        return line == null ? null : line.trim();
    }


    /** 
     * Parses an integer from a string token.
     * 
     * @param token the string token
     * @return the parsed integer, or null if parsing fails
     */
    /**
     * Parses an integer token, returning {@code null} when parsing fails.
     *
     * @param token text to parse
     * @return integer value or {@code null} if invalid
     */
    private Integer parseInteger(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            return null;
        }
    }


    /**
     * Parses a DotsAndBoxesEdge from a string token.
     * 
     * @param token the string token
     * @return the corresponding DotsAndBoxesEdge, or null if unrecognized
     */
    /**
     * Converts user-provided edge identifiers into {@link DotsAndBoxesEdge}
     * values.
     *
     * @param token textual representation of an edge
     * @return corresponding edge or {@code null} if unrecognized
     */
    private DotsAndBoxesEdge parseEdge(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        switch (token.toUpperCase(Locale.ROOT)) {
            case "T":
            case "TOP":
                return DotsAndBoxesEdge.TOP;
            case "B":
            case "BOT":
            case "BOTTOM":
                return DotsAndBoxesEdge.BOTTOM;
            case "L":
            case "LEFT":
                return DotsAndBoxesEdge.LEFT;
            case "R":
            case "RIGHT":
                return DotsAndBoxesEdge.RIGHT;
            default:
                return null;
        }
    }

    /**
     * Prompts the user for a required non-blank value.
     * 
     * @param prompt        the prompt message
     * @param inputService  the input service for reading user input
     * @param outputService the output service for displaying prompts and messages
     * @return the entered value, or null if the user chose to quit
     */
    /**
     * Repeatedly prompts for a non-blank value unless the user quits.
     *
     * @param prompt         message presented to the user
     * @param inputService   input provider
     * @param outputService  output destination for feedback
     * @return trimmed user input or {@code null} if the user elects to quit
     */
    private String promptForRequiredValue(String prompt, InputService inputService, OutputService outputService) {
        while (true) {
            outputService.print(prompt);
            String response = readLineTrimmed(inputService);
            if (response == null || isQuitCommand(response)) {
                return null;
            }
            if (!response.isEmpty()) {
                return response;
            }
            outputService.println("Value cannot be blank. Please try again.");
        }
    }


    /**
     * Repeats a character a specified number of times.
     * 
     * @param ch    the character to repeat
     * @param count the number of times to repeat
     * @return the resulting string
     */
    /**
     * Builds a string comprised of a repeated character.
     *
     * @param ch    character to repeat
     * @param count number of repetitions
     * @return resulting string (empty when count is non-positive)
     */
    private String repeat(char ch, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }


    /**
     * Represents a color choice with its key, display name, and ANSI code.
     */
    /**
     * Lightweight value object capturing a selectable color option along with its
     * normalized key and ANSI code.
     */
    private static final class ColorChoice {
        final String key;
        final String displayName;
        final String ansiCode;

        private ColorChoice(String key, String displayName, String ansiCode) {
            this.key = key;
            this.displayName = displayName;
            this.ansiCode = ansiCode;
        }
    }
}
