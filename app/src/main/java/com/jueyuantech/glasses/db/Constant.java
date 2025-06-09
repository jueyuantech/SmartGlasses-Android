package com.jueyuantech.glasses.db;

import android.provider.BaseColumns;

public interface Constant {
    class SessionEntry implements BaseColumns {

        public static final String TABLE_NAME = "t_session";

        public static final String COLUMN_NAME_ID = "c_id";
        public static final String COLUMN_NAME_UUID = "c_uuid";
        public static final String COLUMN_NAME_USER_ID = "c_user_id";
        public static final String COLUMN_NAME_TITLE = "c_title";
        public static final String COLUMN_NAME_FUNC_TYPE = "c_func_type"; // transcribe, translate or ai
        public static final String COLUMN_NAME_START_TIME = "c_start_time";
        public static final String COLUMN_NAME_END_TIME = "c_end_time";
        public static final String COLUMN_NAME_DURATION = "c_duration";
        public static final String COLUMN_NAME_LATITUDE = "c_latitude";
        public static final String COLUMN_NAME_LONGITUDE = "c_longitude";
        public static final String COLUMN_NAME_AUDIO_SOURCE = "c_audio_source";
        public static final String COLUMN_NAME_AUDIO_FILE_PATH = "c_audio_file_path";
        public static final String COLUMN_NAME_AUDIO_FILE_NAME = "c_audio_file_name";
        public static final String COLUMN_NAME_STATE = "c_state";
        public static final String COLUMN_NAME_DELETE = "c_delete";
        public static final String COLUMN_NAME_CREATE_TIME = "c_create_time";
        public static final String COLUMN_NAME_UPDATE_TIME = "c_update_time";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME_UUID + " TEXT," +
                        COLUMN_NAME_USER_ID + " TEXT," +
                        COLUMN_NAME_TITLE + " TEXT," +
                        COLUMN_NAME_FUNC_TYPE + " INTEGER," +
                        COLUMN_NAME_START_TIME + " BIGINT," +
                        COLUMN_NAME_END_TIME + " BIGINT," +
                        COLUMN_NAME_DURATION + " BIGINT," +
                        COLUMN_NAME_LATITUDE + " TEXT," +
                        COLUMN_NAME_LONGITUDE + " TEXT," +
                        COLUMN_NAME_AUDIO_SOURCE + " TEXT," +
                        COLUMN_NAME_AUDIO_FILE_PATH + " TEXT," +
                        COLUMN_NAME_AUDIO_FILE_NAME + " TEXT," +
                        COLUMN_NAME_STATE + " INTEGER," +
                        COLUMN_NAME_DELETE + " INTEGER," +
                        COLUMN_NAME_CREATE_TIME + " BIGINT," +
                        COLUMN_NAME_UPDATE_TIME + " BIGINT)";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    class MessageEntry implements BaseColumns {

        public static final String TABLE_NAME = "t_message";

        public static final String COLUMN_NAME_ID = "c_id";
        public static final String COLUMN_NAME_SESSION_ID = "c_session_id";
        public static final String COLUMN_NAME_SPEAKER = "c_speaker";
        public static final String COLUMN_NAME_TYPE = "c_type"; // system, stt
        public static final String COLUMN_NAME_START_TIME = "c_start_time";
        public static final String COLUMN_NAME_END_TIME = "c_end_time";
        public static final String COLUMN_NAME_DURATION = "c_duration";
        public static final String COLUMN_NAME_ORIGINAL_TEXT = "c_original_text";
        public static final String COLUMN_NAME_TRANSLATED_TEXT = "c_translated_text";
        public static final String COLUMN_NAME_STATE = "c_state";
        public static final String COLUMN_NAME_DELETE = "c_delete";
        public static final String COLUMN_NAME_CREATE_TIME = "c_create_time";
        public static final String COLUMN_NAME_UPDATE_TIME = "c_update_time";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME_SESSION_ID + " INTEGER," +
                        COLUMN_NAME_SPEAKER + " TEXT," +
                        COLUMN_NAME_TYPE + " TEXT," +
                        COLUMN_NAME_START_TIME + " BIGINT," +
                        COLUMN_NAME_END_TIME + " BIGINT," +
                        COLUMN_NAME_DURATION + " BIGINT," +
                        COLUMN_NAME_ORIGINAL_TEXT + " TEXT," +
                        COLUMN_NAME_TRANSLATED_TEXT + " TEXT," +
                        COLUMN_NAME_STATE + " INTEGER," +
                        COLUMN_NAME_DELETE + " INTEGER," +
                        COLUMN_NAME_CREATE_TIME + " BIGINT," +
                        COLUMN_NAME_UPDATE_TIME + " BIGINT)";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
