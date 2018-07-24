/*
 * Copyright (c) Filip Zocktan Studios 2018. This Project is licenced under the GNU AGPL 3.0. You are allowed to modify, distribute and use commercially. You have to include the license and a copyright notice, state the changes, make your source code public and use the GNU AGPL 3.0 license too.
 */

package de.filipzocktan.cryptochat.server.web;

import de.filipzocktan.cryptochat.server.CryptoChatServer;
import spark.Route;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import static j2html.TagCreator.fileAsString;

public class UserHandler {

    public static HashMap<String, Webuser> loggedinUsers = new HashMap<>();

    public static Route login = (request, response) -> {
        String username = request.queryParams("username");
        String pw = new String(Base64.getEncoder().encode(request.queryParams("password").getBytes()));
        String pwcheck = "";
        Statement stmt = CryptoChatServer.sqlConfig.getSqlConnection().createStatement();
        if (stmt.execute("SELECT PASSWORD, COUNT(*) as SIZE FROM cryptochat.users WHERE USERNAME = '" + username + "';")) {
            ResultSet rs = stmt.getResultSet();
            rs.next();
            if (rs.getInt("SIZE") == 1) {
                pwcheck = rs.getString("PASSWORD");
                if (pw.equals(pwcheck)) {
                    String cookietoken = UUID.randomUUID().toString();
                    loggedinUsers.put(cookietoken, new Webuser(username, cookietoken));
                    response.cookie("/", "cryptochatserver.loginsession", cookietoken, 7200, false);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            loggedinUsers.remove(cookietoken);
                        }
                    }, 7200000);
                } else {
                    response.redirect("/login?wrongpw=1");
                }
            } else {
                response.redirect("/login?wronguser=1");
            }
        }
        response.redirect("/");
        stmt.close();
        return fileAsString("/website/errors/500.html").render();
    };

    public static Route logout = (request, response) -> {

        String cookietoken = request.cookie("cryptochatserver.loginsession");
        if (loggedinUsers.containsKey(cookietoken)) {
            loggedinUsers.remove(cookietoken);
            response.removeCookie("/", "cryptochatserver.loginsession");
            response.redirect("/loggedout");
        } else {
            response.redirect("/");
        }
        return fileAsString("/website/errors/500.html").render();
    };

    public static Route register = (request, response) -> {

        String username = request.queryParams("username");
        String pw = new String(Base64.getEncoder().encode(request.queryParams("password").getBytes()));
        String pwcheck = new String(Base64.getEncoder().encode(request.queryParams("passwordrepeat").getBytes()));
        Statement stmt = CryptoChatServer.sqlConfig.getSqlConnection().createStatement();
        Statement stmt2 = CryptoChatServer.sqlConfig.getSqlConnection().createStatement();

        if (pw.equals(pwcheck)) {
            if (stmt.execute("SELECT COUNT(*) as SIZE  FROM cryptochat.users WHERE USERNAME = '" + username + "';")) {
                ResultSet rs = stmt.getResultSet();
                rs.next();
                if (rs.getInt("SIZE") == 0) {
                    if (!stmt2.execute("insert into cryptochat.users (UUID,USERNAME,PASSWORD) VALUES ('" + UUID.randomUUID().toString() + "', '" + username + "', '" + pw + "');")) {
                        String cookietoken = UUID.randomUUID().toString();
                        loggedinUsers.put(cookietoken, new Webuser(username, cookietoken));
                        response.cookie("/", "cryptochatserver.loginsession", cookietoken, 7200, false);
                        response.redirect("/");
                    }
                } else {
                    response.redirect("/register?userexists=1");
                }
            }
        } else {
            response.redirect("/register?pwnotequal=1");
        }
        stmt.close();
        stmt2.close();
        return fileAsString("/website/errors/500.html").render();
    };

}
