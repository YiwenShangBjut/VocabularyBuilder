package com.example.vocabularybuilder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.example.vocabularybuilder.card_view_pager.CardViewPager;
import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.db.WordContract;
import com.example.vocabularybuilder.primary_class.JsonParser;
import com.example.vocabularybuilder.primary_class.Word;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import static com.example.vocabularybuilder.db.WordContract.WordEntry.MY_WORD_TABLE_NAME;

/**
 * reference for Jsoup: https://jsoup.org/download
 * reference for SpeechRecognizer: https://www.xfyun.cn/services/voicedictation
 * reference for shake: https://blog.csdn.net/LIXIAONA_1101/article/details/82667896
 */

public class MiniDictDisplay extends AppCompatActivity implements SensorEventListener{
    protected static final int RESULT_SPEECH = 1;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isShake=false;

    FloatingActionButton add;
    FloatingActionButton reOrder;
    private static ListView lvContacts;
    private MyDbOpenHelper myDbHelper;
    private MyDbOpenHelper myDbHelper2;
    private static ArrayList<Word> wordList;
    private static Word clickedItem;
    protected SharedPreferences sp;
    private String tablename;
    private String word="";
    private String action;
    private String id;
    EditText English;
    Button btnSpeak;
    EditText Property;
    EditText Chinese;
    EditText Phonetic;
    EditText Meaning;
    ImageView photo;
    TextView location_tv;
    String photoPath="";
    String location="";
    Button Check;
    Button Complete;

    private String English_s;
    private String Property_s;
    private String Chinese_s;
    private String Phonetic_s;
    private String Meaning_s;


    private AlertDialog.Builder builder;
    private static boolean err=false;
    private static Vibrator vibrator;

    private RelativeLayout mTopLayout;
    private RelativeLayout mBottomLayout;
    private ImageView mTopLine;
    private ImageView mBottomLine;
    private MyHandler mHandler;
    private static final String TAG = "MiniDictDisplay";
    private static final int START_SHAKE = 0x1;
    private static final int AGAIN_SHAKE = 0x2;
    private static final int END_SHAKE = 0x3;
    private final String APP_ID="5ddb6e1c";
    private RecognizerDialog recognizerDialog=null;
    private HashMap<String ,String> mIatResults=new LinkedHashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_dict_display);
        Intent intent=getIntent();
        id=intent.getStringExtra("id");
        location=intent.getStringExtra("location");
        tablename= intent.getStringExtra("tablename");
        word=intent.getStringExtra("word");
        photoPath=intent.getStringExtra("photoPath");
        action=intent.getAction();
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(tablename.replaceAll("_", " "));
        setSupportActionBar(toolbar);


        initSpeech();
        initAnimationView();
        mHandler = new MyHandler(MiniDictDisplay.this);
        vibrator=(Vibrator)getApplication().getSystemService(Service.VIBRATOR_SERVICE);

        Log.e("MiniDictDisplay","onCreate---------------location: "+location);

        myDbHelper = MyDbOpenHelper.getInstance(this);
        myDbHelper2 = MyDbOpenHelper.getInstance(this);

        SQLiteDatabase db = myDbHelper.getReadableDatabase();

        Cursor cursor=db.query(tablename,null,null,null,null,null,null);
        wordList=new ArrayList<Word>();
        final ListView[] lvContacts = {getListView(getList(cursor))};

        lvContacts[0].setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickedItem=wordList.get(position);
                //Intent intent=new Intent(MiniDictDisplay.this,DetailActivity.class);
                Intent intent=new Intent(MiniDictDisplay.this, CardViewPager.class);
                intent.putExtra("location",location);
                intent.putExtra("id",clickedItem.getId());
                intent.putExtra("position",position);
                intent.putExtra("tablename",tablename);
                startActivity(intent);
            }
        });

        Log.e("MiniDictDisplay","onCreate2--------------------location: "+location);


        add=findViewById(R.id.addButton);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddDialog();
            }
        });


        if(action=="CREATE NEW WORD"){
            Log.e("MiniDictDisplay","before onClick--------------------location: "+location);
            showAddDialog();
            //add.callOnClick();
        }


    }
    private void initSpeech(){
        SpeechUtility.createUtility(MiniDictDisplay.this, SpeechConstant.APPID+"="+APP_ID);
    }
    private void initAnimationView() {
        mTopLayout = (RelativeLayout) findViewById(R.id.main_linear_top);
        mBottomLayout = ((RelativeLayout) findViewById(R.id.main_linear_bottom));
        mTopLine = (ImageView) findViewById(R.id.main_shake_top_line);
        mBottomLine = (ImageView) findViewById(R.id.main_shake_bottom_line);

        //默认
        mTopLine.setVisibility(View.GONE);
        mBottomLine.setVisibility(View.GONE);
        mTopLayout.setVisibility(View.GONE);
        mBottomLayout.setVisibility(View.GONE);
    }

@Override
   protected void onStart(){
        super.onStart();
       // sensorEventListener=
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        if(sensorManager!=null){
            accelerometerSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(accelerometerSensor!=null){
                sensorManager.registerListener( this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }
    @Override
    protected void onPause(){
        if(sensorManager!=null){
            sensorManager.unregisterListener( this);
        }
        super.onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0,0,0,"Delete");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId()==0){
            showDeleteDialog();
        }
        return true;
    }

    public void reOrderListView(){
        Collections.shuffle(wordList);
        ArrayList<Word> reOrderList=new ArrayList<Word>();
        for(int i=0;i<wordList.size();i++){
            reOrderList.add(wordList.get(i));
        }
        lvContacts =getListView(reOrderList);
        isShake=false;
    }
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:{
                if(requestCode==RESULT_OK&&null!=data){
                    ArrayList<String> text=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    English.setText(text.get(0));
                }
                break;
            }
        }
}
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            Intent intent=new Intent(MiniDictDisplay.this,MiniDictMenuActivity.class);
            clearSp();
            intent.putExtra("location", location);
            startActivity(intent);
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }
    public ArrayList<Word> getList(Cursor cursor){
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry._ID));
            String English = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_ENGLISH));
            String phonetic = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PHONETIC));
            String property = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PROPERTY));
            String Chinese = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_CHINESE));
            String Meaning = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_MEANING));
            String Location=cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_LOCATION));
            Word word = new Word();
            word.setId(id);
            word.setEnglish(English);
            word.setPhonetic(phonetic);
            word.setProperty(property);
            word.setChinese(Chinese);
            word.setMeaning(Meaning);
            word.setLocation(Location);

            wordList.add(word);
        }
        cursor.close();
        return  wordList;
    }

    public ListView getListView(ArrayList<Word> list) {
       // List items = new ArrayList<Word>();

        lvContacts = (ListView) findViewById(R.id.contacts);

        ArrayAdapter<Word> adapter = new ArrayAdapter<Word>(this, R.layout.list_item, list) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                Word word=getItem(position);

                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.list_item, parent, false);
                TextView English=(TextView)view.findViewById(R.id.English);
                TextView property=(TextView)view.findViewById(R.id.Property);
                TextView Chinese=(TextView)view.findViewById(R.id.Chinese);

                English.setText(word.getEnglish());
                property.setText(word.getProperty());
                Chinese.setText(word.getChinese());

                return view;
            }
        };
        lvContacts.setAdapter(adapter);

        return lvContacts;
    }

    private void showTip(String data){
        Toast.makeText(MiniDictDisplay.this, data, Toast.LENGTH_LONG).show();
    }

    public void showAddDialog(){

        View view=View.inflate(MiniDictDisplay.this,R.layout.add_new_word,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MiniDictDisplay.this);
        builder.setView(view);

        builder.setTitle("Add your new word");

        English=view.findViewById(R.id.English);
        Property=view.findViewById(R.id.Property);
        Chinese=view.findViewById(R.id.Chinese);
        Phonetic=view.findViewById(R.id.Phonetic);
        Meaning=view.findViewById(R.id.Meaning);
        photo=view.findViewById(R.id.Photo);
    //    location_tv=findViewById(R.id.Location);

        sp = this.getSharedPreferences("data", 0);
        English.setText(sp.getString("word",""));
        Property.setText(sp.getString("property",""));
        Chinese.setText(sp.getString("chinese",""));
        Phonetic.setText(sp.getString("phonetic",""));
        Meaning.setText(sp.getString("Meaning",""));
        photoPath=sp.getString("photoPath", "");

        Log.e("MiniDictDisplay","showAddDialog-----------location: "+location);

        btnSpeak=view.findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startSpeechDialog();
                RecognizerDialog mDialog=new RecognizerDialog(MiniDictDisplay.this, new MyInitListener());
                mDialog.setParameter(SpeechConstant.LANGUAGE,"en_us");
                mDialog.setListener(new MyRecognizerDialogListener());
                mDialog.show();

            }
        });
        if(action=="CREATE NEW WORD"){
           // English.setText(word);
            //back from Search web page, edit text can not let keyboard comp up
            //so requestfocus
            English.setFocusable(true);
            English.setFocusableInTouchMode(true);
            English.requestFocus();
            English.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });

            MiniDictDisplay.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        if(photoPath!=""){
            Bitmap bm = BitmapFactory.decodeFile(photoPath);
            photo.setImageBitmap(bm);
        }


        Check=view.findViewById(R.id.checkOnInternet);
        Check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                English_s=English.getText().toString();
                if(English_s.length()!=0){
                Intent intent=new Intent(MiniDictDisplay.this,SearchWord.class);
                Property_s=Property.getText().toString();
                Chinese_s=Chinese.getText().toString();
                Phonetic_s=Phonetic.getText().toString();
                Meaning_s=Meaning.getText().toString();
                intent.putExtra("tablename",tablename);
                intent.putExtra("word", English_s);

                SharedPreferences.Editor editor = sp.edit();

                editor.putString("word", English_s);
                editor.putString("property", Property_s);
                editor.putString("chinese", Chinese_s);
                editor.putString("phonetic",Phonetic_s);
                editor.putString("Meaning",Meaning_s);
                editor.putString("photoPath",photoPath);
                editor.putString("location",  location);

                editor.commit();

                Log.i("word",English_s);
                startActivity(intent);}
            }
        });

        Complete=view.findViewById(R.id.complete);
        Complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String English_s=English.getText().toString();
                if(English_s.length()!=0){
                    final String word=English_s;
                    complete(word);
                    errDialog("No result,please check your word or network connection.");
                }

            }
        });

        myDbHelper = MyDbOpenHelper.getInstance(this);

        builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearSp();
                Log.e("MiniDictDisplay","Finish---------------------location: "+location);
                String English_s=English.getText().toString();
                if(!isSQLInjection(English_s)) {
                    String Property_s = Property.getText().toString();
                    String Chinese_s = Chinese.getText().toString();
                    String Phonetic_s = Phonetic.getText().toString();
                    String Meaning_s = Meaning.getText().toString();

                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int date = c.get(Calendar.DATE);
                    String TimeStamp = Integer.toString(year) + "/" + Integer.toString(month) + "/" + Integer.toString(date);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("English", English_s);
                    contentValues.put("Phonetic", Phonetic_s);
                    contentValues.put("Property", Property_s);
                    contentValues.put("Chinese", Chinese_s);
                    contentValues.put("Meaning", Meaning_s);
                    contentValues.put("PhotoPath", photoPath);
                    contentValues.put("TimeStamp", TimeStamp);
                    contentValues.put("Location", location);

                    SQLiteDatabase dbw = myDbHelper.getWritableDatabase();

                    //Create new mini idc

                    myDbHelper.insertWordData(contentValues, dbw, tablename);
                   // myDbHelper.insertWordData(contentValues, dbw, "MyWord");

                    SQLiteDatabase db = myDbHelper.getReadableDatabase();

                    Cursor cursor = db.query(tablename, null, null, null, null, null, null);
                    wordList.clear();
                    lvContacts = getListView(getList(cursor));
                    //send broadcast to main to update the board
                    Intent intent=new Intent();
                    intent.setAction("cn.abel.action.broadcast_addword");
                    sendBroadcast(intent);

                    setClear();
                }else{
                    showErrorDialog("Sorry, it is not a word.");

                    English.setText("");
                    SharedPreferences.Editor editor = sp.edit();

                    editor.putString("word", "");
                    editor.putString("property", Property_s);
                    editor.putString("chinese", Chinese_s);
                    editor.putString("phonetic",Phonetic_s);
                    editor.putString("Meaning",Meaning_s);
                    editor.putString("photoPath",photoPath);
                    editor.putString("location",  location);

                    editor.commit();
                }
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i("onDismiss","This dialog is on Dismiss");
                String English_s=English.getText().toString();
                String Property_s=Property.getText().toString();
                String Chinese_s=Chinese.getText().toString();
                String Phonetic_s=Phonetic.getText().toString();
                String Meaning_s=Meaning.getText().toString();

                SharedPreferences.Editor editor = sp.edit();

                editor.putString("word", English_s);
                editor.putString("property", Property_s);
                editor.putString("chinese", Chinese_s);
                editor.putString("phonetic",Phonetic_s);
                editor.putString("Meaning",Meaning_s);
                editor.putString("photoPath",photoPath);
                editor.putString("location",  location);

                editor.commit();
            }
        });

        builder.show();
    }

    public boolean isSQLInjection(String name){
        if(name.replaceAll("[a-z]", "").replaceAll("[A-Z]","").trim().length()!=0){
            return true;
        }else{
            return false;
        }
    }
//This function is written by me,but Jsoup is not mine

    /**
     *Jaoup
     *reference: https://jsoup.org/download
     */
    public void complete(final String word){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    ArrayList<String> list=new ArrayList<String>();
                    Document doc= Jsoup.connect("https://www.youdao.com/w/eng/"+word+"/#keyfrom=dict2.index").get();
                    Element Chinese=doc.select("#phrsListTab .trans-container ul li").get(0);
                    String result=Chinese.toString().replaceAll("<li>","").replaceAll("</li>","");
                    String[] r=result.split(" ");

                    Elements Phonetic=doc.select(".wordbook-js .baav .phonetic");
                    String phonetic=Phonetic.toString().replace("<span class=\"phonetic\">[","").replace("]</span>","");

                    Element Meaning=doc.select("#tEETrans li>.def").get(0);
                    String meaning=Meaning.toString().replace("<span class=\"def\">","").replace("</span>","");
                    System.out.println(meaning+" ");

                    setPhonetic(phonetic);
                    setProperty(r[0]);
                    setChinese(r[1]);
                    setMeaning(meaning);
                }catch (Exception e){
                    Log.e("complete",e.toString());
                    err=true;
                    showErr();
                    //showErrorDialog("No result,please check your word again.");
                }
            }
        }).start();

    }
    public void setChinese(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Chinese.setText(s);
            }
        });

    }

    public void setLocation(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                location=s;
            }
        });

    }

    public void setPhonetic(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Phonetic.setText(s);
            }
        });
    }

    public void setMeaning(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Meaning.setText(s);
            }
        });
    }

    public void setProperty(final String s){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Property.setText(s);
            }
        });

    }

    public void setClear() {
        Log.i("tag", "setClear");
        English.setText("");
        Property.setText("");
        Chinese.setText("");
        Phonetic.setText("");
        Meaning.setText("");
        photoPath="";
        action="";
    }
    private void clearSp(){
        sp = this.getSharedPreferences("data", 0);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("word", "");
        editor.putString("property", "");
        editor.putString("chinese", "");
        editor.putString("phonetic","");
        editor.putString("Meaning","");
        editor.putString("photoPath","");
        editor.putString("location",  "");

        editor.commit();
    }

    public void showErrorDialog(String str){
        builder=new AlertDialog.Builder(MiniDictDisplay.this);
        builder.setTitle(str);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                add.callOnClick();
            }
        });
        builder.show();
    }

    private void errDialog(String str){

                builder=new AlertDialog.Builder(this).setTitle(str).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }
    private void showErr(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.create().show();
            }
        });

    }

    private void showDeleteDialog(){
        builder=new AlertDialog.Builder(this).setTitle("Are you sure you want to delete this Dict?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myDbHelper.deleteMiniDict(id);//this id is the id in the mini dict table

                Toast.makeText(MiniDictDisplay.this,"Delete mini dict "+tablename+" from Mini Dict Menu",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(MiniDictDisplay.this,MiniDictMenuActivity.class);
                startActivity(intent);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    /**
     * Speech Recognizer
     * reference: Official document: https://www.xfyun.cn/doc/asr/voicedictation/Android-SDK.html
     */
    class MyRecognizerDialogListener implements RecognizerDialogListener{

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            String result=recognizerResult.getResultString();
            String text= JsonParser.parseIatResult(result);
            String sn=null;
            //read sn from json result
            try{
                JSONObject resultJson=new JSONObject(recognizerResult.getResultString());
                sn=resultJson.optString("sn");
            }catch (JSONException e){
                e.printStackTrace();
            }

            mIatResults.put(sn,text);
            StringBuffer resultBuffer=new StringBuffer();
            for(String key:mIatResults.keySet()){
                resultBuffer.append(mIatResults.get(key));
            }

            English.setText(trim(resultBuffer.toString()));
            English.setSelection(English.length());
        }

        public String trim(String result){
            String r=result.toLowerCase();
            if(r.length()!=0){
                r = r.substring(0,r.length() - 1);
            }
            return r;
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    }
    class MyInitListener implements InitListener{

        @Override
        public void onInit(int i) {
            if(i != ErrorCode.SUCCESS){
                showTip("Initialization failed");
            }
        }
    }

    //Shake animation

    /**
     * reference for shake: https://blog.csdn.net/LIXIAONA_1101/article/details/82667896
     */
    //I modify the animation to suit my case, but most of them come from blog
    @Override
    public void onSensorChanged(SensorEvent event){
        int type=event.sensor.getType();
        if(type==Sensor.TYPE_ACCELEROMETER){
            float[] values=event.values;
            float x=values[0];
            float y=values[1];
            float z=values[2];

            if((Math.abs(x)>17||Math.abs(y)>17||Math.abs(z)>17)&&!isShake){
                isShake=true;
                //vibrator.vibrate(VibrationEffect.createOneShot(100,200));
                Toast.makeText(MiniDictDisplay.this, "Disorganize words", Toast.LENGTH_LONG).show();

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Log.d(TAG, "onSensorChanged: Shake");

                            mHandler.obtainMessage(START_SHAKE).sendToTarget();
                            Thread.sleep(500);

                            mHandler.obtainMessage(AGAIN_SHAKE).sendToTarget();
                            Thread.sleep(500);
                            mHandler.obtainMessage(END_SHAKE).sendToTarget();
                            isShake=false;

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static class MyHandler extends Handler {
        //If the handler directly references the activity, it may cause memory leakage
        //https://blog.csdn.net/weiye__lee/article/details/79633017
        private WeakReference<MiniDictDisplay> mReference;
        private MiniDictDisplay mActivity;
        public MyHandler(MiniDictDisplay activity) {
            mReference = new WeakReference<MiniDictDisplay>(activity);
            if (mReference != null) {
                mActivity = mReference.get();
            }
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_SHAKE:
                    //This method requires the caller to hold the permission VIBRATE.
                    // mActivity.mVibrator.vibrate(300);

                    mActivity.mTopLine.setVisibility(View.GONE);
                    mActivity.mBottomLine.setVisibility(View.GONE);
                    mActivity.mTopLayout.setVisibility(View.GONE);
                    mActivity.mBottomLayout.setVisibility(View.GONE);
                    mActivity.startAnimation(false);//picture get together
                    break;
                case AGAIN_SHAKE:
                    mActivity.mTopLine.setVisibility(View.VISIBLE);
                    mActivity.mBottomLine.setVisibility(View.VISIBLE);
                    mActivity.mTopLayout.setVisibility(View.VISIBLE);
                    mActivity.mBottomLayout.setVisibility(View.VISIBLE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100,200));
                    break;
                case END_SHAKE:
                    mActivity.isShake = false;
                    mActivity.startAnimation(true);//picture separate
                    break;
            }
        }
    }
    private void startAnimation(boolean isBack) {
        int type = Animation.RELATIVE_TO_SELF;

        float topFromY;
        float topToY;
        float bottomFromY;
        float bottomToY;
        if (isBack) {
            topFromY = -2f;
            topToY = 0;

            bottomFromY =2f;
            bottomToY = 0;
        } else {
            topFromY = 0;
            topToY = -2f;

            bottomFromY = 0;
            bottomToY = 2f;
        }

        //UP animation
        TranslateAnimation topAnim = new TranslateAnimation(
                type, 0, type, 0, type, topToY, type, topFromY
        );
        topAnim.setDuration(500);
        //Stay at last frame when animation ends
        topAnim.setFillAfter(true);

        //BOTTOM animation
        TranslateAnimation bottomAnim = new TranslateAnimation(
                type, 0, type, 0, type, bottomToY, type,bottomFromY
        );
        bottomAnim.setDuration(500);
        bottomAnim.setFillAfter(true);

        if (isBack) bottomAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                reOrderListView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTopLine.setVisibility(View.GONE);
                mBottomLine.setVisibility(View.GONE);
                mTopLayout.setVisibility(View.GONE);
                mBottomLayout.setVisibility(View.GONE);
            }
        });
        mTopLayout.startAnimation(topAnim);
        mBottomLayout.startAnimation(bottomAnim);

    }


    }

