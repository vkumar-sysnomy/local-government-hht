package com.farthestgate.android.model.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by Hanson on 30/07/2014.
 */
public class ConfigTable extends Model {

    public static final String COL_CONFIG_USER = "Username";
    public static final String COL_CONFIG_PASS = "Password";
    public static final String COL_CONFIG_HOST1 = "Host1";
    public static final String COL_CONFIG_HOST2 = "Host2";
    public static final String COL_CONFIG_Root = "Root";

    public static final String COL_CONFIG_PUB_KEY = "PublishKey";
    public static final String COL_CONFIG_SUBS_KEY = "SubscribeKey";
    public static final String COL_CONFIG_SEC_KEY = "SecretKey";
    public static final String COL_CONFIG_SSL = "SSL";
    public static final String COL_CONFIG_CHANNEL = "Channel";
    public static final String COL_CONFIG_ERROR_CHANNEL = "ErrorChannel";
    public static final String COL_CONFIG_USE_PUB_NUB = "UsePubNub";

    @Column(name = COL_CONFIG_USER)
    private String username;

    @Column(name = COL_CONFIG_PASS)
    private String password;

    @Column(name = COL_CONFIG_HOST1)
    private String host1;

    @Column(name = COL_CONFIG_HOST2)
    private String host2;

    @Column(name = COL_CONFIG_Root)
    private String root;


    @Column(name = COL_CONFIG_PUB_KEY)
    private String publishKey;

    @Column(name = COL_CONFIG_SUBS_KEY)
    private String subscribeKey;

    @Column(name = COL_CONFIG_SEC_KEY)
    private String secretKey;

    @Column(name = COL_CONFIG_SSL)
    private String ssl;

    @Column(name = COL_CONFIG_CHANNEL)
    private String channel;

    @Column(name = COL_CONFIG_ERROR_CHANNEL)
    private String errorChannel;

    @Column(name = COL_CONFIG_USE_PUB_NUB)
    private String usePubNub;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost1() {
        return host1;
    }

    public void setHost1(String host1) {
        this.host1 = host1;
    }

    public String getHost2() {
        return host2;
    }

    public void setHost2(String host2) {
        this.host2 = host2;
    }

    public String getRoot() {
        return root;
    }
    public void setRoot(String root) {
        this.root = root;
    }


    public String getPublishKey() {
        return publishKey;
    }
    public void setPublishKey(String publishKey) {
        this.publishKey = publishKey;
    }

    public String getSubscribeKey() {
        return subscribeKey;
    }
    public void setSubscribeKey(String subscribeKey) {
        this.subscribeKey = subscribeKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSSL() {
        return ssl;
    }
    public void setSSL(String ssl) {
        this.ssl = ssl;
    }

    public String getChannel() {
        return channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getErrorChannel() {
        return errorChannel;
    }
    public void setErrorChannel(String errorChannel) {
        this.errorChannel = errorChannel;
    }

    public String getUsePubNub() {
        return usePubNub;
    }
    public void setUsePubNub(String usePubNub) {
        this.usePubNub = usePubNub;
    }
}
