package com.example.computerapp_aboalrock;
import com.example.computerapp_aboalrock.checkers.Checkers;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.computerapp_aboalrock.checkers.Checkers;

public class MainActivity extends AppCompatActivity {
    private Button attach_fragment, detach_fragment,goConnect4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        attach_fragment = findViewById(R.id.attach_fragment);
        detach_fragment = findViewById(R.id.detach_fragment);
        FragmentManager fragmentManager = getSupportFragmentManager();
        attach_fragment.setOnClickListener(v -> attachFragment(new AuthFragment(), fragmentManager));
        goConnect4=findViewById(R.id.goconnect4);
        goConnect4.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, Checkers.class);
            startActivity(intent);        });
        detach_fragment.setOnClickListener(v -> detachFragment(fragmentManager));
    }

    private void attachFragment(Fragment fragment, FragmentManager fragmentManager) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment existingFragment = fragmentManager.findFragmentByTag("AUTH_FRAGMENT");
        if (existingFragment == null) {
            transaction.add(R.id.auth_fragment, fragment, "AUTH_FRAGMENT");
        } else {
            transaction.attach(existingFragment);
        }
        transaction.commit();
    }

    private void detachFragment(FragmentManager fragmentManager) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment authFragment = fragmentManager.findFragmentByTag("AUTH_FRAGMENT");
        if (authFragment != null) {
            transaction.detach(authFragment);
        }

        transaction.commit();

    }
}
