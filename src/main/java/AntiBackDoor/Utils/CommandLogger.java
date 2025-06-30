package AntiBackDoor.Utils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommandLogger {
    private final File logDirectory;

    public CommandLogger(File dataFolder, String logFileName) {
        this.logDirectory = new File(dataFolder, "logs");
        if (!logDirectory.exists()) logDirectory.mkdirs();
    }

    public void logCommand(String playerName, String uuid, String ip, String command) {
        // Tạo file log theo ngày
        String datePrefix = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dailyLogFile = new File(logDirectory, datePrefix + "_commands.log");
        
        // Ghi vào file
        try (FileWriter fw = new FileWriter(dailyLogFile, true)) {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String logEntry = String.format(
                "[%s] [%s][%s][%s]: %s",
                time, playerName, uuid, ip, command
            );
            fw.write(logEntry + "\n");
            fw.flush(); // Đảm bảo dữ liệu được ghi ngay lập tức
        } catch (Exception e) {
            System.err.println("Lỗi khi ghi log lệnh: " + e.getMessage());
            e.printStackTrace();
        }
    }
}