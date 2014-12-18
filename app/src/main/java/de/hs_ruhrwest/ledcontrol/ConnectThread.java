package de.hs_ruhrwest.ledcontrol;

import java.io.IOException;

        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;
        import android.util.Log;


public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    @SuppressWarnings("unused")
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice.
        try {
            // MY_UUID is the apps UUID string, also used by the server code.
            tmp = device.createRfcommSocketToServiceRecord(MainActivity.MYUUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection.
        MainActivity.btAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            Log.e("ConnectThread -> run", connectException.getMessage());
            // Unable to connect; close the socket and get out.
            try {
                mmSocket.close();
            } catch (IOException closeException) { Log.e("ConnectThread -> run", closeException.getMessage());}
            return;
        }

        // Do work to manage the connection (in a separate thread).
        MainActivity.myInstance.manageConnectedSocket(mmSocket);
    }

    /** Will cancel an in-progress connection, and close the socket. */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

