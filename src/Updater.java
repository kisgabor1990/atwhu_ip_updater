import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.time.Duration;

public class Updater {

    public Logger logger;
    public Settings settings;
    public IPChecker ipChecker;
    private boolean needUpdate;

    public Updater() {
        this.logger     = new Logger( "updater" );
        this.settings   = new Settings();
        this.ipChecker  = new IPChecker();
        this.needUpdate = this.ipChecker.check();
    }

    public boolean update() {
        WebDriver driver = new HtmlUnitDriver(true);
        WebDriverWait wait = new WebDriverWait( driver, Duration.ofSeconds(60) );

        logger.log( "Az iAdmin bejelentkező oldalának megnyitása..." );
        driver.get( "https://iadmin.hu/login/" );
        WebElement usernameElement = wait.until(ExpectedConditions.elementToBeClickable( By.id("i_id") ));
        WebElement passwordElement = wait.until(ExpectedConditions.elementToBeClickable( By.id( "i_passwd" ) ));
        WebElement loginButtonElement = wait.until(ExpectedConditions.elementToBeClickable( By.id( "btn_login" ) ));
        usernameElement.sendKeys( this.settings.getUsername() );
        logger.log( "Felhasználónév elküldve." );
        passwordElement.sendKeys( this.settings.getPassword() );
        logger.log( "Jelszó elküldve." );
        loginButtonElement.click();
        logger.log( "Bejelentkezés..." );

        driver.get( "https://iadmin.hu/portal/content/main/welcome.jsp" );
        if ( driver.getTitle().equals( "ATW iADMIN" )) {
            logger.log( "Sikeres bejelentkezés!" );
        } else {
            logger.log( "Sikertelen bejelentkezés! Ellenőrizd a beállításokat! ");
            driver.quit();
            return false;
        }

        logger.log( "A domain-hez (ID: " + this.settings.getDomainID() + ") tartozó A rekord (ID: " + this.settings.getUpdateID() + ") oldalának megnyitása..." );
        driver.get( "https://iadmin.hu/portal/content/domain/zone_edit.jsp?d=update&domain=" + this.settings.getDomainID() + "&id=" + this.settings.getUpdateID() );


        try {
            WebElement valueElement = wait.until(ExpectedConditions.elementToBeClickable( By.name( "value" ) ));
            valueElement.clear();
            valueElement.sendKeys( this.ipChecker.getCurrentIP() );
            valueElement.submit();
            logger.log( "Az új IP cím elküldve." );
        } catch ( TimeoutException e ) {
            logger.log( "Időtúllépés!" );
            driver.quit();
            return false;
        }

        WebElement mainContentElement = wait.until( ExpectedConditions.elementToBeClickable( By.className( "maincontent" ) ));

        try {
            WebElement positipElement = mainContentElement.findElement( By.className( "positip" ) );
            logger.log( positipElement.getText() );
            driver.get( "https://iadmin.hu/portal/content/domain/zone_edit.jsp?domain=" + this.settings.getDomainID() + "&d=save" );
            logger.log( "A módosítások mentése sikeres!" );

            try ( BufferedWriter bw = new BufferedWriter( new FileWriter( this.ipChecker.getLastIPFile() ) ) ) {
                bw.write( this.ipChecker.getCurrentIP() );
                logger.log( "Az új IP cím (" + this.ipChecker.getCurrentIP() + ") rögzítése fájlba sikeres." );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } catch ( NoSuchElementException e ) {
            WebElement errtipElement = mainContentElement.findElement( By.className( "errtip" ) );
            logger.log(errtipElement.getText().replace( "\n", " " ) );
            driver.quit();
            return false;
        }

        logger.log( "Kijelentkezés..." );
        driver.get( "https://iadmin.hu/login/logout.jsp" );

        driver.quit();
        return true;
    }

    public boolean needUpdate() {
        return this.needUpdate;
    }

}
