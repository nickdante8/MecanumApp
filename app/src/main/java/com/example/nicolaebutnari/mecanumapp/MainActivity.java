package com.example.nicolaebutnari.mecanumapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    /**
     * private ToggleButton directionMotor1;
     * private ToggleButton directionMotor2;
     * private ToggleButton directionMotor3;
     * private ToggleButton directionMotor4;
     * int directionMotor1_param = disable;
     * int directionMotor2_param = disable;
     * int directionMotor3_param = disable;
     * int directionMotor4_param = disable;
     */
    int sendDataAppBarAction = 0;

    private ImageButton directionMotor1;
    private ImageButton directionMotor2;
    private ImageButton directionMotor3;
    private ImageButton directionMotor4;
    int directionMotor1_param = stop;
    int directionMotor2_param = stop;
    int directionMotor3_param = stop;
    int directionMotor4_param = stop;

    private NumberPicker pwmMotor1;
    private NumberPicker pwmMotor2;
    private NumberPicker pwmMotor3;
    private NumberPicker pwmMotor4;
    int pwmMotor1_param = pwmMin;
    int pwmMotor2_param = pwmMin;
    int pwmMotor3_param = pwmMin;
    int pwmMotor4_param = pwmMin;

    // PWM is given in percents
    private static final int pwmMin = 0;
    private static final int pwmMax = 100;

    /**
     // Button state
     private static final int disable = 0;
     private static final int enable = 1;
     */

    // Direction state
    private static final int stop = 0;
    private static final int forward = 1;
    private static final int backward = 2;


    TextView textView;
    TextView textReceive;
    private ProgressDialog progress;

    private static final String TAG = "Bluetooth_TrancReceive";

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket mBluetoothSocket = null;
    private boolean isBtConnected = false;
    private Timer myTimer;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String address = null;
    private String old_address = null;
    private String bt_name = " ";

    private ConnectedThread mConnectedThread;
    final int handlerState = 0;
    private StringBuilder recDataString = new StringBuilder();
    Handler mBluetoothIn;

    // Create two array data for transmission and reception
    String transmissionArray;
    String receptionArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) findViewById(R.id.textView);

        /**
         directionMotor1 = (ToggleButton) findViewById(R.id.direction_motor2);
         directionMotor2 = (ToggleButton) findViewById(R.id.direction_motor2);
         directionMotor3 = (ToggleButton) findViewById(R.id.direction_motor3);
         directionMotor4 = (ToggleButton) findViewById(R.id.direction_motor4);
         */

        directionMotor1 = (ImageButton) findViewById(R.id.direction_motor1);
        directionMotor2 = (ImageButton) findViewById(R.id.direction_motor2);
        directionMotor3 = (ImageButton) findViewById(R.id.direction_motor3);
        directionMotor4 = (ImageButton) findViewById(R.id.direction_motor4);

        pwmMotor1 = (NumberPicker) findViewById(R.id.pwm_motor1);
        pwmMotor2 = (NumberPicker) findViewById(R.id.pwm_motor2);
        pwmMotor3 = (NumberPicker) findViewById(R.id.pwm_motor3);
        pwmMotor4 = (NumberPicker) findViewById(R.id.pwm_motor4);

        pwmMotor1.setMinValue(pwmMin);
        pwmMotor1.setMaxValue(pwmMax);
        pwmMotor1.setWrapSelectorWheel(false);
        pwmMotor1.setOnLongPressUpdateInterval(50);

        pwmMotor2.setMinValue(pwmMin);
        pwmMotor2.setMaxValue(pwmMax);
        pwmMotor2.setWrapSelectorWheel(false);
        pwmMotor2.setOnLongPressUpdateInterval(50);

        pwmMotor3.setMinValue(pwmMin);
        pwmMotor3.setMaxValue(pwmMax);
        pwmMotor3.setWrapSelectorWheel(false);
        pwmMotor3.setOnLongPressUpdateInterval(50);

        pwmMotor4.setMinValue(pwmMin);
        pwmMotor4.setMaxValue(pwmMax);
        pwmMotor4.setWrapSelectorWheel(false);
        pwmMotor4.setOnLongPressUpdateInterval(50);

        // Listening to ImageButton Click and LongPress
        directionMotor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directionMotor1.isPressed()) {
                    switch (directionMotor1_param) {
                        case 0:
                            directionMotor1_param = forward;
                            directionMotor1.setImageResource(R.drawable.forward);
                            break;
                        case 1:
                            directionMotor1_param = backward;
                            directionMotor1.setImageResource(R.drawable.backward);
                            break;
                        case 2:
                            directionMotor1_param = stop;
                            directionMotor1.setImageResource(R.drawable.stop);
                            break;
                    }
                }

                if (sendDataAppBarAction == 1)
                    sendData();
            }
        });
        directionMotor1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                directionMotor1_param = stop;
                directionMotor1.setImageResource(R.drawable.stop);

                if (sendDataAppBarAction == 1)
                    sendData();

                return true;
            }
        });
        directionMotor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directionMotor2.isPressed()) {
                    switch (directionMotor2_param) {
                        case 0:
                            directionMotor2_param = forward;
                            directionMotor2.setImageResource(R.drawable.forward);
                            break;
                        case 1:
                            directionMotor2_param = backward;
                            directionMotor2.setImageResource(R.drawable.backward);
                            break;
                        case 2:
                            directionMotor2_param = stop;
                            directionMotor2.setImageResource(R.drawable.stop);
                            break;
                    }
                }

                if (sendDataAppBarAction == 1)
                    sendData();
            }
        });
        directionMotor2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                directionMotor2_param = stop;
                directionMotor2.setImageResource(R.drawable.stop);

                if (sendDataAppBarAction == 1)
                    sendData();

                return true;
            }
        });
        directionMotor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directionMotor3.isPressed()) {
                    switch (directionMotor3_param) {
                        case 0:
                            directionMotor3_param = forward;
                            directionMotor3.setImageResource(R.drawable.forward);
                            break;
                        case 1:
                            directionMotor3_param = backward;
                            directionMotor3.setImageResource(R.drawable.backward);
                            break;
                        case 2:
                            directionMotor3_param = stop;
                            directionMotor3.setImageResource(R.drawable.stop);
                            break;
                    }
                }

                if (sendDataAppBarAction == 1)
                    sendData();
            }
        });
        directionMotor3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                directionMotor3_param = stop;
                directionMotor3.setImageResource(R.drawable.stop);

                if (sendDataAppBarAction == 1)
                    sendData();

                return true;
            }
        });
        directionMotor4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directionMotor4.isPressed()) {
                    switch (directionMotor4_param) {
                        case 0:
                            directionMotor4_param = forward;
                            directionMotor4.setImageResource(R.drawable.forward);
                            break;
                        case 1:
                            directionMotor4_param = backward;
                            directionMotor4.setImageResource(R.drawable.backward);
                            break;
                        case 2:
                            directionMotor4_param = stop;
                            directionMotor4.setImageResource(R.drawable.stop);
                            break;
                    }
                }

                if (sendDataAppBarAction == 1)
                    sendData();
            }
        });
        directionMotor4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                directionMotor4_param = stop;
                directionMotor4.setImageResource(R.drawable.stop);

                if (sendDataAppBarAction == 1)
                    sendData();

                return true;
            }
        });


        // Listening to NumberPicker modification
        pwmMotor1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                picker.setValue((newVal < oldVal)?oldVal-5:oldVal+5);
                pwmMotor1_param = pwmMotor1.getValue();
                if (sendDataAppBarAction == 1)
                    sendData();
            }
        });
        pwmMotor2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                picker.setValue((newVal < oldVal)?oldVal-5:oldVal+5);
                pwmMotor2_param = pwmMotor2.getValue();
                if (sendDataAppBarAction == 1)
                    sendData();
            }
        });
        pwmMotor3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                picker.setValue((newVal < oldVal)?oldVal-5:oldVal+5);
                pwmMotor3_param = pwmMotor3.getValue();
                if (sendDataAppBarAction == 1)
                    sendData();
            }
        });
        pwmMotor4.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                picker.setValue((newVal < oldVal)?oldVal-5:oldVal+5);
                pwmMotor4_param = pwmMotor4.getValue();
                if (sendDataAppBarAction == 1)
                    sendData();
            }
        });

        /* Get Bluetooth Adapter, there's one BTA for the entire system, and this application can interact with it using this object
        if getDefaultAdapter() returns null, then the device does not support Bluetooth and the story ends here
        */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();

        mBluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                //if message is what we want
                if (msg.what == handlerState) {
                    // msg.arg1 = bytes from connect thread
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);

                    transmissionArray += recDataString;
                    textReceive.setText(transmissionArray);
                    if (transmissionArray.length() > 500) {
                        transmissionArray = "";
                    }

                    recDataString.delete(0, recDataString.length());
                }
            }
        };

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isBtConnected){
                    IntentFilter filter = new IntentFilter();

                    filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

                    registerReceiver(mReceiver, filter);
                }
            }
        }, 0, 500);
    }

    private void sendData() {
        if (CouldTransmit()) {
            mConnectedThread.write("#" + String.valueOf(directionMotor1_param) + "*" + String.valueOf(pwmMotor1_param) + "*" + String.valueOf(directionMotor2_param) + "*" + String.valueOf(pwmMotor2_param) + "*" + String.valueOf(directionMotor3_param) + "*" + String.valueOf(pwmMotor3_param) + "*" + String.valueOf(directionMotor4_param) + "*" + String.valueOf(pwmMotor4_param) + "~");
        }
    }

    private void sendDataAllStop() {
        if (CouldTransmit()) {
            mConnectedThread.write("#" + String.valueOf(0) + "*" + String.valueOf(0) + "*" + String.valueOf(0) + "*" + String.valueOf(0) + "*" + String.valueOf(0) + "*" + String.valueOf(0) + "*" + String.valueOf(0) + "*" + String.valueOf(0) + "~");
        }
    }

    //----------------------------------------------------------------------------------------------
    /* This code is used to jump from this activity to another to search for existent paired devices
    for BlueTooth and active WIFI
     */
    //------------------------------------Bluetooh--------------------------------------------------
    // Some kind of ID transfer  between intents MainActivity and SelectBluetooth
    public final static String EXTRA_BT_MESSAGE = "com.example.erwin.bluetooth.MainActivity";
    /* it's a number send with startActivityForResult, used as kind o an id for the intent
    when the called intent will end it will return here because of the PICK_BLUETOOTH_ADDRESS
     */
    public final static int PICK_BLUETOOTH_ADDRESS_INTENT = 1;
    public void StartBTIntent(MenuItem item){
        Intent intent = new Intent(this, SelectBluetooth.class);;
        String message = "Selecting a bluetooth device";

        // address = null;
        intent.putExtra(EXTRA_BT_MESSAGE, message);
        startActivityForResult(intent, PICK_BLUETOOTH_ADDRESS_INTENT);
    }
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String new_address = null;

        // Checks if it returns from the intent which was called from
        switch (requestCode) {
            case PICK_BLUETOOTH_ADDRESS_INTENT: {
                // Checks if the result returned from the setResult() is fine
                if (resultCode == RESULT_OK) {
                    //textView.setText("OK " + data.getStringExtra(SelectBluetooth.ADDRESS));
                    new_address = data.getStringExtra(SelectBluetooth.ADDRESS);
                    bt_name = data.getStringExtra(SelectBluetooth.BTNAME);

                    checkNewAddress(new_address, bt_name);
                } else {
                    if (address == null) {
                        Log.d(TAG, "You didn't choose a bluetooth Device!");
                        textView.setText("You didn't choose a bluetooth Device!");
                    } else {
                        Log.d(TAG, "If you wanted a new connection then choose a different bluetooth Device!");
                        textView.setText("If you wanted a new connection then choose a a different bluetooth Device!");
                    }
                }

                break;
            }
            case TURN_BLUETOOTH_ON_INTENT: {
                Log.d(TAG, "Turning bluetooth on. Result catch from ActivityResult");
                break;
            }
            default: {
                Log.d(TAG, "Different Activity ID Result! Unknown parameter from ActivityResult");
            }
        }
    }
    //----------------------------------------------------------------------------------------------


    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d(TAG, "Device is now connected");
            }
            else {
                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    Disconnect();
                    Log.d(TAG, "Device has disconnected");
                }
            }
        }
    };



    public void checkNewAddress(String new_address, String bt_name) {
        // if the new address is the same as before
        if (new_address.equals(old_address) && isBtConnected) {
            Log.d(TAG, "It's the same Bluetooth! For a new connection choose a different device.");
            textView.setText("It's the same Bluetooth! For a new connection choose a different device.");
        }
        else {
            // check if there is a need to disconnect
            if (new_address.equals("disconnect")) {
                address = null;
                old_address = null;

                textView.setText("Connected to: no BT device");

                Disconnect();
            }
            // if we have a different device address
            else {
                // remember the new address for future checking
                old_address = new_address;
                // remember the address for the new connection
                address = new_address;

                /*
                First cancel the old connection
                this is done in ConnectBt()
                the new connection should be fine
                */

                Disconnect();

                // To be able to connect to the new device call this class
                new ConnectBT().execute();
            }
        }
    }

    private void SetBTName(){
        textView.setText("Connected to: " + bt_name);
    }

    private void UnsetBTName(){
        textView.setText("Connected to: no bluetooth");
    }

    //----------------------------------------------------------------------------------------------
    // Everything what is needed to be done with the Bluetooth
    public final static int TURN_BLUETOOTH_ON_INTENT = 2;
    private void checkBluetoothState(){
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            errorExit("Fatal Error", "Bluetooth not support");
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, TURN_BLUETOOTH_ON_INTENT);
            }
            else {
                Log.d(TAG, "...Bluetooth is already ON...");
            }
        }
    }

    // Create a socket, a channel through which data will be transmitted
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket tmp = null;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }

        return tmp;
    }

    // Used to connect to the device after the address of the device was found
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            //show a progress dialog
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            //while the progress dialog is shown, the connection is done in background
            Log.d(TAG, "Connecting to a device.");


            try {
                if (mBluetoothSocket == null || !isBtConnected) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                    // Create Insecure Rfcom connection
                    mBluetoothSocket = createBluetoothSocket(device);

                    // Disable discovery before establishing a connection
                    mBluetoothAdapter.cancelDiscovery();

                    // Connect socket for data communication
                    Log.d(TAG, "...Connecting...");
                    try {
                        Log.d(TAG,"Try connecting socket.");
                        mBluetoothSocket.connect();
                        Log.d(TAG,"Socket is connected.");

                        // to be able to transmit or receive data only after the socket was created
                        mConnectedThread = new ConnectedThread(mBluetoothSocket);
                        mConnectedThread.start();
                    } catch (IOException e) {
                        // if not successful close it
                        ConnectSuccess = false;

                        try {
                            Log.d(TAG, "Couldn't connect to socket.");
                            mBluetoothSocket.close();
                            mBluetoothSocket = null;
                            address = null;
                            isBtConnected = false;
                        } catch (IOException e2) {
                            msg("Unable to close socket.");
                        }
                    }
                }
            } catch (IOException e1) {
                //if the try failed, you can check the exception here
                ConnectSuccess = false;
                msg("Socket creation failed." + e1.getMessage() + ".");
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed.");
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
                // Set BT name on the interface after a successful connection
                SetBTName();
            }
            progress.dismiss();
        }
    }
    //----------------------------------------------------------------------------------------------



    //----------------------------------------------------------------------------------------------
    // Send and receive data in a different thread. Do not load the main thread too much!
    private class ConnectedThread extends  Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    mBluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();

            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
    //----------------------------------------------------------------------------------------------

    // Disconnect
    public void Disconnect(){
        if (mBluetoothSocket != null)
        {
            try
            {
                address = null;
                mBluetoothSocket.close();
                isBtConnected = false;
                mBluetoothSocket = null;

                // Unset BT name on the interface after a unsuccessful connection
                UnsetBTName();

                Log.d(TAG, "BT is disconnected!");
                // msg("BT is disconnected!");
            }
            catch (IOException e) {
                msg("Couldn't disconnect BT!");
            }
        }
        else
        {
            Log.d(TAG, "BT is not connected!");
            //msg("BT is not connected!");
        }
    }

    boolean CouldTransmit(){
        if (address != null && mBluetoothSocket.isConnected()) {
            // if the socket exists send data, else it could crash the APP
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (CouldTransmit()) {
            sendDataAllStop();
        }

        Log.d(TAG, "...In onPause()...");
    }
    //----------------------------------------------------------------------------------------------
    // Used in case of errors, will end the activityd
    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    // Used for messages
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    //----------------------------------------------------------------------------------------------
    // Menu
    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        MenuItem send_data = menu.findItem(R.id.send_data);

        switch (item.getItemId()){
            case R.id.action_menu_main_disconnect:
                address = null;
                old_address = null;

                textView.setText("Connected to: no BT device");

                Disconnect();
                break;
            case R.id.stop_car:
                sendDataAllStop();
                sendDataAppBarAction = 0;
                send_data.setIcon(R.drawable.play);
                return true;
            case R.id.send_data:
                if (sendDataAppBarAction == 0) {
                    sendDataAppBarAction = 1;
                    item.setIcon(R.drawable.pause);
                    sendData();
                }
                else {
                    sendDataAppBarAction = 0;
                    item.setIcon(R.drawable.play);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
