package de.filipzocktan.cryptochat.server.web;

public class Webuser {

    private String username;
    private String cookietoken;

    public Webuser(String username, String cookietoken){

        this.username = username;
        this.cookietoken = cookietoken;
    }

}
