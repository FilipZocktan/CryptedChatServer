/*
 * Copyright (c) Filip Zocktan Studios 2018. This Project is licenced under the GNU AGPL 3.0. You are allowed to modify, distribute and use commercially. You have to include the license and a copyright notice, state the changes, make your source code public and use the GNU AGPL 3.0 license too.
 */

package de.filipzocktan.cryptochat.server.web;


import spark.Route;

class StandardRoutes {

    static Route index = (request, response) -> {
        String cookietoken = request.cookie("cryptochatserver.loginsession");
        String username = "";
        if (UserHandler.loggedinUsers.containsKey(cookietoken)) {
            username = UserHandler.loggedinUsers.get(cookietoken).getUsername();
        }
        if ("".equals(username)) {
            return WebPageBuilder.getSite("inDexout", request, response);
        } else {
            return WebPageBuilder.getSite("indexin", request, response);
        }
    };

    static Route login = (request, response) -> WebPageBuilder.getSite("LOGIN", request, response);

    static Route register = (request, response) -> WebPageBuilder.getSite("REGISTER", request, response);

    static Route logout = (request, response) -> WebPageBuilder.getSite("logout", request, response);

}
