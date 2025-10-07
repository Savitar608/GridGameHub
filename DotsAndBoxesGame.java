/**
 * Console implementation of the classic Dots and Boxes game leveraging the
 * existing {@link GridGame} framework. Supports head-to-head play as well as
 * teams of two, providing flexible participant configuration and scoreboard
 * tracking.
 */

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DotsAndBoxesGame extends GridGame<DotsAndBoxesCell> {
    private static final int MIN_SIZE = 2;
    private static final int MAX_SIZE = 20;
    private static final int DEFAULT_ROWS = 3;
    private static final int DEFAULT_COLS = 3;
    private static final int TEAM_SIZE = 2;
    private static final String DOT = "•";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String[] COLOR_OPTIONS = { "red", "blue", "green", "yellow", "magenta", "cyan", "white" };

    private final Map<Player, Integer> playerScores = new LinkedHashMap<>();
    private final Map<Team, Integer> teamScores = new LinkedHashMap<>();
    private final List<Team> matchTeams = new ArrayList<>();
    private final Map<Player, String> playerColors = new LinkedHashMap<>();
    private final Map<Team, String> teamColorCodes = new LinkedHashMap<>();
    private final Set<String> usedColorKeys = new HashSet<>();

    private boolean teamMode;
    private int claimedBoxes;

    public DotsAndBoxesGame() {
        super(DotsAndBoxesCell.class, DEFAULT_ROWS, DEFAULT_COLS);
        initializeBoard();
    }

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

    private boolean configureTeamMode(InputService inputService, OutputService outputService) {
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
                addPlayer(player);
                playerScores.put(player, 0);
                playerColors.put(player, teamColor.ansiCode);
            }
        }
        return true;
    }

    @Override
    protected void validateSize() {
        if (getRows() < MIN_SIZE || getCols() < MIN_SIZE || getRows() > MAX_SIZE || getCols() > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "Board size must be between " + MIN_SIZE + "x" + MIN_SIZE + " and " + MAX_SIZE + "x" + MAX_SIZE);
        }
    }

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
        outputService.println("4. The player or team with the most boxes when the board is full wins.");
        outputService.println("Type 'quit' at any prompt to exit the game.\n");
    }

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

    @Override
    protected void processUserInput() {
        OutputService outputService = getOutputService();
        InputService inputService = getInputService();
        Player currentPlayer = getActivePlayer();

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

    @Override
    protected void makeMove(int row, int col) {
        throw new UnsupportedOperationException("Dots and Boxes uses edge-based moves processed elsewhere.");
    }

    @Override
    protected void displayInvalidInputMessage() {
        getOutputService().println("Invalid move. Use the format 'row col side' where side is T, B, L, or R.");
    }

    @Override
    protected boolean checkWinCondition() {
        return claimedBoxes == getTotalBoxes();
    }

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

    private void initializeBoard() {
        for (int r = 0; r < gameGrid.getRows(); r++) {
            for (int c = 0; c < gameGrid.getCols(); c++) {
                gameGrid.setPiece(r, c, new DotsAndBoxesCell());
            }
        }
        claimedBoxes = 0;
    }

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

    private void incrementScore(Player player, int delta) {
        playerScores.merge(player, delta, Integer::sum);
        player.getTeam().ifPresent(team -> {
            if (teamMode) {
                teamScores.merge(team, delta, Integer::sum);
            }
        });
    }

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

    private String formatHorizontalEdge(DotsAndBoxesCell cell, DotsAndBoxesEdge edge) {
        if (cell != null && cell.hasEdge(edge)) {
            return applyColor("───", cell.getEdgeColor(edge));
        }
        return "   ";
    }

    private String formatVerticalEdge(DotsAndBoxesCell cell, DotsAndBoxesEdge edge) {
        if (cell != null && cell.hasEdge(edge)) {
            return applyColor("│", cell.getEdgeColor(edge));
        }
        return " ";
    }

    private String applyColor(String text, String colorCode) {
        if (text == null) {
            return null;
        }
        if (colorCode == null || colorCode.isEmpty() || ANSI_RESET.equals(colorCode)) {
            return text;
        }
        return colorCode + text + ANSI_RESET;
    }

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

    private ColorChoice newColorChoice(String key, String ansiCode) {
        return new ColorChoice(key, capitalize(key), ansiCode);
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private int getTotalBoxes() {
        return gameGrid.getRows() * gameGrid.getCols();
    }

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

    private String readLineTrimmed(InputService inputService) {
        String line = inputService.readLine();
        return line == null ? null : line.trim();
    }

    private Integer parseInteger(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

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
