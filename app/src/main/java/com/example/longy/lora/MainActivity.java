package com.example.longy.lora;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.hardware.usb.UsbDevice;
import  android.hardware.usb.UsbDeviceConnection;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerEvent, spinnerObject;
    private EditText etObject, etFreeText, etLocation;
    private ImageButton ibEvent, ibObject;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection usbConnection;
    private UsbSerialDevice serialDevice;
    private static String ACTION_USB_PERMISSION = "permision";
    private Button btnDisconnect;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerEvent = findViewById(R.id.sEventCode);
        spinnerObject = findViewById(R.id.sObjectCode);
        etObject = findViewById(R.id.etObjectCode);
        etFreeText = findViewById(R.id.etFreeText);
        etLocation = findViewById(R.id.etLocation);
        btnDisconnect = findViewById(R.id.btnDisconnect);

        startUsbConnecting();


        List<Integer> eventCodes = new ArrayList<Integer>();
        eventCodes.add(1);
	    eventCodes.add(2);
	    eventCodes.add(3);
	    eventCodes.add(4);
	    ArrayAdapter<Integer> dataAdapter1 = new ArrayAdapter<Integer>(this,
		android.R.layout.simple_spinner_item, eventCodes);
	    dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerEvent.setAdapter(dataAdapter1);



	    List<String> objectCodes = new ArrayList<String>();
        objectCodes.add("T");
	    objectCodes.add("S");
	    objectCodes.add("V");
	    objectCodes.add("F");
	    objectCodes.add("A");
	    objectCodes.add("C");
	    ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,
		android.R.layout.simple_spinner_item, objectCodes);
	    dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerObject.setAdapter(dataAdapter2);

	    usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_USB_PERMISSION);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(broadcastReceiver, intentFilter);




}




    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startUsbConnecting() {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (usbDevices.isEmpty()) {
            usbDevices.forEach((key,value) -> {
                boolean keep = true;
                device = value;
                int vendorId = device.getVendorId();
                if (vendorId == 1000 /* my usb number */  ) {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,new Intent(ACTION_USB_PERMISSION),0);
                    usbManager.requestPermission(device,pendingIntent);
                    keep = false;
                    Log.i("serial","connection is successful");
                } else {
                    usbConnection=null;
                    device=null;
                    Log.i("serial","connection not successful");
                }
                if (!keep) {
                    return;
                }
            });
        } else {
            Log.i("serial","no usb devices connected");
        }
    }

    private void sendData(String data) {
        serialDevice.write(data.getBytes());
        Log.i("serial","sending data: "+data.getBytes());
    }

    public void setBtnDisconnect(View view) {
        disconnect();
    }

    private void disconnect() {
        serialDevice.close();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ACTION_USB_PERMISSION) {
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true);
                if (granted) {
                    usbConnection = usbManager.openDevice(device);
                    serialDevice = UsbSerialDevice.createUsbSerialDevice(device, usbConnection);
                    if (serialDevice!=null) {
                        if (serialDevice.isOpen()) {
                            serialDevice.setBaudRate(115200);
                            serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialDevice.setParity(UsbSerialInterface.PARITY_ODD);
                            serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                        } else {
                            Log.i("Serial", "port is not open");
                        }
                    } else {
                        Log.i("Serial", "port not found");
                    }
                } else {
                    Log.i("Permission", "denied");
                }
            } else if (intent.getAction() == UsbManager.ACTION_USB_ACCESSORY_ATTACHED) {
                startUsbConnecting();
            } else if (intent.getAction() == UsbManager.ACTION_USB_ACCESSORY_DETACHED) {
                disconnect();
            }
        }
    };





    public void showEvent(View v) {

    Toast.makeText(this,
    " 0: Confirmation code, in case of this event code the unit receives this message as a response of confirmation that his previous message was received by the intended unit.  \n" +
"\n" +
"1: Help code, in case of this event the unit in question is requesting assistance.  \n" +
"\n" +
"2: Retreat code, in case of this event the unit in question is declaring that they are retreating from their position. \n" +
"\n" +
"3: Downed unit code, in case of this event the unit in question is declaring that they have lost their position and that their unit was either nearly taken out of action. \n" +
"\n" +
"4: Under control code, in this case the unit in question is declaring that they have the situation under control. This code is mainly used to update the base of their current status and supplies. "

    , Toast.LENGTH_LONG).show();
    }


     public void showObject(View v) {

    Toast.makeText(this,
    "Object Code (2 digits for amount + 1 letter) \n" +
    "\n" +
"* **T: Tank** \n" +
"\n" +
"* **S: Soldier** \n" +
"\n" +
"* **V: Vehicle** \n" +
"\n" +
"* **F: Airforce** \n" +
"\n" +
"* **A: Artillery** \n" +
"\n" +
"* **C: Clear** "

    , Toast.LENGTH_LONG).show();
    }

  public void send(View v) {

        String code="";
        String eventCode = spinnerEvent.getSelectedItem().toString();
	    String objectCode = spinnerObject.getSelectedItem().toString() ;
        String etCode = etObject.getText().toString();
        String freeText = etFreeText.getText().toString();
        String location = etLocation.getText().toString();

	    code+=eventCode;
	    code+=etCode;
	    code+=objectCode;
	    code+=freeText;
	    code+=location;


        TextView rfcString = findViewById(R.id.tvCode);

        rfcString.setText(code);
        sendData(code);


  }


}


