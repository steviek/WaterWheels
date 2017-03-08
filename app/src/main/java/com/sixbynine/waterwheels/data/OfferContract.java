package com.sixbynine.waterwheels.data;

import android.provider.BaseColumns;

final class OfferContract {

  private OfferContract() {}

  public static abstract class Offer implements BaseColumns {
    static final String TABLE_NAME = "offer";
    static final String COLUMN_NAME_PRICE = "price";
    static final String COLUMN_NAME_ORIGIN = "origin";
    static final String COLUMN_NAME_DESTINATION = "destination";
    static final String COLUMN_NAME_PHONE = "phone";
    static final String COLUMN_NAME_TIME = "time";
    static final String COLUMN_NAME_POST_ID = "post_id";
    static final String COLUMN_NAME_POST_MESSAGE = "post_message";
    static final String COLUMN_NAME_POST_CREATED_TIME = "post_created_time";
    static final String COLUMN_NAME_POST_UPDATED_TIME = "post_updated_time";
    static final String COLUMN_NAME_POST_FROM_ID = "post_from_id";
    static final String COLUMN_NAME_POST_FROM_NAME = "post_from_name";
  }
}
