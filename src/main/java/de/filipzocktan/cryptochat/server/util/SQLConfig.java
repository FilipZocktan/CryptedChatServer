package de.filipzocktan.cryptochat.server.util;

import de.filipzocktan.util.general.Filetype;
import de.filipzocktan.util.general.ZocFile;
import io.sentry.Sentry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLConfig {

    private ZocFile config;
    private String host;
    private String port;
    private String schema;
    private String user;
    private String password;
    private Connection sqlConnection;


    public SQLConfig() {
        config = new ZocFile("config", "sql", Filetype.fzas);
        try {
            reloadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            Sentry.capture(e);
        }
    }

    private void reloadConfig() throws IOException {
        if (!config.exists()) {
            config.getParentFile().mkdirs();
            config.createNewFile();
            FileWriter writer = new FileWriter(config);
            writer.write("host: localhost\n");
            writer.write("port: 3306\n");
            writer.write("user: crypto\n");
            writer.write("password: \n");
            writer.write("schema: cryptochat\n");
            writer.flush();
            writer.close();
        }
        BufferedReader reader = new BufferedReader(new FileReader(config));
        while (reader.ready()) {
            String[] line = reader.readLine().split(": ");
            switch (line[0].toLowerCase()) {
                case "host":
                    this.host = line[1];
                    break;
                case "port":
                    this.port = line[1];
                    break;
                case "user":
                    this.user = line[1];
                    break;
                case "password":
                    this.password = line[1];
                    break;
                case "schema":
                    this.schema = line[1];
                    break;
            }
        }
        reloadConnection();
    }

    private void reloadConnection() {

        try {
            sqlConnection = DriverManager.getConnection("jdbc:mysql://" + getHost() + ":" + getPort() + "/" + getSchema() + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=CET", getUser(), getPassword());
        } catch (SQLException ex) {
            System.out.println("Error whilst connecting to database. Is the host, port, schema,username and password set correctly? Check your \"sql.fzas\".");
//            System.out.println(getHost());
//            System.out.println(getPort());
//            System.out.println(getUser());
//            System.out.println(getPassword());
//            System.out.println("jdbc:mysql://" + getHost()+":"+getPort()+"/");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            System.exit(0);
        }
        System.out.println("MySQL Connected.");
        try {
            Statement statement = getSqlConnection().createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS `users` (`id` INT NOT NULL auto_increment,`UUID` VARCHAR(36) NOT NULL,`USERNAME` VARCHAR(32) NOT NULL,`PASSWORD` VARCHAR(100) NOT NULL,PRIMARY KEY (`id`),UNIQUE INDEX `USERNAME_UNIQUE` (`USERNAME` ASC) VISIBLE,UNIQUE INDEX `UUID_UNIQUE` (`UUID` ASC) VISIBLE);\n");
            statement.close();
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            Sentry.capture(ex);
        }
    }

    private String getHost() {
        return host;
    }

    private String getPort() {
        return port;
    }

    private String getSchema() {
        return schema;
    }

    private String getUser() {
        return user;
    }

    private String getPassword() {
        return password;
    }

    public Connection getSqlConnection() {
        return sqlConnection;
    }
}
