package com.example.telefonapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothScreen extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;

    private final String DEVICE_ADDRESS = "00:13:EF:00:1B:41"; // Replace with HC-06 MAC Address
    private final UUID UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private InputStream inputStream;
    private OutputStream outputStream;
    private Handler handler = new Handler();
    private Handler handlers = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bluetooth_screen);
        Button buttonSendOne = findViewById(R.id.buttonSendOne);
        Button buttonSendZero = findViewById(R.id.buttonSendZero);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent = getIntent();
        String time = intent.getStringExtra("time"); // Retrieve the string
        long timeLong = Long.parseLong(time);

        handlers.postDelayed(() -> {
            Intent intents = new Intent(BluetoothScreen.this, MainActivity.class);
            startActivity(intents);
            finish();
        }, timeLong*1000);
        // This code runs after 15 seconds
        handler.postDelayed(() -> {
            Intent intents = new Intent(BluetoothScreen.this, MainActivity.class);
            startActivity(intents);
            finish();
        }, 15000);
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Enable Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }

        connectToDevice();

        // Set button click listeners
        buttonSendOne.setOnClickListener(v -> {
            handler.removeCallbacksAndMessages(null);  // Cancel pending tasks
            sendData("1");
        });
        buttonSendZero.setOnClickListener(v -> sendData("0"));
    }

    private void connectToDevice() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_INSECURE);
            bluetoothSocket.connect();

            inputStream = new BufferedInputStream(bluetoothSocket.getInputStream());
            outputStream = bluetoothSocket.getOutputStream();

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
                                // textView.setText(dataBuilder.toString());
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

    private void sendData(String data) {
        try {
            if (outputStream != null) {
                outputStream.write(data.getBytes());
                Toast.makeText(this, "Sent: " + data, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show();
        }
    }
}