package de.hs_ruhrwest.ledcontrol;

import java.io.IOException;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    public AcceptThread() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try
        {
            // MY_UUID is the apps UUID string, also used by the client code.
            tmp = MainActivity.myInstance.btAdapter.listenUsingRfcommWithServiceRecord(MainActivity.serverName, MainActivity.MYUUID);
        }
        catch (IOException e)
        {
            Log.e("AcceptThread",e.getMessage());
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // if a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread).
                MainActivity.myInstance.manageConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    Log.e("bluetooth Accept Thread",e.getMessage());
                }
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}