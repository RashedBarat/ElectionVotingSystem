package com.barat.electionvotingsystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class Create_CandidateActivity extends AppCompatActivity {

    private Spinner mCandPost;
    String [] CandPost = {"President", "Vice President", "General Secretary"};

    private EditText mPartyname;
    private EditText mCandName;
    private EditText mCandSpeech;
    private Button mSubmit;
    private ProgressDialog mProgress;
    private ImageView mCandImage;

    private ProgressBar mCreate_ProgressBar;

    private Uri CandImageUri = null;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create__candidate);


        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,CandPost);
        mCandPost = findViewById(R.id.candpost);
        mCandPost.setAdapter(arrayAdapter);


        mSubmit = (Button) findViewById(R.id.candsubmite);

        mPartyname = (EditText) findViewById(R.id.partyname);
        mCandName = (EditText) findViewById(R.id.candname_result);
        mCandSpeech = (EditText) findViewById(R.id.candspeech);

        mCreate_ProgressBar = (ProgressBar) findViewById(R.id.create_pro);
        mCandImage = (ImageView) findViewById(R.id.candimg);



        mCandImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(Create_CandidateActivity.this);
            }
        });


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String candname = mCandName.getText().toString();
                final String partyname = mPartyname.getText().toString();
                final String candspeech = mCandSpeech.getText().toString();
                final String candpost = mCandPost.getSelectedItem().toString();

                if(!TextUtils.isEmpty(candname) && !TextUtils.isEmpty(partyname) && !TextUtils.isEmpty(candspeech)&& !TextUtils.isEmpty(candpost) && CandImageUri != null) {

                    mProgress = new ProgressDialog(Create_CandidateActivity.this);
                    mProgress.setTitle("Create Candidate");
                    mProgress.setMessage("Please wait while creating Candidate");
                    mProgress.show();
                    mProgress.setCancelable(false);


                    final String randomName = UUID.randomUUID().toString();
                    StorageReference filePath = storageReference.child("Candidate_Images").child(randomName +".jpg");
                    filePath.putFile(CandImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                            final String downloadUri = task.getResult().getDownloadUrl().toString();

                            if (task.isSuccessful()){


                                File newImageFile = new File(CandImageUri.getPath());
                                try {

                                    compressedImageFile = new Compressor(Create_CandidateActivity.this)
                                            .setMaxHeight(20)
                                            .setMaxWidth(20)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();


                                UploadTask uploadTask = storageReference.child("Candidate_Images/thumbs")
                                        .child(randomName+".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                        String downloadthumbUri = taskSnapshot.getDownloadUrl().toString();

                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_uri", downloadUri);
                                        postMap.put("image_thumb", downloadthumbUri);
                                        postMap.put("partyname", partyname);
                                        postMap.put("candidatename", candname);
                                        postMap.put("candidatespeech", candspeech);
                                        postMap.put("candidatepost", candpost);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                        firebaseFirestore.collection("Candidates").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {


                                                if (task.isSuccessful()){

                                                    Toast.makeText(Create_CandidateActivity.this, "Candidate Successfully ", Toast.LENGTH_LONG).show();
                                                    Intent main = new Intent(Create_CandidateActivity.this, MainActivity.class);
                                                    startActivity(main);
                                                    //finish();

                                                }else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(Create_CandidateActivity.this, ""+error, Toast.LENGTH_SHORT).show();

                                                }
                                                mProgress.dismiss();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //Error Handling


                                    }
                                });


                            }else {

                                mProgress.dismiss();

                            }
                        }
                    });

                }else {
                    mProgress.dismiss();


                }

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode ==RESULT_OK){

                CandImageUri = result.getUri();
                mCandImage.setImageURI(CandImageUri);


            }else if (requestCode ==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Exception error = result.getError();
            }

        }
    }
}
