



import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

/**
 *
 * @author Ayudh
 */
public class PlayerCanvas extends Canvas implements PlayerListener{
    
    private FileConnection fileConnection;
    private InputStream inputStream;
    private Player player;
    private String messageString = "press 5 for play/pause";
    private String errorString = null;
    
    public PlayerCanvas(String path) {
        try{
            fileConnection = (FileConnection) Connector.open(path, Connector.READ);
            inputStream = fileConnection.openInputStream();
            player = Manager.createPlayer(inputStream, "audio/mpeg");
            
        }
        catch(Exception e) {
            
        }
    }
    

    protected void paint(Graphics g) {
        g.setColor(0, 255, 0);
        g.drawString( messageString, 0, 10, g.TOP | g.LEFT);
    }

    protected void keyPressed(int keyCode) {
        System.out.println(keyCode == KEY_NUM5);
        if (keyCode == KEY_NUM5) {
            try{
                System.out.println(PlayerState.getCurrentState());
                if(PlayerState.getCurrentState() == PlayerState.TUTORIAL_PLAYING) {
                    System.out.println("paused");
                    player.stop();
                    PlayerState.setState(PlayerState.TUTORIAL_PAUSED);
                }
                else if(PlayerState.getCurrentState() == PlayerState.TUTORIAL_PAUSED) {
                    System.out.println("play");
                    player.start();
                    PlayerState.setState(PlayerState.TUTORIAL_PLAYING);
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    public void go() {
        
        try {
            player.setLoopCount(-1);
            player.prefetch();
            player.realize();
            player.addPlayerListener(this);
            player.start();
        } catch (Exception e) {
            errorString = e.getMessage();
            repaint();
        }
    }
    
    public void cleanUp() {
        try{
            player.close();
            inputStream.close();
            fileConnection.close();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        PlayerState.setState(PlayerState.IDLE);
    }

    public void playerUpdate(Player player, String event, Object eventData) {
    }
    
    
    
}
