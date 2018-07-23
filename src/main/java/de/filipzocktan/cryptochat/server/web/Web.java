package de.filipzocktan.cryptochat.server.web;

import j2html.TagCreator;
import spark.Spark;

import static j2html.TagCreator.*;

public class Web extends Spark {

    public Web() {
        port(getHerokuAssignedPort());
        staticFileLocation("/website/");
        get("/", (request, response) ->
                document(html(
                        TagCreator.head(
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
                                ),
                                p(
                                        text("livechat will go "),
                                        a("here").withHref("/livechat")
                                ),p(
                                        text("Logout "),
                                        a("here").withHref("/logout")
                                )
                        )
                )));
        get("/login", ((request, response) -> fileAsString("/website/login.html").render()));
        post("/loginrequest", UserHandler.login);
        get("/logout",UserHandler.logout);
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
