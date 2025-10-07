/**
 * Tiny launcher that starts the Game Hub application which provides a
 * menu for launching the included games.
 */
public final class GameHub {
    /**
     * Entrypoint which constructs the application and begins the menu loop.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        new GameHubApp().start();
    }
}
