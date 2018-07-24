/*
 * Copyright (c) Filip Zocktan Studios 2018. This Project is licenced under the GNU AGPL 3.0. You are allowed to modify, distribute and use commercially. You have to include the license and a copyright notice, state the changes, make your source code public and use the GNU AGPL 3.0 license too.
 */

package de.filipzocktan.cryptochat.server.web;


import spark.Route;

import static j2html.TagCreator.*;

public class StandardRoutes {

    public static Route index = (request, response) -> {
        String cookietoken = request.cookie("cryptochatserver.loginsession");
        String username = "";
        if (UserHandler.loggedinUsers.containsKey(cookietoken)) {
            username = UserHandler.loggedinUsers.get(cookietoken).getUsername();
        }
        if (username.equals("")) {
            return document(html(
                    head(
                            title("HomePage")
                    ),
                    body(
                            h1("Landing Page for CryptoChatServer"),
                            p("Information will go here"),
                            p(
                                    text("Login will go "),
                                    a("here").withHref("/login")
                            ),
                            p(
                                    text("Register will go "),
                                    a("here").withHref("/register")
                            )
                    )
            ));
        } else {
            return document(html(
                    head(
                            title("HomePage")
                    ),
                    body(
                            h1("Landing Page for CryptoChatServer"),
                            p("Information will go here"),
                            p(
                                    text("Welcome " + username + "!")
                            ),
                            p(
                                    text("livechat will go "),
                                    a("here").withHref("/livechat")
                            ),
                            p(
                                    text("Logout "),
                                    a("here").withHref("/request/logout")
                            )
                    )
            ));
        }
    };


}
