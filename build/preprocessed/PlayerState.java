

/**
 *
 * @author Ayudh
 */
public class PlayerState {
    
    public static final int IDLE = 0;
    public static final int TUTORIAL_PLAYING = 1;
    public static final int TUTORIAL_PAUSED = 2;
    public static final int QUIZ = 3;
    public static final int RECORDING = 4;
    
    private static int playerState = 0;
    
    public static int getCurrentState() {
        return playerState;
    }
    
    public static void setState(int state) {
        playerState = state;
    }
}
