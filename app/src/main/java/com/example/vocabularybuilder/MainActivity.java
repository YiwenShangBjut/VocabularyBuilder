package com.example.vocabularybuilder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDNotifyListener;

import com.example.vocabularybuilder.card_view_pager.CardViewPager;
import com.example.vocabularybuilder.db.MyDbOpenHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Call system camera, store photo and clip photo: reference: https://blog.csdn.net/qq_38843185/article/details/80272308
 * Baidu map SDK official document: reference: http://lbsyun.baidu.com/index.php?title=android-locsdk
 * checkPermission: reference: https://blog.csdn.net/scimence/article/details/85989997
 */

public class MainActivity extends AppCompatActivity {
    //public LocationClient mLocationClient = null;
    //private MyLocationListener myListener=new MyLocationListener();
    private MyDbOpenHelper myDbHelper;
    private LocationService locationService;
    private TextView changePlan;
    private  EditText word_et;
    private  EditText day_et;
    private TextView left;
    private TextView right;
    private Button decide;
    private TextView plan_tv;
    private TextView reset;
    private TextView startDate;
    private String startDate_t;
    private  Calendar cal;
    private LocalDate start;
    private LocalDate now=LocalDate.now();
    private ArrayList<Integer> plan;
    private int Number_of_Date;
    private TextView firstNum;
    private TextView secNum;
    private ProgressBar progressBar;
    private int leftWord;
    private int haveWord=0;
    private int todayWord=0;
    private int planDay;
    private int planWord;

    private float latitude=0;
    private float longitude=0;

    private TextView word_tv;
    private TextView day_tv;

    private MyBroadcastReceiver myBroadcastReceiver;

    private Button addDay_btn;
    private Button addWord_btn;
    private Button screenLock;

    private Button display;
    private Button Mini;
    private Button getLocation;
    private TextView tv;
    private TextView reminder;
    private BottomNavigationBar bottomNavigationBar;
    protected SharedPreferences sp;
    private static boolean gl=true;
    protected BDLocation bdLocation;
    private String location="";
    private Vibrator vibrator;
    private String username="";
    private boolean LocationOn;
    private boolean isShow=false;
    private NotificationChannel notificationChannel;

    private static final String TAG = "MainActivity";

    private static final int REQUEST_IMAGE_GET = 0;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;

    private static final String IMAGE_FILE_NAME = "W_WDict_photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDbHelper = MyDbOpenHelper.getInstance(this);

        LocationOn=false;

       // now=LocalDate.now();
        checkPermission();

        sp = this.getSharedPreferences("data", 0);

        startDate_t=sp.getString("startDate", "");

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("W_WDict");
        setSupportActionBar(toolbar);

        changePlan=findViewById(R.id.changPlan);
        left=findViewById(R.id.left);
        right=findViewById(R.id.TodayWord);
        progressBar=findViewById(R.id.progress_bar);

        display=findViewById(R.id.displayList);
        Mini=findViewById(R.id.Mini_Dict);
        tv=findViewById(R.id.location_tv);
        reminder=findViewById(R.id.setReminder);
        getLocation=findViewById(R.id.getLocation);

        bottomNavigationBar=findViewById(R.id.bottom_navigation_bar);

        firstNum=findViewById(R.id.first_num);
        secNum=findViewById(R.id.second_num);


        if(startDate_t==""){
            showPlanDialog();
        }else if(startDate_t=="0"){

        }
        else{
            String []d=startDate_t.split("-");
            start=LocalDate.of(Integer.valueOf(d[0]).intValue(),Integer.valueOf(d[1]).intValue(),Integer.valueOf(d[2]).intValue());
            Number_of_Date=Math.abs((int)(now.toEpochDay()-start.toEpochDay()));
            //get from dialog
            planWord=sp.getInt("planword",0);
            planDay=sp.getInt("planday",0);

            plan=getPlanList(String.valueOf(planWord),String.valueOf(planDay));
            leftWord=getLeftWord(Number_of_Date);
            haveWord=sp.getInt("haveword", 0);
            todayWord=sp.getInt("todayword", 0);
            Log.e(TAG,"today word: "+todayWord);
            Log.e(TAG,"have word: "+haveWord);

            firstNum.setText(String.valueOf(haveWord));
            secNum.setText(String.valueOf(planWord));

            left.setText(String.valueOf(planDay-Number_of_Date));
            if(Number_of_Date==planDay){
              showDueDialog();
            }
            int right_num=plan.get(Number_of_Date)-todayWord<0?0:plan.get(Number_of_Date)-todayWord;
            right.setText(String.valueOf(right_num));

            progressBar.setMax(planWord);
            if(haveWord==0){
                progressBar.setProgress(0);
            }else if(haveWord==planWord||haveWord>planWord){
                Log.e(TAG,"show success dialog");
                showSuccessDialog();

            } else{
                Log.e(TAG,"have word "+haveWord);
                progressBar.setProgress(haveWord);
            }


        }

        changePlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlanDialog();
            }
        });

        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,VocabularyDisplayActivity.class);
                startActivity(intent);
            }
        });

        Mini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,MiniDictMenuActivity.class);
                Log.e(TAG,"location is "+location);
                intent.putExtra("location", location);
                startActivity(intent);
            }
        });


        locationService=new LocationService(this);
        locationService.registerListener(mListener);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.start();

        Intent intent=getIntent();
        String action=intent.getAction();
        int position= intent.getIntExtra("position",0);//position of bottomNavigation bar

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_RIPPLE);
        bottomNavigationBar.setBarBackgroundColor(R.color.darkGreen);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.md_home_icon,"Home").setActiveColorResource(R.color.lightGreen).setInActiveColor(R.color.white))
                .addItem(new BottomNavigationItem(R.drawable.md_camera_icon,"Camera").setActiveColorResource(R.color.lightGreen).setInActiveColor(R.color.white))
                .setFirstSelectedPosition(position)
                .initialise();

        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                switch (position){
                    case 1:
                        //create file to store the photo
                        PictureUtil.mkdirMyDictRootDirectory();
                        //checkCameraPermission();
                        imageCapture();
                        break;
                }
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });

        if(action=="TAKE PHOTO"){
            bottomNavigationBar.selectTab(1);
        }

       // tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("cn.abel.action.broadcast_location");
        intentFilter.addAction("cn.abel.action.broadcast_addword");

        myBroadcastReceiver=new MyBroadcastReceiver();
        this.registerReceiver( myBroadcastReceiver,intentFilter);

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLocation.getText().toString().equals("Turn on location reminder")) {
                    LocationOn=false;
                    getLocation.setText("Turn off location reminder");
                    locationService.start();
                    reminder.setVisibility(View.VISIBLE);
                } else {
                    LocationOn=true;
                   locationService.stop();
                   getLocation.setText("Turn on location reminder");
                    tv.setText("");
                    reminder.setVisibility(View.GONE);
                }
            }
        });
        //set this place to remind you the word you used to learn at this place
        reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationService.setNotifyLocation(notifyListener,latitude,longitude);
            }
        });

        locationService.registerNotifyListener(notifyListener);
       // notifyListener.SetNotifyLocation(39.881145,116.4908387f,1000,locationService.getClient().getLocOption().getCoorType());
        //notifyListener.SetNotifyLocation(39.881371f,116.491064f,800,locationService.getClient().getLocOption().getCoorType());

       // notifyListener.SetNotifyLocation(39.877366f,116.486351f,850,locationService.getClient().getLocOption().getCoorType());
        //dormitory:116.488336,39.880896 true true
        //No.4 building:116.490779,39.881401 false false
        //No.3 building 116.487851,39.881304 false true
        //Ginkgo Avenue 116.48817,39.882072 false false
        //canteen 116.488111,39.879809 false true
        //south gate 116.486351,39.877366 false true
    }
    public BDNotifyListener notifyListener=new MyNotifyListener(){
        @Override
        public void onNotify(BDLocation mlocation, float distance){
            //已到达设置监听位置附近
            vibrator=(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(1000,200));
            buildNotification();
            Toast.makeText(MainActivity.this, "You have a list to check", Toast.LENGTH_LONG).show();
        }
    };

    private void buildNotification() {
        if (!isShow){ //避免多次显示
            //myDbHelper.deleteLocationDict();//delete last dict
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent intent = new Intent(this,LocationWordListActivity.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude",longitude );
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationChannel=new NotificationChannel("CHANNEL_ONE_ID","LOCATION_REMINDER",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(notificationChannel);
            Notification notification = new Notification.Builder(this).setChannelId("CHANNEL_ONE_ID")
                    .setTicker("You used to collect words in this place")
                    .setAutoCancel(false)
                    .setContentTitle("You used to collect words in this place")
                    .setContentText("Touch to see the detail")
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.dict_orange))
                    .setSmallIcon( R.drawable.dict_orange)
                    .setContentIntent(pendingIntent)
                    .build();
            manager.notify(1, notification);

          //  startForeground(0x11, notification);

            isShow = true;
        }
    }


    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action.equals("cn.abel.action.broadcast_location")){
                if(LocationOn){
                    Toast.makeText(MainActivity.this, "Turn on location reminder", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MainActivity.this, "Turn off location reminder", Toast.LENGTH_LONG).show();
                }
                getLocation.callOnClick();
            }else if(action.equals("cn.abel.action.broadcast_addword")){
                if(startDate_t!=""){
                    haveWord=haveWord+1;
                    todayWord=todayWord+1;
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("haveword", haveWord);
                    editor.putInt("todayword", todayWord);
                    editor.commit();
                    recreate();
                }
                Log.e(TAG,"add today word: "+todayWord);
                Log.e(TAG,"add have word: "+haveWord);
                Toast.makeText(MainActivity.this, "A new word is added! Good Job!", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {

           System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("haveword", haveWord);
        editor.putInt("todayword", todayWord);
        editor.commit();

    }
    @Override
    protected void onResume(){
        super.onResume();
        haveWord=sp.getInt("haveword", 0);
        todayWord=sp.getInt("todayword", 0);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //locationService.unregisteNotifyrListener(notifyListener);
        this.unregisterReceiver( myBroadcastReceiver);
}


    public BDAbstractLocationListener mListener=new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuilder sb = new StringBuilder();
                sb.append(location.getCountry()+" ");
                sb.append(location.getLatitude()+" ");//纬度
                sb.append(location.getLongitude()+" ");//经度
                sb.append(location.getCity()+" ");
                sb.append(location.getDistrict()+" ");
                sb.append(location.getStreet()+" ");
                sb.append(location.getAddrStr()+" ");
                sb.append(location.getLocationDescribe());
                logMsg(sb.toString());

            }
        }
    };

    public void getCoordinate(){
        String str[]=location.split(" ");
        latitude=Float.valueOf(str[1]);//39
        longitude=Float.valueOf(str[2]);//116
    }

//https://blog.csdn.net/a853906126/article/details/84582262
//to get location information
    public void logMsg(final String str) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                   tv.post(new Runnable() {
                        @Override
                        public void run() {
                            location=str;
                            getCoordinate();
                           tv.setText(str);
                          // Log.e("location",str);
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * reference: https://blog.csdn.net/scimence/article/details/85989997
     */
    public void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //Location Permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }
        }
    }

    private void imageCapture(){
        Intent intent;
        Uri pictureUri;

        File pictureFile=new File(PictureUtil.getMyDictRootDirectory(),IMAGE_FILE_NAME);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
            intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri= FileProvider.getUriForFile(this,"com.example.vocabularybuilder.fileprovider",pictureFile);

        }else{
            intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri=Uri.fromFile(pictureFile);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT,pictureUri);
        Log.e(TAG,"before take photo"+pictureUri.toString());
        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);

    }


    public void setPicToView(Uri uri)  {
        if (uri != null) {
            Bitmap photo = null;
            try {
                photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            // 创建 word_photo 文件夹
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //String storage = Environment.getExternalStorageDirectory().getPath();
                File dirFile = new File(PictureUtil.getMyDictRootDirectory(),  "word_photo");
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.d(TAG, "in setPicToView->文件夹创建失败");
                    } else {
                        Log.d(TAG, "in setPicToView->文件夹创建成功");
                    }
                }
                File file = new File(dirFile, System.currentTimeMillis()+".jpg");
                sp = this.getSharedPreferences("data", 0);
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("photo_path",file.getPath());
                editor.commit();
               // InfoPrefs.setData(SyncStateContract.Constants.UserInfo.HEAD_IMAGE,file.getPath());
                //Log.d("result",file.getPath());
                // Log.d("result",file.getAbsolutePath());
                // 保存图片
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 在视图中显示图片
            showCapturedPhoto();
            //circleImageView_user_head.setImageBitmap(InfoPrefs.getData(Constants.UserInfo.GEAD_IMAGE));
        }
    }

    private void showCapturedPhoto(){
        boolean isSdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sdcard是否存在
        if (isSdCardExist) {

           // String path = InfoPrefs.getData(Constants.UserInfo.HEAD_IMAGE);// 获取图片路径
            String path=sp.getString("photo_path","");
            File file = new File(path);
            Intent intent;
            if (file.exists()) {
                //Bitmap bm = BitmapFactory.decodeFile(path);
                // 将图片显示到ImageView中
                intent=new Intent(MainActivity.this, ShowCapturedPhotoActivity.class);
                intent.putExtra("location", location);
                intent.putExtra("photo_path",path);
                startActivity(intent);

            }else{
                Log.e(TAG,"no file");
                intent=new Intent(MainActivity.this, ShowCapturedPhotoActivity.class);
                intent.putExtra("location", "");
                intent.putExtra("photo_path","");
                startActivity(intent);
            }
        } else {
            Log.e(TAG,"no SD card");
            Intent intent=new Intent(MainActivity.this, ShowCapturedPhotoActivity.class);
            intent.putExtra("location", "");
            intent.putExtra("photo_path","");
            startActivity(intent);
        }
    }

    private void startPhotoZoom(Uri uri) {
        Log.d(TAG,"Uri = "+uri.toString());
        //保存裁剪后的图片
        File cropFile=new File(PictureUtil.getMyDictRootDirectory(),"crop.jpg");
        try{
            if(cropFile.exists()){
                cropFile.delete();
                Log.e(TAG,"delete");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        Uri cropUri;
        cropUri = Uri.fromFile(cropFile);

        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1); // 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 1024); // 输出图片大小
        intent.putExtra("outputY", 1024);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);

        Log.e(TAG,"cropUri = "+cropUri.toString());

        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, REQUEST_SMALL_IMAGE_CUTTING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    File pictureFile = new File(PictureUtil.getMyDictRootDirectory(), IMAGE_FILE_NAME);
                    Uri pictureUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        pictureUri = FileProvider.getUriForFile(this, "com.example.vocabularybuilder.fileprovider", pictureFile);
                        Log.e(TAG, "picURI=" + pictureUri.toString());
                    } else {
                        pictureUri = Uri.fromFile(pictureFile);
                        Log.e(TAG, "picURI=" + pictureUri.toString());
                    }
                    startPhotoZoom(pictureUri);
                    break;
                case REQUEST_SMALL_IMAGE_CUTTING:
                    Log.e(TAG,"before show");
                    File cropFile=new File(PictureUtil.getMyDictRootDirectory(),"crop.jpg");
                    Uri cropUri = Uri.fromFile(cropFile);
                    setPicToView(cropUri);
                    break;

                //choose from album
                case REQUEST_IMAGE_GET:
                    Uri uri= PictureUtil.getImageUri(this,data);
                    startPhotoZoom(uri);
                    break;
                default:
            }
        } else {
            Log.e(TAG, "result = " + resultCode + ",request = " + requestCode);
        }
    }
    public void showSimpleDialog(String str){
        final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(str);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
    public void showDueDialog(){
        final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Your plan is due, please reset it.");
        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
                recreate();

            }
        });

        builder.show();
    }
    public void reset(){
        planDay=0;
        planWord=0;
        haveWord=0;
        todayWord=0;
        startDate_t="";
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("haveword", 0);
        editor.putInt("todayword", 0);
        editor.putInt("planday", 0);
        editor.putInt("planword", 0);
        editor.putString("startDate", startDate_t);
        editor.commit();
    }


    public void showSuccessDialog(){
        View view=View.inflate(MainActivity.this, R.layout.success_dialog, null);
        final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);
        word_tv=view.findViewById(R.id.word_tv);
        day_tv=view.findViewById(R.id.day_tv);
        word_tv.setText(String.valueOf(haveWord));
        day_tv.setText(String.valueOf(planDay));
        Button newPlan=view.findViewById(R.id.new_plan);
        newPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
                recreate();
            }
        });
        builder.show();

    }
    public void showPlanDialog(){

        View view=View.inflate(MainActivity.this, R.layout.plan_dialog, null);
        final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setView(view);
        word_et=view.findViewById(R.id.word_num);
        day_et=view.findViewById(R.id.day_num);

        startDate=view.findViewById(R.id.startDate);
        reset=view.findViewById(R.id.reset);
        plan_tv=view.findViewById(R.id.plan_tv);
        decide=view.findViewById(R.id.decide_btn);
        startDate.setText(startDate_t);

        if(startDate_t==""||startDate_t=="0"){
            plan_tv.setVisibility(View.GONE);
            startDate.setVisibility(View.GONE);
            reset.setVisibility(View.GONE);
            start=now;
            startDate_t=start.toString();
            Log.e("StartDate",startDate_t);
        }else{
            word_et.setFocusableInTouchMode(false);//can not edit
            day_et.setFocusableInTouchMode(false);
            plan_tv.setVisibility(View.VISIBLE);
            startDate.setVisibility(View.VISIBLE);
            reset.setVisibility(View.VISIBLE);
            sp = this.getSharedPreferences("data", 0);
            word_et.setText(String.valueOf(sp.getInt("planword",0)));
            day_et.setText(String.valueOf(sp.getInt("planday",0)));
            startDate_t=sp.getString("startDate","");
            decide.setVisibility(View.GONE);
        }
        final AlertDialog dialog= builder.show();
        decide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPlanList(word_et.getText().toString(),day_et.getText().toString());
                if(plan.size()!=0){
                    planWord=Integer.valueOf(word_et.getText().toString()).intValue();
                    planDay=Integer.valueOf(day_et.getText().toString()).intValue();
                    Log.e("NUMBEROFDATE",now.toString()+" "+start.toString());
                    Number_of_Date=0;
                    left.setText(Integer.toString(planDay-Number_of_Date));
                    right.setText(Integer.toString(plan.get(0)));

                    haveWord=0;
                    firstNum.setText(Integer.toString(haveWord));
                    secNum.setText(word_et.getText().toString());

                    SharedPreferences.Editor editor=sp.edit();
                    editor.putInt("planword", planWord);
                    editor.putInt("planday", planDay);
                    editor.putString("startDate", startDate_t);
                    editor.putInt("haveword", 0);
                    editor.putInt("todayword", 0);
                    editor.commit();
                    progressBar.setProgress(0);
                    recreate();
                    dialog.dismiss();
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                final AlertDialog.Builder builder1=new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("Are you sure you want to reset the plan?");

                builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        decide.setVisibility(View.VISIBLE);
                        plan_tv.setVisibility(View.GONE);
                        startDate.setVisibility(View.GONE);
                        reset.setVisibility(View.GONE);
                        word_et.setFocusableInTouchMode(true);//can edit
                        day_et.setFocusableInTouchMode(true);
                        haveWord=0;
                        todayWord=0;
                        start=now;
                        startDate_t=start.toString();
                        startDate.setText(startDate_t);
                        word_et.setText("");
                        day_et.setText("");
                    }
                });
                builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder1.show();
            }
        });


        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                SharedPreferences.Editor editor=sp.edit();
                editor.putString("planword", word_et.getText().toString());
                editor.putString("planday", day_et.getText().toString());
                editor.putString("startDate", startDate_t);
                editor.commit();
            }
        });


    }
    private int getLeftWord(int dayNum){
        int n=0;
        for(int i=dayNum;i<plan.size();i++){
            n=n+plan.get(i);
        }
        return n;
    }

   private ArrayList<Integer> getPlanList(String w, String d){
       plan=new ArrayList<Integer>();
       int wordNum=Integer.valueOf(w).intValue();
       int dayNum=Integer.valueOf(d).intValue();
      if(wordNum==0||dayNum==0){
           String str="Word and day can not be 0";
           showSimpleDialog(str);
       }
        else if(dayNum>wordNum){
            String str="Don't be lazy, at lest learn onw new word per day.";
            showSimpleDialog(str);
        }else if(wordNum/dayNum>20){
            String str="Good ambition. But less word might be better.";
            showSimpleDialog(str);
        }else{
            int n=wordNum/dayNum;
            int m=wordNum%dayNum;
            //to avoid user have too much word to learn on the last day of plan

            for(int i=0;i<m;i++){
                plan.add(n+1);
            }
            for(int i=m;i<dayNum;i++){
                plan.add(n);
            }

            Log.e("PLANLIST","plan size is "+plan.size());

            for(int i=0;i<plan.size();i++){
                Log.e("PLANLIST",plan.get(i).toString());
            }

        }
        return plan;
   }



}
//https://blog.csdn.net/qq_38843185/article/details/80272308
//https://blog.csdn.net/suyimin2010/article/details/80851256