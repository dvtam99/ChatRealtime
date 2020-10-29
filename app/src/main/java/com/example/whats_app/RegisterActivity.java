package com.example.whats_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.PrivateKey;

public class RegisterActivity extends AppCompatActivity {
    private Button registerButton;
    private EditText userEmail, userPassword;
    private TextView alreadyHaveAccount;
    private DatabaseReference rooRef;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rooRef = FirebaseDatabase.getInstance().getReference();

        InitializeField();

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLogin();
            }
        });

    }
    public void onRegister(View view){
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter email and password",
                  Toast.LENGTH_LONG  ).show();
        }else {
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                String currentUserId = mAuth.getCurrentUser().getUid();
                                rooRef.child("Users").child(currentUserId).setValue("");
                                SendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this,
                                        "Account is created", Toast.LENGTH_SHORT).show();
                            }else {
                                String msg = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,
                                        "Error: "+msg  , Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }



    private void InitializeField() {
        registerButton = (Button) findViewById(R.id.register_button);
        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword= (EditText) findViewById(R.id.register_password);
        alreadyHaveAccount = findViewById(R.id.already_have_account_link);
        progressBar =new ProgressBar(RegisterActivity.this, null, android.R.attr.progressBarStyleSmall);
    }
    private void SendUserToLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }
       private void SendUserToMainActivity() {

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}