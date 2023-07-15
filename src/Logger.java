import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger
{

    private String            logFilePath;
    private String            logFolderPath;
    private File              logFile;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter fileNameFormatter;
    private LocalDateTime     now;

    public Logger( String type ) {
        this.dateFormatter     = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
        this.fileNameFormatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
        this.now               = LocalDateTime.now();
        this.logFolderPath     = Main.APP_DIR + "logs" + File.separator + type + File.separator + now.getYear() + File.separator + now.getMonthValue() + File.separator;
        this.logFilePath       = fileNameFormatter.format( now ) + ".log";
        this.logFile           = new File( logFolderPath + logFilePath );
    }

    public void log( String msg ) {
        this.logFile.getParentFile().mkdirs();
        try {
            this.logFile.createNewFile();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try ( BufferedWriter bw = new BufferedWriter( new FileWriter( this.logFile, true ) ) ) {
            bw.write( "[" + this.dateFormatter.format( LocalDateTime.now() ) + "] " + msg );
            bw.newLine();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

}
