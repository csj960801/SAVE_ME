package com.example.find_location;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button location_btn;
    private Button urgent_btn;
    private TextView txtResult;

    private Uri net_uri = null; // Intent로 전화 혹은 문자를 보낼 주소를 전달하고
    private Intent send_intent = null; // Intent는 Uri에서 지정한 데이터 전달 방식(tel, sms 등)에 따라 받은 주소로 전달함.

    // sms 권한
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 2323;

    /**
     * 전화 권한 dialog 담당 함수
     */
    protected void send_call() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("권한이 필요합니다.").setMessage("이 기능을 사용하기 위해서는 단말기의 \"전화걸기\"권한이 필요합니다. 계속하시겠습니까?").setPositiveButton("네", (dialog1, which) -> {
            // 위 리스너랑 다른 범위여서 마쉬멜로우인지 또 체크해주어야 한다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1000);
            }
        }).setNegativeButton("아니요", (dialog12, which) -> Toast.makeText(MainActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show()).create().show();
    } // end of send_call()

    /**
     * 문자 전송 담당 함수
     *
     */
    protected void SmsSend(String strMsg, TextView view) {
        String[] numbers = {"112", "119"};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {//권한이 없다면
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST_SEND_SMS);
        }
        // 권한이 있다면 SMS를 보낸다.
        else {

            try {

                for (String number : numbers) {
                    net_uri = Uri.parse("sms:" + number);
                    send_intent = new Intent(Intent.ACTION_SENDTO, net_uri);
                    send_intent.putExtra("sms_body", strMsg);
                    startActivity(send_intent);
                }
                view.setText("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } // end of SmsSend(String strMsg, TextView view)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location_btn = (Button) findViewById(R.id.location_btn);
        urgent_btn = (Button) findViewById(R.id.urgent_btn);
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtResult.setTextSize(17);
        txtResult.setTextColor(Color.WHITE);

        // 위치 관리자
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 위치 파악 버튼 클릭 시
        location_btn.setOnClickListener(v -> {
            // 마시멜로 이상 버전일 경우 권한 요청
            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

            } else {
                // 가장최근 위치정보 가져오기
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    String provider = location.getProvider(); // 위치파악 장치 정보
                    double longitude = location.getLongitude(); // 위도
                    double latitude = location.getLatitude(); // 경도
                    double altitude = location.getAltitude(); // 고도
                    txtResult.setText("위치정보 : " + provider + "\n" + "위도 : " + longitude + "\n" + "경도 : " + latitude + "\n" + "고도  : " + altitude + "\n" + "현재 전화가 불가능한 상황입니다..");
                }

                // 위치정보를 원하는 시간, 거리마다 갱신해준다.
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, // 1초 단위
                        1, // 1m 단위
                        gpsLocationListener);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, // 1초 단위
                        1, // 1m 단위
                        gpsLocationListener);
            }

            /**
             * 문자 전송
             */
            SmsSend(txtResult.getText().toString(), txtResult);
        }); // end of location_btn.setOnClickListener()

        // 응급 통화 버튼 클릭시
        urgent_btn.setOnClickListener(v -> {
            /**
             *  현재 사용자의 OS버전이 마시멜로우 인지 체크하고 사용자 단말기의 권한 중 전화걸기 권한이 허용되어 있는지 체크한다.
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                //  Package는 Android Application의 ID이다.
                /**
                 *  사용자가 CALL_PHONE 권한을 한번이라도 거부한 적이 있는지 조사한다.
                 *  거부한 이력이 한번이라도 있다면, true를 리턴한다.
                 *  거부한 이력이 없다면 false를 리턴한다.
                 */
                if (shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)) {
                    send_call();
                }
                // 최초로 권한을 요청 할 때
                else {
                    // CALL_PHONE 권한을 안드로이드 OS에 요청합니다.
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1000);
                }
            }
            // call_phonne의 권한이 있을 떄 전화 실행
            else {
                net_uri = Uri.parse("tel:119");
                Intent intent = new Intent(Intent.ACTION_CALL, net_uri);
                startActivity(intent);
            }
        }); //end of urgent_btn.setOnClickListener
    }// end of onCreate(Bundle savedInstanceState)

    // 위치 리스너는 위치정보를 전달할 때 호출되므로 onLocationChanged()메소드 안에 위지청보 처리 작업을 구현 해야합니다.
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