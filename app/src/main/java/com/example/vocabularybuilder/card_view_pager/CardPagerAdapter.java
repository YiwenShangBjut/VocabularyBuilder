package com.example.vocabularybuilder.card_view_pager;
//http://www.cppcns.com/ruanjian/android/230297.html
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.example.vocabularybuilder.DetailActivity;
import com.example.vocabularybuilder.PictureUtil;
import com.example.vocabularybuilder.R;
import com.example.vocabularybuilder.db.MyDbOpenHelper;
import com.example.vocabularybuilder.primary_class.Word;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {
    private MyDbOpenHelper myDbHelper;
    private static final String IMAGE_FILE_NAME = "W_WDict_photo.jpg";
    private static final String TAG = "CardPagerAdapter";
    private List<CardView> cardViewList;
    private List<Word> wordList;
    private float baseElevation;
    private Animation display;
    private boolean isShow = false;
    private Animation dismiss;
    private Context context;
    private String tablename;
    private String location;
    //  private ImageView photo;

    public CardPagerAdapter(Context context, String tablename, String location) {
        cardViewList = new ArrayList<>();
        wordList = new ArrayList<>();
        this.context = context;
        this.tablename = tablename;
        this.location=location;
    }

    public void addCardItem(Word item) {
        cardViewList.add(null);
        wordList.add(item);
    }


    @Override
    public float getBaseElevation() {
        return baseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return cardViewList.get(position);
    }

    @Override
    public int getCount() {
        return wordList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.card_adapter, container, false);
        container.addView(view);

        bind(wordList.get(position), view);

        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (baseElevation == 0) {
            baseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(baseElevation * MAX_ELEVATION_FACTOR);
        cardViewList.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        cardViewList.set(position, null);
    }

    private void bind(final Word item, View view) {
        TextView EnglishTextView = (TextView) view.findViewById(R.id.English);
        TextView PropertyTextView = (TextView) view.findViewById(R.id.Property);
        final TextView ChineseTextView = (TextView) view.findViewById(R.id.Chinese);
        TextView MeaningTextView = (TextView) view.findViewById(R.id.Meaning);
        ChineseTextView.setVisibility(View.INVISIBLE);
        TextView show = (TextView) view.findViewById(R.id.Show);
        final ImageView photo = (ImageView) view.findViewById(R.id.Photo);
        ImageView edit=(ImageView) view.findViewById(R.id.edit);

        EnglishTextView.setText(item.getEnglish());
        PropertyTextView.setText(item.getProperty());
        ChineseTextView.setText(item.getChinese());
        MeaningTextView.setText(item.getMeaning());
        String photoPath = item.getPhotoPath();

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("id", item.getId());
                intent.putExtra("tablename", tablename);
                intent.putExtra("position", wordList.indexOf(item));
                intent.putExtra("location", location);
                context.startActivity(intent);
            }
        });
        if (photoPath != "" || photoPath != null) {
            Bitmap bm = BitmapFactory.decodeFile(photoPath);
            photo.setImageBitmap(bm);

        } else {
            photo.setMaxHeight(50);
            photo.setMaxWidth(50);
            photo.setImageResource(R.drawable.md_add);
        }


        display = new AlphaAnimation(0.0f, 1.0f);
        display.setDuration(2000);

        dismiss = new AlphaAnimation(1.0f, 0.0f);
        dismiss.setDuration(1000);

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ChineseTextView.getVisibility() == View.INVISIBLE) {
                    ChineseTextView.setVisibility(View.VISIBLE);
                    ChineseTextView.startAnimation(display);

                } else {
                    ChineseTextView.startAnimation(dismiss);
                    ChineseTextView.setVisibility(View.INVISIBLE);
                }

            }
        });
    }
}
