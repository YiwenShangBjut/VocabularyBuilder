package com.example.vocabularybuilder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.vocabularybuilder.primary_class.Word;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static android.provider.Contacts.SettingsColumns.KEY;
import static com.example.vocabularybuilder.db.WordContract.WordEntry.TABLE_NAME;
import static com.example.vocabularybuilder.db.WordContract.WordEntry.DICT_TABLE_NAME;
import static com.example.vocabularybuilder.db.WordContract.WordEntry.MY_WORD_TABLE_NAME;


public class MyDbOpenHelper extends SQLiteOpenHelper {

    private final static String TAG= MyDbOpenHelper.class.getSimpleName();

    private static final String SQL_DELETE_ENTRIES="DROP TABLE IF EXISTS "+ TABLE_NAME;
    private static final String SQL_DELETE_ENTRIES_DICT="DROP TABLE IF EXISTS "+ DICT_TABLE_NAME;
    public static final int DATABASE_VERSION=4;
    public static final String DATABASE_NAME="mydb.db";

    private static MyDbOpenHelper myDbOpenHelper;

    public static MyDbOpenHelper getInstance(Context context){
        if(null==myDbOpenHelper){
            synchronized (MyDbOpenHelper.class){
                if(null==myDbOpenHelper){
                    myDbOpenHelper=new MyDbOpenHelper(context, DATABASE_NAME,null,DATABASE_VERSION);
                }
            }
        }
        return myDbOpenHelper;
    }

    private MyDbOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, DATABASE_NAME,factory,version);
    }


    public void createWordTable(SQLiteDatabase database,String tablename){
        try{
            database.execSQL(   "CREATE TABLE "+ tablename+"("+
                    WordContract.WordEntry._ID+" INTEGER PRIMARY KEY, "+
                    WordContract.WordEntry.COLUMN_NAME_ENGLISH+" VARCHAR(30), "+
                    WordContract.WordEntry.COLUMN_NAME_PHONETIC+" VARCHAR(30), "+
                    WordContract.WordEntry.COLUMN_NAME_PROPERTY+" VARCHAR(30), "+
                    WordContract.WordEntry.COLUMN_NAME_CHINESE+" VARCHAR(30), "+
                    WordContract.WordEntry.COLUMN_NAME_MEANING+" VARCHAR(30), "+
                    WordContract.WordEntry.COLUMN_NAME_PHOTOPATH+" VARCHAR(100), "+
                    WordContract.WordEntry.COLUMN_NAME_TIMESTAMP+" VARCHAR(100), "+
                    WordContract.WordEntry.COLUMN_NAME_LOCATION+" VARCHAR(100) "+

                    ")" );
            Log.i("Success","Create new mini dict success!");
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG, TAG+"Error in create table: "+e.toString());
        }
    }

    private void createDictTable(SQLiteDatabase database){
        try{
            database.execSQL(   "CREATE TABLE "+ WordContract.WordEntry.DICT_TABLE_NAME+"("+
                    WordContract.WordEntry.DICT_ID+" INTEGER PRIMARY KEY, "+
                    WordContract.WordEntry.DICT_COLUMN_NAME_NAME+" VARCHAR(5), "+
                    WordContract.WordEntry.DICT_COLUMN_NAME_COMMENT+" VARCHAR(30), "+
                    WordContract.WordEntry.DICT_COLUMN_NAME_TIMESTAMP+" VARCHAR(30),"+
                    WordContract.WordEntry.USER_COLUMN_NAME_USERID+" VARCHAR(30)"+")"
            );
            Log.i(TAG, TAG+"create dict table");
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG, TAG+"Error in create dict table: "+e.toString());
        }
    }


    public void modifyWordPhoto(SQLiteDatabase database, String id,String tableName,ContentValues contentValues){
        try{
            database.update(tableName,contentValues, "_id=?",new String[]{""+id});

            Log.i(TAG, TAG+"modifyWordPhoto");
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG, TAG+"Error in modifyWordPhoto: "+e.toString());
        }
    }

    public void dropTable(String tablename){
        SQLiteDatabase database=getWritableDatabase();
        try{
            database.execSQL("DROP TABLE IF EXISTS "+ tablename);
        }catch(Exception e){
            e.printStackTrace();
            Log.d(TAG,"Error in drop table: "+e.toString());
        }finally{
            if(null!=database){
                database.close();
            }
        }
    }

    public void insertData(List<Word> words, SQLiteDatabase database){

        try{
            for(Word word: words){
                ContentValues contentValues=new ContentValues();
                contentValues.clear();
                contentValues.put("English",word.getEnglish());
                contentValues.put("Phonetic",word.getPhonetic());
                contentValues.put("Property",word.getProperty());
                contentValues.put("Chinese",word.getChinese());
                contentValues.put("Meaning",word.getMeaning());
                contentValues.put("PhotoPath",word.getPhotoPath());
                contentValues.put("TimeStamp",word.getPhotoPath());
                contentValues.put("Location",word.getLocation());

                database.insert(TABLE_NAME,"_id",contentValues);
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"Error on insert data: "+e.toString());
        }finally{
            if(null!=database){
                database.close();
            }
        }
    }
    public void insertDataToLocationDict(List<Word> words, SQLiteDatabase database){

        try{
            for(Word word: words){
                ContentValues contentValues=new ContentValues();
                contentValues.clear();
                contentValues.put("English",word.getEnglish());
                contentValues.put("Phonetic",word.getPhonetic());
                contentValues.put("Property",word.getProperty());
                contentValues.put("Chinese",word.getChinese());
                contentValues.put("Meaning",word.getMeaning());
                contentValues.put("PhotoPath",word.getPhotoPath());
                contentValues.put("TimeStamp",word.getPhotoPath());
                contentValues.put("Location",word.getLocation());

                database.insert("Location_Dict","_id",contentValues);
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"Error on insert data: "+e.toString());
        }finally{
            if(null!=database){
                database.close();
            }
        }
    }

    public void insertDictData(ContentValues contentValues,SQLiteDatabase database){
       //SQLiteDatabase database=getWritableDatabase();
        try{
            database.insert(DICT_TABLE_NAME,"_id",contentValues);

        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"Error on insert mini dict: "+e.toString());
        }finally{
            if(null!=database){

            }
        }
    }
    public void insertWordData(ContentValues contentValues,SQLiteDatabase database,String tablename){
        //SQLiteDatabase database=getWritableDatabase();
        try{
            database.insert(tablename,"_id",contentValues);
        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"Error on insert data: "+e.toString());
        }finally{
            if(null!=database){
                database.close();
            }
        }
    }


    public void deleteWordData(String id,String tablename){
        SQLiteDatabase database=getWritableDatabase();
        try{
            String deleteSql="delete from "+tablename+" where _id=?";
            database.execSQL(deleteSql,new String[]{id});

        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"Error on delete data: "+e.toString());
        }finally{
            if(null!=database){
                database.close();
            }
        }
    }


    public void deleteMiniDict(String id){
        SQLiteDatabase database=getWritableDatabase();
        try{
            String deleteSql="delete from "+DICT_TABLE_NAME+" where _id=?";
            database.execSQL(deleteSql,new String[]{id});

        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"Error on delete data: "+e.toString());
        }finally{
            if(null!=database){
                database.close();
            }
        }
    }

    public void deleteLocationDict(){
        SQLiteDatabase database=getWritableDatabase();
        try{
            String deleteSql="delete from "+DICT_TABLE_NAME+" where Name=?";
            database.execSQL(deleteSql,new String[]{"Location_Dict"});


        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG,"Error on delete data: "+e.toString());
        }finally{
            if(null!=database){
                database.close();
            }
        }
    }


    @Override
    public void onCreate(SQLiteDatabase database){
       initDb(database);
    }

    //If the version of the database has been update, them use this method.
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
        database.execSQL(SQL_DELETE_ENTRIES);
        database.execSQL(SQL_DELETE_ENTRIES_DICT);
        Log.e(TAG,"db on upgread---------------------------------");
        onCreate(database);
    }

    private void initDb(SQLiteDatabase database){
        createWordTable(database,TABLE_NAME);
        createWordTable(database,MY_WORD_TABLE_NAME);
        createDictTable(database);
    }

}
/*
reference:
https://www.cnblogs.com/android-deli/p/10147045.html*/
