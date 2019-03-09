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
    private String str;

    public TCPConnection(String host, int port, TCPConnectionListener tcpConnectionListener, CommandListener commandListener) {
        this.reconnectDelay = 2000;
        this.host = host;
        this.port = port;
        this.tcpConnectionListener = tcpConnectionListener;
        this.commandListener = commandListener;
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Sends string to the server
     *
     * @param value         The string that will be send to the server
     * @param endLineSymbol The symbol that indicated end of line
     */
    public void sendString(final String value, String endLineSymbol) {
        sendString(value, null, 0, endLineSymbol);
    }

    /**
     * Sends string to the server
     *
     * @param value The string that will be send to the server
     */
    public void sendString(final String value) {
        sendString(value, null, 0, "\n");
    }

    /**
     * Sends string to the server
     *
     * @param value         The string that will be send to the server
     * @param view          The view that caused the dispatch
     * @param commandDelay  "Optional parameter, that can be used in onCommand callback"
     * @param endLineSymbol The symbol that indicated end of line,
     */
    public void sendString(final String value, View view, int commandDelay, String endLineSymbol) {
        if (socket == null || socket.isClosed())
            return;
        int id = 0;
        if (view != null)
            id = view.getId();
        Sender sender;
        sender = new Sender(socket, out, tcpConnectionListener, commandListener, TCPConnection.this, value, id, commandDelay);
        sender.setEndLineSymbol(endLineSymbol);
        sender.send();
    }

    /**
     * Disconnect client from server with a specified reconnectDelay parameter by command(setReconnectDelay)
     */
    public void disconnect() {
        disconnect(reconnectDelay);
    }

    /**
     * Disconnect client from server with a  reconnectDelay delay in ms
     *
     * @param reconnectDelay delay in ms after that the "onDisconnect" callback will be dispatched
     */
    public void disconnect(int reconnectDelay) {
        cancel(true);
        try {
            if (socket != null) {
                this.socket.close();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        tcpConnectionListener.onDisconnect(TCPConnection.this);
                    }
                }, reconnectDelay);
            }
        } catch (IOException var2) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    tcpConnectionListener.onDisconnect(TCPConnection.this);
                }
            }, reconnectDelay);
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
                str = in.readLine();
                if (str.length() > 0)
                    publishProgress();

            }
        } catch (Exception e) {
            Log.d("EXCEPTION", e.getLocalizedMessage());
            disconnect();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        tcpConnectionListener.onMessageReceived(TCPConnection.this, str);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        disconnect();
    }

    /**
     * @return host name of current tcp connection
     */
    //region GETTERS_SETTERS
    public String getHost() {
        return host;
    }

    /**
     * @return port of current tcp connection
     */
    public int getPort() {
        return port;
    }

    /**
     * @return socket of current tcp connection
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @return tcpConnectionListener
     */
    public TCPConnectionListener getTcpConnectionListener() {
        return tcpConnectionListener;
    }

    /**
     * Sets the tcpConnectionListener interface
     */
    public void setTcpConnectionListener(TCPConnectionListener tcpConnectionListener) {
        this.tcpConnectionListener = tcpConnectionListener;
    }

    /**
     * @return bufferedReader of tcp connection
     */
    public BufferedReader getIn() {
        return in;
    }

    /**
     * @return bufferedWriter of tcp connection
     */
    public BufferedWriter getOut() {
        return out;
    }

    /**
     * @return commandListener interface
     */
    public CommandListener getCommandListener() {
        return commandListener;
    }

    /**
     * Sets the commandListener interface
     */
    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    /**
     * @return reconnect delay in ms
     */
    public int getReconnectDelay() {
        return reconnectDelay;
    }

    /**
     * Sets the delay of "onDisconnect" callback with a real disconnect,
     * 0 means "onDisconnect" calls immediately after real disconnect
     *
     * @param reconnectDelay Delay in ms
     */
    public void setReconnectDelay(int reconnectDelay) {
        this.reconnectDelay = reconnectDelay;
    }

    //endregion
}
