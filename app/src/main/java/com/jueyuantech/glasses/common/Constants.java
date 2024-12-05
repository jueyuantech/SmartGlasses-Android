package com.jueyuantech.glasses.common;

public class Constants {
    public final static String MMKV_ASR_LANGUAGE_CONFIG_KEY = "asrLanguageConfig";

    public final static String MMKV_ASR_ENGINE_KEY = "asrEngine";
    public final static String ASR_ENGINE_AISPEECH = "AiSpeech";
    public final static String ASR_ENGINE_IFLYTEK = "IFlyTek";
    public final static String ASR_ENGINE_AZURE = "Azure";
    public final static String ASR_ENGINE_MOCK = "Mock";
    public final static String ASR_ENGINE_DEFAULT = ASR_ENGINE_MOCK;

    public final static String MMKV_TRANS_SHOW_MODE_KEY = "transShowMode";
    public final static int TRANS_SHOW_MODE_ONLY = 0;
    public final static int TRANS_SHOW_MODE_COMMON = 1;
    public final static int TRANS_SHOW_MODE_DEFAULT = TRANS_SHOW_MODE_COMMON;

    public final static String MMKV_TRANS_TTS_KEY = "transTts";
    public final static int TRANS_TTS_DISABLED = 0;
    public final static int TRANS_TTS_ENABLED = 1;

    public final static String MMKV_HEARTBEAT_KEY = "heartbeat";
    public final static int HEARTBEAT_DISABLED = 0;
    public final static int HEARTBEAT_ENABLED = 1;
    public final static int HEARTBEAT_DEFAULT = HEARTBEAT_ENABLED;

    public final static String MMKV_PROTOCOL_VER_KEY = "protocolVer";
    public final static String PROTOCOL_VER_1 = "V1";
    public final static String PROTOCOL_VER_2 = "V2";
    public final static String PROTOCOL_VER_DEFAULT = PROTOCOL_VER_2;

    public final static String MMKV_BT_UUID_DATA_KEY = "dataUUID";
    public final static String BT_UUID_DATA = "88888888";
    public final static String BT_UUID_COMMON = "00001101";

    public final static String MMKV_AUDIO_INPUT_KEY = "audioInput";
    public final static int AUDIO_INPUT_PHONE = 0;
    public final static int AUDIO_INPUT_SCO = 1;
    public final static int AUDIO_INPUT_DEFAULT = AUDIO_INPUT_PHONE;

    public final static String MMKV_ASR_FUNC_KEY = "asrFunc";
    public final static String ASR_FUNC_TRANSCRIBE = "transcribe";
    public final static String ASR_FUNC_TRANSLATE = "translate";
    public final static String ASR_FUNC_DEFAULT = ASR_FUNC_TRANSLATE;

    public final static String MMKV_NOTIFICATION_PUSH_KEY = "notificationPush";
    public final static int NOTIFICATION_PUSH_DISABLED = 0;
    public final static int NOTIFICATION_PUSH_ENABLED = 1;

    public final static String MMKV_NOTIFICATION_PUSH_PKG_SET_KEY = "notificationPushPkgSet";
}
