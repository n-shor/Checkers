package com.example.checkersnadav;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

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

/**
 * Activity that handles user login, including input validation, Firebase authentication, and notification settings configuration.
 */
public class LoginActivity extends AppCompatActivity
{
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewError;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find views by their IDs
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewError = findViewById(R.id.textViewError);
        textViewError.setVisibility(View.GONE);  // Initially hide the error text view.

        // Notification permission check and configuration dialog
        configureNotificationSettings();

        // Schedule daily notifications
        AlarmScheduler.scheduleMidnightAlarm(this, this);
    }

    /**
     * Configures the notification settings by prompting the user with a dialog if notifications are disabled.
     */
    private void configureNotificationSettings()
    {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (!notificationManagerCompat.areNotificationsEnabled() &&
                !getSharedPreferences("app_preferences", MODE_PRIVATE).getBoolean("skip_notification_dialog", false))
        {

            AlertDialog notificationDialog = new AlertDialog.Builder(this)
                    .setTitle("Notifications Permission")
                    .setMessage("Notifications are disabled. Please enable them to receive important updates, such as reminders to claim your daily win bonuses.")
                    .setPositiveButton("Settings", (dialog, which) ->
                    {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid);
                        startActivity(intent);
                    })
                    .setNegativeButton("Don't Ask Again\t\t ", (dialog, which) ->
                    {
                        SharedPreferences.Editor editor = getSharedPreferences("app_preferences", MODE_PRIVATE).edit();
                        editor.putBoolean("skip_notification_dialog", true);
                        editor.apply();
                    })
                    .setNeutralButton("Cancel", null)
                    .create();

            notificationDialog.show();
            styleDialogButtons(notificationDialog);
        }
    }

    /**
     * Styles the dialog buttons and sets them to non-uppercase text.
     * @param dialog The dialog containing the buttons to be styled.
     */
    private void styleDialogButtons(AlertDialog dialog)
    {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.BLACK);

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);

        if (positiveButton != null) positiveButton.setAllCaps(false);
        if (negativeButton != null) negativeButton.setAllCaps(false);
        if (neutralButton != null) neutralButton.setAllCaps(false);
    }

    /**
     * Attempts to log in the user using the entered email and password.
     * @param view The view that triggered this method.
     */
    public void login(View view)
    {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        // Validate input fields
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            textViewError.setText("Email and/or Password fields are empty.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        // Authenticate using Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                    {
                        mDatabase.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if (snapshot.exists())
                                {
                                    textViewError.setVisibility(View.GONE);
                                    Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                    intent.putExtra("userId", snapshot.getChildren().iterator().next().getKey());
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    displayLoginFailedError();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {
                                textViewError.setText(error.toString());
                                textViewError.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    else
                    {
                        displayLoginFailedError(task.getException());
                    }
                });
    }

    /**
     * Displays a generic login failed error message along with the exception detail.
     * @param exception The exception thrown during the login process.
     */
    private void displayLoginFailedError(Exception exception)
    {
        textViewError.setText("Login failed: " + (exception != null ? exception.getMessage() : "Unknown error"));
        textViewError.setVisibility(View.VISIBLE);
    }

    /**
     * Displays a login failed error when no specific exception is provided.
     */
    private void displayLoginFailedError()
    {
        textViewError.setText("Authentication failed. Incorrect email or password.");
        textViewError.setVisibility(View.VISIBLE);
    }

    /**
     * Attempts to register a new user using Firebase authentication.
     * @param view The view that triggered this method.
     */
    public void register(View view)
    {
        String username = editTextUsername.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            textViewError.setText("Username, Email and/or Password fields are empty.");
            textViewError.setVisibility(View.VISIBLE);
            return;
        }

        // Hash password using SHA-256
        String hashedPassword = hashPassword(password);

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, hashedPassword)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                    {
                        // Save user data to Firebase Database
                        saveUserToDatabase(username, email, hashedPassword);
                        retrieveAndNavigateToMenu(email);
                    }
                    else
                    {
                        handleRegistrationFailure(task);
                    }
                });
    }

    /**
     * Hashes the password using SHA-256 algorithm.
     * @param password The plain text password to hash.
     * @return The hashed password as a hexadecimal string.
     */
    private static String hashPassword(String password)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes)
            {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            Log.e("LoginActivity", "Error hashing password: " + e.getMessage());
            return "";
        }
    }

    /**
     * Saves the user's data to the Firebase database.
     * @param username The username of the user.
     * @param email The email of the user.
     * @param hashedPassword The hashed password of the user.
     */
    private static void saveUserToDatabase(String username, String email, String hashedPassword)
    {
        Player user = new Player(username, email, hashedPassword);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("users").push().setValue(user);
    }

    /**
     * Retrieves the user's ID from Firebase and navigates to the Menu activity.
     * @param email The email used to retrieve the user's data.
     */
    private void retrieveAndNavigateToMenu(String email)
    {
        mDatabase.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    String userId = snapshot.getChildren().iterator().next().getKey();
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    textViewError.setText("Authentication failed. Please try again later.");
                    textViewError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                textViewError.setText(error.toString());
                textViewError.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Handles registration failure by displaying appropriate error messages.
     * @param task The completed task containing the failure details.
     */
    private void handleRegistrationFailure(Task<AuthResult> task)
    {
        if (task.getException() instanceof FirebaseAuthUserCollisionException)
        {
            textViewError.setText("This email is already in use.");
        }
        else
        {
            textViewError.setText("Registration failed: " + Objects.requireNonNull(task.getException()));
        }
        textViewError.setVisibility(View.VISIBLE);
    }
}