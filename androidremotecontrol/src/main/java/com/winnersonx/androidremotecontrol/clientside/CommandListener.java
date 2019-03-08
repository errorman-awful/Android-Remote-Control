package com.winnersonx.androidremotecontrol.clientside;

public interface CommandListener {
    void onCommand(String command, int id, int delay);

    void onCommand(String command, int delay);

    void onCommand(String command);

    void onSendException(TCPConnection tcpConnection, Exception e);
}
