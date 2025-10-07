/**
 * Console implementation of the classic Dots and Boxes game leveraging the
 * existing {@link GridGame} framework. Supports head-to-head play as well as
 * teams of two, providing flexible participant configuration and scoreboard
 * tracking.
 */

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DotsAndBoxesGame extends GridGame<DotsAndBoxesCell> {
    private static final int MIN_SIZE = 2;
    private static final int MAX_SIZE = 20;
    private static final int DEFAULT_ROWS = 3;
    private static final int DEFAULT_COLS = 3;
    private static final int TEAM_SIZE = 2;
    private static final String DOT = "•";

    private final Map<Player, Integer> playerScores = new LinkedHashMap<>();
    private final Map<Team, Integer> teamScores = new LinkedHashMap<>();
    private final List<Team> matchTeams = new ArrayList<>();

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
    protected boolean configurePlayers(InputService inputService, OutputService outputService) {
        clearPlayers();
        playerScores.clear();
        teamScores.clear();
        matchTeams.clear();
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
            String teamColor = promptForOptionalValue("Team color (optional): ", inputService, outputService);
            if (teamColor == null && isExitRequested()) {
                return false;
            }
            Team team = new Team(teamName, teamTag, teamColor);
            matchTeams.add(team);
            teamScores.put(team, 0);

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
                topLine.append(cell.hasEdge(DotsAndBoxesEdge.TOP) ? "───" : "   ");
                topLine.append(DOT);
            }
            outputService.println(topLine.toString());

            StringBuilder middleLine = new StringBuilder();
            for (int c = 0; c < cols; c++) {
                DotsAndBoxesCell cell = gameGrid.getPiece(r, c);
                middleLine.append(cell.hasEdge(DotsAndBoxesEdge.LEFT) ? "│" : " ");
                middleLine.append(centerToken(cell.getDisplayToken()));
            }
            DotsAndBoxesCell lastCell = gameGrid.getPiece(r, cols - 1);
            middleLine.append(lastCell.hasEdge(DotsAndBoxesEdge.RIGHT) ? "│" : " ");
            outputService.println(middleLine.toString());
        }

        // Bottom border
        StringBuilder bottomLine = new StringBuilder();
        bottomLine.append(DOT);
        for (int c = 0; c < gameGrid.getCols(); c++) {
            DotsAndBoxesCell cell = gameGrid.getPiece(gameGrid.getRows() - 1, c);
            bottomLine.append(cell.hasEdge(DotsAndBoxesEdge.BOTTOM) ? "───" : "   ");
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
        if (!primary.addEdge(edge)) {
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
            neighbor.addEdge(edge.opposite());
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
            outputService.println(String.format(Locale.ROOT, "  %s: %d", player.getName(), score));
        }
        if (teamMode && !teamScores.isEmpty()) {
            outputService.println("Team totals:");
            for (Team team : matchTeams) {
                int score = teamScores.getOrDefault(team, 0);
                outputService.println(String.format(Locale.ROOT, "  %s (%s): %d", team.getName(), team.getTag(), score));
            }
        }
        outputService.println("");
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

    private String promptForOptionalValue(String prompt, InputService inputService, OutputService outputService) {
        outputService.print(prompt);
        String response = readLineTrimmed(inputService);
        if (response == null || isQuitCommand(response)) {
            requestExit();
            return null;
        }
        return response.isEmpty() ? null : response;
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
}
