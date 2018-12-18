package com.barat.electionvotingsystem;

import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class AuthActivity extends AppCompatActivity {


    private Button mVotebtns;
    private TextView mVoteauthtxt;
    private TextView mPassword;

    private String currentUser_id;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private TextView mcou;
    long diff;
    long endLong;
    long currentLong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);


        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mVotebtns = (Button) findViewById(R.id.votebtns);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUser_id = mAuth.getCurrentUser().getUid();

        mVoteauthtxt = findViewById(R.id.voteauthtxt);
        mPassword = findViewById(R.id.password);
        mcou = findViewById(R.id.cou);


        firebaseFirestore.collection("ElectionDate").document("B7Aza6M4nVPytBpS05jQ").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String endTime = task.getResult().getString("EndDate");
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault());
                        String currentTime = formatter.format(Calendar.getInstance().getTime());

                        /*String oldTime = "01.09.2018, 12:00";//Timer date 1
                        String NewTime = "05.09.2018, 14:00";//Timer date 2*/
                        Date endDate, currentDate;
                        try {
                            endDate = formatter.parse(endTime);
                            currentDate = formatter.parse(currentTime);
                            endLong = endDate.getTime();
                            currentLong = currentDate.getTime();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (currentLong > endLong) {

                            mcou.setText(getString(R.string.election_ended));
                            showElectionResult();

                        } else {
                            showVotingOptions();
                            diff = endLong - currentLong;
                            MyCount counter = new MyCount(diff, 1000);
                            counter.start();
                        }
                    }

                }
            }
        });


        mVotebtns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                firebaseFirestore.collection("Users").document(currentUser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        String password = mPassword.getText().toString().trim();

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                if (task.getResult().getString("Password").equals(password)) {

                                    String finish = task.getResult().getString("Finish");

                                    if (finish == null) {

                                        Intent main = new Intent(AuthActivity.this, Candidate_List_Activity.class);
                                        startActivity(main);
                                        finish();

                                    } else if (finish.equals("Voted")) {

                                        Toast.makeText(AuthActivity.this, "You already finish your Vote", Toast.LENGTH_SHORT).show();

                                        showElectionResult();

                                    }

                                } else {

                                    Toast.makeText(AuthActivity.this, "Incorrect password, it does not match with our database", Toast.LENGTH_LONG).show();

                                }


                            } else {

                                Toast.makeText(AuthActivity.this, "Data does not exit", Toast.LENGTH_LONG).show();
                            }

                        } else {

                            String error = task.getException().getMessage();
                            Toast.makeText(AuthActivity.this, "Retriving Error" + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }

    // countdowntimer is an abstract class, so extend it and fill in methods
    public class MyCount extends CountDownTimer {
        MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            showElectionResult();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
            String hms = (TimeUnit.MILLISECONDS.toDays(millis)) + "Day "
                    + (TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis)) + ":")
                    + (TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)) + ":"
                    + (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
            mcou.setText(/*context.getString(R.string.ends_in) + " " +*/ hms);
        }
    }

    private void showVotingOptions() {

        mVotebtns.setVisibility(View.VISIBLE);
        mVoteauthtxt.setVisibility(View.VISIBLE);
        mPassword.setVisibility(View.VISIBLE);
    }

    private void showElectionResult() {

        Intent Result = new Intent(AuthActivity.this, ElectionResultActivity.class);
        startActivity(Result);
        finish();
    }
}
