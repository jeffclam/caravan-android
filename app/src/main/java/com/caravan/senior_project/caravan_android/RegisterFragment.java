package com.caravan.senior_project.caravan_android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    private String TAG = "RegisterFragment";

    private Fragment self = this;
    private View view;
    private AutoCompleteTextView mEmailView;
    private Button register;
    private Button cancel;
    private EditText mPasswordView1;
    private EditText mPasswordView2;

    private FirebaseAuth mAuth;

    private String email;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();

        try {
            email = getArguments().getString("email");
            Log.d(TAG, "Email is: " + email);
        } catch (Exception e) {
            Log.e(TAG, "Could not pull email from LoginActivity");
        }

        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.register_email);
        if (email != null)
            mEmailView.setText(email);
        mPasswordView1 = (EditText) view.findViewById(R.id.register_password);
        mPasswordView2 = (EditText) view.findViewById(R.id.confirm_password);

        register = (Button) view.findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "register button clicked");
                createAccount();
            }
        });

        cancel = (Button) view.findViewById(R.id.register_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "cancel button clicked");
                hideRegFrag();
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    public void createAccount() {
        View focusView = null;
        boolean cancel = false;

        // Reset Errors
        mEmailView.setError(null);
        mPasswordView1.setError(null);
        mPasswordView2.setError(null);

        // Store values in attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView1.getText().toString();
        String password2 = mPasswordView2.getText().toString();

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(password2)) {
            if(!password.equals(password2)) {
                mPasswordView1.setError("Passwords do not match");
                mPasswordView2.setError("Passwords do not match");
                focusView = mPasswordView1;
                cancel = true;
            }
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView1.setError("You need a password");
            cancel = true;
        } else if (TextUtils.isEmpty(password2)) {
            mPasswordView1.setError("You need to confirm your password");
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail: Success");
                                hideRegFrag();
                            } else {
                                Log.w(TAG, "createUserWithEmail: Failure", task.getException());
                                Toast.makeText(getActivity(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void hideRegFrag() {
        FrameLayout frameLayout = (FrameLayout) getActivity().findViewById(R.id.fragment_container);
        frameLayout.setVisibility(View.GONE);
        ScrollView scrollView = (ScrollView) getActivity().findViewById(R.id.login_form);
        scrollView.setVisibility(View.VISIBLE);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
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
