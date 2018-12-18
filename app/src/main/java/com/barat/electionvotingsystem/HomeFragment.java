package com.barat.electionvotingsystem;


import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
public class HomeFragment extends Fragment {

    private RecyclerView mCandidate_List_View;

    private FirebaseAuth firebaseAuth;
    private List<CandidatePost> candidate_list;

    private FirebaseFirestore firebaseFirestore;
    private CandidateRecyclerAdapter candidateRecyclerAdapter;

    private ConstraintLayout mCandidate_btn;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        candidate_list = new ArrayList<>();
        mCandidate_List_View = view.findViewById(R.id.candidate_list_view);
        mCandidate_btn = view.findViewById(R.id.candidate_btn);

        firebaseAuth = FirebaseAuth.getInstance();

        candidateRecyclerAdapter = new CandidateRecyclerAdapter(candidate_list);

        mCandidate_List_View.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCandidate_List_View.setAdapter(candidateRecyclerAdapter);

        if(firebaseAuth.getCurrentUser() != null) {

        firebaseFirestore = FirebaseFirestore.getInstance();

            mCandidate_List_View.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        CandidatePost candidatePosts = doc.getDocument().toObject(CandidatePost.class).withId(candId);
                       // candidate_list.add(candidatePosts);

                        if (isFirstPageFirstLoad){


                            candidate_list.add(candidatePosts);

                        }else {

                            candidate_list.add(0, candidatePosts);
                        }


                        candidateRecyclerAdapter.notifyDataSetChanged();
                    }
                }
                isFirstPageFirstLoad = false;
            }
        });

    }
        // Inflate the layout for this fragment
        return view;


    }

    public  void loadMorePost() {

        if (firebaseAuth.getCurrentUser() !=null){
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
                                CandidatePost blogPost = doc.getDocument().toObject(CandidatePost.class).withId(blogPostId);
                                candidate_list.add(blogPost);

                                candidateRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
    }

}
