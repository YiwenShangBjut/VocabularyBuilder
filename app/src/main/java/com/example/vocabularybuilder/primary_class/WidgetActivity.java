package com.example.vocabularybuilder.primary_class;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.vocabularybuilder.LocationService;
import com.example.vocabularybuilder.MainActivity;
import com.example.vocabularybuilder.R;
import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.db.WordContract;

import java.util.ArrayList;

public class WidgetActivity extends AppWidgetProvider {
    private static final String TAG = "WidgetActivity";
    public static final String CHANGE_MODULE="android.appwidget.action.CHANGE_MODULE";
    public static final String SET_LOCATION="android.appwidget.action.SET_LOCATION";
    private static int i=0;
    private static Word word;
    MyDbOpenHelper myDbHelper;


    private static ArrayList<Word> wordList =new ArrayList<Word>();
    @Override
    public void onReceive(Context context, Intent intent){
        Log.e("WidgetActivity","on Receive");
        String action=intent.getAction();

        if(CHANGE_MODULE.equals(action)) {
            Log.e("WidgetActivity","CHANGE_MODULE");
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.activity_widget);
            i=getRandom();
            Log.e("id","the id of next is "+i);
            setI(i);

            myDbHelper=MyDbOpenHelper.getInstance(context);
            SQLiteDatabase db = myDbHelper.getReadableDatabase();

            Cursor cursor = db.query(WordContract.WordEntry.TABLE_NAME, null, null, null, null, null, null);

            getList(cursor,views);

            AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);

            ComponentName componentName=new ComponentName(context,WidgetActivity.class);
            appWidgetManager.updateAppWidget(componentName,views);
        }
        super.onReceive(context,intent);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.e("WidgetActivity","on Update");
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        myDbHelper=MyDbOpenHelper.getInstance(context);
        SQLiteDatabase db = myDbHelper.getReadableDatabase();

        Cursor cursor = db.query(WordContract.WordEntry.TABLE_NAME, null, null, null, null, null, null);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.activity_widget);
        getList(cursor,views);

        //Location
        Intent skipIntent1=new Intent("cn.abel.action.broadcast_location");
        skipIntent1.putExtra("switchLocation", "switch");
        //PendingIntent pi1=PendingIntent.getActivity(context,200,skipIntent1,PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pi1=PendingIntent.getBroadcast(context,200,skipIntent1, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.Location,pi1);

        //detail
        Intent skipIntent=new Intent(context, MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(context,200,skipIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.Detail,pi);


        //next
        Intent nextIntent=new Intent();
        nextIntent.setAction(CHANGE_MODULE);
        nextIntent.setClass(context,WidgetActivity.class);
        PendingIntent pi2=PendingIntent.getBroadcast(context,0,nextIntent,0);
        views.setOnClickPendingIntent(R.id.Next,pi2);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
    @Override
    public void onEnabled(Context context) {
        i=0;
    }

    @Override
    public void onDisabled(Context context) {
        i=0;
    }
    public int getRandom(){
        int max=wordList.size();
        int min=0;
        int n=(int)(Math.random()*(max-min)+min);
        return n;
    }

    public Word getWord(int i){
        return wordList.get(i);
    }

    public void setI(int n){
        i=n;
    }
    public int getI(){
        return i;
    }

    public void getList(Cursor cursor,RemoteViews views){

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

            //  items.add(word);
            wordList.add(word);
        }
        cursor.close();


        views.setTextViewText(R.id.Word, wordList.get(i).getEnglish());
        views.setTextViewText(R.id.Chinese, wordList.get(i).getChinese());

    }
}
