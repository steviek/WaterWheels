package com.sixbynine.waterwheels.data;

import android.provider.BaseColumns;

public final class OfferContract {

    private OfferContract() {}

    public static abstract class Offer implements BaseColumns {
        public static final String TABLE_NAME = "offer";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_ORIGIN = "origin";
        public static final String COLUMN_NAME_DESTINATION = "destination";
        public static final String COLUMN_NAME_PHONE = "phone";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_POST_ID = "post_id";
        public static final String COLUMN_NAME_POST_MESSAGE = "post_message";
        public static final String COLUMN_NAME_POST_CREATED_TIME = "post_created_time";
        public static final String COLUMN_NAME_POST_UPDATED_TIME = "post_updated_time";
        public static final String COLUMN_NAME_POST_FROM_ID = "post_from_id";
        public static final String COLUMN_NAME_POST_FROM_NAME = "post_from_name";
        public static final String COLUMN_NAME_PINNED = "pinned";
    }

}
