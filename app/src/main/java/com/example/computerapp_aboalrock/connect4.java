package com.example.computerapp_aboalrock;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class connect4 extends AppCompatActivity {
    private GridLayout whiteBackgroundGridLayout;
    private Button showsquare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect4);
        whiteBackgroundGridLayout=findViewById(R.id.whiteBackgroundGridLayout);
        GridLayout.LayoutParams params=new GridLayout.LayoutParams();
        View blackSquare= new View(this);
        blackSquare.setBackgroundColor(Color.BLACK);
        showsquare=findViewById(R.id.showsquare);
        Toast.makeText(this, "you clicked me", Toast.LENGTH_SHORT).show();

        showsquare.setOnClickListener(v->{
            Toast.makeText(this, "you clicked me", Toast.LENGTH_SHORT).show();
            params.rowSpec=GridLayout.spec(0,1);
            params.columnSpec=GridLayout.spec(0,1);
            whiteBackgroundGridLayout.addView(blackSquare);
        });

    }
}
