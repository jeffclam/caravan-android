package com.caravan.senior_project.caravan_android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    private String TAG = "RegisterFragment";

    private Fragment self = this;
    private View view;
    private AutoCompleteTextView mEmailView;
    private Button register;
    private EditText mPasswordView;

    private String email;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_register, container, false);

        try {
            email = getArguments().getString("email");
            Log.d(TAG, "Email is: " + email);
        } catch (Exception e) {
            Log.e(TAG, "Could not pull email from LoginActivity");
        }

        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.register_email);
        if (email != null)
            mEmailView.setText(email);

        register = (Button) view.findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "register button clicked");
                hideRegFrag();
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    public void hideRegFrag() {
        FrameLayout frameLayout = (FrameLayout) getActivity().findViewById(R.id.fragment_container);
        frameLayout.setVisibility(View.GONE);
        ScrollView scrollView = (ScrollView) getActivity().findViewById(R.id.login_form);
        scrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        hideRegFrag();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideRegFrag();
    }

}
