package com.barat.electionvotingsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

        String studentId = getSharedPreferences("USER_ID", MODE_PRIVATE).getString(SetupActivity.EXTRA_ID, "");

        if (!studentId.isEmpty()) {
            FirebaseFirestore.getInstance().collection("Users").document(studentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {

                        if (task.getResult().exists()) {

                            String name = task.getResult().getString("Fullname");
                            String address = task.getResult().getString("Address");
                            String gender = task.getResult().getString("Gender");
                            String nationalId = task.getResult().getString("National");
                            String profile_image = task.getResult().getString("Images");

                            User user = new User();
                            user.setName(name);
                            user.setAddress(address);
                            user.setImagePath(profile_image);
                            user.setGender(gender);
                            user.setNationalId(nationalId);

                            Intent main = new Intent(HomeActivity.this, MainActivity.class);
                            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            main.putExtra(MainActivity.EXTRA_USER_INFO, user);
                            startActivity(main);
                            finish();
                        }

                    } else {
                        String errormsg = task.getException().getMessage();
                        Toast.makeText(HomeActivity.this, "" + errormsg, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

        mLog_Reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent register = new Intent(HomeActivity.this, RegisterActivity.class);
                startActivity(register);
            }
        });

    }
}
