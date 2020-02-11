package com.example.vocabularybuilder.card_view_pager;

import androidx.cardview.widget.CardView;
/**
 * https://github.com/open-android/ViewPagerCards
 */
public interface CardAdapter {

    int MAX_ELEVATION_FACTOR = 8;
    float getBaseElevation();
    CardView getCardViewAt(int position);
    int getCount();
}
