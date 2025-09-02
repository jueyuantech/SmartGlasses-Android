package com.jueyuantech.glasses.stt;

import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;
import static com.jueyuantech.glasses.common.Constants.MMKV_STT_ENGINE_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_STT_FUNC_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_STT_LANGUAGE_CONFIG_KEY;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jueyuantech.glasses.bean.SttLanguageConfig;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.util.MmkvUtil;

import java.util.ArrayList;
import java.util.List;

public class SttConfigManager {
    private volatile static SttConfigManager mInstance;

    private Gson gson = new Gson();
    private List<SttLanguageConfig> sttLanguageConfigs;
    private SttLanguageConfig mSttLanguageConfig;

    private SttConfigManager() {
        init();
    }

    public static SttConfigManager getInstance() {
        if (mInstance == null) {
            synchronized (SttConfigManager.class) {
                if (mInstance == null) {
                    mInstance = new SttConfigManager();
                }
            }
        }
        return mInstance;
    }

    private void init() {
        String sttLanguageConfigStr = MmkvUtil.decodeString(MMKV_STT_LANGUAGE_CONFIG_KEY, "");
        if (TextUtils.isEmpty(sttLanguageConfigStr)) {
            sttLanguageConfigs = loadDefaultConfigs();
            save();
        } else {
            sttLanguageConfigs = gson.fromJson(sttLanguageConfigStr, new TypeToken<List<SttLanguageConfig>>() {
            }.getType());
        }

        updateEngineConfig();
    }

    private void save() {
        MmkvUtil.encode(MMKV_STT_LANGUAGE_CONFIG_KEY, gson.toJson(sttLanguageConfigs));
    }

    public String getEngine() {
        return MmkvUtil.decodeString(MMKV_STT_ENGINE_KEY, STT_ENGINE_DEFAULT);
    }

    public void setEngine(String sttEngine) {
        MmkvUtil.encode(MMKV_STT_ENGINE_KEY, sttEngine);
        updateEngineConfig();
    }

    public void updateEngineConfig() {
        String engine = getEngine();
        for (SttLanguageConfig sttLanguageConfig : sttLanguageConfigs) {
            if (engine.equals(sttLanguageConfig.getName())) {
                mSttLanguageConfig = sttLanguageConfig;
                break;
            }
        }
    }

    public String getFunc() {
        return MmkvUtil.decodeString(MMKV_STT_FUNC_KEY, STT_FUNC_DEFAULT);
    }

    public void setFunc(String func) {
        MmkvUtil.encode(MMKV_STT_FUNC_KEY, func);
    }

    public List<LanguageTag> getSourceLanTags(String engine, String func) {
        List<LanguageTag> tags = new ArrayList<>();
        for (SttLanguageConfig sttLanguageConfig : sttLanguageConfigs) {
            if (engine.equals(sttLanguageConfig.getName())) {
                mSttLanguageConfig = sttLanguageConfig;
                if (STT_FUNC_TRANSCRIBE.equals(func)) {
                    tags.addAll(mSttLanguageConfig.getTranscribe());
                } else if (STT_FUNC_TRANSLATE.equals(func)) {
                    tags.addAll(mSttLanguageConfig.getTranslate());
                }
            }
        }
        return tags;
    }

    public void setSourceLanTag(String sttEngine, String func, String sourceTagKey) {
        for (SttLanguageConfig sttLanguageConfig : sttLanguageConfigs) {
            if (sttEngine.equals(sttLanguageConfig.getName())) {
                mSttLanguageConfig = sttLanguageConfig;
                if (STT_FUNC_TRANSCRIBE.equals(func)) {
                    for (LanguageTag tag : mSttLanguageConfig.getTranscribe()) {
                        if (sourceTagKey.equals(tag.getTag())) {
                            tag.setSelected(true);
                        } else {
                            tag.setSelected(false);
                        }
                    }
                } else if (STT_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag tag : mSttLanguageConfig.getTranslate()) {
                        if (sourceTagKey.equals(tag.getTag())) {
                            tag.setSelected(true);
                        } else {
                            tag.setSelected(false);
                        }
                    }
                }
            }
        }
        save();
    }

    public LanguageTag getSourceLanTag(String sttEngine, String func) {
        for (SttLanguageConfig sttLanguageConfig : sttLanguageConfigs) {
            if (sttEngine.equals(sttLanguageConfig.getName())) {
                mSttLanguageConfig = sttLanguageConfig;
                if (STT_FUNC_TRANSCRIBE.equals(func)) {
                    for (LanguageTag tag : sttLanguageConfig.getTranscribe()) {
                        if (tag.isSelected()) {
                            return tag;
                        }
                    }
                } else if (STT_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag tag : sttLanguageConfig.getTranslate()) {
                        if (tag.isSelected()) {
                            return tag;
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<LanguageTag> getTargetLanTags(String sttEngine, String func, String sourceKey) {
        List<LanguageTag> tags = new ArrayList<>();
        for (SttLanguageConfig sttLanguageConfig : sttLanguageConfigs) {
            if (sttEngine.equals(sttLanguageConfig.getName())) {
                mSttLanguageConfig = sttLanguageConfig;
                if (STT_FUNC_TRANSCRIBE.equals(func)) {
                    tags.addAll(mSttLanguageConfig.getTranscribe());
                } else if (STT_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag sourceTag : mSttLanguageConfig.getTranslate()) {
                        if (sourceTag.getTag().equals(sourceKey)) {
                            tags.addAll(sourceTag.getTarget());
                        }
                    }
                }
            }
        }
        return tags;
    }

    public void setTargetLanTag(String sttEngine, String func, String sourceTagKey, String targetTagKey) {
        for (SttLanguageConfig sttLanguageConfig : sttLanguageConfigs) {
            if (sttEngine.equals(sttLanguageConfig.getName())) {
                mSttLanguageConfig = sttLanguageConfig;
                if (STT_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag tag : mSttLanguageConfig.getTranslate()) {
                        if (sourceTagKey.equals(tag.getTag())) {
                            for (LanguageTag targetTag : tag.getTarget()) {
                                if (targetTag.getTag().equals(targetTagKey)) {
                                    targetTag.setSelected(true);
                                } else {
                                    targetTag.setSelected(false);
                                }
                            }
                        }
                    }
                }
            }
        }
        save();
    }

    public LanguageTag getTargetLanTag(String sttEngine, String func, String sourceKey) {
        for (SttLanguageConfig sttLanguageConfig : sttLanguageConfigs) {
            if (sttEngine.equals(sttLanguageConfig.getName())) {
                mSttLanguageConfig = sttLanguageConfig;
                if (STT_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag sourceTag : sttLanguageConfig.getTranslate()) {
                        if (sourceKey.equals(sourceTag.getTag())) {
                            for (LanguageTag targetTag : sourceTag.getTarget()) {
                                if (targetTag.isSelected()) {
                                    return targetTag;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void setSelected(List<LanguageTag> tags, LanguageTag selectedTag) {
        for (LanguageTag tag : tags) {
            if (tag.getTag().equals(selectedTag.getTag())) {
                tag.setSelected(true);
            } else {
                tag.setSelected(false);
            }
        }
    }

    public static LanguageTag copyTag(LanguageTag tag) {
        LanguageTag newTag = new LanguageTag();
        newTag.setTag(tag.getTag());
        newTag.setLanguage(tag.getLanguage());
        newTag.setRegion(tag.getRegion());
        newTag.setDescription(tag.getDescription());
        newTag.setListeningStr(tag.getListeningStr());
        newTag.setTitle(tag.getTitle());
        return newTag;
    }

    /**
     * 创建目标语言列表，排除指定的源语言标签
     * @param excludeTag 要排除的源语言标签
     * @return 包含所有其他翻译语言标签的列表
     */
    private static List<LanguageTag> createTargetListExcluding(LanguageTag excludeTag) {
        List<LanguageTag> targetList = new ArrayList<>();
        
        // 所有可用的翻译语言标签
        LanguageTag[] allTranslateTags = {
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH_TW,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EN,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_JA,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_KO,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_IT,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_DE,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_RU,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FR,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FIL,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_HA,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_NL,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_CS,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_RO,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_MS,
            // IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_BN,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_PT,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_SV,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_SW,
            // IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TA,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TH,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TR,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UR,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UK,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UZ,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ES,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EL,
            // IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_HI,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ID,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_VI,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_AR,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_BG,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_PL,
            IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FA
        };
        
        // 添加所有标签，但排除指定的源语言标签
        for (LanguageTag tag : allTranslateTags) {
            if (!tag.getTag().equals(excludeTag.getTag())) {
                targetList.add(copyTag(tag));
            }
        }
        
        return targetList;
    }


    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("zh-CN", "Chinese", "China", "Mainland China, simplified characters", "正在听...", "中文");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_ZH_TW = new LanguageTag("zh-TW", "", "", "", "正在聽...", "中文（繁体）");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_EN = new LanguageTag("en-US", "English", "United States", "US English", "Listening...", "English");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_JA = new LanguageTag("ja-JP", "Japanese", "Japan", "Japanese (Japan)", "聞いています...", "日本語");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_KO = new LanguageTag("ko-KR", "Korean", "Korea", "Korean (Korea)", "듣고 있어요...", "한국어");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_RU = new LanguageTag("ru-RU", "Russian", "Russia", "Russian (Russia)", "Слушаю...", "Русский");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_DE = new LanguageTag("de-DE", "German", "Germany", "German (Germany)", "Hre...", "Deutsch");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_ES = new LanguageTag("es-ES", "Spanish", "Spain", "Spanish (Spain)", "Escuchando...", "español");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_FR = new LanguageTag("fr-FR", "French", "France", "French (France)", "coute...", "français");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_IT = new LanguageTag("it-IT", "Italian", "Italy", "Italian (Italy)", "Ascoltando...", "italiano");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_PT = new LanguageTag("pt-BR", "Portuguese", "Brazil", "Portuguese (Brazil)", "Ouvindo...", "português(Brasil)");

    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_ZH = new LanguageTag("zh-Hans", "", "", "", "正在听...", "中文");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_ZH_TW = new LanguageTag("zh-Hant", "", "", "", "正在聽...", "中文（繁体）");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_EN = new LanguageTag("en", "", "", "", "Listening...", "English");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "聞いています...", "日本語");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_KO = new LanguageTag("ko", "", "", "", "듣고 있어요...", "한국어");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_RU = new LanguageTag("ru", "", "", "", "Слушаю...", "Русский");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_DE = new LanguageTag("de", "", "", "", "Hre...", "Deutsch");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_FR = new LanguageTag("fr", "", "", "", "coute...", "Français");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_ES = new LanguageTag("es", "", "", "", "Escuchando...", "Español");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_IT = new LanguageTag("it", "", "", "", "Ascoltando...", "italiano");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_PT = new LanguageTag("pt", "", "", "", "Ouvindo...", "Português");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_AR = new LanguageTag("ar", "", "", "", "أستمع...", "العربية");

    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("cn", "", "", "", "正在听...", "中文");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "聞いています...", "日本語");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_HENANESE = new LanguageTag("cn_henanese", "", "", "", "正在听...", "河南话");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_XINANESE = new LanguageTag("cn_xinanese", "", "", "", "正在听...", "西南官话（川渝云贵）");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_CANTONESE = new LanguageTag("cn_cantonese", "", "", "", "正在听...", "广东话");

    private static LanguageTag IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("zh_cn", "", "", "", "正在听...", "中文");
    private static LanguageTag IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_ZH_TW = new LanguageTag("zh_tw", "", "", "", "正在聽...", "中文（繁体）");
    private static LanguageTag IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_EN = new LanguageTag("en_us", "", "", "", "Listening...", "English");
    private static LanguageTag IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_KO = new LanguageTag("ko_kr", "", "", "", "듣고 있어요...", "한국어");
    private static LanguageTag IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_JA = new LanguageTag("ja_jp", "", "", "", "聞いています...", "日本語");

    private static LanguageTag IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH = new LanguageTag("cn", "", "", "", "正在听...", "中文");
    private static LanguageTag IFLYTEKWEBASR_TRANSLATE_LAN_TAG_EN = new LanguageTag("en", "", "", "", "Listening...", "English");
    private static LanguageTag IFLYTEKWEBASR_TRANSLATE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "聞いています...", "日本語");
    private static LanguageTag IFLYTEKWEBASR_TRANSLATE_LAN_TAG_KO = new LanguageTag("ko", "", "", "", "듣고 있어요...", "한국어");

    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("zh_cn", "", "", "", "正在听...", "中文");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ZH_TW = new LanguageTag("zh_tw", "", "", "", "正在聽...", "中文（繁体）");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_EN = new LanguageTag("en_us", "", "", "", "Listening...", "English");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_KO = new LanguageTag("ko_kr", "", "", "", "듣고 있어요...", "한국어");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_JA = new LanguageTag("ja_jp", "", "", "", "聞いています...", "日本語");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_IT = new LanguageTag("it_IT", "", "", "", "Ascoltando...", "Italiano");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_DE = new LanguageTag("de_DE", "", "", "", "Hörend...", "Deutsch");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_RU = new LanguageTag("ru-ru", "", "", "", "Слушаю...", "Русский");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_FR = new LanguageTag("fr_fr", "", "", "", "Écoute...", "Français");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_FIL = new LanguageTag("fil_PH", "", "", "", "Nakikinig...", "Filipino");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_HA = new LanguageTag("ha_NG", "", "", "", "Sauraron...", "Hausa");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_NL = new LanguageTag("nl_NL", "", "", "", "Luisteren...", "Nederlands");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_CS = new LanguageTag("cs_CZ", "", "", "", "Poslouchám...", "Čeština");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_RO = new LanguageTag("ro_ro", "", "", "", "Ascult...", "Română");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_MS = new LanguageTag("ms_MY", "", "", "", "Mendengar...", "Bahasa Melayu");
    // private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_BN = new LanguageTag("bn_BD", "", "", "", "শুনছি...", "বাংলা");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_PT = new LanguageTag("pt_PT", "", "", "", "Ouvindo...", "Português");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_SV = new LanguageTag("sv_SE", "", "", "", "Lyssnar...", "Svenska");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_SW = new LanguageTag("sw_KE", "", "", "", "Kusikiliza...", "Kiswahili");
    // private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_TA = new LanguageTag("ta_in", "", "", "", "கேட்கிறேன்...", "தமிழ்");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_TH = new LanguageTag("th_TH", "", "", "", "กำลังฟัง...", "ไทย");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_TR = new LanguageTag("tr_TR", "", "", "", "Dinliyorum...", "Türkçe");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_UR = new LanguageTag("ur_IN", "", "", "", "سن رہا ہوں...", "اردو");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_UK = new LanguageTag("uk_UA", "", "", "", "Слухаю...", "Українська");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_UZ = new LanguageTag("uz_UZ", "", "", "", "Eshitmoqda...", "O'zbek");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ES = new LanguageTag("es_es", "", "", "", "Escuchando...", "Español");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_EL = new LanguageTag("el_GR", "", "", "", "Ακούω...", "Ελληνικά");
    // private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_HI = new LanguageTag("hi_in", "", "", "", "सुन रहा हूं...", "हिन्दी");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ID = new LanguageTag("id_ID", "", "", "", "Mendengarkan...", "Bahasa Indonesia");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_VI = new LanguageTag("vi_VN", "", "", "", "Đang nghe...", "Tiếng Việt");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_AR = new LanguageTag("ar_il", "", "", "", "أستمع...", "العربية");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_BG = new LanguageTag("bg_bg", "", "", "", "Слушам...", "Български");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_PL = new LanguageTag("pl_pl", "", "", "", "Słucham...", "Polski");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_FA = new LanguageTag("fa_IR", "", "", "", "گوش می‌دهم...", "فارسی");

    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH = new LanguageTag("cn", "", "", "", "正在听...", "中文");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH_TW = new LanguageTag("cht", "", "", "", "正在聽...", "中文（繁体）");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EN = new LanguageTag("en", "", "", "", "Listening...", "English");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "聞いています...", "日本語");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_KO = new LanguageTag("ko", "", "", "", "듣고 있어요...", "한국어");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_IT = new LanguageTag("it", "", "", "", "Ascoltando...", "Italiano");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_DE = new LanguageTag("de", "", "", "", "Hörend...", "Deutsch");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_RU = new LanguageTag("ru", "", "", "", "Слушаю...", "Русский");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FR = new LanguageTag("fr", "", "", "", "Écoute...", "Français");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FIL = new LanguageTag("fil", "", "", "", "Nakikinig...", "Filipino");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_HA = new LanguageTag("ha", "", "", "", "Sauraron...", "Hausa");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_NL = new LanguageTag("nl", "", "", "", "Luisteren...", "Nederlands");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_CS = new LanguageTag("cs", "", "", "", "Poslouchám...", "Čeština");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_RO = new LanguageTag("ro", "", "", "", "Ascult...", "Română");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_MS = new LanguageTag("ms", "", "", "", "Mendengar...", "Bahasa Melayu");
    // private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_BN = new LanguageTag("bn", "", "", "", "শুনছি...", "বাংলা");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_PT = new LanguageTag("pt", "", "", "", "Ouvindo...", "Português");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_SV = new LanguageTag("sv", "", "", "", "Lyssnar...", "Svenska");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_SW = new LanguageTag("sw", "", "", "", "Kusikiliza...", "Kiswahili");
    // private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TA = new LanguageTag("ta", "", "", "", "கேட்கிறேன்...", "தமிழ்");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TH = new LanguageTag("th", "", "", "", "กำลังฟัง...", "ไทย");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TR = new LanguageTag("tr", "", "", "", "Dinliyorum...", "Türkçe");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UR = new LanguageTag("ur", "", "", "", "سن رہا ہوں...", "اردو");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UK = new LanguageTag("uk", "", "", "", "Слухаю...", "Українська");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UZ = new LanguageTag("uz", "", "", "", "Eshitmoqda...", "O'zbek");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ES = new LanguageTag("es", "", "", "", "Escuchando...", "Español");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EL = new LanguageTag("el", "", "", "", "Ακούω...", "Ελληνικά");
    // private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_HI = new LanguageTag("hi", "", "", "", "सुन रहा हूँ...", "हिन्दी");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ID = new LanguageTag("id", "", "", "", "Mendengarkan...", "Bahasa Indonesia");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_VI = new LanguageTag("vi", "", "", "", "Đang nghe...", "Tiếng Việt");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_AR = new LanguageTag("ar", "", "", "", "أستمع...", "العربية");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_BG = new LanguageTag("bg", "", "", "", "Слушам...", "Български");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_PL = new LanguageTag("pl", "", "", "", "Słucham...", "Polski");
    private static LanguageTag IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FA = new LanguageTag("fa", "", "", "", "گوش می‌دهم...", "فارسی");

    private static LanguageTag IFLYTEKWEBIATMUL_TRANSCRIBE_LAN_TAG_AUTO = new LanguageTag("auto", "", "", "", "正在听...", "自动识别");

    private static LanguageTag AISPEECH_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("Chinese", "", "", "", "正在听...", "中文");
    private static LanguageTag AISPEECH_TRANSLATE_LAN_TAG_ZH = new LanguageTag("Chinese", "", "", "", "正在听...", "中文");
    private static LanguageTag AISPEECH_TRANSLATE_LAN_TAG_EN = new LanguageTag("English", "", "", "", "Listening...", "English");

    private List<SttLanguageConfig> loadDefaultConfigs() {
        List<SttLanguageConfig> configs = new ArrayList<>();

        /* Azure START */
        SttLanguageConfig azureConfig = new SttLanguageConfig();

        List<LanguageTag> azureTranscribeList = new ArrayList<>();
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_ZH));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_ZH_TW));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_EN));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_JA));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_KO));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_RU));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_DE));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_ES));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_FR));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_IT));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_PT));
        setSelected(azureTranscribeList, AZURE_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> azureTranslateList = new ArrayList<>();

        LanguageTag azureTranslateSource_zh = copyTag(AZURE_TRANSCRIBE_LAN_TAG_ZH);
        List<LanguageTag> azureTranslateSource_zh_targetList = new ArrayList<>();
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_JA));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_KO));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_RU));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_DE));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_FR));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ES));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_IT));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_PT));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_AR));
        setSelected(azureTranslateSource_zh_targetList, AZURE_TRANSLATE_LAN_TAG_EN);
        azureTranslateSource_zh.setTarget(azureTranslateSource_zh_targetList);

        LanguageTag azureTranslateSource_en = copyTag(AZURE_TRANSCRIBE_LAN_TAG_EN);
        List<LanguageTag> azureTranslateSource_en_targetList = new ArrayList<>();
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH_TW));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_JA));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_KO));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_RU));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_DE));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_FR));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ES));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_IT));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_PT));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_AR));
        setSelected(azureTranslateSource_en_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_en.setTarget(azureTranslateSource_en_targetList);

        LanguageTag azureTranslateSource_ja = copyTag(AZURE_TRANSCRIBE_LAN_TAG_JA);
        List<LanguageTag> azureTranslateSource_ja_targetList = new ArrayList<>();
        azureTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH_TW));
        azureTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_ja_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_ja.setTarget(azureTranslateSource_ja_targetList);

        LanguageTag azureTranslateSource_ko = copyTag(AZURE_TRANSCRIBE_LAN_TAG_KO);
        List<LanguageTag> azureTranslateSource_ko_targetList = new ArrayList<>();
        azureTranslateSource_ko_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_ko_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH_TW));
        azureTranslateSource_ko_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_ko_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_ko.setTarget(azureTranslateSource_ko_targetList);

        LanguageTag azureTranslateSource_ru = copyTag(AZURE_TRANSCRIBE_LAN_TAG_RU);
        List<LanguageTag> azureTranslateSource_ru_targetList = new ArrayList<>();
        azureTranslateSource_ru_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_ru_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_ru_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_ru.setTarget(azureTranslateSource_ru_targetList);

        LanguageTag azureTranslateSource_de = copyTag(AZURE_TRANSCRIBE_LAN_TAG_DE);
        List<LanguageTag> azureTranslateSource_de_targetList = new ArrayList<>();
        azureTranslateSource_de_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_de_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_de_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_de.setTarget(azureTranslateSource_de_targetList);

        LanguageTag azureTranslateSource_es = copyTag(AZURE_TRANSCRIBE_LAN_TAG_ES);
        List<LanguageTag> azureTranslateSource_es_targetList = new ArrayList<>();
        azureTranslateSource_es_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_es_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_es_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_es.setTarget(azureTranslateSource_es_targetList);

        LanguageTag azureTranslateSource_fr = copyTag(AZURE_TRANSCRIBE_LAN_TAG_FR);
        List<LanguageTag> azureTranslateSource_fr_targetList = new ArrayList<>();
        azureTranslateSource_fr_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_fr_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_fr_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_fr.setTarget(azureTranslateSource_fr_targetList);

        LanguageTag azureTranslateSource_it = copyTag(AZURE_TRANSCRIBE_LAN_TAG_IT);
        List<LanguageTag> azureTranslateSource_it_targetList = new ArrayList<>();
        azureTranslateSource_it_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_it_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_it_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_it.setTarget(azureTranslateSource_it_targetList);

        LanguageTag azureTranslateSource_pt = copyTag(AZURE_TRANSCRIBE_LAN_TAG_PT);
        List<LanguageTag> azureTranslateSource_pt_targetList = new ArrayList<>();
        azureTranslateSource_pt_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_pt_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_pt_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_pt.setTarget(azureTranslateSource_pt_targetList);

        azureTranslateList.add(azureTranslateSource_zh);
        azureTranslateList.add(azureTranslateSource_en);
        azureTranslateList.add(azureTranslateSource_ja);
        azureTranslateList.add(azureTranslateSource_ko);
        azureTranslateList.add(azureTranslateSource_ru);
        azureTranslateList.add(azureTranslateSource_de);
        azureTranslateList.add(azureTranslateSource_es);
        azureTranslateList.add(azureTranslateSource_fr);
        azureTranslateList.add(azureTranslateSource_it);
        azureTranslateList.add(azureTranslateSource_pt);
        setSelected(azureTranslateList, azureTranslateSource_zh);

        azureConfig.setName("Azure");
        azureConfig.setTranscribe(azureTranscribeList);
        azureConfig.setTranslate(azureTranslateList);

        configs.add(azureConfig);
        /* Azure END */

        /* AzureWestUS Start */
        SttLanguageConfig azureWestUSConfig = new SttLanguageConfig();

        List<LanguageTag> azureWestUSTranscribeList = new ArrayList<>();
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_ZH));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_EN));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_JA));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_KO));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_RU));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_DE));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_ES));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_FR));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_IT));
        azureWestUSTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_PT));
        setSelected(azureWestUSTranscribeList, AZURE_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> azureWestUSTranslateList = new ArrayList<>();

        LanguageTag azureWestUSTranslateSource_zh = copyTag(AZURE_TRANSCRIBE_LAN_TAG_ZH);
        List<LanguageTag> azureWestUSTranslateSource_zh_targetList = new ArrayList<>();
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_JA));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_KO));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_RU));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_DE));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_FR));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ES));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_IT));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_PT));
        azureWestUSTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_AR));
        setSelected(azureWestUSTranslateSource_zh_targetList, AZURE_TRANSLATE_LAN_TAG_EN);
        azureWestUSTranslateSource_zh.setTarget(azureWestUSTranslateSource_zh_targetList);

        LanguageTag azureWestUSTranslateSource_en = copyTag(AZURE_TRANSCRIBE_LAN_TAG_EN);
        List<LanguageTag> azureWestUSTranslateSource_en_targetList = new ArrayList<>();
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_JA));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_KO));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_RU));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_DE));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_FR));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ES));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_IT));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_PT));
        azureWestUSTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_AR));
        setSelected(azureWestUSTranslateSource_en_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_en.setTarget(azureWestUSTranslateSource_en_targetList);

        LanguageTag azureWestUSTranslateSource_ja = copyTag(AZURE_TRANSCRIBE_LAN_TAG_JA);
        List<LanguageTag> azureWestUSTranslateSource_ja_targetList = new ArrayList<>();
        azureWestUSTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureWestUSTranslateSource_ja_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_ja.setTarget(azureWestUSTranslateSource_ja_targetList);

        LanguageTag azureWestUSTranslateSource_ko = copyTag(AZURE_TRANSCRIBE_LAN_TAG_KO);
        List<LanguageTag> azureWestUSTranslateSource_ko_targetList = new ArrayList<>();
        azureWestUSTranslateSource_ko_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_ko_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureWestUSTranslateSource_ko_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_ko.setTarget(azureWestUSTranslateSource_ko_targetList);

        LanguageTag azureWestUSTranslateSource_ru = copyTag(AZURE_TRANSCRIBE_LAN_TAG_RU);
        List<LanguageTag> azureWestUSTranslateSource_ru_targetList = new ArrayList<>();
        azureWestUSTranslateSource_ru_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_ru_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureWestUSTranslateSource_ru_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_ru.setTarget(azureWestUSTranslateSource_ru_targetList);

        LanguageTag azureWestUSTranslateSource_de = copyTag(AZURE_TRANSCRIBE_LAN_TAG_DE);
        List<LanguageTag> azureWestUSTranslateSource_de_targetList = new ArrayList<>();
        azureWestUSTranslateSource_de_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_de_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureWestUSTranslateSource_de_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_de.setTarget(azureWestUSTranslateSource_de_targetList);

        LanguageTag azureWestUSTranslateSource_es = copyTag(AZURE_TRANSCRIBE_LAN_TAG_ES);
        List<LanguageTag> azureWestUSTranslateSource_es_targetList = new ArrayList<>();
        azureWestUSTranslateSource_es_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_es_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureWestUSTranslateSource_es_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_es.setTarget(azureWestUSTranslateSource_es_targetList);

        LanguageTag azureWestUSTranslateSource_fr = copyTag(AZURE_TRANSCRIBE_LAN_TAG_FR);
        List<LanguageTag> azureWestUSTranslateSource_fr_targetList = new ArrayList<>();
        azureWestUSTranslateSource_fr_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_fr_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureWestUSTranslateSource_fr_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_fr.setTarget(azureWestUSTranslateSource_fr_targetList);

        LanguageTag azureWestUSTranslateSource_it = copyTag(AZURE_TRANSCRIBE_LAN_TAG_IT);
        List<LanguageTag> azureWestUSTranslateSource_it_targetList = new ArrayList<>();
        azureWestUSTranslateSource_it_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_it_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureWestUSTranslateSource_it_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_it.setTarget(azureWestUSTranslateSource_it_targetList);

        LanguageTag azureWestUSTranslateSource_pt = copyTag(AZURE_TRANSCRIBE_LAN_TAG_PT);
        List<LanguageTag> azureWestUSTranslateSource_pt_targetList = new ArrayList<>();
        azureWestUSTranslateSource_pt_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureWestUSTranslateSource_pt_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureWestUSTranslateSource_pt_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureWestUSTranslateSource_pt.setTarget(azureWestUSTranslateSource_pt_targetList);

        azureWestUSTranslateList.add(azureWestUSTranslateSource_zh);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_en);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_ja);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_ko);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_ru);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_de);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_es);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_fr);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_it);
        azureWestUSTranslateList.add(azureWestUSTranslateSource_pt);
        setSelected(azureWestUSTranslateList, azureWestUSTranslateSource_zh);

        azureWestUSConfig.setName("AzureWestUS");
        azureWestUSConfig.setTranscribe(azureWestUSTranscribeList);
        azureWestUSConfig.setTranslate(azureWestUSTranslateList);

        configs.add(azureWestUSConfig);
        /* AzureWestUS END */

        /* AzureSwedenCentral Start */
        SttLanguageConfig azureSwedenCentralConfig = new SttLanguageConfig();

        List<LanguageTag> azureSwedenCentralTranscribeList = new ArrayList<>();
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_ZH));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_EN));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_JA));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_KO));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_RU));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_DE));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_ES));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_FR));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_IT));
        azureSwedenCentralTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_PT));
        setSelected(azureSwedenCentralTranscribeList, AZURE_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> azureSwedenCentralTranslateList = new ArrayList<>();

        LanguageTag azureSwedenCentralTranslateSource_zh = copyTag(AZURE_TRANSCRIBE_LAN_TAG_ZH);
        List<LanguageTag> azureSwedenCentralTranslateSource_zh_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_JA));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_KO));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_RU));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_DE));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_FR));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ES));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_IT));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_PT));
        azureSwedenCentralTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_AR));
        setSelected(azureSwedenCentralTranslateSource_zh_targetList, AZURE_TRANSLATE_LAN_TAG_EN);
        azureSwedenCentralTranslateSource_zh.setTarget(azureSwedenCentralTranslateSource_zh_targetList);

        LanguageTag azureSwedenCentralTranslateSource_en = copyTag(AZURE_TRANSCRIBE_LAN_TAG_EN);
        List<LanguageTag> azureSwedenCentralTranslateSource_en_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_JA));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_KO));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_RU));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_DE));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_FR));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ES));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_IT));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_PT));
        azureSwedenCentralTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_AR));
        setSelected(azureSwedenCentralTranslateSource_en_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_en.setTarget(azureSwedenCentralTranslateSource_en_targetList);

        LanguageTag azureSwedenCentralTranslateSource_ja = copyTag(AZURE_TRANSCRIBE_LAN_TAG_JA);
        List<LanguageTag> azureSwedenCentralTranslateSource_ja_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureSwedenCentralTranslateSource_ja_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_ja.setTarget(azureSwedenCentralTranslateSource_ja_targetList);

        LanguageTag azureSwedenCentralTranslateSource_ko = copyTag(AZURE_TRANSCRIBE_LAN_TAG_KO);
        List<LanguageTag> azureSwedenCentralTranslateSource_ko_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_ko_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_ko_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureSwedenCentralTranslateSource_ko_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_ko.setTarget(azureSwedenCentralTranslateSource_ko_targetList);

        LanguageTag azureSwedenCentralTranslateSource_ru = copyTag(AZURE_TRANSCRIBE_LAN_TAG_RU);
        List<LanguageTag> azureSwedenCentralTranslateSource_ru_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_ru_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_ru_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureSwedenCentralTranslateSource_ru_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_ru.setTarget(azureSwedenCentralTranslateSource_ru_targetList);

        LanguageTag azureSwedenCentralTranslateSource_de = copyTag(AZURE_TRANSCRIBE_LAN_TAG_DE);
        List<LanguageTag> azureSwedenCentralTranslateSource_de_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_de_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_de_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureSwedenCentralTranslateSource_de_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_de.setTarget(azureSwedenCentralTranslateSource_de_targetList);

        LanguageTag azureSwedenCentralTranslateSource_es = copyTag(AZURE_TRANSCRIBE_LAN_TAG_ES);
        List<LanguageTag> azureSwedenCentralTranslateSource_es_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_es_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_es_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureSwedenCentralTranslateSource_es_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_es.setTarget(azureSwedenCentralTranslateSource_es_targetList);

        LanguageTag azureSwedenCentralTranslateSource_fr = copyTag(AZURE_TRANSCRIBE_LAN_TAG_FR);
        List<LanguageTag> azureSwedenCentralTranslateSource_fr_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_fr_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_fr_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureSwedenCentralTranslateSource_fr_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_fr.setTarget(azureSwedenCentralTranslateSource_fr_targetList);

        LanguageTag azureSwedenCentralTranslateSource_it = copyTag(AZURE_TRANSCRIBE_LAN_TAG_IT);
        List<LanguageTag> azureSwedenCentralTranslateSource_it_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_it_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_it_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureSwedenCentralTranslateSource_it_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_it.setTarget(azureSwedenCentralTranslateSource_it_targetList);

        LanguageTag azureSwedenCentralTranslateSource_pt = copyTag(AZURE_TRANSCRIBE_LAN_TAG_PT);
        List<LanguageTag> azureSwedenCentralTranslateSource_pt_targetList = new ArrayList<>();
        azureSwedenCentralTranslateSource_pt_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureSwedenCentralTranslateSource_pt_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureSwedenCentralTranslateSource_pt_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureSwedenCentralTranslateSource_pt.setTarget(azureSwedenCentralTranslateSource_pt_targetList);

        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_zh);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_en);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_ja);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_ko);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_ru);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_de);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_es);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_fr);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_it);
        azureSwedenCentralTranslateList.add(azureSwedenCentralTranslateSource_pt);
        setSelected(azureSwedenCentralTranslateList, azureSwedenCentralTranslateSource_zh);

        azureSwedenCentralConfig.setName("AzureSwedenCentral");
        azureSwedenCentralConfig.setTranscribe(azureSwedenCentralTranscribeList);
        azureSwedenCentralConfig.setTranslate(azureSwedenCentralTranslateList);

        configs.add(azureSwedenCentralConfig);
        /* AzureSwedenCentral END */

        /* IFlyTek START */
        SttLanguageConfig IFlyTekConfig = new SttLanguageConfig();

        List<LanguageTag> IFlyTekTranscribeList = new ArrayList<>();
        IFlyTekTranscribeList.add(copyTag(IFLYTEK_TRANSCRIBE_LAN_TAG_ZH));
        IFlyTekTranscribeList.add(copyTag(IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_HENANESE));
        IFlyTekTranscribeList.add(copyTag(IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_XINANESE));
        IFlyTekTranscribeList.add(copyTag(IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_CANTONESE));
        setSelected(IFlyTekTranscribeList, IFLYTEK_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> IFlyTekTranslateList = new ArrayList<>();

        IFlyTekConfig.setName("IFlyTek");
        IFlyTekConfig.setTranscribe(IFlyTekTranscribeList);
        IFlyTekConfig.setTranslate(IFlyTekTranslateList);

        configs.add(IFlyTekConfig);
        /* IFlyTek END */

        /* IFlyTek Web Asr START */
        SttLanguageConfig IFlyTekWebAsrConfig = new SttLanguageConfig();

        List<LanguageTag> IFlyTekWebAsrTranscribeList = new ArrayList<>();
        IFlyTekWebAsrTranscribeList.add(copyTag(IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_ZH));
        IFlyTekWebAsrTranscribeList.add(copyTag(IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_ZH_TW));
        IFlyTekWebAsrTranscribeList.add(copyTag(IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_EN));
        IFlyTekWebAsrTranscribeList.add(copyTag(IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_JA));
        IFlyTekWebAsrTranscribeList.add(copyTag(IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_KO));
        setSelected(IFlyTekWebAsrTranscribeList, IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> IFlyTekWebAsrTranslateList = new ArrayList<>();

        LanguageTag IFlyTekWebAsrTranslateSource_cn = copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH);
        List<LanguageTag> IFlyTekWebAsrTranslateSource_cn_targetList = new ArrayList<>();
        IFlyTekWebAsrTranslateSource_cn_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_EN));
        IFlyTekWebAsrTranslateSource_cn_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_JA));
        IFlyTekWebAsrTranslateSource_cn_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_KO));
        setSelected(IFlyTekWebAsrTranslateSource_cn_targetList, IFLYTEKWEBASR_TRANSLATE_LAN_TAG_EN);
        IFlyTekWebAsrTranslateSource_cn.setTarget(IFlyTekWebAsrTranslateSource_cn_targetList);

        LanguageTag IFlyTekWebAsrTranslateSource_en = copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_EN);
        List<LanguageTag> IFlyTekWebAsrTranslateSource_en_targetList = new ArrayList<>();
        IFlyTekWebAsrTranslateSource_en_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH));
        IFlyTekWebAsrTranslateSource_en_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_JA));
        IFlyTekWebAsrTranslateSource_en_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_KO));
        setSelected(IFlyTekWebAsrTranslateSource_en_targetList, IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebAsrTranslateSource_en.setTarget(IFlyTekWebAsrTranslateSource_en_targetList);

        LanguageTag IFlyTekWebAsrTranslateSource_ja = copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_JA);
        List<LanguageTag> IFlyTekWebAsrTranslateSource_ja_targetList = new ArrayList<>();
        IFlyTekWebAsrTranslateSource_ja_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH));
        IFlyTekWebAsrTranslateSource_ja_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_EN));
        IFlyTekWebAsrTranslateSource_ja_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_KO));
        setSelected(IFlyTekWebAsrTranslateSource_ja_targetList, IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebAsrTranslateSource_ja.setTarget(IFlyTekWebAsrTranslateSource_ja_targetList);

        LanguageTag IFlyTekWebAsrTranslateSource_ko = copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_KO);
        List<LanguageTag> IFlyTekWebAsrTranslateSource_ko_targetList = new ArrayList<>();
        IFlyTekWebAsrTranslateSource_ko_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH));
        IFlyTekWebAsrTranslateSource_ko_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_EN));
        IFlyTekWebAsrTranslateSource_ko_targetList.add(copyTag(IFLYTEKWEBASR_TRANSLATE_LAN_TAG_JA));
        setSelected(IFlyTekWebAsrTranslateSource_ko_targetList, IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebAsrTranslateSource_ko.setTarget(IFlyTekWebAsrTranslateSource_ko_targetList);

        IFlyTekWebAsrTranslateList.add(IFlyTekWebAsrTranslateSource_cn);
        IFlyTekWebAsrTranslateList.add(IFlyTekWebAsrTranslateSource_en);
        IFlyTekWebAsrTranslateList.add(IFlyTekWebAsrTranslateSource_ja);
        IFlyTekWebAsrTranslateList.add(IFlyTekWebAsrTranslateSource_ko);
        setSelected(IFlyTekWebAsrTranslateList, IFlyTekWebAsrTranslateSource_cn);

        IFlyTekWebAsrConfig.setName("IFlyTekWebAsr");
        IFlyTekWebAsrConfig.setTranscribe(IFlyTekWebAsrTranscribeList);
        IFlyTekWebAsrConfig.setTranslate(IFlyTekWebAsrTranslateList);

        configs.add(IFlyTekWebAsrConfig);
        /* IFlyTek Web Asr END */

        /* IFlyTek Web Niu Asr START */
        SttLanguageConfig IFlyTekWebNiuAsrConfig = new SttLanguageConfig();

        List<LanguageTag> IFlyTekWebNiuAsrTranscribeList = new ArrayList<>();
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ZH));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ZH_TW));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_EN));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_KO));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_JA));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_IT));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_DE));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_RU));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_FR));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_FIL));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_HA));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_NL));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_CS));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_RO));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_MS));
        // IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_BN));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_PT));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_SV));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_SW));
        // IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_TA));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_TH));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_TR));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_UR));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_UK));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_UZ));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ES));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_EL));
        // IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_HI));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ID));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_VI));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_AR));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_BG));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_PL));
        IFlyTekWebNiuAsrTranscribeList.add(copyTag(IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_FA));
        setSelected(IFlyTekWebNiuAsrTranscribeList, IFLYTEKWEBNIUASR_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> IFlyTekWebNiuAsrTranslateList = new ArrayList<>();

        LanguageTag IFlyTekWebNiuAsrTranslateSource_cn = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_cn_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        setSelected(IFlyTekWebNiuAsrTranslateSource_cn_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EN);
        IFlyTekWebNiuAsrTranslateSource_cn.setTarget(IFlyTekWebNiuAsrTranslateSource_cn_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_cn_tw = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH_TW);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_cn_tw_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH_TW);
        setSelected(IFlyTekWebNiuAsrTranslateSource_cn_tw_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EN);
        IFlyTekWebNiuAsrTranslateSource_cn_tw.setTarget(IFlyTekWebNiuAsrTranslateSource_cn_tw_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_en = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EN);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_en_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EN);
        setSelected(IFlyTekWebNiuAsrTranslateSource_en_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_en.setTarget(IFlyTekWebNiuAsrTranslateSource_en_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_ko = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_KO);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ko_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_KO);
        setSelected(IFlyTekWebNiuAsrTranslateSource_ko_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_ko.setTarget(IFlyTekWebNiuAsrTranslateSource_ko_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_ja = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_JA);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ja_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_JA);
        setSelected(IFlyTekWebNiuAsrTranslateSource_ja_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_ja.setTarget(IFlyTekWebNiuAsrTranslateSource_ja_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_it = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_IT);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_it_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_IT);
        setSelected(IFlyTekWebNiuAsrTranslateSource_it_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_it.setTarget(IFlyTekWebNiuAsrTranslateSource_it_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_de = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_DE);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_de_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_DE);
        setSelected(IFlyTekWebNiuAsrTranslateSource_de_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_de.setTarget(IFlyTekWebNiuAsrTranslateSource_de_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_ru = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_RU);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ru_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_RU);
        setSelected(IFlyTekWebNiuAsrTranslateSource_ru_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_ru.setTarget(IFlyTekWebNiuAsrTranslateSource_ru_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_fr = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FR);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_fr_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FR);
        setSelected(IFlyTekWebNiuAsrTranslateSource_fr_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_fr.setTarget(IFlyTekWebNiuAsrTranslateSource_fr_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_fil = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FIL);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_fil_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FIL);
        setSelected(IFlyTekWebNiuAsrTranslateSource_fil_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_fil.setTarget(IFlyTekWebNiuAsrTranslateSource_fil_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_ha = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_HA);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ha_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_HA);
        setSelected(IFlyTekWebNiuAsrTranslateSource_ha_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_ha.setTarget(IFlyTekWebNiuAsrTranslateSource_ha_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_nl = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_NL);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_nl_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_NL);
        setSelected(IFlyTekWebNiuAsrTranslateSource_nl_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_nl.setTarget(IFlyTekWebNiuAsrTranslateSource_nl_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_cs = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_CS);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_cs_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_CS);
        setSelected(IFlyTekWebNiuAsrTranslateSource_cs_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_cs.setTarget(IFlyTekWebNiuAsrTranslateSource_cs_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_ro = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_RO);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ro_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_RO);
        setSelected(IFlyTekWebNiuAsrTranslateSource_ro_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_ro.setTarget(IFlyTekWebNiuAsrTranslateSource_ro_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_ms = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_MS);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ms_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_MS);
        setSelected(IFlyTekWebNiuAsrTranslateSource_ms_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_ms.setTarget(IFlyTekWebNiuAsrTranslateSource_ms_targetList);

        // LanguageTag IFlyTekWebNiuAsrTranslateSource_bn = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_BN);
        // List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_bn_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_BN);
        // setSelected(IFlyTekWebNiuAsrTranslateSource_bn_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        // IFlyTekWebNiuAsrTranslateSource_bn.setTarget(IFlyTekWebNiuAsrTranslateSource_bn_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_pt = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_PT);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_pt_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_PT);
        setSelected(IFlyTekWebNiuAsrTranslateSource_pt_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_pt.setTarget(IFlyTekWebNiuAsrTranslateSource_pt_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_sv = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_SV);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_sv_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_SV);
        setSelected(IFlyTekWebNiuAsrTranslateSource_sv_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_sv.setTarget(IFlyTekWebNiuAsrTranslateSource_sv_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_sw = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_SW);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_sw_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_SW);
        setSelected(IFlyTekWebNiuAsrTranslateSource_sw_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_sw.setTarget(IFlyTekWebNiuAsrTranslateSource_sw_targetList);

        // LanguageTag IFlyTekWebNiuAsrTranslateSource_ta = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TA);
        // List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ta_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TA);
        // setSelected(IFlyTekWebNiuAsrTranslateSource_ta_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        // IFlyTekWebNiuAsrTranslateSource_ta.setTarget(IFlyTekWebNiuAsrTranslateSource_ta_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_th = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TH);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_th_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TH);
        setSelected(IFlyTekWebNiuAsrTranslateSource_th_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_th.setTarget(IFlyTekWebNiuAsrTranslateSource_th_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_tr = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TR);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_tr_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_TR);
        setSelected(IFlyTekWebNiuAsrTranslateSource_tr_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_tr.setTarget(IFlyTekWebNiuAsrTranslateSource_tr_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_ur = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UR);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ur_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UR);
        setSelected(IFlyTekWebNiuAsrTranslateSource_ur_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_ur.setTarget(IFlyTekWebNiuAsrTranslateSource_ur_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_uk = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UK);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_uk_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UK);
        setSelected(IFlyTekWebNiuAsrTranslateSource_uk_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_uk.setTarget(IFlyTekWebNiuAsrTranslateSource_uk_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_uz = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UZ);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_uz_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_UZ);
        setSelected(IFlyTekWebNiuAsrTranslateSource_uz_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_uz.setTarget(IFlyTekWebNiuAsrTranslateSource_uz_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_es = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ES);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_es_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ES);
        setSelected(IFlyTekWebNiuAsrTranslateSource_es_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_es.setTarget(IFlyTekWebNiuAsrTranslateSource_es_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_el = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EL);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_el_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_EL);
        setSelected(IFlyTekWebNiuAsrTranslateSource_el_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_el.setTarget(IFlyTekWebNiuAsrTranslateSource_el_targetList);

        // LanguageTag IFlyTekWebNiuAsrTranslateSource_hi = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_HI);
        // List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_hi_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_HI);
        // setSelected(IFlyTekWebNiuAsrTranslateSource_hi_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        // IFlyTekWebNiuAsrTranslateSource_hi.setTarget(IFlyTekWebNiuAsrTranslateSource_hi_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_id = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ID);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_id_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ID);
        setSelected(IFlyTekWebNiuAsrTranslateSource_id_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_id.setTarget(IFlyTekWebNiuAsrTranslateSource_id_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_vi = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_VI);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_vi_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_VI);
        setSelected(IFlyTekWebNiuAsrTranslateSource_vi_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_vi.setTarget(IFlyTekWebNiuAsrTranslateSource_vi_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_ar = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_AR);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_ar_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_AR);
        setSelected(IFlyTekWebNiuAsrTranslateSource_ar_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_ar.setTarget(IFlyTekWebNiuAsrTranslateSource_ar_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_bg = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_BG);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_bg_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_BG);
        setSelected(IFlyTekWebNiuAsrTranslateSource_bg_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_bg.setTarget(IFlyTekWebNiuAsrTranslateSource_bg_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_pl = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_PL);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_pl_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_PL);
        setSelected(IFlyTekWebNiuAsrTranslateSource_pl_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_pl.setTarget(IFlyTekWebNiuAsrTranslateSource_pl_targetList);

        LanguageTag IFlyTekWebNiuAsrTranslateSource_fa = copyTag(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FA);
        List<LanguageTag> IFlyTekWebNiuAsrTranslateSource_fa_targetList = createTargetListExcluding(IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_FA);
        setSelected(IFlyTekWebNiuAsrTranslateSource_fa_targetList, IFLYTEKWEBNIUASR_TRANSLATE_LAN_TAG_ZH);
        IFlyTekWebNiuAsrTranslateSource_fa.setTarget(IFlyTekWebNiuAsrTranslateSource_fa_targetList);

        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_cn);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_cn_tw);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_en);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ko);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ja);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_it);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_de);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ru);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_fr);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_fil);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ha);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_nl);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_cs);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ro);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ms);
        // IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_bn);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_pt);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_sv);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_sw);
        // IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ta);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_th);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_tr);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ur);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_uk);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_uz);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_es);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_el);
        // IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_hi);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_id);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_vi);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_ar);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_bg);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_pl);
        IFlyTekWebNiuAsrTranslateList.add(IFlyTekWebNiuAsrTranslateSource_fa);
        setSelected(IFlyTekWebNiuAsrTranslateList, IFlyTekWebNiuAsrTranslateSource_cn);

        IFlyTekWebNiuAsrConfig.setName("IFlyTekWebNiuAsr");
        IFlyTekWebNiuAsrConfig.setTranscribe(IFlyTekWebNiuAsrTranscribeList);
        IFlyTekWebNiuAsrConfig.setTranslate(IFlyTekWebNiuAsrTranslateList);

        configs.add(IFlyTekWebNiuAsrConfig);
        /* IFlyTek Web Niu Asr END */

        /* IFlyTek Web IatMul START */
        SttLanguageConfig IFlyTekWebIatMulConfig = new SttLanguageConfig();

        List<LanguageTag> IFlyTekWebIatMulTranscribeList = new ArrayList<>();
        IFlyTekWebIatMulTranscribeList.add(copyTag(IFLYTEKWEBIATMUL_TRANSCRIBE_LAN_TAG_AUTO));
        setSelected(IFlyTekWebIatMulTranscribeList, IFLYTEKWEBIATMUL_TRANSCRIBE_LAN_TAG_AUTO);

        List<LanguageTag> IFlyTekWebIatMulTranslateList = new ArrayList<>();

        IFlyTekWebIatMulConfig.setName("IFlyTekWebIatMul");
        IFlyTekWebIatMulConfig.setTranscribe(IFlyTekWebIatMulTranscribeList);
        IFlyTekWebIatMulConfig.setTranslate(IFlyTekWebIatMulTranslateList);

        configs.add(IFlyTekWebIatMulConfig);
        /* IFlyTek Web IatMul END */

        /* AiSpeech START */
        SttLanguageConfig AiSpeechConfig = new SttLanguageConfig();

        List<LanguageTag> AiSpeechTranscribeList = new ArrayList<>();
        AiSpeechTranscribeList.add(copyTag(AISPEECH_TRANSCRIBE_LAN_TAG_ZH));
        setSelected(AiSpeechTranscribeList, AISPEECH_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> AiSpeechTranslateList = new ArrayList<>();

        LanguageTag AiSpeechTranslateSource_zh = copyTag(AISPEECH_TRANSLATE_LAN_TAG_ZH);
        List<LanguageTag> AiSpeechTranslateSource_zh_targetList = new ArrayList<>();
        AiSpeechTranslateSource_zh_targetList.add(copyTag(AISPEECH_TRANSLATE_LAN_TAG_EN));
        setSelected(AiSpeechTranslateSource_zh_targetList, AISPEECH_TRANSLATE_LAN_TAG_EN);
        AiSpeechTranslateSource_zh.setTarget(AiSpeechTranslateSource_zh_targetList);

        AiSpeechTranslateList.add(AiSpeechTranslateSource_zh);
        setSelected(AiSpeechTranslateList, AISPEECH_TRANSLATE_LAN_TAG_ZH);

        AiSpeechConfig.setName("AiSpeech");
        AiSpeechConfig.setTranscribe(AiSpeechTranscribeList);
        AiSpeechConfig.setTranslate(AiSpeechTranslateList);

        configs.add(AiSpeechConfig);
        /* AiSpeech END */

        /* Mock START */
        SttLanguageConfig mockConfig = new SttLanguageConfig();

        List<LanguageTag> mockTranscribeList = new ArrayList<>();
        mockTranscribeList.add(copyTag(AISPEECH_TRANSCRIBE_LAN_TAG_ZH));
        setSelected(mockTranscribeList, AISPEECH_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> mockTranslateList = new ArrayList<>();

        LanguageTag mockTranslateSource_zh = copyTag(AISPEECH_TRANSLATE_LAN_TAG_ZH);
        List<LanguageTag> mockTranslateSource_zh_targetList = new ArrayList<>();
        mockTranslateSource_zh_targetList.add(copyTag(AISPEECH_TRANSLATE_LAN_TAG_EN));
        setSelected(mockTranslateSource_zh_targetList, AISPEECH_TRANSLATE_LAN_TAG_EN);
        mockTranslateSource_zh.setTarget(mockTranslateSource_zh_targetList);

        mockTranslateList.add(mockTranslateSource_zh);
        setSelected(mockTranslateList, AISPEECH_TRANSLATE_LAN_TAG_ZH);

        mockConfig.setName("Mock");
        mockConfig.setTranscribe(mockTranscribeList);
        mockConfig.setTranslate(mockTranslateList);

        configs.add(mockConfig);
        /* Mock END */

        return configs;
    }
}