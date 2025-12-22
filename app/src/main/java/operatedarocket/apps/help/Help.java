package operatedarocket.apps.help;

import operatedarocket.Utilities;

public class Help implements Runnable {
    @Override
    public void run() {
        Utilities.getPort("/web/Manual/Index.html", true);
    }
}
