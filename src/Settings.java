import org.jasypt.util.text.AES256TextEncryptor;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class Settings {

    public Logger logger;
    private String username = "", password = "";
    private String domainID = "", updateID = "";
    private boolean validSettings;
    private final File settingsFile;
    private final AES256TextEncryptor aesEncryptor;

    public Settings() {
        String secretPassword = "cMnYk#p6L.yKg'@6";
        this.logger       = new Logger( "settings" );
        this.settingsFile = new File( Main.APP_DIR + "settings.conf" );
        this.aesEncryptor = new AES256TextEncryptor();
        this.aesEncryptor.setPassword( secretPassword );

        try {
            if ( this.settingsFile.createNewFile() ) {
                System.out.println("Az alkalmazás első indítása. Adja meg a futtatáshoz nélkülözhetetlen adatokat!");
                this.saveSettings();
                this.createSettingsFile();
                this.logger.log( "Első indítás. A settings.conf fájl létrejött!" );
            }
            this.readSettings();
            this.checkSettings();
            if ( !this.isValidSettings() ) {
                this.logger.log( "A beállítások fájl tartalma hiányos! Minden sort ki kell tölteni!" );
            } else {
                this.logger.log( "Az adatok hiánytalanul ki vannak töltve." );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public boolean isValidSettings() {
        return this.validSettings;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDomainID() {
        return domainID;
    }

    public String getUpdateID() {
        return updateID;
    }

    private void saveSettings() {
        Scanner scanner = new Scanner( System.in );
        while ( this.username.equals( "" ) ) {
            System.out.print( "Felhasználónév: ");
            this.username = scanner.nextLine();
        }
        while ( this.password.equals( "" ) ) {
            System.out.print( "Jelszó: ");
            this.password = scanner.nextLine();
        }
        while ( this.domainID.equals( "" ) ) {
            System.out.print( "Domain azonosító: ");
            this.domainID = scanner.nextLine();
        }
        while ( this.updateID.equals( "" ) ) {
            System.out.print( "Frissíteni kívánt rekord azonosítója: ");
            this.updateID = scanner.nextLine();
        }
        scanner.close();
    }

    private void readSettings() {
        this.logger.log( "Fájl megnyitása: settings.conf..." );
        try ( BufferedReader br = new BufferedReader( new FileReader( this.settingsFile ) ) ) {
            this.logger.log( "Fájl megnyitása sikeres!" );
            String line, property, value;
            String[] values;
            while ( (line = br.readLine()) != null ) {
                if ( line.startsWith( "//" ) || !line.contains( "=" ) ) {
                    continue;
                }

                values   = line.split( "=" );
                property = values[0].trim();
                value    = String.join("=", Arrays.stream( values ).skip( 1 ).map( String::trim ).toList());

                switch ( property ) {
                    case "USERNAME" -> this.username = value;
                    case "PASSWORD" -> this.password = value.equals( "" ) ? "" : this.decrypt(value);
                    case "DOMAIN_ID" -> this.domainID = value;
                    case "UPDATE_ID" -> this.updateID = value;
                }
            }
            this.logger.log( "Az adatok beolvasása fájlból sikeres!" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        if ( this.password.equals( "" ) ) {
            this.password = this.setNewPassword();
            this.createSettingsFile();
        }
    }

    private String setNewPassword() {
        Scanner scanner = new Scanner( System.in );
        String passwd = "";
        while ( passwd.equals( "" ) ) {
            System.out.print( "Új jelszó: ");
            passwd = scanner.nextLine();
        }
        scanner.close();

        return passwd;
    }

    private void checkSettings() {
        this.logger.log( "Adatok ellenőrzése..." );
        this.validSettings = !this.username.equals( "" ) && !this.password.equals( "" ) && !this.domainID.equals( "" ) && !this.updateID.equals( "" );
    }

    private void createSettingsFile() {
        try ( BufferedWriter bw = new BufferedWriter( new FileWriter( this.settingsFile ) ) ) {
            bw.write( """
                    // E-mail cím a bejelentkezéshez
                    USERNAME = %s
                    
                    // Jelszó a bejelentkezéshez. Ha új jelszót szeretnél beállítani, töröld ki a jelenlegit, és indítsd el az alkalmazást
                    PASSWORD = %s
                    
                    // A módosítani kívánt domain azonosítója. A böngésző címsorában megtalálod (zone_edit.jsp?domain=<???>)
                    DOMAIN_ID = %s
                    
                    // A módosítani kívánt rekord azonosítója. A böngésző címsorában megtalálod (zone_edit.jsp?d=update&domain=???&id=<???>)
                    UPDATE_ID = %s
                    """.formatted( this.username, this.encrypt( this.password ), this.domainID, this.updateID ) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private String encrypt(String password) {
        return aesEncryptor.encrypt(password);
    }

    private String decrypt(String password) {
        return aesEncryptor.decrypt( password );
    }

}
