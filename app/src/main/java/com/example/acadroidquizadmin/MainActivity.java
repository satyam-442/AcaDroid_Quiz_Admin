package com.example.acadroidquizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText Email, Password;
    Button Login;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        if (mAuth.getCurrentUser() != null)
        {
            //Category Intent
            SendAdminToCategoryPage();
            return;
        }

        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        Login = findViewById(R.id.login);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowAdminToLigin();
            }
        });
    }

    private void AllowAdminToLigin() {
        String email = Email.getText().toString();
        String password = Password.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            //Toast.makeText(this, "Email Required", Toast.LENGTH_SHORT).show();
            Email.setError("Email Required");
            return;
        }
        else
        {
            Email.setError(null);
        }
        if (TextUtils.isEmpty(password))
        {
            //Toast.makeText(this, "Password Required", Toast.LENGTH_SHORT).show();
            Password.setError("Password Required");
            return;
        }
        else
        {
            Password.setError(null);
        }
        loadingBar.setMessage("please wait");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "LOGGED IN!", Toast.LENGTH_SHORT).show();
                    SendAdminToCategoryPage();
                    loadingBar.dismiss();
                }
                else
                {
                    String msg = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendAdminToCategoryPage() {
        Intent category = new Intent(this,HomeActivity.class);
        startActivity(category);
        finish();
    }
}