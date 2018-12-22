package com.barat.electionvotingsystem;

import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {


    private Button mReg_btn;
    private Button mLog_btn;
    private EditText mregphonetxt;
    private EditText mCodeText;
    private FirebaseAuth mAuth;
    private ProgressBar mRegprocess;
    private TextView merrortext;
    private TextView mnote;
    private RelativeLayout mRegpanel;
    private RelativeLayout mVerifypanel;
    private int btnType = 0;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLog_btn = findViewById(R.id.reglogbtn);
        mReg_btn = findViewById(R.id.regsubmitebtn);
        mregphonetxt = findViewById(R.id.regphonetxt);
        mRegprocess = findViewById(R.id.regprocessbar);
        mnote = findViewById(R.id.textView2);
        mRegpanel = findViewById(R.id.regpanel);
        mVerifypanel = findViewById(R.id.verypanel);
        mCodeText = findViewById(R.id.mCodetxt);


        mLog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent register = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(register);
            }
        });


        mReg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btnType == 0) {
                    String phonenum = mregphonetxt.getText().toString().trim();

                    if (!phonenum.isEmpty()) {
                        mRegprocess.setVisibility(View.VISIBLE);

                        mregphonetxt.setEnabled(false);
                        mReg_btn.setEnabled(false);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(

                                phonenum,

                                60,
                                TimeUnit.SECONDS,
                                RegisterActivity.this,
                                mCallbacks

                        );

                    } else {
                        Toast.makeText(RegisterActivity.this, "Please enter a valid number!", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    String verification = mCodeText.getText().toString().trim();

                    if (!verification.isEmpty()) {
                        mReg_btn.setEnabled(false);
                        mVerifypanel.setVisibility(View.VISIBLE);

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verification);
                        signInWithPhoneAuthCredential(credential);

                    } else {
                        Toast.makeText(RegisterActivity.this, "Please enter a valid OTP!", Toast.LENGTH_SHORT).show();
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

                mRegprocess.setVisibility(View.INVISIBLE);
                mReg_btn.setText("Verify");
                mReg_btn.setEnabled(true);
                mRegpanel.setVisibility(View.INVISIBLE);
                mnote.setVisibility(View.INVISIBLE);
                mVerifypanel.setVisibility(View.VISIBLE);

                /*Intent otpactivity = new Intent(RegisterActivity.this, OTPVerifyActivity.class);
                startActivity(otpactivity);
                finish();*/

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

                            FirebaseUser user = task.getResult().getUser();

                            Intent setupactivity = new Intent(RegisterActivity.this, SetupActivity.class);
                            startActivity(setupactivity);
                            finish();
                            // ...
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
