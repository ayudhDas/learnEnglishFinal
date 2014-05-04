import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.*;
import javax.microedition.rms.RecordStore;

/**
 * @author Ayudh
 */
public class MainMidlet extends MIDlet implements CommandListener{
    
    private final String ROOT = "/";
    private final String PROTOCOL = "file://";
    private final String DSNAME = "learnEnglishDataStore";
    private FileConnection fc;
    private RecordStore rs;
    
    private Command exitCommand;
    private Command backCommand;
    private Command setCommand;
    
    private String currPath = null;
    private List installerList;
    private List tutList;
    private Command openCommand;
    
    private PlayerCanvas myCanvas;
    
    public MainMidlet() {
        tutList = new List("tutorials", List.IMPLICIT);
        exitCommand = new Command("exit", Command.EXIT, 1);
        openCommand = new Command("open", Command.OK, 1);
        setCommand = new Command("set", Command.ITEM, 1);
        backCommand = new Command("back", Command.BACK, 1);
        tutList.addCommand(openCommand);
        tutList.addCommand(exitCommand);
        tutList.setCommandListener(this);
    }

    public void startApp() {
        if(isInstalled()){
            
            currPath = getPathFromDb();
            listTuts(currPath);
        }
        //if not, install
        else{
            setup();
        }
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }

    public void commandAction(Command c, Displayable d) {
        if(c == exitCommand) {
            destroyApp(true);
        }
        
        if(d == tutList && c == openCommand) {
            playTutorial(tutList.getString(tutList.getSelectedIndex()));
        }
        
        if(d == myCanvas && c == backCommand){
            myCanvas.cleanUp();
            Display.getDisplay(this).setCurrent(tutList);
        }
        
        if(d == installerList) {
            if(c == openCommand) {
                //check if selected is folder
                //if yes go inside
                try{
                    String temp = currPath + installerList.getString(installerList.getSelectedIndex());
                    fc = (FileConnection) Connector.open(PROTOCOL + temp, Connector.READ);
                    if(fc.isDirectory()) {
                        //update currPath
                        currPath = temp;
                        //refresh list
                        installerList.deleteAll();
                        Enumeration tempList = fc.list();
                        while(tempList.hasMoreElements()) {
                            installerList.append((String)tempList.nextElement(), null);
                        }
                    }
                    fc.close();
                }
                catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            
            if(c == backCommand) {
                if(currPath.equals(ROOT)) destroyApp(true);
                else {
                    try{
                    //update current path
                    //System.out.println(currPath);
                    currPath = currPath.substring(0, currPath.lastIndexOf('/', currPath.length() - 2) + 1);
                    //System.out.println(temp);
                    //refresh list
                    if(currPath.equals(ROOT)){
                        Enumeration ls = FileSystemRegistry.listRoots();
                        installerList.deleteAll();
                        while(ls.hasMoreElements()) {
                                installerList.append((String)ls.nextElement(), null);
                            }
                    }
                    else{
                        fc = (FileConnection) Connector.open(PROTOCOL + currPath, Connector.READ);
                        installerList.deleteAll();
                            Enumeration tempList = fc.list();
                            while(tempList.hasMoreElements()) {
                                installerList.append((String)tempList.nextElement(), null);
                            }
                        fc.close();
                    }
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            
            if(c == setCommand) {
                try{
                    String temp = currPath + installerList.getString(installerList.getSelectedIndex());
                    fc = (FileConnection) Connector.open(PROTOCOL + temp, Connector.READ);
                    if(fc.isDirectory()){
                        rs = RecordStore.openRecordStore(DSNAME, true);
                        rs.addRecord(temp.getBytes(), 0, temp.getBytes().length);
                        rs.closeRecordStore();
                    }
                    fc.close();
                    listTuts(getPathFromDb());
                }
                catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    
    
    private void playTutorial(String tutName) {
        String resourcePath = currPath + tutName;
        myCanvas = new PlayerCanvas(PROTOCOL + resourcePath);
        myCanvas.addCommand(backCommand);
        myCanvas.addCommand(exitCommand);
        myCanvas.setCommandListener(this);
        Display.getDisplay(this).setCurrent(myCanvas);
        PlayerState.setState(PlayerState.TUTORIAL_PLAYING);
        myCanvas.go();
        System.out.println(tutName);
    }
    
    private boolean isInstalled() {
        try{
            String[] ds = RecordStore.listRecordStores();
            for(int i = 0; i<ds.length; i++) {
                if(ds[i].equals(DSNAME)) return true;
            }
        }
        catch(Exception e){
            System.out.println("isInstalled(): "+e.getMessage());
        }
        
        return false;
    }
    
    private String getPathFromDb() {
        try{
            rs = RecordStore.openRecordStore(DSNAME, false);
            String temp = new String(rs.getRecord(1));
            System.out.println("temp: "+temp);
            rs.closeRecordStore();
            return temp;
        }
        catch(Exception e) {
            System.out.println("getPathFromDb(): "+e.getMessage());
        }
        return null;
    }
    
    private void setup() {
        try{
            currPath = ROOT;
            //list roots
            
            installerList = new List("set target folder", List.IMPLICIT);
            Enumeration flist = FileSystemRegistry.listRoots();
            while(flist.hasMoreElements()){
                installerList.append((String) flist.nextElement(), null);
            }
            installerList.addCommand(exitCommand);
            installerList.addCommand(setCommand);
            installerList.addCommand(openCommand);
            installerList.addCommand(backCommand);
            installerList.setCommandListener(this);
            Display.getDisplay(this).setCurrent(installerList);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void listTuts(String path) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open(PROTOCOL + path, Connector.READ);
            Enumeration fileList = fileConnection.list();
            while (fileList.hasMoreElements()) {
                tutList.append((String)fileList.nextElement(), null);
            }
            fileConnection.close();
            Display.getDisplay(this).setCurrent(tutList);
        }
        catch(Exception e) {
            System.out.println("listTuts(): "+e.getMessage());
            System.out.println(e.getClass());
        }
    }
}
