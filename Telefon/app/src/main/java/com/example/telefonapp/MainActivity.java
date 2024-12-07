package com.example.telefonapp;

import static com.stripe.android.core.injection.NamedConstantsKt.PUBLISHABLE_KEY;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.telefonapp.R;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.firebase.FirebaseApp;
import com.parse.Parse;
import com.parse.ParseObject;
import com.google.android.gms.wallet.button.PayButton;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.googlepaylauncher.GooglePayEnvironment;
import com.stripe.android.googlepaylauncher.GooglePayLauncher;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.*;


import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.stripe.android.PaymentConfiguration;
// Add the following lines to build.gradle to use this example's networking library:
//   implementation 'com.github.kittinunf.fuel:fuel:2.3.1'
import com.google.firebase.firestore.FirebaseFirestore;


import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private static final String TAG = "CheckoutActivity";

    private String paymentClientSecret;


    private final String DEVICE_ADDRESS = "00:13:EF:00:1B:41"; // Replace with HC-06 MAC Address
    private final UUID UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private TextView textView;
    private Button buttonSendOne, buttonSendZero;
    Button stripeButton;
    EditText amountEditText;
    PaymentSheet paymentSheet;
    String paymentIntentClientSecret,amount;
    PaymentSheet.CustomerConfiguration customerConfig;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if(mAuth.getCurrentUser()==null) {


            setContentView(R.layout.activity_main);
            EditText emailField = findViewById(R.id.emailField);
            EditText passwordField = findViewById(R.id.passwordField);
            Button signUpButton = findViewById(R.id.signUpButton);
            Button signInButton = findViewById(R.id.signInButton);

            signUpButton.setOnClickListener(v -> {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                signUp(email, password);
            });

            signInButton.setOnClickListener(v -> {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                signIn(email, password);
            });

        }
        else{
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
            finish(); // Close the LoginActivity
        }
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        if (bluetoothAdapter == null) {
//            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//
//        if (!bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, "Enable Bluetooth", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//
//        connectToDevice();
//
//        // Set button click listeners
//        buttonSendOne.setOnClickListener(v -> sendData("1"));
//        buttonSendZero.setOnClickListener(v -> sendData("0"));
    } void getDetails(){

        Fuel.INSTANCE.post("https://us-central1-polihack-2ede7.cloudfunctions.net/helloWorld?amt=" +amount+"&email="+mAuth.getCurrentUser().getEmail().toString(),null)
                .responseString(new Handler<String>() {
                    @Override
                    public void success(String s) {
                        try{
                            JSONObject result = new JSONObject(s);
                            customerConfig = new PaymentSheet.CustomerConfiguration(
                                    result.getString("customer"),
                                    result.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret = result.getString("paymentIntent");
                            PaymentConfiguration.init(getApplicationContext(), result.getString("publishableKey"));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showStripePaymentSheet();
                                }
                            });
                        }catch (JSONException e){
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(@NonNull FuelError fuelError) {

                    }
                });

    }
    void showStripePaymentSheet(){
        final PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("CoDer")
                .customer(customerConfig)
                .allowsDelayedPaymentMethods(true)
                .build();
        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                configuration
        );

    }
    void onPaymentSheetResult(
            final PaymentSheetResult paymentSheetResult
    ) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment canceled!", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this,((PaymentSheetResult.Failed) paymentSheetResult).getError().toString(), Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment complete!", Toast.LENGTH_SHORT).show();
        }
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, SecondActivity.class));
                        finish(); // Close the LoginActivity
                    } else {
                        Toast.makeText(this, "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show();
                        saveUserToFirestore(user.getUid(), "User Name", email);
                        startActivity(new Intent(MainActivity.this, SecondActivity.class));
                        finish(); // Close the LoginActivity
                    } else {
                        Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveUserToFirestore(String userId, String userName, String userEmail) {
        Map<String, Object> user = new HashMap<>();
        user.put("userName", userName);
        user.put("userEmail", userEmail);
        user.put("Tokens", 0);
        user.put("Dizabilitati", 0);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "User data saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show());
    }
    public void signOut() {
        mAuth.signOut();
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
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





