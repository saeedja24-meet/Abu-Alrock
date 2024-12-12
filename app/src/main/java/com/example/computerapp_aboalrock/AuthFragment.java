package com.example.computerapp_aboalrock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class AuthFragment extends Fragment {
    private TextView textview;
    private LinearLayout authFragment;
    private TextInputEditText currentEmail, currentPassword;
    private Button logIn, signUp, signOutButton, forgotPasswordButton;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth, container, false);
        textview=view.findViewById(R.id.textView2);
        mAuth = FirebaseAuth.getInstance();
        authFragment = view.findViewById(R.id.auth_fragment);
        currentEmail = view.findViewById(R.id.userEmail);
        currentPassword = view.findViewById(R.id.userPassword);
        logIn = view.findViewById(R.id.loginButton);
        signUp = view.findViewById(R.id.signUpButton);
        forgotPasswordButton = view.findViewById(R.id.passwordForgot);
        signOutButton = view.findViewById(R.id.signoutButton);

        setUpListeners();

        return view;
    }

    private void setUpListeners() {
        signUp.setOnClickListener(v -> {
            String email = currentEmail.getText().toString().trim();
            String password = currentPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Email and password must not be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Registration successful!", Toast.LENGTH_SHORT).show();
                            deattachfragment();
                        } else {
                            handleError(task.getException());
                        }
                    });
        });

        logIn.setOnClickListener(v -> {
            String email = currentEmail.getText().toString().trim();
            String password = currentPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getActivity(), "Email and password must not be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Login successful!", Toast.LENGTH_SHORT).show();
                            deattachfragment();
                        } else {
                            handleError(task.getException());
                        }
                    });
        });

        forgotPasswordButton.setOnClickListener(v -> {
            String email = currentEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(getActivity(), "Please enter your email.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
    private void deattachfragment(){
        currentEmail.setText("");
        currentPassword.setText("");
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .detach(AuthFragment.this)
                .commit();
    }

    private void handleError(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(getActivity(), "This email is already registered. Try logging in.", Toast.LENGTH_SHORT).show();
        } else if (e instanceof FirebaseAuthWeakPasswordException) {
            Toast.makeText(getActivity(), "Weak password. Use at least 6 characters.", Toast.LENGTH_SHORT).show();
        } else if (e instanceof FirebaseAuthInvalidUserException) {
            Toast.makeText(getActivity(), "No account found with this email. Please register.", Toast.LENGTH_SHORT).show();
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            String errorCode = ((FirebaseAuthInvalidCredentialsException) e).getErrorCode();
            if (errorCode.equals("ERROR_INVALID_EMAIL")) {
                Toast.makeText(getActivity(), "Invalid email format. Please check your email address.", Toast.LENGTH_SHORT).show();
            } else if (errorCode.equals("ERROR_WRONG_PASSWORD")) {
                Toast.makeText(getActivity(), "Incorrect password. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
