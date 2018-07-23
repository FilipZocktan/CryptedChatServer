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
                    loggedinUsers.put(cookietoken,new Webuser(username, cookietoken));
                    response.cookie("cryptochatserver.loginsession",cookietoken,3600);
                } else {
                    response.redirect("/login?wrongpw=1");
                }
            } else {
                response.redirect("/login?wronguser=1");
            }
        }
        response.redirect("/");
        return fileAsString("/website/errors/500.html").render();
    };

    public static Route logout = (request, response) -> {

        String cookietoken = request.cookie("cryptochatserver.loginsession");
        if(loggedinUsers.containsKey(cookietoken)) {
            loggedinUsers.remove(cookietoken);
            response.removeCookie("cryptochatserver.loginsession");
            response.redirect("/logout");
        }
        response.redirect("/");
        return fileAsString("/website/errors/500.html").render();
    };

}
