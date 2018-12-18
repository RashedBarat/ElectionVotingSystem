package com.barat.electionvotingsystem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResultFragment extends Fragment {

private RecyclerView candidate_result_list_view;

private List<Candidate_Result> result_list;

private FirebaseFirestore firebaseFirestore;
private FirebaseAuth firebaseAuth;
private Candidate_Result_Adapter candidate_result_adapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public ResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        result_list = new ArrayList<>();
        candidate_result_list_view = view.findViewById(R.id.candidate_result_list_view);

        candidate_result_adapter = new Candidate_Result_Adapter(result_list);
        // Inflate the layout for this fragment

        candidate_result_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        candidate_result_list_view.setAdapter(candidate_result_adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if(firebaseAuth.getCurrentUser() != null) {

            candidate_result_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachdBottoms = !recyclerView.canScrollVertically(1);

                    if(reachdBottoms){
                        String candidatename = lastVisible.getString("candidatename");
                        // Toast.makeText(container.getContext(), "Reached"+candidatename, Toast.LENGTH_LONG).show();
                        loadMorePost();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection("Candidates").orderBy("timestamp", Query.Direction.DESCENDING);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (isFirstPageFirstLoad){

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);

                    }
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String candId = doc.getDocument().getId();
                            Candidate_Result candidate_result = doc.getDocument().toObject(Candidate_Result.class).withId(candId);
                            // candidate_list.add(candidatePosts);

                            if (isFirstPageFirstLoad){


                                result_list.add(candidate_result);

                            }else {

                                result_list.add(0, candidate_result);
                            }


                            candidate_result_adapter.notifyDataSetChanged();
                        }
                    }
                    isFirstPageFirstLoad = false;
                }
            });



    }

        return view;
    }


    public  void loadMorePost() {

        if (firebaseAuth.getCurrentUser() !=null) {
            Query nextQurey = firebaseFirestore.collection("Candidates")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQurey.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                Candidate_Result candidate_result = doc.getDocument().toObject(Candidate_Result.class).withId(blogPostId);
                                result_list.add(candidate_result);
                                candidate_result_adapter.notifyDataSetChanged();

                            }



                        }



                    }
                }
            });

        }
    }


}
