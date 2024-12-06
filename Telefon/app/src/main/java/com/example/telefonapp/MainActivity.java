package com.example.telefonapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.telefonapp.R;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.RecursiveTask;

public class MainActivity extends AppCompatActivity {

    private final String DEVICE_ADDRESS = "00:13:EF:00:1B:41"; // Replace with HC-06 MAC Address
    private final UUID UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Enable Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }

        connectToDevice();
    }

    //    private void connectToDevice() {
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
//
//        try {
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_INSECURE);
//            bluetoothSocket.connect();
//
//            InputStream inputStream = bluetoothSocket.getInputStream();
//            OutputStream outputStream = bluetoothSocket.getOutputStream();
//
//            // Read data from Bluetooth
//            new Thread(() -> {
//                byte[] buffer = new byte[1024];
//                int bytes;
//
//                while (true) {
//                    try {
//                        bytes = inputStream.read(buffer);
//                        String received = new String(buffer, 0, bytes);
//                        runOnUiThread(() -> textView.setText(received)); // Display received data
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        break;
//                    }
//                }
//            }).start();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
//        }
//    }
    private void connectToDevice() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_INSECURE);
            bluetoothSocket.connect();

            InputStream inputStream = new BufferedInputStream(bluetoothSocket.getInputStream());
            OutputStream outputStream = bluetoothSocket.getOutputStream();

            // Read data from Bluetooth 
            new Thread(() -> {
                byte[] buffer = new byte[1024]; // Buffer to hold incoming bytes
                int bytes; // Number of bytes read
                StringBuilder dataBuilder = new StringBuilder(); // Accumulate received data

                while (true) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            String received = new String(buffer, 0, bytes, "UTF-8"); // Decode bytes to String
                            dataBuilder.append(received); // Append received data to the StringBuilder

                            runOnUiThread(() -> {
                                // Display received data in the TextView
                                textView.setText(dataBuilder.toString());
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show()
                        );
                        break;
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
        }
    }

}