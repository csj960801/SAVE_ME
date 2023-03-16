package com.example.find_location;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button location_btn; // 위치파악 버튼
    private Button urgent_btn; // 긴급(119) 연락 버튼
    private Button camera_btn; // 긴급(119) 연락 버튼
    private TextView txtResult; // 추출한 gps 데이터 출력

    private Switch background_switch; // 배경화면 변경 스위치

    private Uri net_uri = null; // Intent로 전화 혹은 문자를 보낼 주소를 전달하고
    private Intent send_intent = null; // Intent는 Uri에서 지정한 데이터 전달 방식(tel, sms 등)에 따라 받은 주소로 전달함.

    /*----------------------------------------------------------------------------------------------------------------------------------*/

    /**
     * 전화 dialog 담당 함수
     */
    protected void send_call() {

        net_uri = Uri.parse("tel:119");
        Intent intent = new Intent(Intent.ACTION_DIAL, net_uri);
        startActivity(intent);

    } // end of send_call()

    /**
     * gps 데이터 파악 함수
     */
    protected void find_user_info() {
        // 마시멜로 이상 버전일 경우 권한 요청
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        } else {

            // 위치 관리자 실행
            final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // 가장최근 위치정보 가져오기
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                String provider = location.getProvider(); // 위치파악 장치 정보
                double longitude = location.getLongitude(); // 위도
                double latitude = location.getLatitude(); // 경도
                double altitude = location.getAltitude(); // 고도

                txtResult = (TextView) findViewById(R.id.txtResult);
                txtResult.setTextSize(17);
                txtResult.setTextColor(Color.WHITE);
                txtResult.setText("위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도  : " + altitude + "\n" + "현재 전화가 불가능한 상황입니다..");

                // 문자 전송
                SmsSend(txtResult.getText().toString(), txtResult);
            }

            // 위치정보를 원하는 시간, 거리마다 갱신해준다.
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, // 1초 단위
                    1, // 1m 단위
                    gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, // 1초 단위
                    1, // 1m 단위
                    gpsLocationListener);
        }
    }//end of find_gps()

    /**
     * 문자 전송 담당 함수(find_user_info() 함수 안에서 동작)
     */
    protected void SmsSend(String strMsg, TextView view) {

        String[] numbers = {"112", "119"};
        for (String number : numbers) {
            net_uri = Uri.parse("sms:" + number);
            send_intent = new Intent(Intent.ACTION_SENDTO, net_uri);
            send_intent.putExtra("sms_body", strMsg);
            startActivity(send_intent);
        }

        view.setText("");
    } // end of SmsSend(String strMsg, TextView view)

    /**
     * 카메라 기능 담당 함수
     */
    protected void take_picture() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위협하는 존재를 촬영하시겠습니까?");
        builder.setMessage("촬영 시 초상권 침해의 소지가 있으나,\n용의자의 인상착의를 증거로 남길 수 있습니다.");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // 카메라 기능 실행
                int camera_permission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);

                // 권한 요청할 때 마시멜로 이상 버전일 경우 권한 요청하도록
                if (Build.VERSION.SDK_INT >= 23 && camera_permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                }

                // 권한 부여 받은 이후 카메라 실행
                else {
                    // Toast.makeText(MainActivity.this, "권한 거부 되었습니다.", Toast.LENGTH_LONG);
                    startActivity(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                }
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Toast.makeText(MainActivity.this, "카메라기능을 활성화 시키지 않습니다.", Toast.LENGTH_LONG).show();

            }
        });
        // builder.setNeutralButton("취소", null);
        builder.create().show();
    } // end of take_picture();

    /**
     * 배경화면 및 버튼 색상 변경 담당 함수
     */
    protected void background_switch() {
        //String test = background_switch.isChecked() ? "on" : "off";
        //Toast.makeText(MainActivity.this, test, Toast.LENGTH_LONG).show();

        if (background_switch.isChecked()) {
            location_btn.setBackgroundColor(Color.WHITE);
            location_btn.setTextColor(Color.BLACK);

            urgent_btn.setBackgroundColor(Color.WHITE);
            urgent_btn.setTextColor(Color.BLACK);

            camera_btn.setBackgroundColor(Color.WHITE);
            camera_btn.setTextColor(Color.BLACK);
        } else {
            location_btn.setBackgroundColor(Color.BLACK);
            location_btn.setTextColor(Color.WHITE);

            urgent_btn.setBackgroundColor(Color.BLACK);
            urgent_btn.setTextColor(Color.WHITE);

            camera_btn.setBackgroundColor(Color.BLACK);
            camera_btn.setTextColor(Color.WHITE);
        }
    }//end of background_switch()

    /*----------------------------------------------------------------------------------------------------------------------------------*/

    /**
     * 함수 실행
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // activity_main.xml과 매핑

        // 응급 통화 버튼 클릭시 전화앱에서 다이얼로 번호 넘기도록.
        urgent_btn = (Button) findViewById(R.id.urgent_btn);
        urgent_btn.setOnClickListener(v -> {
            send_call();
        }); //end of urgent_btn.setOnClickListener

        // 위치 파악 버튼 클릭
        location_btn = (Button) findViewById(R.id.location_btn);
        location_btn.setOnClickListener(v -> {
            find_user_info();
        }); // end of location_btn.setOnClickListener()

        // 촬영 버튼 클릭
        camera_btn = (Button) findViewById(R.id.camera_btn);
        camera_btn.setOnClickListener(v -> {
            take_picture();
        });

        // 배경화면 변경(진행중)
        background_switch = (Switch) findViewById(R.id.background_switch);
        background_switch.setOnClickListener(v -> {
            background_switch();
        });
    }// end of onCreate(Bundle savedInstanceState)

    /**
     * 위치 리스너는 위치정보를 전달할 때 호출되므로 onLocationChanged()메소드 안에 위지청보 처리 작업을 구현 해야합니다
     */
    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            String provider = location.getProvider();  // 위치정보
            double longitude = location.getLongitude(); // 위도
            double latitude = location.getLatitude(); // 경도
            double altitude = location.getAltitude(); // 고도
            // txtResult.setText("위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도 : " + altitude);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onProviderDisabled(String provider) {

        }
    };

}// end of class