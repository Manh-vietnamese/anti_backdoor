package AntiBackDoor.Utils;

import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class LogManager {
    private final File logFile;

    public LogManager(File dataFolder, String logFileName) {
        File logsDir = new File(dataFolder, "logs");
        if (!logsDir.exists()) logsDir.mkdirs();
        this.logFile = new File(logsDir, logFileName);
    }

    public void logIP(Player player) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        String logEntry = String.format(
            "[%s] [%s][%s][%s]: đã đăng nhập.",
            time, player.getName(), player.getUniqueId(),
            player.getAddress().getAddress().getHostAddress()
        );
        
        appendToLog(logEntry);
    }

    private void appendToLog(String message) {
        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write(message + "\n");
        } catch (IOException e) {
            // Xử lý lỗi nếu cần
        }
    }
}