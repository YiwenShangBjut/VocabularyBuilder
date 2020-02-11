package com.example.vocabularybuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.vocabularybuilder.card_view_pager.CardViewPager;
import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.db.WordContract;
import com.example.vocabularybuilder.primary_class.Dict;
import com.example.vocabularybuilder.primary_class.Word;

import java.util.ArrayList;
import static com.example.vocabularybuilder.db.WordContract.WordEntry.MY_WORD_TABLE_NAME;
import static com.example.vocabularybuilder.db.WordContract.WordEntry.DICT_TABLE_NAME;

public class LocationWordListActivity extends AppCompatActivity {
    private MyDbOpenHelper myDbHelper;

    private static ArrayList<Word> wordList;
    private static ArrayList<Word> locationList;
    private static ArrayList<String> dictList;
    private static Word clickedItem;
    private static ListView lvContacts;
    private static String locationDict_id;

    private float latitude;
    private float longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_word_list);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("You created those words at here");
        setSupportActionBar(toolbar);
        Intent intent=getIntent();
        latitude=intent.getFloatExtra("latitude", 0);
        longitude=intent.getFloatExtra("longitude", 0);

        myDbHelper = MyDbOpenHelper.getInstance(this);

        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        dictList=new ArrayList<String>();

        Cursor dictCursor=db.query(DICT_TABLE_NAME,null,null,null,null,null,null);
        dictList=getDictList(dictCursor);
        dictCursor.close();
        wordList=new ArrayList<Word>();
        locationList=new ArrayList<Word>();

        for(int i=0;i<dictList.size();i++){
            Cursor cursor=db.query(dictList.get(i),null,null,null,null,null,null);
            getList(cursor);
        }
        for(int i=0;i<wordList.size();i++){
            if(wordList.get(i).getLocation()!=null&&!wordList.get(i).getLocation().equals("")){
                Log.e("Location","Let's see the location of the word "+wordList.get(i).getLocation());
                float lo=Float.valueOf(wordList.get(i).getLocation().split(" ")[2]);
                float la=Float.valueOf(wordList.get(i).getLocation().split(" ")[1]);

                if(getDistance(longitude, latitude, lo,la)<500){
                    locationList.add(wordList.get(i));
                }
            }
        }

        final ListView[] lvContacts = {getListView(locationList)};

//        if(ifDictExist("Location_Dict")){
//            myDbHelper.deleteLocationDict();//delete from dict menu
//            myDbHelper.dropTable("Location_Dict");
//        }
//
//        ContentValues contentValues1 = new ContentValues();
//
//        contentValues1.put("Name","Location_Dict");
//        contentValues1.put("Comment","");
//        contentValues1.put("TimeStamp","");
//        SQLiteDatabase dbw = myDbHelper.getWritableDatabase();
//        myDbHelper.insertDictData(contentValues1,dbw);
//
//        myDbHelper.createWordTable(dbw,"Location_Dict");
//
//        for(int i=0;i<locationList.size();i++){
//            Word word=locationList.get(i);
//            ContentValues contentValues = new ContentValues();
//            contentValues.put("English",word.getEnglish());
//            contentValues.put("Phonetic", word.getPhonetic());
//            contentValues.put("Property", word.getProperty());
//            contentValues.put("Chinese", word.getChinese());
//            contentValues.put("Meaning", word.getMeaning());
//            contentValues.put("PhotoPath", word.getPhotoPath());
//            contentValues.put("TimeStamp", word.getTimeStamp());
//            contentValues.put("Location", word.getLocation());
//            myDbHelper.insertWordData(contentValues, dbw,"Location_Dict");
//        }


        lvContacts[0].setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                clickedItem=wordList.get(position);
//                //Intent intent=new Intent(MiniDictDisplay.this,DetailActivity.class);
//                Intent intent=new Intent(LocationWordListActivity.this, CardViewPager.class);
//                intent.putExtra("id",clickedItem.getId());
//                intent.putExtra("position",position);
//                intent.putExtra("tablename","Location_Dict");
//                startActivity(intent);
            }
        });
    }
//    public boolean ifDictExist(String name){
//        for(int i=0;i<dictList.size();i++){
//            if(dictList.get(i).equals(name)){
//                return true;
//            }
//        }
//        return false;
//    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dictList.clear();
        wordList.clear();
        locationList.clear();
    }

//reference: https://www.cnblogs.com/qdwyg2013/p/5594002.html
public static float getDistance(float longitude1, float latitude1, float longitude2, float latitude2) {
    float lat1 = (float) ((Math.PI / 180) * latitude1);
    float lat2 = (float) ((Math.PI / 180) * latitude2);

    float lon1 = (float) ((Math.PI / 180) * longitude1);
    float lon2 = (float) ((Math.PI / 180) * longitude2);

    //  r of earth
    float R = 6371;

    float d = (float) (Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R);

    return d * 1000;
}
    public ArrayList<String> getDictList(Cursor cursor){
        dictList.clear();
        while(cursor.moveToNext()){
            dictList.add(cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.DICT_COLUMN_NAME_NAME)));
        }
        cursor.close();
        return dictList;
    }
    public void getList(Cursor cursor){
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

            //  items.add(word);
            wordList.add(word);
        }
        cursor.close();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            Intent intent=new Intent(LocationWordListActivity.this,MainActivity.class);
            startActivity(intent);
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }
}
