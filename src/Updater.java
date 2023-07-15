import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Updater
{

    public  Logger    logger;
    public  Settings  settings;
    public  IPChecker ipChecker;
    private boolean   needUpdate;

    public Updater() {
        this.logger     = new Logger( "updater" );
        this.settings   = new Settings();
        this.ipChecker  = new IPChecker();
        this.needUpdate = this.ipChecker.check();
    }

    public boolean update() {
        System.setProperty( "webdriver.chrome.driver", Main.APP_DIR + "chromedriver.exe" );
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--ignore-certificate-errors",
                "--disable-extensions",
                "--no-sandbox",
                "--disable-dev-shm-usage"
        );
        WebDriver     driver = new ChromeDriver( options );
        WebDriverWait wait   = new WebDriverWait( driver, Duration.ofSeconds( 30 ) );

        try {
            logger.log( "Az iAdmin bejelentkező oldalának megnyitása..." );
            driver.get( "https://iadmin.hu/login/" );

            WebElement usernameBox = wait.until( ExpectedConditions.elementToBeClickable( By.id( "i_id" ) ) );
            usernameBox.sendKeys( this.settings.getUsername() );
            logger.log( "Felhasználónév elküldve." );

            WebElement passwordBox = wait.until( ExpectedConditions.elementToBeClickable( By.id( "i_passwd" ) ) );
            passwordBox.sendKeys( this.settings.getPassword() );
            logger.log( "Jelszó elküldve." );

            WebElement signinButton = wait.until( ExpectedConditions.elementToBeClickable( By.id( "btn_login" ) ) );
            signinButton.click();
            logger.log( "Bejelentkezés..." );

            wait.until( ExpectedConditions.visibilityOfElementLocated( By.className( "maincontent" ) ) );

            String welcomeURL = "https://iadmin.hu/portal/content/main/welcome.jsp";
            driver.get( welcomeURL );
            if ( driver.getCurrentUrl().equals( welcomeURL ) ) {
                logger.log( "Sikeres bejelentkezés!" );
            } else {
                logger.log( "Sikertelen bejelentkezés! Ellenőrizd a beállításokat! " );
                driver.quit();
                return false;
            }

            logger.log( "A tömeges IP módosítás oldalának megnyitása..." );
            driver.get( "https://iadmin.hu/portal/content/domain/mass_ip.jsp" );

            WebElement actualIPElement = wait.until( ExpectedConditions.elementToBeClickable( By.id( "i_ip_act" ) ) );
            actualIPElement.sendKeys( this.ipChecker.getLastIP() );

            WebElement newIPElement = wait.until( ExpectedConditions.elementToBeClickable( By.id( "i_ip_new" ) ) );
            newIPElement.click();
            wait.until( ExpectedConditions.elementToBeClickable( newIPElement ) );
            newIPElement.sendKeys( this.ipChecker.getCurrentIP() );

            WebElement updateButtonElement = wait.until( ExpectedConditions.elementToBeClickable( By.id( "btn_changeIP" ) ) );
            updateButtonElement.click();
            wait.until( ExpectedConditions.elementToBeClickable( updateButtonElement ) );
            updateButtonElement.click();

            logger.log( "Az új IP cím elküldve." );

            WebElement overlay    = wait.until( ExpectedConditions.elementToBeClickable( By.id( "overlay" ) ) );
            WebElement overlayBox = overlay.findElement( By.id( "overlaybox" ) );
            WebElement okButton   = overlay.findElement( By.tagName( "button" ) );
            if ( ! overlayBox.getText().contains( "Siker" ) ) {
                throw new Exception( "Sikertelen módosítás!" );
            }
            logger.log( "Sikeres módosítás!" );
            okButton.click();

            this.ipChecker.saveLastIPFile( this.ipChecker.getCurrentIP() );
        } catch ( Exception e ) {
            logger.log( e.getMessage() );
            System.out.println( e.getMessage() );
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
