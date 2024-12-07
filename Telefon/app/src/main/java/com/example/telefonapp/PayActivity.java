package com.example.telefonapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PayActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView dataTextView;  // Declare the TextView
    private FirebaseAuth mAuth;
    PaymentSheet paymentSheet;
    String paymentIntentClientSecret,amount;
    PaymentSheet.CustomerConfiguration customerConfig;
    List<String> myList = new ArrayList<>();
    int rezervedValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialize Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_pay);
        // Initialize the TextView from XML
        dataTextView = findViewById(R.id.dataTextView);
        // The userId variable, which you want to use to find a specific user
        Intent intent = getIntent();
        String userId = intent.getStringExtra("Marker"); // Retrieve the string

        // Query for the specific user document based on userId
        db.collection("park")  // Reference to "park" collection
                .whereEqualTo("Parknumber", userId)  // Query where the "Parknumber" field matches the variable
                .get()  // Perform the query
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Check if the query found any results
                            if (!task.getResult().isEmpty()) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);  // Get the first document

                                // Retrieve all fields of the document
                                Map<String, Object> documentData = document.getData();

                                if (documentData != null) {
                                    // Initialize a variable to check Rezerved
                                        rezervedValue = 0;

                                    // Check if the Rezerved field exists and equals 1
                                    if (documentData.containsKey("Rezerved")) {
                                        Object rezerved = documentData.get("Rezerved");
                                        Object pph = documentData.get("Priceperhour");

                                        if (rezerved != null && rezerved.toString().equals("0") &&pph !=null) {
                                            rezervedValue = 1;
                                                Intent intent = new Intent(PayActivity.this, Payscreen.class);
                                                intent.putExtra("rezerved", "0"); // Add data
                                                intent.putExtra("pph", pph.toString()); // Add data
                                                intent.putExtra("userId", userId); // Add data
                                                startActivity(intent);
                                                finish();


                                        }
                                    }

                                    // Optional: Build a string with all field data
                                    StringBuilder displayData = new StringBuilder();
                                    for (Map.Entry<String, Object> entry : documentData.entrySet()) {
                                        String field = entry.getKey();  // Field name
                                        Object value = entry.getValue();  // Field value
                                        displayData.append(field).append(": ").append(value.toString()).append("\n");
                                    }

                                    // Add Rezerved status to displayData
                                    displayData.append("Rezerved Value: ").append(rezervedValue).append("\n");
                                    if(rezervedValue==0) {
                                        startActivity(new Intent(PayActivity.this, SecondActivity.class));
                                    }

                                    // Set the constructed string to the TextView
                                    dataTextView.setText(displayData.toString());
                                } else {
                                    // If no data is found for the document
                                    dataTextView.setText("No data found for user ID: " + userId);
                                }
                            } else {
                                // If no document is found for the given userId
                                dataTextView.setText("No user found with ID: " + userId);
                            }
                        } else {
                            // If there was an error with the query
                            Log.e("Firestore", "Error getting documents.", task.getException());
                            dataTextView.setText("Error retrieving data.");
                        }
                    }
                });


    }
    void getDetails(){

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
                            Toast.makeText(PayActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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

}