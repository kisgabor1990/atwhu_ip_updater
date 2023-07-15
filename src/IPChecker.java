import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

public class IPChecker
{

    public Logger loggerChanged, loggerChecked;
    private String lastIP, currentIP;
    private File lastIPFile;

    public IPChecker() {
        this.loggerChanged = new Logger( "ipChanged" );
        this.loggerChecked = new Logger( "ipChecked" );
        this.lastIPFile    = new File( Main.APP_DIR + "lastIP.txt" );
        try {
            if ( this.lastIPFile.createNewFile() ) {
                System.out.println( "A 'lastIP.txt' fájl nem található! Kérem, adja meg a frissíteni kívánt IP címet!" );
                this.setLastIP();
                this.saveLastIPFile( this.lastIP );
                this.loggerChecked.log( "A 'lastIP.txt' fájl létrejött!" );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        this.lastIP    = this.getLastIPFromFile();
        this.currentIP = this.getCurrentIPFromWeb();
    }

    public boolean check() {
        if ( ! Objects.equals( this.lastIP, this.currentIP ) ) {
            this.loggerChanged.log( "Az IP cím megváltozott! Új IP cím: " + this.currentIP );

            return true;
        }
        return false;
    }

    public String getLastIP() {
        return this.lastIP;
    }

    public String getCurrentIP() {
        return this.currentIP;
    }

    public File getLastIPFile() {
        return lastIPFile;
    }

    public void setLastIP() {
        Scanner scanner = new Scanner( System.in );
        String  IP      = "";

        while ( IP.equals( "" ) ) {
            System.out.print( "IP cím: " );
            IP = scanner.nextLine();
            if ( ! this.isValidIP( IP ) ) {
                System.out.println( "Az IP cím formátuma nem megfelelő! Próbáld újra!" );
                IP = "";
            }
        }

        this.lastIP = IP;
    }

    public void saveLastIPFile( String IP ) {
        try ( BufferedWriter bw = new BufferedWriter( new FileWriter( this.getLastIPFile() ) ) ) {
            bw.write( IP );
            this.loggerChecked.log( "Az új IP cím (" + IP + ") rögzítése fájlba sikeres." );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private String getLastIPFromFile() {
        this.loggerChecked.log( "Fájl megnyitása: lastIP.txt..." );
        String lastip = "";
        try ( BufferedReader br = new BufferedReader( new FileReader( this.lastIPFile ) ) ) {
            this.loggerChecked.log( "Fájl megnyitása sikeres!" );
            lastip = br.readLine();
            this.loggerChecked.log( "A fájlban tárolt IP cím: " + lastip );
        } catch ( IOException e ) {
            e.printStackTrace();
            this.loggerChecked.log( "A lastIP.txt megnyitása sikertelen!" );
            lastip = "0.0.0.0";
        }

        return lastip;
    }

    private String getCurrentIPFromWeb() {
        this.loggerChecked.log( "A http://checkip.amazonaws.com oldal felkeresése..." );
        String myip = "";
        try {
            URL            checkip = new URL( "http://checkip.amazonaws.com" );
            BufferedReader br      = new BufferedReader( new InputStreamReader( checkip.openStream() ) );
            myip = br.readLine();
            this.loggerChecked.log( "A jelenlegi IP cím: " + myip );
        } catch ( IOException e ) {
            e.printStackTrace();
            this.loggerChecked.log( "A jelenlegi IP cím meghatározása sikertelen!" );
            myip = "0.0.0.0";
        }

        return myip;
    }

    private boolean isValidIP( String ip ) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith( "." ) ) {
                return false;
            }

            return true;
        } catch ( NumberFormatException nfe ) {
            return false;
        }
    }

}
