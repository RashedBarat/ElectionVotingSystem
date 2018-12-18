package com.barat.electionvotingsystem;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {


    private Button mLog_Reg_btn;

    //private EditText mPassword;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();


        mLog_Reg_btn = findViewById(R.id.logregbtn);


        mLog_Reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent register = new Intent(HomeActivity.this, RegisterActivity.class);
                startActivity(register);
            }
        });


    }




   @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){

            sendtomain();

            //blogLikeBtn.setColorFilter(R.color.colorAccent);

        }
    }

    private void sendtomain() {
        Intent main = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
