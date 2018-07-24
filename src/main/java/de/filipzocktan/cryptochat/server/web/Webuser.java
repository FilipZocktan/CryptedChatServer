/*
 * Copyright (c) Filip Zocktan Studios 2018. This Project is licenced under the GNU AGPL 3.0. You are allowed to modify, distribute and use commercially. You have to include the license and a copyright notice, state the changes, make your source code public and use the GNU AGPL 3.0 license too.
 */

package de.filipzocktan.cryptochat.server.web;

public class Webuser {

    private String username;
    private String cookietoken;

    public Webuser(String username, String cookietoken){

        this.username = username;
        this.cookietoken = cookietoken;
    }

    public String getUsername() {
        return username;
    }

    public String getCookietoken() {
        return cookietoken;
    }
}
