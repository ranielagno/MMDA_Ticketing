package com.example.hpfromdoha.mmda_ticketing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    BackgroundWorker backgroundWorker;
    Button signIn;
    EditText username, password;
    String user, pass;
    boolean log = false;

    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);

        signIn = (Button)findViewById(R.id.signIn);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        username.setText("EI-00001");
        password.setText("EI-00001");

        signIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                OnLogin(view);

            }

        });
    }

    private void OnLogin(View view){
        user = username.getText().toString();
        pass = password.getText().toString();
        String type = "login";
        if (!(user.isEmpty()) && !(pass.isEmpty())) {
            backgroundWorker = new BackgroundWorker(this);
            backgroundWorker.execute(type , user, pass);
        }
    }


}

