
/*
* 데이터베이스 이름 : guentae
*
* 테이블 이름 : Employee
* empNum VARCHAR(PK), name VARCHAR, phone VARCHAR
* 컬럼추가 : CompanySeq int(FK), EmpType INT
*
* 테이블 이름 : EmployeeInOut
* empNum VARCHAR(PK), seq int(FK, 자동 증가), Intime varchar(20), Outtime varchar(20)
* seq를 pk로 변경하기, Date(nchar 8) 추가
*
* 테이블 추가 생성 필요 : Company
* CompanySeq int(자동증가, PK), CompanyName VARCHAR, Latitude VARCHAR, Longitude VARCHAR
* * */

// 35.0619024, 128.0768098
// 좌표값 오차범위 : +- 0.0004000

package com.example.checkinandout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button btn_moveToAdmin;
    int int_adminCode = 0; //0 = false, 1 = true
    int int_loginSuccess = 0; // 0 = false, 1 = true, 로그인 여부 검사
    public String str_ip = "192.168.0.59";//server IP입력
    public String str_TAG = "phpquery"; //php 태그
    private char state;
    public String str_empName;
    public String str_empNum;
    public String str_phone;
    public String str_inEmpNum;
    public String str_inName;
    public String str_userType;
    public Double dbl_longitude;
    public Double dbl_latitude;
    public Double[] arr_longitude = new Double[5];
    public Double[] arr_latitude = new Double[5];
    Location location;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        * 로그인 실패 시 다이어로그 창 무한으로 나오게 구현
        * */
        checkSelfPermission();


        // DB 연결
        /*if(int_loginSuccess == 0){
            showLoginDialog();
        }*/


        int int_locationCount = 0 ;
        /*출근시간에 따른 위치 계신*/
        final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10000, //10초마다 업데이트, minTime과 minDistance는 and 조건 만족 시 실행
                0,
                gpsLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                10000,
                0,
                gpsLocationListener);



        /*
         * 관리자 버튼 클릭 시 권한 검사 후 권리자 페이지 이동
         * */
        findViewById(R.id.btn_moveToAdmin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), AdminActivirty.class);
                    startActivity(intent);

            }
        });
    }

    public void showLoginDialog(){
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout loginLayout = (LinearLayout) vi.inflate(R.layout.logindialog, null);

        final EditText empNum = (EditText)loginLayout.findViewById(R.id.empNum);
        final EditText empName = (EditText)loginLayout.findViewById(R.id.empName);
        new AlertDialog.Builder(this).setTitle("Login").setView(loginLayout).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //테스트코드
                Toast.makeText(MainActivity.this, "사번 : "+empNum.getText().toString() + "이름 : "+empName.getText().toString(),Toast.LENGTH_SHORT).show();
                str_inEmpNum = empNum.getText().toString();
                str_inName = empName.getText().toString();
                GetData task = new GetData();
                // 로그인
                task.execute(str_inEmpNum, str_inName);

            }
        }).show();

    }




    public class GetData extends AsyncTask<String, Void, String>{
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() { //작업 시작 전 초기 호출
            super.onPreExecute();

            progressDialog =ProgressDialog.show(MainActivity.this, "please wait", null, true, true);

        }

        @Override
        protected void onPostExecute(String result) { //AsyncTask의 모든 작업이 완료된 후 최종 1회 호출, 자동로그인을 위함
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(str_TAG, "respose : " + result);
            if(result == null){
            }
            else{
                try{
                    JSONObject jsonObject = new JSONObject(result);

                    JSONArray jsonArray = jsonObject.getJSONArray("jaemin");



                    for(int i = 0 ; i<jsonArray.length() ; i++){
                        JSONObject subObject = jsonArray.getJSONObject(i);
                        str_empNum = subObject.getString("empNum");
                        str_empName = subObject.getString("name");
                        str_phone = subObject.getString("phone");
                        str_userType = subObject.getString("userType");

                    }
                    /*
                    * 자동로그인 구현 후 int_loginSuccess = 1 초기화
                    * */
                    int_adminCode = Integer.parseInt(str_userType);
                    System.out.println("----------------------------------------------------");
                    System.out.println(str_empNum);
                    System.out.println(str_empName);
                    System.out.println("----------------------------------------------------");

                    if(str_empNum.equals(str_inEmpNum) && str_empName.equals(str_inName)){
                        Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                        System.out.println("로그인성공");
                        int_loginSuccess = 1;
                        // if를 통해 유저 type을 판독하여 관리자페이지 진입 여부 활성화




                        //if 좌표값 오차범위 내 일경우
                        /*
                        long now = System.currentTimeMillis();
                        Date mDate = new Date(now);
                        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String getTime = simpleDate.format(mDate);
                        System.out.println("현재시간 : "+getTime);
                        */



                        //InsertData insertTask = new InsertData();
                        //insertTask.execute(str_empNum, "2021-11-20 07:00:00", "2021-11-20 16:30:00");



                    }
                    else{
                        Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                        int_loginSuccess = 0;
                        showLoginDialog();
                    }

                }
                catch (JSONException e){
                    //Toast.makeText(getApplicationContext(), "정보 확인 요망", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                    int_loginSuccess = 0;
                    showLoginDialog();
                    e.printStackTrace();
                }
            }

        }

        @Override
        protected String doInBackground(String... params) { //스레드에서 처리 될 내용 기술
            String str_search1 = params[0];
            String str_search2 = params[1];
            String str_serverURL = "http://"+str_ip+"/loginCheck.php";
            String str_postParameters = "empNum=" + str_search1 + "&name=" +str_search2;


            try {

                URL url = new URL(str_serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(str_postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(str_TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(str_TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){ // 권한을 허용했을 경우
            int length  = permissions.length;
            for(int i = 0 ; i<length ; i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    //권한 허용
                    Log.d("MainActivity", "권한허용 : " + permissions[i]);
                }
            }
        }
    }

    private void checkSelfPermission() {
        String temp = "";
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            temp = temp + Manifest.permission.ACCESS_FINE_LOCATION + " ";
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            temp = temp + Manifest.permission.ACCESS_COARSE_LOCATION + " ";
        }
        if(TextUtils.isEmpty(temp) == false){ // 권한 없을 시 요청
            ActivityCompat.requestPermissions(this, temp.trim().split(" "), 1);
        }
        else
            Toast.makeText(this, "권한 허용 완료", Toast.LENGTH_SHORT).show();
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {

            String provider = location.getProvider();
            dbl_longitude = location.getLongitude();
            dbl_latitude = location.getLatitude();
            double altitude = location.getAltitude();


            System.out.println("측정 후 시간 변환에 따른 갱신");
            System.out.println("latitude : " + dbl_latitude);
            System.out.println("longitude : " + dbl_longitude);
            // 현재시간과 정해진 시간을 비교로 특정 시간 범위에 들어가면 측정 값을 배열에 담음. 평균값을 계산 후 bool 변수를 초기화하여 다시 배열에 담기지 못하게 함
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

}
