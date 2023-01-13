package com.example.find_location;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class intro_activity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduce_activity);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // 일종의 메시지 객체. 즉, 각각의 액티비티 및 앱 구성요소에 작업요청을 하는 역할
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            // 인트로 실행 후 바로 MainActivity로 넘어감.
            startActivity(intent);
            finish();
        }, 3000); //3초 후 인트로 실행

    }// end of onCreate

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}