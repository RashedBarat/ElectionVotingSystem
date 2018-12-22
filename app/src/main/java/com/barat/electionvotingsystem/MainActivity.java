package com.barat.electionvotingsystem;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_USER_INFO = "EXTRA_USER_INFO";

    private Button mVoteauthbtn;
    private TextView fullname;
    private TextView address;
    private TextView gender;
    private ImageView mCreateCand;

    private StorageReference storageReference;
    private CircleImageView mprofile_image;
    private Uri mainImageUrl = null;

    private String mStudentId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mVoteauthbtn = (Button) findViewById(R.id.votebtn);

        mCreateCand = (ImageView) findViewById(R.id.create);

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        fullname = findViewById(R.id.ufullname);
        address = findViewById(R.id.uaddress);
        gender = findViewById(R.id.ugender);
        mprofile_image = findViewById(R.id.profile_image);

        User user = getIntent().getParcelableExtra(EXTRA_USER_INFO);
        String fullnames = user.getName();
        String addresss = user.getAddress();
        String genders = user.getGender();
        String national = user.getNationalId();
        String profile_images = user.getImagePath();

        if (national.equals("testadmin")) {

            mCreateCand.setVisibility(View.VISIBLE);
        } else {

            mCreateCand.setVisibility(View.GONE);

        }
        address.setText(addresss);
        fullname.setText(fullnames);
        gender.setText(genders);
        mainImageUrl = Uri.parse(profile_images);

        RequestOptions placeholderimg = new RequestOptions();
        placeholderimg.placeholder(R.drawable.profile);
        Glide.with(MainActivity.this).setDefaultRequestOptions(placeholderimg).load(profile_images).into(mprofile_image);


        mVoteauthbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent register = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(register);

            }
        });

        mCreateCand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent create = new Intent(MainActivity.this, Create_CandidateActivity.class);
                startActivity(create);
            }
        });
    }
}
