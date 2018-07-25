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
//            System.out.println(request.pathInfo());
        });
        get("/", StandardRoutes.index);
        get("/login", StandardRoutes.login);
        get("/logout", (request, response) -> {
            response.redirect("/request/logout");
            return fileAsString("/website/errors/500.html");
        });
        get("/register", StandardRoutes.register);
        get("/loggedout", StandardRoutes.logout);

        path("/request", () -> {
            post("/login", UserHandler.login);
            get("/logout", UserHandler.logout);
            post("/register", UserHandler.register);
        });

        after((request, response) -> response.header("Content-Encoding", "gzip"));

        notFound(((request, response) -> fileAsString("/website/errors/404.html").render()));
        internalServerError(((request, response) -> fileAsString("/website/errors/500.html").render()));
    }

    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 80; //return default port if heroku-port isn't set (i.e. on localhost)
    }

}
