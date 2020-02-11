package com.example.vocabularybuilder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vocabularybuilder.card_view_pager.CardViewPager;
import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.db.WordContract;
import com.example.vocabularybuilder.primary_class.Word;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    private MyDbOpenHelper myDbHelper;
    private String id;
    private String tablename;
    private int position;
    private String location;
    private  TextView English;
    private  TextView Phonetic;
    private  TextView Property;
    private  TextView Chinese;
    private  TextView Meaning;
    private ImageView photo;
    private TextView Location;
    private  Cursor cursor;
    private static ArrayList<String> dictList=new ArrayList<String>();
    private static Word word=new Word();

    private static final String TAG = "DetailActivity";
    private static final int REQUEST_IMAGE_GET = 0;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;

    private static final String IMAGE_FILE_NAME = "W_WDict_photo.jpg";
    private AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int i=0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        myDbHelper=MyDbOpenHelper.getInstance(this);
        Intent intent=getIntent();
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(tablename);
        setSupportActionBar(toolbar);
        position=intent.getIntExtra("position", 0);
        id= intent.getStringExtra("id");
        tablename= intent.getStringExtra("tablename");
        location= intent.getStringExtra("location");
        Log.i("info", "detail on create, id is "+id);
        SQLiteDatabase db=myDbHelper.getReadableDatabase();

        String[] selectionArgs=new String[]{id};
        if(tablename==null){
            cursor=db.query(WordContract.WordEntry.TABLE_NAME,null,"_id=?",selectionArgs,null,null,null);
        }else{
            cursor=db.query(tablename,null,"_id=?",selectionArgs,null,null,null);
        }

        while (cursor.moveToNext()) {

           English=(TextView)findViewById(R.id.English);
           Phonetic=(TextView)findViewById(R.id.Phonetic);
           Property=(TextView)findViewById(R.id.Property);
            Chinese=(TextView)findViewById(R.id.Chinese);
            Meaning=(TextView)findViewById(R.id.Meaning);
            photo=(ImageView)findViewById(R.id.Photo);
            Location=(TextView)findViewById(R.id.Location);

            id=cursor.getString(cursor.getColumnIndex(WordContract.WordEntry._ID));
            English.setText(cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_ENGLISH)));
            Phonetic.setText(cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PHONETIC)));
            Property.setText(cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PROPERTY)));
            Chinese.setText(cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_CHINESE)));
            Meaning.setText(cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_MEANING)));
            String photoPath=cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_PHOTOPATH));
            String location1=cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.COLUMN_NAME_LOCATION));
            Location.setText(location1);

            if(photoPath!=""||photoPath!=null){
                Bitmap bm = BitmapFactory.decodeFile(photoPath);
                photo.setImageBitmap(bm);
                photo.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        checkFilePermission();
                        return true;
                    }
                });

            }else {
                photo.setMaxHeight(50);
                photo.setMaxWidth(50);
                photo.setImageResource(R.drawable.md_add);
                photo.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        checkFilePermission();
                        return true;
                    }
                });
            }
            word.setId(String.valueOf(i));
            word.setEnglish(English.getText().toString());
            word.setPhonetic( Phonetic.getText().toString());
            word.setProperty(Property.getText().toString());
            word.setChinese(Chinese.getText().toString());
            word.setMeaning(Meaning.getText().toString());
            word.setPhotoPath(photoPath);
            word.setLocation(location1);
            i++;
        }
        cursor.close();
//get dict name string list
        Cursor dictCursor=db.query(WordContract.WordEntry.DICT_TABLE_NAME, null, null, null, null, null, null);
        dictList=getDictList(dictCursor);
        dictCursor.close();

    }

    public void checkFilePermission(){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                Toast.makeText(DetailActivity.this, "Don't find album", Toast.LENGTH_SHORT).show();
            }
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
                    File file=setPicInFile(cropUri);
                    Bitmap bitmap = null;
                    try {
                       bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropUri));
                       photo.setImageBitmap(bitmap);

                        String photoPath=file.getPath();
                        saveBmpToGallery(DetailActivity.this, photoPath, System.currentTimeMillis());
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("PhotoPath",photoPath);
                        SQLiteDatabase dbw = myDbHelper.getWritableDatabase();
                        myDbHelper.modifyWordPhoto(dbw, id,tablename,contentValues);

                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }

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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {

            if(tablename==null){
                Intent intent=new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                Log.e("Detail","on key down location: "+location);
                Intent intent=new Intent(DetailActivity.this, CardViewPager.class);
                intent.putExtra("id",id);
                intent.putExtra("position",position);
                intent.putExtra("tablename",tablename);
                intent.putExtra("location", location);
                startActivity(intent);
                finish();
            }


        }
        return super.onKeyDown(keyCode, event);
    }

    public static void saveBmpToGallery(Context context, String photoPath, long picName){
        Log.i(TAG,"enter save to gallery");
        Bitmap bm = BitmapFactory.decodeFile(photoPath);
        String galleryPath= Environment.getExternalStorageDirectory()
                +File.separator+Environment.DIRECTORY_DCIM
                +File.separator;

        File file=null;
        String fileName=null;
        FileOutputStream outputStream=null;
        try{
            File dirFile=new File(galleryPath,"W_WDict");//create new file under album

            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.d(TAG, "in setPicToView->文件夹创建失败");
                } else {
                    Log.d(TAG, "in setPicToView->文件夹创建成功");
                }
            }
            file=new File(dirFile,picName+".jpg");
            fileName=file.toString();
            outputStream=new FileOutputStream(fileName);
            if(null!=outputStream){
                bm.compress(Bitmap.CompressFormat.JPEG,90,outputStream);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(outputStream!=null){
                    outputStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        MediaStore.Images.Media.insertImage(context.getContentResolver(),bm,fileName,null);
        Intent intent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri=Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);

        Toast.makeText(context,"Download success",Toast.LENGTH_LONG).show();
    }

    public File setPicInFile(Uri uri)  {
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
                return file;
            }

        }
        return null;
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
    public boolean onCreateOptionsMenu(Menu menu){
        SubMenu subMenu=menu.addSubMenu(0,1,0,"Add to Mini Dict");

        for(int i=0;i<dictList.size();i++){
            subMenu.add(0,1+Menu.FIRST+i,i,dictList.get(i));
        }
        menu.add(0,dictList.size()+2,2,"Delete");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        String dictname=null;
        if(item.getItemId()==dictList.size()+2){
           showDeleteDialog();

        }

        for(int i=0;i<dictList.size();i++){
            if(item.getItemId()==1+Menu.FIRST+i){
                dictname=dictList.get(i);

                ContentValues contentValues = new ContentValues();
                contentValues.put("English",word.getEnglish());
                contentValues.put("Phonetic",word.getPhonetic());
                contentValues.put("Property",word.getProperty());
                contentValues.put("Chinese",word.getChinese());
                contentValues.put("Meaning",word.getMeaning());
                contentValues.put("PhotoPath",word.getPhotoPath());
                contentValues.put("Location",word.getLocation());
                SQLiteDatabase dbw = myDbHelper.getWritableDatabase();
                myDbHelper.insertWordData(contentValues,dbw,dictList.get(i).replaceAll(" ","_"));
            }
        }
        if(dictname!=null){
            Toast.makeText(this,"Add word "+word.getEnglish()+" in to "+dictname,Toast.LENGTH_LONG).show();
        }

        Log.i("addToList","Add word "+word.getEnglish()+" in to mini dict "+dictname);
        return true;
    }

    public ArrayList<String> getDictList(Cursor cursor){
        dictList.clear();
        while(cursor.moveToNext()){
            dictList.add(cursor.getString(cursor.getColumnIndex(WordContract.WordEntry.DICT_COLUMN_NAME_NAME)));
        }
        cursor.close();
        return dictList;
    }

    private void showDeleteDialog(){
            builder=new AlertDialog.Builder(this).setTitle("Are you sure you want to delete this word?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            myDbHelper.deleteWordData(id,tablename);//this id is the id in the mini dict table
             Toast.makeText(DetailActivity.this,"Delete word "+word.getEnglish()+" from "+tablename,Toast.LENGTH_LONG).show();
            Intent intent=new Intent(DetailActivity.this,MiniDictDisplay.class);
            intent.putExtra("tablename", tablename);
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

}
