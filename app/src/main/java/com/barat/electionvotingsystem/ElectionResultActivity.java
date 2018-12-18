package com.barat.electionvotingsystem;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ElectionResultActivity extends AppCompatActivity {

    private ResultFragment resultFragment;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election_result);

        resultFragment = new ResultFragment();
        replaceFragment(resultFragment);

    }


    private void replaceFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.result_container, fragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onBackPressed() {
//        if (backPressedTime + 2000 > System.currentTimeMillis()) {
//            Intent main = new Intent(ElectionResultActivity.this, AuthActivity.class);
//            startActivity(main);
//            finish();
//        } else {
//            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
//        }
//
//        backPressedTime = System.currentTimeMillis();
        finish();
    }
}
