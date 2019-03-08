package com.winnersonx.androidremotecontrol.clientside;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Sender extends AsyncTask<Void, String, Void> {
    private CommandListener commandListener;
    private int id;
public Sender(Socket socket, BufferedWriter oos, TCPConnectionListener tcpConnectionListener, CommandListener commandListener, TCPConnection tcpConnection, String msg){
    this(socket,oos,tcpConnectionListener,commandListener,tcpConnection,msg,0);
}
 public   Sender(Socket socket, BufferedWriter oos, TCPConnectionListener tcpConnectionListener, CommandListener commandListener, TCPConnection tcpConnection, String msg, int id) {
        this.oos = oos;
        this.tcpConnectionListener = tcpConnectionListener;
        this.commandListener = commandListener;
        this.tcpConnection = tcpConnection;
        this.msg = msg;
        this.socket = socket;
        this.id = id;
    }

    private Socket socket;
    private BufferedWriter oos;
    private TCPConnectionListener tcpConnectionListener;
    private TCPConnection tcpConnection;
    private String msg;

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            if (socket != null && !socket.isClosed() && oos != null) {
                Log.d("LISTENER", "SENDING");
                oos.write(msg+"\n");
                oos.flush();
                if(id!=0)
                publishProgress(msg);
            } else {
                if (socket != null)
                    socket.close();
                    tcpConnectionListener.onDisconnect(tcpConnection);
            }
        } catch (IOException e) {
            try {
                Log.d("EXCEPTION",e.getLocalizedMessage());
                socket.close();
                tcpConnectionListener.onDisconnect(tcpConnection);
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.d("EXCEPTION",e1.getLocalizedMessage());
            }
            tcpConnectionListener.onException(tcpConnection, e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        commandListener.onCommand(msg, id);
    }
}
