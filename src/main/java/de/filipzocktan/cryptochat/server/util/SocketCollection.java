package de.filipzocktan.cryptochat.server.util;

import io.sentry.Sentry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketCollection {

    private final Socket chatSocket;
    private final Socket userSocket;
    private final Socket keySocket;
    private final Socket statusSocket;
    private final BufferedReader chatIn;
    private final PrintWriter chatOut;
    private final BufferedReader userIn;
    private final PrintWriter userOut;
    private final BufferedReader keyIn;
    private final PrintWriter keyOut;
    private final BufferedReader statusIn;
    private final PrintWriter statusOut;

    public SocketCollection(Socket chatSocket, Socket userSocket, Socket keySocket, Socket statusSocket) {
        this.chatSocket = chatSocket;
        this.userSocket = userSocket;
        this.keySocket = keySocket;
        this.statusSocket = statusSocket;
        BufferedReader chatIn1;
        PrintWriter chatOut1;
        try {
            chatIn1 = new BufferedReader(new InputStreamReader(getChatSocket().getInputStream()));
            chatOut1 = new PrintWriter(getChatSocket().getOutputStream());
        } catch (Exception e) {
            chatIn1 = null;
            chatOut1 = null;
        }
        chatIn = chatIn1;
        chatOut = chatOut1;
        BufferedReader userIn1;
        PrintWriter userOut1;
        try {
            userIn1 = new BufferedReader(new InputStreamReader(getUserSocket().getInputStream()));
            userOut1 = new PrintWriter(getUserSocket().getOutputStream());
        } catch (Exception e) {
            userIn1 = null;
            userOut1 = null;
        }
        userIn = userIn1;
        userOut = userOut1;
        BufferedReader keyIn1;
        PrintWriter keyOut1;
        try {
            keyIn1 = new BufferedReader(new InputStreamReader(getKeySocket().getInputStream()));
            keyOut1 = new PrintWriter(getKeySocket().getOutputStream());
        } catch (Exception e) {
            keyIn1 = null;
            keyOut1 = null;
        }
        keyIn = keyIn1;
        keyOut = keyOut1;
        BufferedReader statusIn1;
        PrintWriter statusOut1;
        try {
            statusIn1 = new BufferedReader(new InputStreamReader(getStatusSocket().getInputStream()));
            statusOut1 = new PrintWriter(getStatusSocket().getOutputStream());
        } catch (Exception e) {
            statusIn1 = null;
            statusOut1 = null;
        }
        statusIn = statusIn1;
        statusOut = statusOut1;
    }

    private Socket getChatSocket() {
        return chatSocket;
    }

    private Socket getUserSocket() {
        return userSocket;
    }

    private Socket getKeySocket() {
        return keySocket;
    }

    private Socket getStatusSocket() {
        return statusSocket;
    }

    public BufferedReader getChatIn() {
        return chatIn;
    }

    PrintWriter getChatOut() {
        return chatOut;
    }

    public BufferedReader getUserIn() {
        return userIn;
    }

    public PrintWriter getUserOut() {
        return userOut;
    }

    public BufferedReader getKeyIn() {
        return keyIn;
    }

    public PrintWriter getKeyOut() {
        return keyOut;
    }

    public BufferedReader getStatusIn() {
        return statusIn;
    }

    public PrintWriter getStatusOut() {
        return statusOut;
    }

    public void close() {
        try {
            getChatIn().close();
            getUserIn().close();
            getKeyIn().close();
            getStatusIn().close();
            getChatOut().close();
            getUserOut().close();
            getKeyOut().close();
            getStatusOut().close();
            getChatSocket().close();
            getUserSocket().close();
            getKeySocket().close();
            getStatusSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
            Sentry.capture(e);
        }
    }

}
