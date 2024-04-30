package com.example.checkersnadav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewError;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find Views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewError = findViewById(R.id.textViewError);
        textViewError.setVisibility(View.GONE);

    }

    public void login(View view) {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            textViewError.setText("Email and/or Password fields are empty.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        String hashedPassword = hashPassword(password);

        // Firebase Authentication with username check
        mAuth.signInWithEmailAndPassword(email, hashedPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        mDatabase.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    Player player = snapshot.getValue(Player.class);
                                    if(player != null) {
                                        textViewError.setVisibility(View.GONE);
                                        Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginActivity.this, Menu.class);
                                        intent.putExtra("userEmail", email);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        textViewError.setText("Authentication failed. Incorrect email or password.");
                                        textViewError.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                textViewError.setText(error.toString());
                                textViewError.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    else {
                        textViewError.setText("Authentication failed. Incorrect username or password.");
                        textViewError.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void register(View view) {
        String username = editTextUsername.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            textViewError.setText("Username, Email and/or Password fields are empty.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        // Hash password
        String hashedPassword = hashPassword(password);

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, hashedPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        textViewError.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();

                        // Save user (including username) to database
                        saveUserToDatabase(username, email, hashedPassword);

                        Intent intent = new Intent(LoginActivity.this, Menu.class);
                        intent.putExtra("userEmail", email); // Pass the user's email
                        startActivity(intent);
                        finish();

                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            textViewError.setText("This email is already in use.");
                        } else {
                            textViewError.setText("Registration failed: " + Objects.requireNonNull(task.getException()));
                        }
                        textViewError.setVisibility(View.VISIBLE);
                    }
                });
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("LoginActivity", "Error hashing password: " + e.getMessage());
            return "";
        }
    }

    private static void saveUserToDatabase(String username, String email, String hashedPassword) {
        Player user = new Player(username, email, hashedPassword); // Include username
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("users").push().setValue(user);
    }

}