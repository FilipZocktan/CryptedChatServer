package de.filipzocktan.cryptochat.server;

import de.filipzocktan.cryptochat.server.util.SQLConfig;
import de.filipzocktan.cryptochat.server.util.SocketCollection;
import de.filipzocktan.cryptochat.server.util.User;
import de.filipzocktan.cryptochat.server.web.Web;
import de.filipzocktan.util.chat.Message;
import de.filipzocktan.util.crypto.Crypto;
import io.sentry.Sentry;

import javax.naming.OperationNotSupportedException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

public class CryptoChatServer {

    public static Crypto crypto;
    public static SQLConfig sqlConfig;
    private final int port;
    private List<User> users;
    private List<Handler> handlers;
    private List<String> usernames;
    private boolean running1 = true;

    //ChatSocket
    private ServerSocket chatSocket;

    //UserSocket
    private ServerSocket userSocket;

    //KeySocket
    private ServerSocket keySocket;

    //StatusSocket
    private ServerSocket statusSocket;

    private Timer consoleTimer;

    private User serverAsUser;

    public CryptoChatServer(int port) throws Exception {
        this.port = port;
        crypto = new Crypto();
        chatSocket = new ServerSocket(this.port);
        userSocket = new ServerSocket(this.port + 1);
        keySocket = new ServerSocket(this.port + 2);
        statusSocket = new ServerSocket(this.port + 3);
        serverAsUser = new User(null);
        serverAsUser.setUsername("Server");
        sqlConfig = new SQLConfig();
        users = new LinkedList<>();
        handlers = new LinkedList<>();
        usernames = new LinkedList<>();
        consoleTimer = new Timer();
        consoleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (running1) {
                    try {
                        String input = reader.readLine();
                        switch (input.toUpperCase()) {
                            case "SENTRYTAEST":
                                try {
                                    throw new OperationNotSupportedException("The user tested the Sentry-int");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Sentry.capture(ex);
                                }
                                break;
                            case "STOP":
                                System.out.println("test");
                                for (Handler h : handlers) {
                                    h.user.getSockets().getStatusOut().write("SERVERCLOSED\n");
                                    h.user.getSockets().getStatusOut().flush();
                                    h.stopThread();
                                }
                                userSocket.close();
                                chatSocket.close();
                                statusSocket.close();
                                keySocket.close();
                                consoleTimer.cancel();
                                consoleTimer.purge();
                                System.out.println("Stopping server.");
                                running1 = false;
                                System.exit(0);
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Sentry.capture(e);
                    }
                }
            }
        }, 0);


        while (running1) {
            try {
                User u = new User(new SocketCollection(chatSocket.accept(), userSocket.accept(), keySocket.accept(), statusSocket.accept()));
                users.add(u);
                Handler h = new Handler(u);
                handlers.add(h);
                h.start();
            } catch (IOException e) {
                if (running1) {
                    e.printStackTrace();
                    Sentry.capture(e);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        //start senrty logger instance
        Sentry.init();


        //Start Webserver and ChatServer
        new Web();
        new CryptoChatServer(Integer.parseInt(args[0]));
    }

    private void broadcastMessage(Message message) {
        for (User user : users) {
            user.sendMessage(message);
        }
        System.out.println("<" + message.getUser().getUsername() + "> " + message.getMessage());
    }

    private class Handler extends Thread {

        User user;
        Timer chatTimer;
        Timer userTimer;
        Timer keyTimer;
        Timer statusTimer;
        boolean running = true;

        private Handler(User user) {
            this.user = user;
        }

        public void run() {
            try {
                chatTimer = new Timer();
                chatTimer.schedule(new TimerTask() {


                    @Override
                    public void run() {
                        while (running) {
                            try {
                                String input = user.getSockets().getChatIn().readLine();

                                if (input == null) {
                                    return;
                                }
                                if (input.equals("")) {
                                    return;
                                }
                                if (!user.hasUsername()) {
                                    return;
                                }
                                if (!user.hasPublicKey()) {
                                    return;
                                }
                                String message = new String(crypto.decrypt(Base64.getDecoder().decode(input), user.getPublicKey()));
                                broadcastMessage(new Message(user, message));

                            } catch (SocketException e) {
                                if (running) {
                                    e.printStackTrace();
                                    Sentry.capture(e);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Sentry.capture(e);
                            }
                        }
                    }
                }, 0);
                userTimer = new Timer();
                userTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        while (running) {
                            try {

                                String input = user.getSockets().getUserIn().readLine();

                                if (input == null) {
                                    return;
                                }
                                if (input.equals("")) {
                                    return;
                                }

                                System.out.println("UserSocket-Input: " + input);
                                String[] inputArr = input.split(";;");
                                switch (inputArr[0]) {
                                    case "LOGIN":
                                        if (inputArr.length == 3) {
                                            if (!usernames.contains(inputArr[1])) {
                                                String pw = "";
                                                Statement stmt = sqlConfig.getSqlConnection().createStatement();

                                                if (stmt.execute("SELECT PASSWORD, UUID, COUNT(*) as SIZE FROM cryptochat.users WHERE USERNAME = '" + inputArr[1] + "';")) {
                                                    ResultSet rs = stmt.getResultSet();
                                                    rs.next();
                                                    if (rs.getInt("SIZE") == 1) {
                                                        pw = rs.getString("PASSWORD");
                                                        user.setUuid(UUID.fromString(rs.getString("UUID")));
                                                    } else {
                                                        user.getSockets().getUserOut().print("LOGINANSWER;;WRONGUSER\n");
                                                        user.getSockets().getUserOut().flush();
                                                        stopThread();
                                                        return;
                                                    }
                                                }

                                                if (pw.equals(inputArr[2])) {
                                                    user.setUsername(inputArr[1]);
                                                    usernames.add(user.getUsername());
                                                    System.out.println("User " + user.getUsername() + " logged in.");
                                                    while (!user.hasPublicKey()) {
                                                    }
                                                    broadcastMessage(new Message(serverAsUser, "User " + user.getUsername() + " joined."));
                                                    user.getSockets().getStatusOut().write("HEARTBEAT\n");
                                                    user.getSockets().getStatusOut().flush();
                                                } else {
                                                    user.getSockets().getUserOut().print("LOGINANSWER;;WRONGPASSWORD\n");
                                                    user.getSockets().getUserOut().flush();
                                                    stopThread();
                                                    return;
                                                }
                                            } else {
                                                user.getSockets().getUserOut().print("LOGINANSWER;;USERONLINE\n");
                                                user.getSockets().getUserOut().flush();
                                                stopThread();
                                                return;
                                            }

                                        }
                                        break;
                                }
                            } catch (SocketException e) {
                                if (running) {
                                    e.printStackTrace();
                                    Sentry.capture(e);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Sentry.capture(e);
                            }
                        }
                    }
                }, 0);
                keyTimer = new Timer();
                keyTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        while (running) {
                            try {

                                String input = user.getSockets().getKeyIn().readLine();

                                if (input == null) {
                                    return;
                                }
                                if (input.equals("")) {
                                    return;
                                }
                                user.setPublicKey(Base64.getDecoder().decode(input));
                                user.getSockets().getKeyOut().write(new String(Base64.getEncoder().encode(crypto.getPubKey().getEncoded())) + "\n");
                                user.getSockets().getKeyOut().flush();

                            } catch (SocketException e) {
                                if (running) {
                                    e.printStackTrace();
                                    Sentry.capture(e);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Sentry.capture(e);
                            }
                        }
                    }
                }, 0);
                statusTimer = new Timer();
                statusTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            BufferedReader in = user.getSockets().getStatusIn();
                            PrintWriter out = user.getSockets().getStatusOut();

                            while (running) {
                                String cmd = in.readLine();
                                if (cmd == null) {
                                    return;
                                }
                                System.out.println("StatusSocket-Input: " + cmd);
                                switch (cmd) {
                                    case "DISCONNECT":
                                        broadcastMessage(new Message(serverAsUser, "User " + user.getUsername() + " disconnected."));
                                        usernames.remove(user.getUsername());
                                        stopThread();
                                        user.getSockets().close();
                                        break;
                                    case "HEARTBEAT":
                                        new Timer().schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                out.print("HEARTBEAT\n");
                                                out.flush();
                                            }
                                        }, 5000);
                                }
                            }

                        } catch (SocketException e) {
                            if (running) {
                                e.printStackTrace();
                                Sentry.capture(e);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Sentry.capture(e);
                        }
                    }
                }, 0);
            } catch (Exception e) {
                e.printStackTrace();
                Sentry.capture(e);
            }
        }

        private void stopThread() {
            running = false;
            users.remove(user);
            this.chatTimer.cancel();
            this.chatTimer.purge();
            this.userTimer.cancel();
            this.userTimer.purge();
            this.keyTimer.cancel();
            this.keyTimer.purge();
            this.statusTimer.cancel();
            this.statusTimer.purge();
            this.interrupt();
        }

    }
}
