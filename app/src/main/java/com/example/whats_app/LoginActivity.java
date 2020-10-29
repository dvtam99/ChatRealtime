package com.example.whats_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
//    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rooRef;

    private Button loginButton;
    private EditText userEmail, userPassword;
    private TextView needNewAccount, forgetPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth= FirebaseAuth.getInstance();
        rooRef = FirebaseDatabase.getInstance().getReference();
//        currentUser = mAuth.getCurrentUser();
        InitializeField();

        needNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }


        });
    }
    private void AllowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter email and password",
                    Toast.LENGTH_LONG  ).show();
        }else {
            progressDialog.setTitle("Signing...");
            progressDialog.setMessage("Please wait");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
           mAuth.signInWithEmailAndPassword(email, password).
                   addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if(task.isSuccessful()){
                               SendUserToMainActivity();
                               Toast.makeText(LoginActivity.this,
                                       "Login successfully", Toast.LENGTH_SHORT).show();
                               progressDialog.dismiss();
                           }else {
                               String msg = task.getException().toString();
                               Toast.makeText(LoginActivity.this,
                                       "Error: "+msg  , Toast.LENGTH_SHORT).show();
                               progressDialog.dismiss();
                           }
                       }
                   });

        }
    }
    private void InitializeField() {
        loginButton = (Button) findViewById(R.id.login_button);
        userEmail = (EditText) findViewById(R.id.login_email);
        userPassword= (EditText) findViewById(R.id.login_password);
        needNewAccount = findViewById(R.id.need_account_link);
        forgetPassword = findViewById(R.id.forget_password_link);
        progressDialog = new ProgressDialog(this);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(currentUser!=null){
//            SendUserToMainActivity();
//        }
//    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void SendUserToRegisterActivity() {
        Intent mainIntent = new Intent(this, RegisterActivity.class);
        startActivity(mainIntent);
    }
}