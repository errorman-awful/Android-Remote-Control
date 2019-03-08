package com.winnersonx.androidremotecontrol.clientside;
public interface TCPConnectionListener {
    void onConnectionReady(TCPConnection var1);

    void onMessageReceived(TCPConnection var1, String var2);

    void onDisconnect(TCPConnection var1);

    void onException(TCPConnection var1, Exception var2);
}
