package de.filipzocktan.cryptochat.server;

import de.filipzocktan.cryptochat.server.util.SQLConfig;
import de.filipzocktan.cryptochat.server.util.SocketCollection;
import de.filipzocktan.cryptochat.server.util.User;
import de.filipzocktan.cryptochat.server.web.Web;
import de.filipzocktan.util.chat.Message;
import de.filipzocktan.util.crypto.Crypto;

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
    public final int port;
    List<User> users;
    List<Handler> handlers;
    List<String> usernames;
    public static SQLConfig sqlConfig;
    boolean running1 = true;

    //ChatSocket
    ServerSocket chatSocket;

    //UserSocket
    ServerSocket userSocket;

    //KeySocket
    ServerSocket keySocket;

    //StatusSocket
    ServerSocket statusSocket;

    Timer consoleTimer;

    User serverAsUser;

    public CryptoChatServer(int port) throws Exception {
        this.port = port;
        crypto = new Crypto();
        chatSocket = new ServerSocket(port);
        userSocket = new ServerSocket(port + 1);
        keySocket = new ServerSocket(port + 2);
        statusSocket = new ServerSocket(port + 3);
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
                if (running1)
                    e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Web();
        new CryptoChatServer(Integer.parseInt(args[0]));
    }

    public void broadcastMessage(Message message) {
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

        public Handler(User user) {
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
                                if (input == "") {
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
                                if (running)
                                    e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
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
                                if (input == "") {
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

                                                if (stmt.execute("SELECT PASSWORD, COUNT(*) as SIZE FROM cryptochat.users WHERE USERNAME = '" + inputArr[1] + "';")) {
                                                    ResultSet rs = stmt.getResultSet();
                                                    rs.next();
                                                    if (rs.getInt("SIZE") == 1) {
                                                        pw = rs.getString("PASSWORD");
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
                                if (running)
                                    e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
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
                                if (running)
                                    e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
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
                            if (running)
                                e.printStackTrace();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stopThread() {
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
