package operatedarocket.util.PlayerData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class PlayerDataSaverAndReader {

    private static final String SAVE_FILE = "PlayerData/playerdata.dat";

    public static void init() {
        PlayerDataSaverAndReader.save(PlayerDataSaverAndReader.load());
    }

    // Save --------------------------------------------------------------
    public static void save(PlayerData player) {
        File file = resolveWritablePath(SAVE_FILE);
        ensureParentDir(file);

        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(player);
            System.out.println("Player saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save player data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load --------------------------------------------------------------
    public static PlayerData load() {
        File file = resolveWritablePath(SAVE_FILE);

        if (!file.exists()) {
            System.out.println("No existing player data. Returning defaults.");
            return defaultPlayerData();
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Object obj = ois.readObject();
            if (obj instanceof PlayerData pd) {
                return pd;
            } else {
                System.out.println("Saved data is not PlayerData. Returning defaults.");
                return defaultPlayerData();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load player data: " + e.getMessage());
            e.printStackTrace();
            return defaultPlayerData();
        }
    }

    // Helpers -----------------------------------------------------------
    private static File resolveWritablePath(String relative) {
        // Always use a writable user-home directory
        String userHome = System.getProperty("user.home");
        Path base = Path.of(userHome, "OperateDaRocket");
        Path path = base.resolve(relative);
        return path.toFile();
    }

    private static void ensureParentDir(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            try {
                Files.createDirectories(parent.toPath());
            } catch (IOException e) {
                System.err.println("Failed to create directories for " + parent);
                e.printStackTrace();
            }
        }
    }

    private static PlayerData defaultPlayerData() {
        PlayerData pd = new PlayerData();
        pd.name = "Manas";
        pd.date = LocalDate.of(2125, 12, 5);
        return pd;
    }
}
