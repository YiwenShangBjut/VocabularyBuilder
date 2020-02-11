package com.example.vocabularybuilder.card_view_pager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.example.vocabularybuilder.DetailActivity;
import com.example.vocabularybuilder.MiniDictDisplay;
import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.R;
import com.example.vocabularybuilder.primary_class.Word;
import com.example.vocabularybuilder.db.WordContract;

import java.util.ArrayList;

public class CardViewPager extends AppCompatActivity {

    private ViewPager cardViewPager;
    private CardPagerAdapter cardPagerAdapter;
    private ShadowTransformer shadowTransformer;
    private MyDbOpenHelper myDbHelper;
    private static ArrayList<Word> items;
    private String id;
    private String tablename;
    private int position;
    private String location="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        cardViewPager=(ViewPager)findViewById(R.id.viewPager);
        Intent intent=getIntent();
        id= intent.getStringExtra("id");
        tablename= intent.getStringExtra("tablename");
        position=intent.getIntExtra("position", 0);
        location=intent.getStringExtra("location");
        cardPagerAdapter=new CardPagerAdapter(this,tablename,location);

        myDbHelper = MyDbOpenHelper.getInstance(this);
        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        Cursor cursor = db.query(tablename, null, null, null, null, null, null);
        items=new ArrayList<Word>();
        items=getList(cursor);

        for(int i=0;i<items.size();i++){
            cardPagerAdapter.addCardItem(items.get(i));
        }

        shadowTransformer=new ShadowTransformer(cardViewPager,cardPagerAdapter);

        cardViewPager.setAdapter(cardPagerAdapter);
        cardViewPager.setPageTransformer(false,shadowTransformer);
        cardViewPager.setOffscreenPageLimit(3);
        cardViewPager.setCurrentItem(position);

        shadowTransformer.enableScaling(true);

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            Log.e("CardViewPager","on key down location: "+location);
            Intent intent=new Intent(CardViewPager.this, MiniDictDisplay.class);
            intent.putExtra("tablename",tablename);
            intent.putExtra("location", location);
            startActivity(intent);
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }

    public Context getContext(){
        return CardViewPager.this;
    }
    public String getTablename(){
        return tablename;
    }
    public ArrayList<Word> getList(Cursor cursor) {

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry._ID));
            String English = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_ENGLISH));
            String phonetic = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PHONETIC));
            String property = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PROPERTY));
            String Chinese = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_CHINESE));
            String Meaning = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_MEANING));
            String photoPath=cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PHOTOPATH));
            String location=cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_LOCATION));
            Word word = new Word();
            word.setId(id);
            word.setEnglish(English);
            word.setPhonetic(phonetic);
            word.setProperty(property);
            word.setChinese(Chinese);
            word.setMeaning(Meaning);
            word.setPhotoPath(photoPath);
            word.setLocation(location);

            items.add(word);
        }
        cursor.close();
        return items;
    }

}
