package com.barat.electionvotingsystem;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

/**
 * Created by Heskay Win 10 on 8/15/2018.
 */

public class CandidateId {

    @Exclude
    public  String CandidateId;

    public <T extends CandidateId> T withId(@NonNull final String id){
        this.CandidateId = id;
        return (T) this;
    }
}
