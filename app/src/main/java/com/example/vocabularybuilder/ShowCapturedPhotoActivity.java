package com.example.vocabularybuilder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.db.WordContract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Call system camera, store photo and clip photo: reference: https://blog.csdn.net/qq_38843185/article/details/80272308
 * Baidu map SDK official document: reference: http://lbsyun.baidu.com/index.php?title=android-locsdk
 */
public class ShowCapturedPhotoActivity extends AppCompatActivity {
    private MyDbOpenHelper myDbHelper;
    private ImageView  photoImageView;
    private String photoPath;
    private static final String PHOTO_FILE_NAME = "W_WDict";
    private static final String TAG = "ShowPhotoActivity";
    private TextView location_tv;
    private String location;
    private Button addNewWord;
    private Button download;
    private Button retake;
    private Button back;
    private static String dictname="";
    private EditText editText;
    protected SharedPreferences sp;
    private static ArrayList<String> dictList=new ArrayList<String>();
    private ArrayAdapter<String> arrayAdapter;
    private Spinner spinner;
    private LocationService locationService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_captured_photo);
        final Intent intent=getIntent();
        photoPath=intent.getStringExtra("photo_path");
        location=intent.getStringExtra("location");
        photoImageView=findViewById(R.id.capturedPhoto);
        if(photoPath!=""){
            Bitmap bm = BitmapFactory.decodeFile(photoPath);
            photoImageView.setImageBitmap(bm);
        }else{
            photoImageView.setImageResource(R.drawable.md_camera_icon);
        }
        myDbHelper = MyDbOpenHelper.getInstance(this);
        SQLiteDatabase db=myDbHelper.getReadableDatabase();
        Cursor dictCursor=db.query(WordContract.WordEntry.DICT_TABLE_NAME, null, null, null, null, null, null);
        dictList=getDictList(dictCursor);
        dictCursor.close();

        location_tv=findViewById(R.id.Location);
        addNewWord=findViewById(R.id.addNewWord);
        download=findViewById(R.id.download);
        retake=findViewById(R.id.retake);
        back=findViewById(R.id.back);

        addNewWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBmpToGallery(ShowCapturedPhotoActivity.this,photoPath,System.currentTimeMillis());
                showDictDialog();
                locationService.stop();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBmpToGallery(ShowCapturedPhotoActivity.this,photoPath,System.currentTimeMillis());
                locationService.stop();
            }
        });

        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(ShowCapturedPhotoActivity.this,MainActivity.class);
                intent1.setAction("TAKE PHOTO");
                //intent1.putExtra("position",1);
                startActivity(intent1);
                locationService.stop();
                ShowCapturedPhotoActivity.this.finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(ShowCapturedPhotoActivity.this,MainActivity.class);
                intent1.putExtra("position",0);
                startActivity(intent1);
                locationService.stop();
                ShowCapturedPhotoActivity.this.finish();
            }
        });

        locationService=new LocationService(this);
        locationService.registerListener(mListener);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());

        locationService.start();

    }

    public void showNewDictDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(ShowCapturedPhotoActivity.this);
        builder.setTitle("This dict name dose not exist, do you want to create a new mini dict?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent=new Intent(ShowCapturedPhotoActivity.this,MiniDictMenuActivity.class);
                intent.putExtra("dictname",editText.getText().toString());
                intent.putExtra("location",location);
                intent.putExtra("photoPath",photoPath);
                intent.setAction("CREATE NEW DICT");
                startActivity(intent);
                editText.setText("");
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editText.setText("");
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void showDictDialog(){
        View view=View.inflate(ShowCapturedPhotoActivity.this,R.layout.choose_mini_dict,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(ShowCapturedPhotoActivity.this);
        builder.setView(view);
        builder.setTitle("Please select a dictionary to add：");
        editText=view.findViewById(R.id.et);
        sp = this.getSharedPreferences("data", 0);
        editText.setText(sp.getString("dictname",""));

        spinner=view.findViewById(R.id.dictSpinner);
        arrayAdapter=new ArrayAdapter<String>(ShowCapturedPhotoActivity.this,android.R.layout.simple_spinner_item,dictList);
        spinner.setAdapter((arrayAdapter));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editText.setText(dictList.get(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

       builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               if(ifDictExist(editText.getText().toString())){
                   Intent intent=new Intent(ShowCapturedPhotoActivity.this,MiniDictDisplay.class);
                   intent.putExtra("tablename",editText.getText().toString().replaceAll(" ","_"));
                   intent.putExtra("photoPath",photoPath);
                  // Log.e("ShowCapturePhoto","Finish--------------------location: "+location);
                   intent.putExtra("location",location);
                   intent.setAction("CREATE NEW WORD");
                   startActivity(intent);
                   editText.setText("");
               }else{
                   showNewDictDialog();
               }

           }
       });
       builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
           @Override
           public void onDismiss(DialogInterface dialog) {
               SharedPreferences.Editor editor = sp.edit();
               editor.putString("dictname",dictname);
               editor.putString("photoPath",photoPath);
               editor.putString("location",location);
               editor.commit();
           }
       });
        builder.show();
    }

    public boolean ifDictExist(String name){
        for(int i=0;i<dictList.size();i++){
            if(dictList.get(i).equals(name)){
                return true;
            }
        }
        return false;
    }
   @Override
   public void onCreateContextMenu(ContextMenu menu, View source,ContextMenu.ContextMenuInfo menuInfo){
       super.onCreateContextMenu(menu,source,menuInfo);
       for(int i=0;i<dictList.size();i++){
           menu.add(0,Menu.FIRST+i,i,dictList.get(i));
       }

       Log.e(TAG,"on create context menu "+dictname);
   }
   @Override
   public boolean onContextItemSelected(MenuItem item){
        Log.e(TAG,"the selected item id is "+item.getItemId());
       for(int i=0;i<dictList.size();i++){
           if(item.getItemId()==Menu.FIRST+i){
               dictname=dictList.get(i);
               Log.e(TAG,"inner the selected dict name is "+dictname);
           }
       }
       Log.e(TAG,"outer the selected dict name is "+dictname);

       editText.setText(dictname);
       Toast.makeText(this,"You select mini dict"+dictname,Toast.LENGTH_LONG).show();
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


    /**
     * Baidu map SDK official document: reference: http://lbsyun.baidu.com/index.php?title=android-locsdk
     * Baidu map locationService: https://www.jianshu.com/p/63846d0d22cf
     */
    public BDAbstractLocationListener mListener=new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                StringBuilder sb = new StringBuilder();
                sb.append(location.getCountry()+" ");
                sb.append(location.getCityCode()+" ");
                sb.append(location.getCity()+" ");
                sb.append(location.getDistrict()+" ");
                sb.append(location.getStreet()+" ");
                sb.append(location.getAddrStr()+" ");
                sb.append(location.getLocationDescribe());
                logMsg(sb.toString());
                //setLocation(sb.toString());
            }
        }
    };
    public void logMsg(final String str) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    location_tv.post(new Runnable() {
                        @Override
                        public void run() {
                            // location=str;
                            location_tv.setText(str);
                            Log.e("ShowCapturedPhoto",location);
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *Call system camera, store photo and clip photo: reference: https://blog.csdn.net/qq_38843185/article/details/80272308
     */
    public static void saveBmpToGallery(Context context,String photoPath,long picName){
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

}
