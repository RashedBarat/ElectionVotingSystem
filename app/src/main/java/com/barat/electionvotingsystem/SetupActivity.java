package com.barat.electionvotingsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "EXTRA_ID";
    public static final String EXTRA_PHONE = "EXTRA_PHONE";

    private CircleImageView mSelect_image;
    private EditText mFullname;
    private EditText mNationId;
    private EditText mAddress;
    private EditText mPassword;
    private RadioGroup mGender;
    private RadioButton radioButton;

    private ProgressBar mSetprogress;
    private Button mSetup_Save;


    private Uri mainImageUrl = null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private String mStudentId;
    private String mPhoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mStudentId = getIntent().getStringExtra(EXTRA_ID);
        mPhoneNum = getIntent().getStringExtra(EXTRA_PHONE);

        firebaseFirestore = FirebaseFirestore.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();

        mSetup_Save = findViewById(R.id.save_changes);
        mFullname = findViewById(R.id.fullname);
        mNationId = findViewById(R.id.nationalid);
        mPassword = findViewById(R.id.password);
        mAddress = findViewById(R.id.address);
        mGender = findViewById(R.id.gender);


        mSetprogress = findViewById(R.id.setup_progress);
        mSelect_image = findViewById(R.id.select_image);


        mSetup_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String fullname = mFullname.getText().toString().trim();
                final String national = mNationId.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                final String address = mAddress.getText().toString().trim();
                int radioId = mGender.getCheckedRadioButtonId();
                radioButton = findViewById(radioId);

                if (!TextUtils.isEmpty(fullname) && !TextUtils.isEmpty(national)
                        && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(address) && mainImageUrl != null) {

                    mSetprogress.setVisibility(View.VISIBLE);

                    StorageReference image_path = storageReference.child("profile_image").child(mStudentId + ".jpg");
                    image_path.putFile(mainImageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                final Uri download_uri = task.getResult().getDownloadUrl();

                                Map<String, String> userMap = new HashMap<>();
                                userMap.put("Fullname", fullname);
                                userMap.put("Gender", radioButton.getText().toString());
                                userMap.put("National", national);
                                userMap.put("StudentId", mStudentId);
                                userMap.put("Password", password);
                                userMap.put("Address", address);
                                userMap.put("Phone", mPhoneNum);
                                userMap.put("Images", download_uri.toString());

                                firebaseFirestore.collection("Users").document(mStudentId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            //Saving student id for later use
                                            SharedPreferences.Editor editor = getSharedPreferences("USER_ID", Context.MODE_PRIVATE).edit();
                                            editor.putString(EXTRA_ID, mStudentId);
                                            editor.apply();

                                            User user = new User();
                                            user.setName(fullname);
                                            user.setAddress(address);
                                            user.setImagePath(download_uri.toString());
                                            user.setGender(radioButton.getText().toString());
                                            user.setNationalId(national);

                                            Toast.makeText(SetupActivity.this, "User Successfully Created", Toast.LENGTH_LONG).show();
                                            Intent main = new Intent(SetupActivity.this, MainActivity.class);
                                            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            main.putExtra(MainActivity.EXTRA_USER_INFO, user);
                                            startActivity(main);
                                            finish();

                                        } else {
                                            mSetprogress.setVisibility(View.INVISIBLE);
                                            String errorimage = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "FireStore Error" + errorimage, Toast.LENGTH_LONG).show();
                                        }

                                    }
                                });

                            } else {
                                mSetprogress.setVisibility(View.INVISIBLE);
                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "Image Upload Error " + error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                } else {
                    Toast.makeText(SetupActivity.this, "Select image and all inputs are required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Select image
        mSelect_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        cropImage();
                    }
                } else {

                    cropImage();
                }
            }
        });

    }

    private void cropImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUrl = result.getUri();
                mSelect_image.setImageURI(mainImageUrl);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

    public void checkButton(View v) {

        int radioId = mGender.getCheckedRadioButtonId();
        radioButton = findViewById(radioId);
    }
}
