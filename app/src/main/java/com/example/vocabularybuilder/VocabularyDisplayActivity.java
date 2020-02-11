package com.example.vocabularybuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.db.WordContract;
import com.example.vocabularybuilder.primary_class.Word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class VocabularyDisplayActivity extends AppCompatActivity {

    private static ListView lvContacts;
    private MyDbOpenHelper myDbHelper;
    private static ArrayList<Word> items;
    private static Word clickedItem;
    protected SharedPreferences sp;
    private static ArrayList<String> dictList=new ArrayList<String>();
    private static Word word;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_display);

        InputStream input=getResources().openRawResource(R.raw.word_file);

        myDbHelper = MyDbOpenHelper.getInstance(this);
        SQLiteDatabase dbw = myDbHelper.getWritableDatabase();
        initDb(dbw,input);

       SQLiteDatabase db = myDbHelper.getReadableDatabase();

       Cursor cursor = db.query(WordContract.WordEntry.TABLE_NAME, null, null, null, null, null, null);
        items=new ArrayList<Word>();
        ListView lvContacts =  getListView(cursor);

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickedItem=items.get(position);
                Intent intent=new Intent(VocabularyDisplayActivity.this,DetailActivity.class);
                intent.putExtra("id",clickedItem.getId());
                startActivity(intent);
            }
        });

        Cursor dictCursor=db.query(WordContract.WordEntry.DICT_TABLE_NAME, null, null, null, null, null, null);
        dictList=getDictList(dictCursor);
    }
    public ArrayList<String> getDictList(Cursor cursor){
        dictList.clear();
        while(cursor.moveToNext()){
            dictList.add(cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.DICT_COLUMN_NAME_NAME)));
        }
        cursor.close();
        return dictList;
    }
    private void initDb(SQLiteDatabase db,InputStream input){
        myDbHelper.insertData(ReadData(input),db);
    }



    public ListView getListView(Cursor cursor) {

        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry._ID));
            String English = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_ENGLISH));
            String phonetic = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PHONETIC));
            String property = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PROPERTY));
            String Chinese = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_CHINESE));
            String Meaning = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_MEANING));
            Word word = new Word();
            word.setId(id);
            word.setEnglish(English);
            word.setPhonetic(phonetic);
            word.setProperty(property);
            word.setChinese(Chinese);
            word.setMeaning(Meaning);

            items.add(word);
        }
        cursor.close();

        lvContacts = (ListView) findViewById(R.id.contacts);

        ArrayAdapter<Word> adapter = new ArrayAdapter<Word>(this, R.layout.list_item, items) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                word=getItem(position);

                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.list_item, parent, false);
                TextView English=(TextView)view.findViewById(R.id.English);
                TextView property=(TextView)view.findViewById(R.id.Property);
                TextView Chinese=(TextView)view.findViewById(R.id.Chinese);

                English.setText(word.getEnglish());
                property.setText(word.getProperty());
                Chinese.setText(word.getChinese());

               // TextView add=(TextView)view.findViewById(R.id.addToDict);
              //  registerForContextMenu(add);

                return view;
            }
        };
        lvContacts.setAdapter(adapter);

        return lvContacts;
    }


@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        for(int i=0;i<dictList.size();i++){
            menu.add(0,Menu.FIRST+i,i,dictList.get(i));
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        String dictname=null;
        for(int i=0;i<dictList.size();i++){
            if(item.getItemId()==Menu.FIRST+i){
                dictname=dictList.get(i);

                ContentValues contentValues = new ContentValues();
                contentValues.put("English",clickedItem.getEnglish());
                contentValues.put("Phonetic",clickedItem.getPhonetic());
                contentValues.put("Property",clickedItem.getProperty());
                contentValues.put("Chinese",clickedItem.getChinese());
                contentValues.put("Meaning",clickedItem.getMeaning());
                SQLiteDatabase dbw = myDbHelper.getWritableDatabase();
                myDbHelper.insertWordData(contentValues,dbw,dictList.get(i));
            }
        }
        Toast.makeText(this,"Add word "+clickedItem.getEnglish()+" in to mini dict "+dictname,Toast.LENGTH_LONG).show();
        Log.i("addToList","Add word "+clickedItem.getEnglish()+" in to mini dict "+dictname);
        return super.onContextItemSelected(item);
    }


    public static List<Word> ReadData( InputStream input) {

        BufferedReader br = null;
        ArrayList<Word> list=new ArrayList<Word>();
        int index=0;

        try {
            InputStreamReader inputStreamReader=new InputStreamReader(input,"UTF-8");

           // System.out.println("Reading the file using readLine() method:");
            br=new BufferedReader(inputStreamReader);
            String contentLine = br.readLine();
            while (contentLine != null) {
                // System.out.println(contentLine);
                if(findData(contentLine,index)!=null) {
                    list.add(findData(contentLine,index));
                    contentLine = br.readLine();
                    index++;
                }else {
                    contentLine = br.readLine();
                }

            }
            br.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return list;
    }

    public static Word findData(String str,int index) {
        Word word=new Word();
        String []strings=str.split("  ");

        if(strings.length==6) {
            // System.out.println(strings[0]);
            //System.out.println(strings[2]);
            String indexStr=Integer.toString(index);
            word.setId(indexStr);
            word.setEnglish(strings[1]);
            word.setPhonetic(strings[2]);
            word.setProperty(strings[3]);
            word.setChinese(strings[4]);
            word.setMeaning(strings[5]);
            return word;
        }
        else {
            return null;
        }
    }

//
//
//    @Override
//    protected void onPause(){
//        super.onPause();
//        ArrayList<Word> pirmaryList=items;
//
//        SharedPreferences.Editor editor=sp.edit();
//
//        editor.putStringSet()
//        editor.putString("m_c",module_code);
//        editor.putString("m_n",module_name);
//        editor.putString("type",checkedType);
//        editor.putString("day",checkedDay);
//        editor.putString("s_t",sTime);
//        editor.putString("e_t",eTime);
//        editor.putString("location",location);
//        editor.putString("comments",comments);
//        editor.commit();
//    }
//
//    @Override
//    protected void onResume(){
//        super.onResume();
//        Module_Code.setText(sp.getString("m_c",""));
//        Module_Name.setText(sp.getString("m_n",""));
//        if(sp.getString("type","").equals("Lecture")){
//            checkedType="Lecture";
//            radioGroup.check(Lecture.getId());
//        }else{
//            checkedType="Practice";
//            radioGroup.check(Practice.getId());
//        }
//        checkedDay=sp.getString("day","");
//        Day.setText(checkedDay);
//
//        StartTime.setText(sp.getString("s_t",""));
//        EndTime.setText(sp.getString("e_t",""));
//        Location.setText(sp.getString("location",""));
//        Comments.setText(sp.getString("comments",""));
//    }

}