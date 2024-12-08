package com.example.telefonapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

public class Payscreen extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView dataTextView;  // Declare the TextView
    private FirebaseAuth mAuth;
    PaymentSheet paymentSheet;
    String paymentIntentClientSecret, amount;
    PaymentSheet.CustomerConfiguration customerConfig;
    double pricePerHourDouble;
    String amountText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        String priceperhour = intent.getStringExtra("pph"); // Retrieve the string
        setContentView(R.layout.activity_payscreen);
        // Initialize the TextView from XML
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payscreen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText amountEditText = findViewById(R.id.amountEditText1);
        Button stripeButton = findViewById(R.id.stripeButton1);
        stripeButton.setOnClickListener(v -> {
            int value = Integer.parseInt(amountEditText.getText().toString());
            if (value < 40) {
                Toast.makeText(this, "Amount cannot be under 40", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(amountEditText.getText().toString())) {
                Toast.makeText(this, "Amount cannot be empty ", Toast.LENGTH_SHORT).show();
            } else {
                amountText = amountEditText.getText().toString();  // Get text from EditText
                pricePerHourDouble = Double.parseDouble(priceperhour);
                double amounts = Double.parseDouble(amountText);
                double result = (pricePerHourDouble / 60) * amounts;
                String resultString = String.valueOf(result);
                amount = resultString;
                getDetails();
            }
        });
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
    }

    void getDetails() {

        Fuel.INSTANCE.post("https://us-central1-polihack-2ede7.cloudfunctions.net/helloWorld?amt=" + amount + "&email=" + mAuth.getCurrentUser().getEmail().toString(), null)
                .responseString(new Handler<String>() {
                    @Override
                    public void success(String s) {
                        try {
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
                        } catch (JSONException e) {
                            Toast.makeText(Payscreen.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(@NonNull FuelError fuelError) {

                    }
                });

    }

    void showStripePaymentSheet() {
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
            Toast.makeText(this, ((PaymentSheetResult.Failed) paymentSheetResult).getError().toString(), Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment completed!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Payscreen.this, BluetoothScreen.class);
            intent.putExtra("time", amountText); // Add
            startActivity(intent);
            finish();
        }
    }
}