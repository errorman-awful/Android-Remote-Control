package com.winnersonx.androidremotecontrol.clientside;

import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

class Sender extends AsyncTask<Void, String, Void> {
    private CommandListener commandListener;
    private int id;
    private String endLineSymbol;
    private int commandDelay;

    public Sender(Socket socket, BufferedWriter oos, TCPConnectionListener tcpConnectionListener, CommandListener commandListener, TCPConnection tcpConnection, String msg) {
        this(socket, oos, tcpConnectionListener, commandListener, tcpConnection, msg, 0);
    }

    public Sender(Socket socket, BufferedWriter oos, TCPConnectionListener tcpConnectionListener, CommandListener commandListener, TCPConnection tcpConnection, String msg, int id) {
        this.oos = oos;
        this.tcpConnectionListener = tcpConnectionListener;
        this.commandListener = commandListener;
        this.tcpConnection = tcpConnection;
        this.msg = msg;
        this.socket = socket;
        this.id = id;
        this.endLineSymbol = "\n";
        commandDelay = 0;
    }

    private Socket socket;
    private BufferedWriter oos;
    private TCPConnectionListener tcpConnectionListener;
    private TCPConnection tcpConnection;
    private String msg;

    public void send() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            if (socket != null && !socket.isClosed() && oos != null) {
                if (msg.equals(""))
                    return null;
                oos.write(msg + "\n");
                oos.flush();
                if (id != 0)
                    publishProgress(msg);
            } else {
                if (socket != null)
                    socket.close();
                tcpConnectionListener.onDisconnect(tcpConnection);
            }
        } catch (IOException e) {
            try {
                socket.close();
                commandListener.onSendException(tcpConnection, e);
                tcpConnectionListener.onDisconnect(tcpConnection);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            commandListener.onSendException(tcpConnection, e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        commandListener.onCommand(msg, id, commandDelay);
    }

    public CommandListener getCommandListener() {
        return commandListener;
    }

    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public BufferedWriter getOos() {
        return oos;
    }

    public void setOos(BufferedWriter oos) {
        this.oos = oos;
    }

    public TCPConnectionListener getTcpConnectionListener() {
        return tcpConnectionListener;
    }

    public void setTcpConnectionListener(TCPConnectionListener tcpConnectionListener) {
        this.tcpConnectionListener = tcpConnectionListener;
    }

    public TCPConnection getTcpConnection() {
        return tcpConnection;
    }

    public void setTcpConnection(TCPConnection tcpConnection) {
        this.tcpConnection = tcpConnection;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getEndLineSymbol() {
        return endLineSymbol;
    }

    public void setEndLineSymbol(String endLineSymbol) {
        this.endLineSymbol = endLineSymbol;
    }

    public int getCommandDelay() {
        return commandDelay;
    }

    public void setCommandDelay(int commandDelay) {
        this.commandDelay = commandDelay;
    }
}
