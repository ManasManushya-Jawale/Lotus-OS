package operatedarocket.apps.FileManager;

import operatedarocket.Utilities;
import operatedarocket.apps.HTMLEngine.HTMLEngineApp;

public class FileManagerApp extends HTMLEngineApp {
    public FileManagerApp(String title, String imgPath) {
        super(title, imgPath);

        addTab(title, Utilities.getLink("https://www.youtube.com",false));
    }
}
