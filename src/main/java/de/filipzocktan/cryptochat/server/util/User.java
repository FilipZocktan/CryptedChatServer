package de.filipzocktan.cryptochat.server.util;

import de.filipzocktan.cryptochat.server.CryptoChatServer;
import de.filipzocktan.util.chat.Message;
import io.sentry.Sentry;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

public class User {

    private final SocketCollection sockets;
    private UUID uuid;
    private String username;
    private PublicKey pubKey;
    private String nickname;
    private boolean hasPubKey = false;

    public User(SocketCollection sockets) {
        this.sockets = sockets;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        setNickname(username);
    }

    public SocketCollection getSockets() {
        return sockets;
    }

    private String getNickname() {
        return nickname;
    }

    private void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean hasUsername() {
        if (username == null) {
            return false;
        } else return !username.equals("");
    }

    private boolean hasNickname() {
        if (nickname == null) {
            return false;
        } else return !nickname.equals("");
    }

    public PublicKey getPublicKey() {
        return pubKey;
    }

    public void setPublicKey(byte[] key) {
        PublicKey pubKey1;
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        try {
            pubKey1 = KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            pubKey1 = null;
        }
        this.pubKey = pubKey1;
        hasPubKey = true;
    }

    public boolean hasPublicKey() {
        return hasPubKey;
    }

    public void sendMessage(Message msg) {
        User user_from = msg.getUser();
        String str_msg = msg.getMessage();
        try {
            String from_name;
            if (user_from.hasNickname()) {
                from_name = user_from.getNickname();
            } else if (user_from.hasUsername()) {
                from_name = user_from.getUsername();
            } else {
                return;
            }
            String message = new String(Base64.getEncoder().encode(CryptoChatServer.crypto.encrypt(new String("<" + from_name + "> " + str_msg))));
            getSockets().getChatOut().print(message + "\n");
            getSockets().getChatOut().flush();
        } catch (Exception e) {
            e.printStackTrace();
            Sentry.capture(e);
        }
    }
}
