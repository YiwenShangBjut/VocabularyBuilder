package com.example.vocabularybuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.db.WordContract;
import com.example.vocabularybuilder.primary_class.Dict;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class MiniDictMenuActivity extends AppCompatActivity {
    FloatingActionButton add;
    EditText name;
    EditText comment;
    private static ListView lvContacts;
    private static ArrayList<Dict> dictList=new ArrayList<Dict>();
    Dict clickedItem;
    private String dictname="";
    private String action="";
    private String photoPath="";
    private String location="";
    protected SharedPreferences sp;
    private MyDbOpenHelper myDbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_dict_menu);
        Intent intent=getIntent();
        dictname=intent.getStringExtra("dictname");
        photoPath=intent.getStringExtra("photoPath");
        location=intent.getStringExtra("location");
        action=intent.getAction();
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Mini Dict Menu");
        setSupportActionBar(toolbar);


        add=findViewById(R.id.addButton);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
        Log.e("MiniDictMenuActivity","action is"+action);
        if(action=="CREATE NEW DICT"){
            showAddDialog();
        }
        myDbHelper = MyDbOpenHelper.getInstance(this);

        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        Cursor cursor = db.query(WordContract.WordEntry.DICT_TABLE_NAME, null, null, null, null, null, null);

        final ListView lvContacts=getFromDb(cursor);

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // position: The position of the view in the adapter.
            // id: The row id of the item that was clicked.
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0;i<dictList.size();i++){
                    Log.i("dictlist",i+" "+dictList.get(i).getId()+dictList.get(i).getName());
                }
                clickedItem = dictList.get(position);
                Log.i("info", "onItemClick, position is " + position);
                Log.i("clickeditem",clickedItem.getId()+clickedItem.getName());

                Intent intent = new Intent(MiniDictMenuActivity.this,MiniDictDisplay.class);
                Log.i("info", "onItemClick, id is " + clickedItem.getId());
                Log.e("MiniDictMenuActivity","onCreate---------------location: "+location);
                intent.putExtra("id", clickedItem.getId());
                intent.putExtra("location", location);
                intent.putExtra("tablename", clickedItem.getName().replaceAll(" ","_"));
                startActivity(intent);
            }
        });
    }
    public ListView getFromDb(Cursor cursor){
        List items = new ArrayList<Dict>();
        while (cursor.moveToNext()) {

            String id = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry._ID));
            String Name = cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.DICT_COLUMN_NAME_NAME));
            String Comment = cursor.getString(cursor.getColumnIndex( WordContract.WordEntry.DICT_COLUMN_NAME_COMMENT));
            String TimeStamp= cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.DICT_COLUMN_NAME_TIMESTAMP));
            if(!Name.equals("Location_Dict")){
                items.add(new Dict(id, Name,Comment,TimeStamp));
                dictList.add(new Dict(id, Name,Comment,TimeStamp));
            }

        }
        cursor.close();

        lvContacts = (ListView) findViewById(R.id.dict_contacts);

        ArrayAdapter<Dict> adapter = new ArrayAdapter<Dict>(this, R.layout.dict_list_item, items) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

               Dict dict = getItem(position);

                LayoutInflater layoutInflater = getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.dict_list_item, parent, false);

                TextView name = (TextView) view.findViewById(R.id.dict_name);
                TextView time = (TextView) view.findViewById(R.id.time);

                name.setText(dict.getName());
                time.setText(dict.getTimeStamp());
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
            Intent intent=new Intent(MiniDictMenuActivity.this,MainActivity.class);
            startActivity(intent);
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }

    public void showAddDialog(){
        View view=View.inflate(MiniDictMenuActivity.this,R.layout.add_mini_dict,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(MiniDictMenuActivity.this);
        builder.setView(view);

        builder.setTitle("Create your mini dict");

        name=view.findViewById(R.id.name);
        comment=view.findViewById(R.id.comment);
     //   create=view.findViewById(R.id.create);

        sp = this.getSharedPreferences("data", 0);
        name.setText(sp.getString("n",""));
        comment.setText(sp.getString("c",""));
        if(dictname!=""){
            name.setText(dictname);
        }

        myDbHelper = MyDbOpenHelper.getInstance(this);

        builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String Name=name.getText().toString();
                String Comment=comment.getText().toString();
                if(ifDictExist(Name))
                {
                    showErrorDialog("This dic name already exists.");
                    name.setText("");
                    //save comment
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("c", Comment);
                    editor.commit();
                }else if( ifSQLInjection(Name)){
                    showErrorDialog("Name cannot contain special characters");
                    name.setText("");
                    //save comment
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("c", Comment);
                    editor.commit();
                }else if(ifSpecial(Name)){
                    showErrorDialog("Please change a name");
                    name.setText("");
                    //save comment
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("c", Comment);
                    editor.commit();
                } else{

                Calendar c=Calendar.getInstance();
                int year=c.get(Calendar.YEAR);
                int month=c.get(Calendar.MONTH);
                int date=c.get(Calendar.DATE);
                String TimeStamp=Integer.toString(year)+"/"+Integer.toString(month)+"/"+Integer.toString(date);

                ContentValues contentValues = new ContentValues();
                String Name_=Name.replaceAll(" ","_");
               // Name_=Name_+"_dict";
                contentValues.put("Name",Name_);
                contentValues.put("Comment",Comment);
                contentValues.put("TimeStamp",TimeStamp);
                SQLiteDatabase dbw = myDbHelper.getWritableDatabase();
                //Create new mini idc

                myDbHelper.insertDictData(contentValues,dbw);
                myDbHelper.createWordTable(dbw,Name_);

                SQLiteDatabase db = myDbHelper.getReadableDatabase();

                Cursor cursor = db.query(WordContract.WordEntry.DICT_TABLE_NAME, null, null, null, null, null, null);
                dictList.clear();
                lvContacts=getFromDb(cursor);

                if(action=="CREATE NEW DICT"){
                    Intent intent=new Intent(MiniDictMenuActivity.this,MiniDictDisplay.class);
                    intent.putExtra("tablename",name.getText().toString().replaceAll(" ","_"));
                    intent.putExtra("photoPath",photoPath);
                    intent.setAction("CREATE NEW WORD");
                    startActivity(intent);
                }

                setClear();
            }}
            });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i("onDismiss","This dialog is on Dismiss");
                String Name=name.getText().toString();
                String Comment=comment.getText().toString();

                SharedPreferences.Editor editor = sp.edit();

                editor.putString("n", Name);
                editor.putString("c", Comment);

                editor.commit();
            }
        });

        builder.show();
        }
        public boolean ifSpecial(String name){
            if(name.contains("null")||name.contains("is")||name.contains("from")||name.contains("and")||name.contains("where")||name.contains("new")||name.contains("table")||name.contains("group")||name.contains("which")||name.contains("delete")||name.contains("create")){
                return true;
            }else{
                return false;
            }
        }

    public boolean ifDictExist(String name){
        for(int i=0;i<dictList.size();i++){
            if(dictList.get(i).getName().equals(name)){
                return true;
            }
        }
        return false;
    }
    //https://www.cnblogs.com/interdrp/p/5586587.html
    public boolean ifSQLInjection(String name){
      if(name.replaceAll("[a-z]*[A-Z]*\\d*-*_*\\s*", "" ).length()!=0){
          return true;
      }else{
          return false;
      }
    }

    public void setClear() {
        Log.i("tag", "setClear");
       name.setText("");
       comment.setText("");
    }
    public void showErrorDialog(String str){
        final AlertDialog.Builder builder=new AlertDialog.Builder(MiniDictMenuActivity.this);
        builder.setTitle(str);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                add.callOnClick();
            }
        });
        builder.show();
    }


}
//https://blog.csdn.net/qq_42618969/article/details/81364007
//learn how to get timestamp and convert it into date