package de.hs_ruhrwest.ledcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AndroidException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Message;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    //
    public boolean change=true;
    public static final int MESSAGE_READ = 10;
    private Button btnON,btnOFF,btnSearch,btnVisible;
    public static BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice=null;
    private Set<BluetoothDevice>pairedDevices;
    private ListView lvFound,lvPaired;
    private BluetoothSocket btSocket = null;
    private OutputStream outputStream = null;
    private static final String TAG = "bluetooth1";

    //vom jensiboy
    public static UUID MYUUID = UUID.fromString("98e28230-851b-11e4-b4a9-0800200c9a66");
    public static String clientName = "myClient";
    public static String serverName = "myServer";
    public static Handler mHandler;
    public static MainActivity myInstance = null;
    public static Boolean isServer = false;
    private ConnectedThread myConnectedThread = null;
    private AcceptThread myAcceptThread = null;
    private ConnectThread myConnectThread = null;
    private boolean confirmed=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myInstance=this;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        //lvFound=(ListView) findViewById(R.id.lvFound);
        lvPaired=(ListView) findViewById(R.id.lvPaired);
        btnON=(Button) findViewById(R.id.btnON);
        btnOFF=(Button)findViewById(R.id.btnOFF);
        btnVisible=(Button)findViewById(R.id.btnVisible);
        btnSearch=(Button)findViewById(R.id.btnSearch);

        lvPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        for (BluetoothDevice btd : btAdapter.getBondedDevices())
        {
            btDevice=btd;
        }


        mHandler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message inputMessage)
            {
                switch (inputMessage.what)
                {
                    case MESSAGE_READ:
                        updateText((String)inputMessage.obj);
                        break;
                    default:
                        super.handleMessage(inputMessage);
                }
            }
        };

    }


    public void updateText(String input)
    {
        ((TextView)findViewById(R.id.tvOutput)).setText(input);
        if(myConnectedThread!=null && input.endsWith(" ... confirmed!")!=true )
        {
            myConnectedThread.write((input+" ... confirmed!").getBytes());
        }
    }

    public void manageConnectedSocket(BluetoothSocket socket)
    {
        Log.e("manageConnectedSocket", "REACHED!!!");
        myConnectedThread = new ConnectedThread(socket);

        myConnectedThread.start();

        if(isServer)
        {
            Log.e("manageConnectedSocket", "server: listening");
        }
        else
        {
            Log.e("manageConnectedSocket", "client: sending");
            myConnectedThread.write("hello world!".getBytes());
            Log.e("manageConnectedSocket", "client: send");
        }

    }



/*    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }*/

    public void btnON(View view){
        if(!btAdapter.isEnabled()){
            Intent turnOn=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Already on",Toast.LENGTH_LONG).show();
        }
    }

    public void btnSearch(View view){
        //geräte suchen
        if(!btAdapter.startDiscovery()){
            Toast.makeText(getApplicationContext(),"Searching for Devices",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_LONG).show();
        }
        pairedDevices=btAdapter.getBondedDevices();
        ArrayList arlPaired = new ArrayList();
        for(BluetoothDevice btd : pairedDevices)
            arlPaired.add(btd.getName());

        Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_LONG).show();
        final ArrayAdapter araPaired = new ArrayAdapter(this,android.R.layout.simple_list_item_1, arlPaired);
        lvPaired.setAdapter(araPaired);
    }

    public void btnOFF(View view){
        btAdapter.disable();
        Toast.makeText(getApplicationContext(),"Turned off",Toast.LENGTH_LONG).show();
    }

    public void btnVisible(View view){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getIntent(),0);
    }

    public void chooseDevice(View view){

    }

    public void onClickStart(View view)
    {
        if(isServer)
        {
            myAcceptThread = new AcceptThread();
            myAcceptThread.start();
        }
        else
        {
            myConnectThread = new ConnectThread(btDevice);
            myConnectThread.start();
        }
    }

    public void onClickServer(View view)
    {
        isServer=true;
    }

    public void onClickClient(View view)
    {
        isServer=false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
