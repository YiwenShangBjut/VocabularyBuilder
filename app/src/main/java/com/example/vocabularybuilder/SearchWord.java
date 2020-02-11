package com.example.vocabularybuilder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SearchWord extends AppCompatActivity {
private WebView webView;
private String word;
private String tablename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_word);
        Intent intent=getIntent();
        tablename= intent.getStringExtra("tablename");
        word= intent.getStringExtra("word");
        init(word);

    }

    private void init(String word){
        webView= findViewById(R.id.webView);
        if(word==null||word==""){
            webView.loadUrl("http://www.youdao.com/");
        }else{
            webView.loadUrl("https://www.youdao.com/w/eng/"+word+"/#keyfrom=dict2.index");
        }

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }

        });
        WebSettings webSettings=webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            if(webView.canGoBack())
            {
                webView.goBack();//返回上一页面
                return true;
            }
            else
            {
                Intent intent=new Intent(SearchWord.this,MiniDictDisplay.class);
                intent.setAction("CREATE NEW WORD");
                intent.putExtra("word",word);
                intent.putExtra("tablename",tablename);
                startActivity(intent);
                finish();
               // System.exit(0);//退出程序
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
