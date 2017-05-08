package com.example.hpfromdoha.mmda_ticketing;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zj.com.cn.bluetooth.sdk.BluetoothService;
import com.zj.com.cn.bluetooth.sdk.DeviceListActivity;
import com.zj.com.command.sdk.Command;
import com.zj.com.command.sdk.PrinterCommand;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends Activity{

    private Button scanBtn, printBtn, bScan;
    private TextView date, time, enforcer_id, enforcer_name,license,driver_name,car_plate, penalty;
    public Spinner offense;
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = true;
    String space = "\n"+"\n";
    String enter = "\n";
    String off_number = "", officer = "", formattedDate = "", formattedTime = "",
            scanContent = "", dName = "", licnse = "", plate = "", violation = "", price = "" ;
    String sDate, sTime, sEnforcer_id, sEnforcer_name, sLicense, sDriver_name, sCar_plate, sPrice;
    BackgroundWorker backgroundWorker;

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTION_LOST = 6;
    public static final int MESSAGE_UNABLE_CONNECT = 7;

    /*******************************************************************************************************/
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter bluetoothAdapter = null;
    // Member object for the services
    private BluetoothService mService = null;

    private static final String CHINESE = "GBK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanBtn = (Button) findViewById(R.id.scan_button);
        printBtn = (Button)findViewById(R.id.print_button);
        bScan = (Button)findViewById(R.id.button_scan);

        printBtn.setEnabled(false);
        scanBtn.setEnabled(false);

        date = (TextView)findViewById(R.id.date);
        time = (TextView)findViewById(R.id.time);
        enforcer_id = (TextView)findViewById(R.id.enforcer_id);
        enforcer_name = (TextView)findViewById(R.id.enforcer_name);
        license = (TextView)findViewById(R.id.driver_license);
        driver_name = (TextView)findViewById(R.id.driver_name);
        car_plate = (TextView)findViewById(R.id.car_plate);
        penalty = (TextView)findViewById(R.id.penalty);
        //Spinner
        offense = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.violation_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        offense.setAdapter(adapter);
        offense.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                violation = adapterView.getSelectedItem().toString();
                if (violation.equals("Select Violation")){
                        violation = null;
                        price = null;
                        penalty.setText("Price: 0 php");
                }else if(violation.equals("Delinquent/Invalid Registration")) {
                        price = "450 php";
                        penalty.setText("Price: "+price);
                }else if(violation.equals("Illegal Parking")) {
                        price = "200 php";
                        penalty.setText("Price: "+price);
                }else if(violation.equals("No Crash Helmet")) {
                        price = "150 php";
                        penalty.setText("Price: "+price);
                }else if(violation.equals("Loading/Unloading In Prohibited Zone")) {
                        price = "500 php";
                        penalty.setText("Price: "+price);
                }else if(violation.equals("No Plate Lights")) {
                        price = "150 php";
                        penalty.setText("Price: "+price);
                } else if(violation.equals("Reckless Driving")) {
                        price = "500 php";
                        penalty.setText("Price: "+price);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                    violation = null;
                    price = null;
            }
        });

        sEnforcer_id = off_number = getIntent().getStringExtra("id");
        sEnforcer_name = officer = getIntent().getStringExtra("name");

        off_number = enforcer_id.getText().toString()+off_number;
        officer = enforcer_name.getText().toString()+"\n"+officer;
        enforcer_id.setText(off_number);
        enforcer_name.setText(officer);

        updateTime();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        scanBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.initiateScan();
            }
        });

        bScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

            }
        });
        printBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                updateTime();
                String mmda = " MMDA Ticketing "+space;
                String footer = "   God bless!   ";
                String msg = print();

                    if (msg.length() > 0) {
                        SendDataByte(PrinterCommand.POS_Print_Text(mmda, CHINESE, 0, 1, 1, 0));
                        SendDataByte(PrinterCommand.POS_Print_Text(msg, CHINESE, 0, 0, 0, 0));
                        SendDataByte(PrinterCommand.POS_Print_Text(footer, CHINESE, 0, 1, 1, 0));
                        SendDataByte(PrinterCommand.POS_Print_Text(space, CHINESE, 0, 0, 0, 0));
                        SendDataByte(Command.LF);
                    }

            }
        });


    }

    private String print(){

        String result = "";

        if(!violation.equals(null)) {
            String type = "barcode";
            String send = sDate+";"+sTime+";"+sEnforcer_id+";"+sEnforcer_name+";"+sLicense+";"+sDriver_name+";"+sCar_plate+";"+violation+";"+price;
            backgroundWorker = new BackgroundWorker(this);
            backgroundWorker.execute(type, "", "", send);
            price = "Price: "+price;
            result = formattedDate + enter + formattedTime + enter + off_number + enter + officer + enter
                    + licnse + enter + dName + enter + plate + enter + "Violation: "+"\n"+violation + enter+ price + space;
            violation = "";
            price = "";
        }else {
            Toast.makeText(getApplicationContext(), "Please select violation!", Toast.LENGTH_SHORT).show();
            result = null;
        }

        return result;
    }

    public void updateTime(){

        date.setText("Date: ");
        time.setText("Time: ");

        Calendar c = Calendar.getInstance();

        SimpleDateFormat d = new SimpleDateFormat("MM-dd-yyyy");
        sDate = formattedDate = d.format(c.getTime());
        SimpleDateFormat t = new SimpleDateFormat("HH:mm:ss");
        sTime = formattedTime = t.format(c.getTime());

        formattedDate = date.getText().toString()+formattedDate;
        formattedTime = time.getText().toString()+formattedTime;
        date.setText(formattedDate);
        time.setText(formattedTime);

    }

    public void onStart() {
        super.onStart();

        // If Bluetooth is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the session
        } else {
            if (mService == null)
                KeyListenerInit();//监听
        }
    }

    private void KeyListenerInit() {
        mService = new BluetoothService(this, mHandler);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (DEBUG)
            Log.d(TAG, "onActivityResult " + resultCode);

        switch(requestCode){

            case REQUEST_CONNECT_DEVICE:{
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = intent.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    if (BluetoothAdapter.checkBluetoothAddress(address)) {
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                        // Attempt to connect to the device
                        mService.connect(device);

                    }
                }
                break;
            }
            case REQUEST_ENABLE_BT:{
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a session
                    KeyListenerInit();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
            default:{
                IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (scanningResult != null) {
                    scanContent = scanningResult.getContents();

                    updateTime();

                    licnse = "License No: ";
                    dName = "Driver's Name: ";
                    plate = "Plate Number: ";

                    String arr[] = scanContent.split("\n",3);
                    String li[] = arr[0].split("License_No: ",2);
                    String na[] = arr[1].split("Name: ",2);
                    String cp[] = arr[2].split("Car_Plate: ",2);

                    sLicense = li[1];
                    sDriver_name = na[1];
                    sCar_plate = cp[1];

                    licnse = licnse+li[1];
                    dName = dName+"\n"+na[1];
                    plate = plate+cp[1];

                    license.setText(licnse);
                    driver_name.setText(dName);
                    car_plate.setText(plate);

                }else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "No scan data received!", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }

        }


    }
    public void send(String send){
        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute("barcode","","",send);
    }

    public synchronized void onResume() {
        super.onResume();

        if (mService != null) {

            if (mService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                mService.start();
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        if (mService != null)
            mService.stop();
        if (DEBUG)
            Log.e(TAG, "--- ON DESTROY ---");
    }

    private void SendDataByte(byte[] data) {

        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mService.write(data);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (DEBUG)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(),
                                    "Connected to " + mConnectedDeviceName,
                                    Toast.LENGTH_SHORT).show();
                            printBtn.setEnabled(true);
                            scanBtn.setEnabled(true);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            bScan.setEnabled(false);
                            bScan.setText("Connecting...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:

                    break;
                case MESSAGE_READ:

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    bScan.setText("Connected to " + mConnectedDeviceName);
                    bScan.setEnabled(false);
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                    bScan.setEnabled(true);
                    bScan.setText("Bluetooth Scan");
                    scanBtn.setEnabled(false);
                    printBtn.setEnabled(false);

                    Toast.makeText(getApplicationContext(), "Device connection was lost",
                            Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    Toast.makeText(getApplicationContext(), "Unable to connect device",
                            Toast.LENGTH_SHORT).show();
                    bScan.setText("Bluetooth Scan");
                    bScan.setEnabled(true);
                    break;
            }
        }
    };


}

