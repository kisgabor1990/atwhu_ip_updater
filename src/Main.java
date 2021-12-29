import java.io.File;
import java.net.URISyntaxException;

public class Main {

    public static final String APP_DIR = getAppDir();

    public static void main(String[] args) {
        Logger logger = new Logger("app");
        Updater updater = new Updater();

        logger.log("Az alkalmazás indítása...");

        logger.log( "Adatok beolvasása a settings.conf fájlból..." );
        if ( !updater.settings.isValidSettings() ) {
            logger.log( "A beállítások fájl tartalma hiányos. Az alkalmazás leáll." );
            System.exit( 0 );
        }
        logger.log( "Adatok beolvasása sikeres." );

        logger.log( "IP cím ellenőrzése..." );
        if ( updater.needUpdate() ) {
            if ( updater.ipChecker.getLastIP().equals( "0.0.0.0" ) || updater.ipChecker.getCurrentIP().equals( "0.0.0.0" ) ) {
                logger.log( "Az IP cím ellenőrzése sikertelen! További információ az ipChecked log-okban. Az alkalmazás leáll." );
                System.exit( 0 );
            }

            logger.log( "Az IP cím megváltozott. Előző: " + updater.ipChecker.getLastIP() + ". Jelenlegi: " + updater.ipChecker.getCurrentIP() + "." );

            logger.log( updater.update() ? "IP cím sikeresen frissítve!" : "Az IP cím frissítése sikertelen. További információ az updater log-okban." );
        } else {
            logger.log( "Az IP cím nem változott. Nincs szükség a frissítésre!" );
        }

        logger.log( "Kilépés az alkalmazásból..." );

    }

    private static String getAppDir() {
        try {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + File.separator;
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
            return "";
        }
    }

}
