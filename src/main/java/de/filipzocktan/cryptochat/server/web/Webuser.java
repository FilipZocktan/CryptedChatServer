/*
 * Copyright (c) Filip Zocktan Studios 2018. This Project is licenced under the GNU AGPL 3.0. You are allowed to modify, distribute and use commercially. You have to include the license and a copyright notice, state the changes, make your source code public and use the GNU AGPL 3.0 license too.
 */

package de.filipzocktan.cryptochat.server.web;

class Webuser {

    private String username;

    Webuser(String username) {

        this.username = username;
    }

    String getUsername() {
        return username;
    }
}
