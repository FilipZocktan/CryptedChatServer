/*
 * Copyright (c) Filip Zocktan Studios 2018. This Project is licenced under the GNU AGPL 3.0. You are allowed to modify, distribute and use commercially. You have to include the license and a copyright notice, state the changes, make your source code public and use the GNU AGPL 3.0 license too.
 */

package de.filipzocktan.cryptochat.server.web;

import spark.Spark;

import static j2html.TagCreator.fileAsString;

public class Web extends Spark {

    public Web() {
        port(getHerokuAssignedPort());
        staticFileLocation("/website/");
        before((request, response) -> {
            if (!UserHandler.loggedinUsers.containsKey(request.cookie("cryptochatserver.loginsession"))) {
                response.removeCookie("/", "cryptochatserver.loginsession");
            }
        });
        get("/", StandardRoutes.index);
        get("/login", ((request, response) -> fileAsString("/website/login.html").render()));
        get("/logout", (request, response) -> fileAsString("/website/logout.html").render());
        get("/register", (request, response) -> fileAsString("/website/register.html").render());

        path("/request", () -> {
            post("/login", UserHandler.login);
            get("/logout", UserHandler.logout);
            post("/register", UserHandler.register);
        });

        notFound(((request, response) -> fileAsString("/website/errors/404.html").render()));
        internalServerError(((request, response) -> fileAsString("/website/errors/500.html").render()));
    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 80; //return default port if heroku-port isn't set (i.e. on localhost)
    }

}
