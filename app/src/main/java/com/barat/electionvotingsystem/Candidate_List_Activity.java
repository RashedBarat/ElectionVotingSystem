package com.barat.electionvotingsystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Candidate_List_Activity extends AppCompatActivity {
    private HomeFragment homeFragment;
    private Button mVotefinish;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUser_id;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate__list_);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        homeFragment = new HomeFragment();
        replaceFragment(homeFragment);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUser_id = mAuth.getCurrentUser().getUid();


        mVotefinish = findViewById(R.id.votefinish);

        firebaseFirestore.collection("Users").document(currentUser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String finish = task.getResult().getString("Finish");

                        if (finish == null) {

                            voteFinish();


                        } else {

                            mVotefinish.setVisibility(View.INVISIBLE);
                        }

                    }

                }

            }
        });

    }


    public void voteFinish() {
        mVotefinish.setVisibility(View.VISIBLE);

        mVotefinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Users").document(currentUser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                String finish = "Voted";

                                Map<String, Object> voteMap = new HashMap<>();
                                voteMap.put("Finish", finish);

                                firebaseFirestore.collection("Users").document(currentUser_id).update(voteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        mVotefinish.setEnabled(false);

                                        mProgress = new ProgressDialog(Candidate_List_Activity.this);
                                        mProgress.setTitle("Vote Processing");
                                        mProgress.setMessage("Please wait while system is processing election result");
                                        mProgress.show();
                                        mProgress.setCancelable(false);

                                        Intent HomeInt = new Intent(Candidate_List_Activity.this, ElectionResultActivity.class);
                                        startActivity(HomeInt);
                                        finish();
                                    }
                                });

                            }

                        }

                    }
                });


            }
        });
    }

    private void replaceFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_layout, fragment);
        fragmentTransaction.commit();

    }
}
