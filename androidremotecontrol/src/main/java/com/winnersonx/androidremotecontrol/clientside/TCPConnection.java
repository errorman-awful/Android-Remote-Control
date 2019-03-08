package com.winnersonx.androidremotecontrol.clientside;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

public class TCPConnection extends AsyncTask<Void, String, Void> {
    private String host;
    private int port;
    private Socket socket;
    private TCPConnectionListener tcpConnectionListener;
    private BufferedReader in;
    private BufferedWriter out;
    private CommandListener commandListener;
    private int reconnectDelay;

    public TCPConnection(String host, int port, TCPConnectionListener tcpConnectionListener, CommandListener commandListener) {
        this.reconnectDelay = 2000;
        this.host = host;
        this.port = port;
        this.tcpConnectionListener = tcpConnectionListener;
        this.commandListener = commandListener;
    }

    public void initiateConnection() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void sendString(final String value) {
        sendString(value, null, 0);
    }

    public void sendString(final String value, View view, int commandDelay) {
        if (socket == null || socket.isClosed())
            return;
        int id = 0;
        int del = 0;
        if (view != null)
            id = view.getId();
        Sender sender;
        sender = new Sender(socket, out, tcpConnectionListener, commandListener, TCPConnection.this, value, id, commandDelay);
            sender.send();
    }

    public void disconnect() {
        cancel(true);
        try {
            if (socket != null)
                this.socket.close();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    tcpConnectionListener.onDisconnect(TCPConnection.this);
                }
            }, reconnectDelay);
        } catch (IOException var2) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    tcpConnectionListener.onDisconnect(TCPConnection.this);
                }
            }, 2000);
            this.tcpConnectionListener.onException(this, var2);
            var2.printStackTrace();
        }

    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
            tcpConnectionListener.onConnectionReady(TCPConnection.this);
            while (!isCancelled()) {
                String str= in.readLine();
                if (str.length()>0)
                    tcpConnectionListener.onMessageReceived(TCPConnection.this, str);

            }
        } catch (Exception e) {
            Log.d("EXCEPTION", e.getLocalizedMessage());
            disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        disconnect();
    }

    //region GETTERS_SETTERS
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public TCPConnectionListener getTcpConnectionListener() {
        return tcpConnectionListener;
    }

    public void setTcpConnectionListener(TCPConnectionListener tcpConnectionListener) {
        this.tcpConnectionListener = tcpConnectionListener;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public BufferedWriter getOut() {
        return out;
    }

    public void setOut(BufferedWriter out) {
        this.out = out;
    }

    public CommandListener getCommandListener() {
        return commandListener;
    }

    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    public int getReconnectDelay() {
        return reconnectDelay;
    }

    public void setReconnectDelay(int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    //endregion
}
