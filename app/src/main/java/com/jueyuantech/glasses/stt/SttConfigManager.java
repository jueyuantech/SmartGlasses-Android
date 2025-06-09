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


    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("zh-CN", "Chinese", "China", "Mainland China, simplified characters", "正在听...", "中文");
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
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_EN = new LanguageTag("en", "", "", "", "Listening...", "English");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "聞いています...", "日本語");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_KO = new LanguageTag("ko", "", "", "", "Listening...", "한국어");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_RU = new LanguageTag("ru", "", "", "", "Listening...", "Русский");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_DE = new LanguageTag("de", "", "", "", "Listening...", "Deutsch");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_FR = new LanguageTag("fr", "", "", "", "Listening...", "Français");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_ES = new LanguageTag("es", "", "", "", "Listening...", "Español");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_IT = new LanguageTag("it", "", "", "", "Listening...", "italiano");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_PT = new LanguageTag("pt", "", "", "", "Listening...", "Português");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_AR = new LanguageTag("ar", "", "", "", "Listening...", "العربية");

    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("cn", "", "", "", "正在听...", "中文");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "聞いています...", "日本語");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_HENANESE = new LanguageTag("cn_henanese", "", "", "", "正在听...", "河南话");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_XINANESE = new LanguageTag("cn_xinanese", "", "", "", "正在听...", "西南官话（川渝云贵）");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH_CANTONESE = new LanguageTag("cn_cantonese", "", "", "", "正在听...", "广东话");

    private static LanguageTag IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("zh_cn", "", "", "", "正在听...", "中文");
    private static LanguageTag IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_EN = new LanguageTag("en_us", "", "", "", "Listening...", "English");

    private static LanguageTag IFLYTEKWEBASR_TRANSLATE_LAN_TAG_ZH = new LanguageTag("cn", "", "", "", "正在听...", "中文");
    private static LanguageTag IFLYTEKWEBASR_TRANSLATE_LAN_TAG_EN = new LanguageTag("en", "", "", "", "Listening...", "English");
    private static LanguageTag IFLYTEKWEBASR_TRANSLATE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "聞いています...", "日本語");
    private static LanguageTag IFLYTEKWEBASR_TRANSLATE_LAN_TAG_KO = new LanguageTag("ko", "", "", "", "Listening...", "한국어");

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
        azureTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_ja_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_ja.setTarget(azureTranslateSource_ja_targetList);

        LanguageTag azureTranslateSource_ko = copyTag(AZURE_TRANSCRIBE_LAN_TAG_KO);
        List<LanguageTag> azureTranslateSource_ko_targetList = new ArrayList<>();
        azureTranslateSource_ko_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
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
        IFlyTekWebAsrTranscribeList.add(copyTag(IFLYTEKWEBASR_TRANSCRIBE_LAN_TAG_EN));
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

        IFlyTekWebAsrTranslateList.add(IFlyTekWebAsrTranslateSource_cn);
        IFlyTekWebAsrTranslateList.add(IFlyTekWebAsrTranslateSource_en);
        setSelected(IFlyTekWebAsrTranslateList, IFlyTekWebAsrTranslateSource_cn);

        IFlyTekWebAsrConfig.setName("IFlyTekWebAsr");
        IFlyTekWebAsrConfig.setTranscribe(IFlyTekWebAsrTranscribeList);
        IFlyTekWebAsrConfig.setTranslate(IFlyTekWebAsrTranslateList);

        configs.add(IFlyTekWebAsrConfig);
        /* IFlyTek Web Asr END */

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