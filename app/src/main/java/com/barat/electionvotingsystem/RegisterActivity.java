package com.barat.electionvotingsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private Button mReg_btn;
    private EditText mregphonetxt;
    private EditText mStudentId;
    private FirebaseAuth mAuth;
    private ProgressBar mRegprocess;
    private TextView mnote;
    private int btnType = 0;
    private String verifiedPhoneNum;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mReg_btn = findViewById(R.id.regsubmitebtn);
        mregphonetxt = findViewById(R.id.regphonetxt);
        mStudentId = findViewById(R.id.student_id);

        mRegprocess = findViewById(R.id.regprocessbar);
        mnote = findViewById(R.id.textView2);

        mReg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phonenum = mregphonetxt.getText().toString().trim();

                if (btnType == 0) {
                    String studentid = mStudentId.getText().toString().trim();

                    if (!phonenum.isEmpty() && !studentid.isEmpty()) {
                        mRegprocess.setVisibility(View.VISIBLE);
                        mReg_btn.setEnabled(false);
                        mregphonetxt.setEnabled(false);
                        mStudentId.setEnabled(false);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenum, 60, TimeUnit.SECONDS,
                                RegisterActivity.this, mCallbacks);

                    } else {
                        Toast.makeText(RegisterActivity.this, "Please enter all info!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    if (!phonenum.isEmpty()) {
                        mReg_btn.setEnabled(false);

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, phonenum);
                        signInWithPhoneAuthCredential(credential);

                    } else {
                        Toast.makeText(RegisterActivity.this, "Please enter OTP!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                //merrortext.setText("Invalid Verification code enter");
                mRegprocess.setVisibility(View.INVISIBLE);
                mregphonetxt.setEnabled(true);
                mStudentId.setEnabled(true);
                mReg_btn.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Invalid number", Toast.LENGTH_LONG).show();
            }


            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                btnType = 1;

//                mRegprocess.setVisibility(View.INVISIBLE);
                mReg_btn.setText("Verify");
                mReg_btn.setEnabled(true);
                mStudentId.setVisibility(View.INVISIBLE);
                mnote.setVisibility(View.INVISIBLE);

                verifiedPhoneNum = mregphonetxt.getText().toString();
                mregphonetxt.setText("");
                mregphonetxt.setHint(getString(R.string.otpverify));

            }
        };
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseFirestore.getInstance().collection("Users").document(mStudentId.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        if (task.getResult().exists()) {

                                            if (task.getResult().getString("Phone").equals(verifiedPhoneNum)) {
                                                //Saving student id for later use
                                                SharedPreferences.Editor editor = getSharedPreferences("USER_ID", Context.MODE_PRIVATE).edit();
                                                editor.putString(SetupActivity.EXTRA_ID, mStudentId.getText().toString());
                                                editor.apply();

                                                String name = task.getResult().getString("Fullname");
                                                String address = task.getResult().getString("Address");
                                                String gender = task.getResult().getString("Gender");
                                                String nationalId = task.getResult().getString("National");
                                                String profile_image = task.getResult().getString("Images");

                                                User user = new User();
                                                user.setStudentId(mStudentId.getText().toString());
                                                user.setName(name);
                                                user.setAddress(address);
                                                user.setImagePath(profile_image);
                                                user.setGender(gender);
                                                user.setNationalId(nationalId);

                                                Intent main = new Intent(RegisterActivity.this, MainActivity.class);
                                                main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                main.putExtra(MainActivity.EXTRA_USER_INFO, user);
                                                startActivity(main);

                                            } else {
                                                Toast.makeText(RegisterActivity.this, "This student ID is already registered!", Toast.LENGTH_LONG).show();
                                                finish();
                                            }

                                        } else {

                                            FirebaseFirestore.getInstance().collection("StudentIDs").document(mStudentId.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                    if (task.isSuccessful()) {
                                                        if (task.getResult().exists()) {

                                                            Intent setupactivity = new Intent(RegisterActivity.this, SetupActivity.class);
                                                            setupactivity.putExtra(SetupActivity.EXTRA_ID, mStudentId.getText().toString());
                                                            setupactivity.putExtra(SetupActivity.EXTRA_PHONE, verifiedPhoneNum);
                                                            startActivity(setupactivity);

                                                        } else {

                                                            Toast.makeText(RegisterActivity.this, "Your student ID is not enlisted in our database, you cannot proceed further!", Toast.LENGTH_LONG).show();
                                                            finish();
                                                        }

                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "Something went wrong try again!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        }

                                        finish();

                                    } else {
                                        String errormsg = task.getException().getMessage();
                                        Toast.makeText(RegisterActivity.this, "" + errormsg, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });


                        } else {
                            // Sign in failed, display a message and update the UI
                            mRegprocess.setVisibility(View.INVISIBLE);
                            mregphonetxt.setEnabled(true);
                            mReg_btn.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}
