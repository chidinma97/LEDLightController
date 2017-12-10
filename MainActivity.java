package com.example.chidinmashp.ledproject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.burb.rgbled.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Purpose of this app is send data (strings, being more specific) to Arduino
 * through a bluetooth connection. Exciting thing is that, it won't be sent just
 * a single byte but a string, it means several bytes at time.
 *
 * @author Giuseppe Barbato
 *
 */
public class MainActivity extends Activity {

    private final int MAX_VALUE_SEEK = 255; // PWM pass a value between 0 and

    private Button btnPower;
    private boolean pState = false;
    private TextView rVal, gVal, bVal;
    private SeekBar rSeek, gSeek, bSeek;
    private TextView errHandle;
    private int firstTimeOn = 1;

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String btAddress = "98:D3:31:FC:6A:73";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnPower = (Button) findViewById(R.id.power);

        rSeek = (SeekBar) findViewById(R.id.rSeek);
        gSeek = (SeekBar) findViewById(R.id.gSeek);
        bSeek = (SeekBar) findViewById(R.id.bSeek);

        rVal = (TextView) findViewById(R.id.redInten);
        gVal = (TextView) findViewById(R.id.greenInten);
        bVal = (TextView) findViewById(R.id.blueInten);

        errHandle = (TextView) findViewById(R.id.error);


        errHandle.setVisibility(View.GONE);


        // Disable seekbars at start of application
        rSeek.setEnabled(false);
        gSeek.setEnabled(false);
        bSeek.setEnabled(false);

        rSeek.setMax(MAX_VALUE_SEEK);
        gSeek.setMax(MAX_VALUE_SEEK);
        bSeek.setMax(MAX_VALUE_SEEK);

        rSeek.setProgress(rSeek.getMax());
        gSeek.setProgress(gSeek.getMax());
        bSeek.setProgress(bSeek.getMax());

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        //ON OFF Button
        btnPower.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pState == false) {
                    if(firstTimeOn == 1) {
                        turnOnLed("red");
                        turnOnLed("green");
                        turnOnLed("blue");

                        notifier("LED On");

                        rVal.setVisibility(View.VISIBLE);
                        rVal.setText("Int: " + rSeek.getProgress());
                        rSeek.setEnabled(true);

                        gVal.setVisibility(View.VISIBLE);
                        gVal.setText("Int: " + gSeek.getProgress());
                        gSeek.setEnabled(true);

                        bVal.setVisibility(View.VISIBLE);
                        bVal.setText("Int: " + bSeek.getProgress());
                        bSeek.setEnabled(true);

                        btnPower.setText("TURN OFF");
                        pState = true;

                        firstTimeOn = 0;
                    }else{
                        setIntensity("RED", rSeek.getProgress());
                        setIntensity("GREEN", gSeek.getProgress());
                        setIntensity("BLUE", bSeek.getProgress());
                        notifier("LED On");

                        rVal.setVisibility(View.VISIBLE);
                        rVal.setText("Int: " + rSeek.getProgress());
                        rSeek.setEnabled(true);

                        gVal.setVisibility(View.VISIBLE);
                        gVal.setText("Int: " + gSeek.getProgress());
                        gSeek.setEnabled(true);

                        bVal.setVisibility(View.VISIBLE);
                        bVal.setText("Int: " + bSeek.getProgress());
                        bSeek.setEnabled(true);

                        btnPower.setText("TURN OFF");
                        pState = true;
                    }

                } else {
                    turnOffLed("RED");
                    turnOffLed("GREEN");
                    turnOffLed("BLUE");
                    notifier("Light LED Off");
                    rSeek.setEnabled(false);
                    gSeek.setEnabled(false);
                    bSeek.setEnabled(false);
                    btnPower.setText("TURN ON");
                    pState = false;
                }
            }
        });

        // SeekBars
        rSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rVal.setText("Value " + Integer.toString(progress));
                setIntensity("RED", rSeek.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        gSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gVal.setText("Value " + Integer.toString(progress));
                setIntensity("GREEN", gSeek.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        bSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bVal.setText("Value " + Integer.toString(progress));
                setIntensity("BLUE", bSeek.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

        });

    }

    @Override
    public void onResume() {
        super.onResume();

        BluetoothDevice device = btAdapter.getRemoteDevice(btAddress);

        try {
            btSocket = device.createRfcommSocketToServiceRecord(BT_UUID);
        } catch (IOException e) {
            errorExit("Error", "In onResume() happened the following error: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Error", "Uunable to close connection after connection failure: " + e2.getMessage() + ".");
            }
        }
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            errorExit("Error", "Stream creation failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }
        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    //Checks the bluetooth connection between Android and Arduino.
    private void checkBTState() {
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void notifier(String message) {
        Toast msg = Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT);
        msg.show();
    }

    private void turnOnLed(String led) {
        if (btSocket != null) {
            try {
                String value = "ON:";
                btSocket.getOutputStream().write(value.getBytes());
            } catch (IOException e) {
                errHandle.setText("Error turning on");
                errHandle.setVisibility(View.VISIBLE);
            }
        }
    }

    private void turnOffLed(String led) {
        if (btSocket != null) {
            try {
                String value = "OFF:";
                btSocket.getOutputStream().write(value.getBytes());
            } catch (IOException e) {
                errHandle.setText("Error turning off");
                errHandle.setVisibility(View.VISIBLE);
            }
        }
    }

    //sending light intensity info to arduino
    private void setIntensity(String led, int intensity) {
        if (btSocket != null) {
            try {
                String value = "*" + led.toUpperCase() + " " + intensity + ":";
                btSocket.getOutputStream().write(value.getBytes());
            } catch (IOException e) {
                notifier("Error by changing LED intensity");
                errHandle.setText("Error by changing LED intensity");
                errHandle.setVisibility(View.VISIBLE);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast msg = Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }


}
