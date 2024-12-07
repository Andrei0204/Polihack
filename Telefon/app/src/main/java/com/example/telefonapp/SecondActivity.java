package com.example.telefonapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SecondActivity extends AppCompatActivity {
    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_second);
        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            signOut();
        });

        if(mAuth.getCurrentUser()==null) {
            startActivity(new Intent(SecondActivity.this, MainActivity.class));
            finish(); // Close the LoginActivity
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void signOut() {
        mAuth.signOut();
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(SecondActivity.this, MainActivity.class));
        finish(); // Close the LoginActivity
    }

}