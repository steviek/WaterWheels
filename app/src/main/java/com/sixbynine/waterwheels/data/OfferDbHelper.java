package com.sixbynine.waterwheels.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sixbynine.waterwheels.data.OfferContract.Offer;


public final class OfferDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "offer.db";

    public OfferDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Offer.TABLE_NAME + "(" +
                Offer._ID + " INTEGER PRIMARY KEY," +
                Offer.COLUMN_NAME_DESTINATION + " TEXT," +
                Offer.COLUMN_NAME_ORIGIN + " TEXT," +
                Offer.COLUMN_NAME_PHONE + " TEXT," +
                Offer.COLUMN_NAME_PRICE + " INTEGER," +
                Offer.COLUMN_NAME_TIME + " INTEGER," +
                Offer.COLUMN_NAME_POST_CREATED_TIME + " INTEGER," +
                Offer.COLUMN_NAME_POST_ID + " TEXT," +
                Offer.COLUMN_NAME_POST_MESSAGE + " TEXT," +
                Offer.COLUMN_NAME_POST_FROM_ID + " TEXT," +
                Offer.COLUMN_NAME_POST_FROM_NAME + " TEXT )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }


}
