package com.jueyuantech.glasses.common;

public class Constants {
    public final static String MMKV_AGREEMENT_ACCEPT_KEY = "agreementAccept";
    public final static int AGREEMENT_UNACCEPTED = 0;
    public final static int AGREEMENT_ACCEPTED = 1;
    public final static int AGREEMENT_ACCEPT_DEFAULT = AGREEMENT_UNACCEPTED;

    public final static String MMKV_STT_LANGUAGE_CONFIG_KEY = "sttLanguageConfig";

    public final static String MMKV_STT_ENGINE_KEY = "sttEngine";
    public final static String STT_ENGINE_AISPEECH = "AiSpeech";
    public final static String STT_ENGINE_IFLYTEK = "IFlyTek";
    public final static String STT_ENGINE_IFLYTEK_WEB_ASR = "IFlyTekWebAsr";
    public final static String STT_ENGINE_IFLYTEK_WEB_IAT_MUL = "IFlyTekWebIatMul";
    public final static String STT_ENGINE_AZURE = "Azure";
    public final static String STT_ENGINE_AZURE_WESTUS = "AzureWestUS";
    public final static String STT_ENGINE_AZURE_SWEDENCENTRAL = "AzureSwedenCentral";
    public final static String STT_ENGINE_MOCK = "Mock";
    public final static String STT_ENGINE_DEFAULT = STT_ENGINE_MOCK;

    public final static String MMKV_SIMPLIFIED_MODE_KEY = "simplifiedMode";
    public final static int SIMPLIFIED_MODE_DISABLED = 0;
    public final static int SIMPLIFIED_MODE_ENABLED = 1;
    public final static int SIMPLIFIED_MODE_DEFAULT = SIMPLIFIED_MODE_ENABLED;

    public final static String MMKV_TEXT_MODE_KEY = "textMode";
    public final static int TEXT_MODE_CURRENT_ONLY = 0;
    public final static int TEXT_MODE_CURRENT_AND_HISTORICAL = 1;
    public final static int TEXT_MODE_DEFAULT = TEXT_MODE_CURRENT_ONLY;

    public final static String MMKV_TRANS_SHOW_MODE_KEY = "transShowMode";
    public final static int TRANS_SHOW_MODE_ONLY = 0;
    public final static int TRANS_SHOW_MODE_COMMON = 1;
    public final static int TRANS_SHOW_MODE_DEFAULT = TRANS_SHOW_MODE_COMMON;

    public final static String MMKV_TRANS_TTS_KEY = "transTts";
    public final static int TRANS_TTS_DISABLED = 0;
    public final static int TRANS_TTS_ENABLED = 1;

    public final static String MMKV_AUDIO_RECORD_KEY = "audioRecord";
    public final static int AUDIO_RECORD_DISABLED = 0;
    public final static int AUDIO_RECORD_ENABLED = 1;
    public final static int AUDIO_RECORD_DEFAULT = AUDIO_RECORD_DISABLED;

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

    public final static String MMKV_MIC_DIRECTIONAL_KEY = "micDirectional";
    public final static int MIC_DIRECTIONAL_OMNI = 0;
    public final static int MIC_DIRECTIONAL_FRONT = 1;
    public final static int MIC_DIRECTIONAL_DEFAULT = MIC_DIRECTIONAL_OMNI;

    public final static String MMKV_STT_FUNC_KEY = "sttFunc";
    public final static String STT_FUNC_TRANSCRIBE = "transcribe";
    public final static String STT_FUNC_TRANSLATE = "translate";
    public final static String STT_FUNC_DEFAULT = STT_FUNC_TRANSCRIBE;

    public final static String MMKV_NOTIFICATION_PUSH_KEY = "notificationPush";
    public final static int NOTIFICATION_PUSH_DISABLED = 0;
    public final static int NOTIFICATION_PUSH_ENABLED = 1;

    public final static String MMKV_NOTIFICATION_PUSH_PKG_SET_KEY = "notificationPushPkgSet";

    public final static String MMKV_APP_VERSION_KEY = "appVersion";
    public final static int APP_VERSION_DEFAULT = 0;
}
