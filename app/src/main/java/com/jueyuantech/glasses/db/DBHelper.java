package com.jueyuantech.glasses.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jueyuantech.glasses.bean.SpeechMessage;
import com.jueyuantech.glasses.bean.SpeechSession;
import com.jueyuantech.glasses.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "glasses.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Constant.SessionEntry.SQL_CREATE_TABLE);
        db.execSQL(Constant.MessageEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long createSession(SpeechSession speechSession) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constant.SessionEntry.COLUMN_NAME_UUID, speechSession.getUuid());
        values.put(Constant.SessionEntry.COLUMN_NAME_TITLE, speechSession.getTitle());
        values.put(Constant.SessionEntry.COLUMN_NAME_FUNC_TYPE, speechSession.getFuncType());
        values.put(Constant.SessionEntry.COLUMN_NAME_AUDIO_SOURCE, speechSession.getAudioSource());
        values.put(Constant.SessionEntry.COLUMN_NAME_CREATE_TIME, System.currentTimeMillis());

        long rowId = db.insert(Constant.SessionEntry.TABLE_NAME, null, values);
        if (-1 != rowId) {
            ContentValues updateValues = new ContentValues();
            updateValues.put(Constant.SessionEntry.COLUMN_NAME_TITLE, speechSession.getTitle() + " " + rowId);
            db.update(
                    Constant.SessionEntry.TABLE_NAME,
                    updateValues,
                    Constant.SessionEntry.COLUMN_NAME_UUID + " = ?",
                    new String[]{speechSession.getUuid()});
        }
        return rowId;
    }

    public List<SpeechSession> getSessions() {
        SQLiteDatabase db = getReadableDatabase();

        List<SpeechSession> speechSessions = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    Constant.SessionEntry.TABLE_NAME,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    Constant.SessionEntry.COLUMN_NAME_CREATE_TIME + " DESC"               // The sort order
            );

            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_ID));
                String uuid = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_UUID));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_USER_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_TITLE));
                String funcType = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_FUNC_TYPE));
                long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_START_TIME));
                long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_END_TIME));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_DURATION));
                String latitude = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_LATITUDE));
                String longitude = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_LONGITUDE));
                String audio_source = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_AUDIO_SOURCE));
                String audio_file_path = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_AUDIO_FILE_PATH));
                String audio_file_name = cursor.getString(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_AUDIO_FILE_NAME));
                int state = cursor.getInt(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_STATE));
                int delete = cursor.getInt(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_DELETE));
                long createTime = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_CREATE_TIME));
                long updateTime = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.SessionEntry.COLUMN_NAME_UPDATE_TIME));

                SpeechSession speechSession = new SpeechSession();
                speechSession.setId(id);
                speechSession.setUuid(uuid);
                speechSession.setUserId(userId);
                speechSession.setTitle(title);
                speechSession.setFuncType(funcType);
                speechSession.setStartTime(startTime);
                speechSession.setEndTime(endTime);
                speechSession.setDuration(duration);
                speechSession.setLatitude(latitude);
                speechSession.setLongitude(longitude);
                speechSession.setAudioSource(audio_source);
                speechSession.setAudioFilePath(audio_file_path);
                speechSession.setAudioFileName(audio_file_name);
                speechSession.setState(state);
                speechSession.setDelete(delete);
                speechSession.setCreateDate(createTime);
                speechSession.setUpdateDate(updateTime);

                speechSessions.add(speechSession);
            }
        } finally {
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
        }

        return speechSessions;
    }

    public int updateSessionAudioFileName(long sessionId, String fileName) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constant.SessionEntry.COLUMN_NAME_AUDIO_FILE_NAME, fileName);

        String selection = Constant.SessionEntry.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(sessionId)};

        return db.update(
                Constant.SessionEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    public int deleteSession(long sessionId) {
        SQLiteDatabase db = getWritableDatabase();
        // 首先删除与该会话关联的所有消息
        db.delete(Constant.MessageEntry.TABLE_NAME,
                Constant.MessageEntry.COLUMN_NAME_SESSION_ID + " = ?",
                new String[]{String.valueOf(sessionId)});
        // 然后删除会话本身
        return db.delete(Constant.SessionEntry.TABLE_NAME,
                Constant.SessionEntry.COLUMN_NAME_ID + " = ?",
                new String[]{String.valueOf(sessionId)});
    }

    public boolean addMessage(SpeechMessage speechMessage) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        LogUtil.i("SessionId : " + speechMessage.getSessionId());
        values.put(Constant.MessageEntry.COLUMN_NAME_SESSION_ID, speechMessage.getSessionId());
        values.put(Constant.MessageEntry.COLUMN_NAME_ORIGINAL_TEXT, speechMessage.getOriginalText());
        values.put(Constant.MessageEntry.COLUMN_NAME_TRANSLATED_TEXT, speechMessage.getTranslatedText());
        values.put(Constant.MessageEntry.COLUMN_NAME_TYPE, speechMessage.getType());
        values.put(Constant.MessageEntry.COLUMN_NAME_CREATE_TIME, System.currentTimeMillis());

        long lineIdx = db.insert(Constant.MessageEntry.TABLE_NAME, null, values);
        return lineIdx != -1;
    }

    public List<SpeechMessage> getMessagesBySessionId(int targetSessionId) {
        SQLiteDatabase db = getReadableDatabase();

        List<SpeechMessage> speechMessages = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    Constant.MessageEntry.TABLE_NAME,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    Constant.MessageEntry.COLUMN_NAME_SESSION_ID + " = ?",              // The columns for the WHERE clause
                    new String[]{String.valueOf(targetSessionId)},          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    Constant.MessageEntry.COLUMN_NAME_CREATE_TIME + " DESC"               // The sort order
            );

            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_ID));
                int sessionId = cursor.getInt(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_SESSION_ID));
                String speaker = cursor.getString(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_SPEAKER));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_TYPE));
                long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_START_TIME));
                long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_END_TIME));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_DURATION));
                String originalText = cursor.getString(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_ORIGINAL_TEXT));
                String translatedText = cursor.getString(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_TRANSLATED_TEXT));
                int state = cursor.getInt(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_STATE));
                int delete = cursor.getInt(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_DELETE));
                long createTime = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_CREATE_TIME));
                long updateTime = cursor.getLong(cursor.getColumnIndexOrThrow(Constant.MessageEntry.COLUMN_NAME_UPDATE_TIME));

                SpeechMessage speechMessage = new SpeechMessage();
                speechMessage.setId(id);
                speechMessage.setSessionId(sessionId);
                speechMessage.setSpeaker(speaker);
                speechMessage.setType(type);
                speechMessage.setStartTime(startTime);
                speechMessage.setEndTime(endTime);
                speechMessage.setDuration(duration);
                speechMessage.setOriginalText(originalText);
                speechMessage.setTranslatedText(translatedText);
                speechMessage.setState(state);
                speechMessage.setDelete(delete);
                speechMessage.setCreateDate(createTime);
                speechMessage.setUpdateDate(updateTime);

                speechMessages.add(speechMessage);
            }
        } finally {
            if (null != cursor) {
                cursor.close();
                cursor = null;
            }
        }

        return speechMessages;
    }
}
