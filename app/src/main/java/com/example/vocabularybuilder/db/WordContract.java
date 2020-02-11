package com.example.vocabularybuilder.db;

import android.provider.BaseColumns;

public class WordContract {
    private WordContract(){}
    public static class WordEntry implements BaseColumns{
        public static final String TABLE_NAME="Word";
        public static final String _ID="_id";
        public static final String COLUMN_NAME_ENGLISH="English";
        public static final String COLUMN_NAME_PHONETIC="Phonetic";
        public static final String COLUMN_NAME_PROPERTY="Property";
        public static final String COLUMN_NAME_CHINESE="Chinese";
        public static final String COLUMN_NAME_MEANING="Meaning";
        public static final String COLUMN_NAME_PHOTOPATH="PhotoPath";
        public static final String COLUMN_NAME_TIMESTAMP="TimeStamp";
        public static final String COLUMN_NAME_LOCATION="Location";

        public static final String DICT_TABLE_NAME="Mini_Dict";
        public static final String DICT_ID="_id";
        public static final String DICT_COLUMN_NAME_NAME="Name";
        public static final String DICT_COLUMN_NAME_COMMENT="Comment";
        public static final String DICT_COLUMN_NAME_TIMESTAMP="TimeStamp";
        public static final String USER_COLUMN_NAME_USERID="UserId";

        public static final String MY_WORD_TABLE_NAME="MyWord";

        public static final String ACH_TABLE_NAME="Achievement";
        public static final String ACH_ID="_id";
        public static final String ACH_COLUMN_NAME_PHOTOPATH="PhotoPath";
        public static final String ACH_COLUMN_NAME_TIMESTAMP="TimeStamp";






    }
}
