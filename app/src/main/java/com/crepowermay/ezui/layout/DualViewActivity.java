package com.crepowermay.ezui.layout;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class DualViewActivity extends AppCompatActivity {

    private FrameLayout viewContainer1;
    private FrameLayout viewContainer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_view);

        viewContainer1 = findViewById(R.id.view_container_1);
        viewContainer2 = findViewById(R.id.view_container_2);
    }

    public void setView1(View view) {
        viewContainer1.removeAllViews();
        viewContainer1.addView(view);
    }

    public void setView2(View view) {
        viewContainer2.removeAllViews();
        viewContainer2.addView(view);
    }

    public void switchViews() {
        View temp = viewContainer1.getChildAt(0);
        viewContainer1.removeAllViews();
        viewContainer1.addView(viewContainer2.getChildAt(0));
        viewContainer2.removeAllViews();
        viewContainer2.addView(temp);
    }
}
