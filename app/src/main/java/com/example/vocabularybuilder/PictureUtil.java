package com.example.vocabularybuilder;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Call system camera, store photo and clip photo: reference: https://blog.csdn.net/qq_38843185/article/details/80272308
 */
public class PictureUtil {
    private static final String TAG = "PictureUtil";
    private static final String MyDictRootDirectory = Environment.getExternalStorageDirectory() + File.separator+"W_WDict";

    public static String getMyDictRootDirectory(){
        return MyDictRootDirectory;
    }

    public static Uri getImageUri(Context context,Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        Log.e("PictureUtil",data.toString());
        if(Build.VERSION.SDK_INT >= 19){
            if(DocumentsContract.isDocumentUri(context,uri)){
                String docId = DocumentsContract.getDocumentId(uri);
                if("com.android.providers.media.documents".equals(uri.getAuthority())){
                    String id = docId.split(":")[1];
                    String selection = MediaStore.Images.Media._ID+"="+id;
                    imagePath = getImagePath(context,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
                }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                    imagePath = getImagePath(context,contentUri,null);
                }
            }else if("content".equalsIgnoreCase(uri.getScheme())){
                imagePath = getImagePath(context,uri,null);
            }else if("file".equalsIgnoreCase(uri.getScheme())){
                imagePath = uri.getPath();
            }
        }else{
            uri= data.getData();
            imagePath = getImagePath(context,uri,null);
        }
        File file = new File(imagePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context,
                  "com.example.vocabularybuilder.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }

        return uri;
    }

    private static String getImagePath(Context context,Uri uri, String selection) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public static void mkdirMyDictRootDirectory(){
        boolean isSdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);//if sdcard exist
        if (isSdCardExist) {
            File MyDictRoot = new File(getMyDictRootDirectory());
            if (!MyDictRoot.exists()) {
                try {
                    MyDictRoot.mkdir();
                    Log.d(TAG, "mkdir success");
                } catch (Exception e) {
                    Log.e(TAG, "exception->" + e.toString());
                }
            }
        }
    }
}
//https://github.com/NeedKwok/MyPet