package com.example.vocabularybuilder.primary_class;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.example.vocabularybuilder.R;

public class WidgetService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateView(){
        RemoteViews views=new RemoteViews(getPackageName(), R.layout.activity_widget);
    }
}
