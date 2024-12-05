package com.jueyuantech.glasses.stt;

import static com.jueyuantech.glasses.common.Constants.ASR_ENGINE_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSLATE;
import static com.jueyuantech.glasses.common.Constants.MMKV_ASR_ENGINE_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_ASR_FUNC_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_ASR_LANGUAGE_CONFIG_KEY;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jueyuantech.glasses.bean.AsrLanguageConfig;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.util.MmkvUtil;

import java.util.ArrayList;
import java.util.List;

public class AsrConfigManager {
    private volatile static AsrConfigManager mInstance;

    private Gson gson = new Gson();
    private List<AsrLanguageConfig> asrLanguageConfigs;
    private AsrLanguageConfig mAsrLanguageConfig;

    private AsrConfigManager() {
        init();
    }

    public static AsrConfigManager getInstance() {
        if (mInstance == null) {
            synchronized (AsrConfigManager.class) {
                if (mInstance == null) {
                    mInstance = new AsrConfigManager();
                }
            }
        }
        return mInstance;
    }

    private void init() {
        String asrLanguageConfigStr = MmkvUtil.decodeString(MMKV_ASR_LANGUAGE_CONFIG_KEY, "");
        if (TextUtils.isEmpty(asrLanguageConfigStr)) {
            asrLanguageConfigs = loadDefaultConfigs();
            save();
        } else {
            asrLanguageConfigs = gson.fromJson(asrLanguageConfigStr, new TypeToken<List<AsrLanguageConfig>>() {
            }.getType());
        }

        updateEngineConfig();
    }

    private void save() {
        MmkvUtil.encode(MMKV_ASR_LANGUAGE_CONFIG_KEY, gson.toJson(asrLanguageConfigs));
    }

    public String getEngine() {
        return MmkvUtil.decodeString(MMKV_ASR_ENGINE_KEY, ASR_ENGINE_DEFAULT);
    }

    public void setEngine(String asrEngine) {
        MmkvUtil.encode(MMKV_ASR_ENGINE_KEY, asrEngine);
        updateEngineConfig();
    }

    public void updateEngineConfig() {
        String engine = getEngine();
        for (AsrLanguageConfig asrLanguageConfig : asrLanguageConfigs) {
            if (engine.equals(asrLanguageConfig.getName())) {
                mAsrLanguageConfig = asrLanguageConfig;
                break;
            }
        }
    }

    public String getFunc() {
        return MmkvUtil.decodeString(MMKV_ASR_FUNC_KEY, ASR_FUNC_DEFAULT);
    }

    public void setFunc(String func) {
        MmkvUtil.encode(MMKV_ASR_FUNC_KEY, func);
    }

    public List<LanguageTag> getSourceLanTags(String engine, String func) {
        List<LanguageTag> tags = new ArrayList<>();
        for (AsrLanguageConfig asrLanguageConfig : asrLanguageConfigs) {
            if (engine.equals(asrLanguageConfig.getName())) {
                mAsrLanguageConfig = asrLanguageConfig;
                if (ASR_FUNC_TRANSCRIBE.equals(func)) {
                    tags.addAll(mAsrLanguageConfig.getTranscribe());
                } else if (ASR_FUNC_TRANSLATE.equals(func)) {
                    tags.addAll(mAsrLanguageConfig.getTranslate());
                }
            }
        }
        return tags;
    }

    public void setSourceLanTag(String asrEngine, String func, String sourceTagKey) {
        for (AsrLanguageConfig asrLanguageConfig : asrLanguageConfigs) {
            if (asrEngine.equals(asrLanguageConfig.getName())) {
                mAsrLanguageConfig = asrLanguageConfig;
                if (ASR_FUNC_TRANSCRIBE.equals(func)) {
                    for (LanguageTag tag : mAsrLanguageConfig.getTranscribe()) {
                        if (sourceTagKey.equals(tag.getTag())) {
                            tag.setSelected(true);
                        } else {
                            tag.setSelected(false);
                        }
                    }
                } else if (ASR_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag tag : mAsrLanguageConfig.getTranslate()) {
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

    public LanguageTag getSourceLanTag(String asrEngine, String func) {
        for (AsrLanguageConfig asrLanguageConfig : asrLanguageConfigs) {
            if (asrEngine.equals(asrLanguageConfig.getName())) {
                mAsrLanguageConfig = asrLanguageConfig;
                if (ASR_FUNC_TRANSCRIBE.equals(func)) {
                    for (LanguageTag tag : asrLanguageConfig.getTranscribe()) {
                        if (tag.isSelected()) {
                            return tag;
                        }
                    }
                } else if (ASR_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag tag : asrLanguageConfig.getTranslate()) {
                        if (tag.isSelected()) {
                            return tag;
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<LanguageTag> getTargetLanTags(String asrEngine, String func, String sourceKey) {
        List<LanguageTag> tags = new ArrayList<>();
        for (AsrLanguageConfig asrLanguageConfig : asrLanguageConfigs) {
            if (asrEngine.equals(asrLanguageConfig.getName())) {
                mAsrLanguageConfig = asrLanguageConfig;
                if (ASR_FUNC_TRANSCRIBE.equals(func)) {
                    tags.addAll(mAsrLanguageConfig.getTranscribe());
                } else if (ASR_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag sourceTag : mAsrLanguageConfig.getTranslate()) {
                        if (sourceTag.getTag().equals(sourceKey)) {
                            tags.addAll(sourceTag.getTarget());
                        }
                    }
                }
            }
        }
        return tags;
    }

    public void setTargetLanTag(String asrEngine, String func, String sourceTagKey, String targetTagKey) {
        for (AsrLanguageConfig asrLanguageConfig : asrLanguageConfigs) {
            if (asrEngine.equals(asrLanguageConfig.getName())) {
                mAsrLanguageConfig = asrLanguageConfig;
                if (ASR_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag tag : mAsrLanguageConfig.getTranslate()) {
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

    public LanguageTag getTargetLanTag(String asrEngine, String func, String sourceKey) {
        for (AsrLanguageConfig asrLanguageConfig : asrLanguageConfigs) {
            if (asrEngine.equals(asrLanguageConfig.getName())) {
                mAsrLanguageConfig = asrLanguageConfig;
                if (ASR_FUNC_TRANSLATE.equals(func)) {
                    for (LanguageTag sourceTag : asrLanguageConfig.getTranslate()) {
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
        newTag.setTitle(tag.getTitle());
        return newTag;
    }


    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("zh-CN", "Chinese", "China", "Mainland China, simplified characters", "中文");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_EN = new LanguageTag("en-US", "English", "United States", "US English", "English");
    private static LanguageTag AZURE_TRANSCRIBE_LAN_TAG_JA = new LanguageTag("ja-JP", "Japanese", "Japan", "Japanese (Japan)", "日本語");

    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_ZH = new LanguageTag("zh-Hans", "", "", "", "中文");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_EN = new LanguageTag("en", "", "", "", "English");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "日本語");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_RU = new LanguageTag("ru", "", "", "", "Русский");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_DE = new LanguageTag("de", "", "", "", "Deutsch");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_FR = new LanguageTag("fr", "", "", "", "Français");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_ES = new LanguageTag("es", "", "", "", "Español");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_PT = new LanguageTag("pt", "", "", "", "Português");
    private static LanguageTag AZURE_TRANSLATE_LAN_TAG_AR = new LanguageTag("ar", "", "", "", "العربية");

    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("zh", "", "", "", "中文");
    private static LanguageTag IFLYTEK_TRANSCRIBE_LAN_TAG_JA = new LanguageTag("ja", "", "", "", "日本語");

    private static LanguageTag AISPEECH_TRANSCRIBE_LAN_TAG_ZH = new LanguageTag("Chinese", "", "", "", "中文");
    private static LanguageTag AISPEECH_TRANSLATE_LAN_TAG_ZH = new LanguageTag("Chinese", "", "", "", "中文");
    private static LanguageTag AISPEECH_TRANSLATE_LAN_TAG_EN = new LanguageTag("English", "", "", "", "English");

    private List<AsrLanguageConfig> loadDefaultConfigs() {
        List<AsrLanguageConfig> configs = new ArrayList<>();

        /* Azure START */
        AsrLanguageConfig azureConfig = new AsrLanguageConfig();

        List<LanguageTag> azureTranscribeList = new ArrayList<>();
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_ZH));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_EN));
        azureTranscribeList.add(copyTag(AZURE_TRANSCRIBE_LAN_TAG_JA));
        setSelected(azureTranscribeList, AZURE_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> azureTranslateList = new ArrayList<>();

        LanguageTag azureTranslateSource_zh = copyTag(AZURE_TRANSLATE_LAN_TAG_ZH);
        List<LanguageTag> azureTranslateSource_zh_targetList = new ArrayList<>();
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_JA));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_RU));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_DE));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_FR));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ES));
        azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_PT));
        //azureTranslateSource_zh_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_AR));
        setSelected(azureTranslateSource_zh_targetList, AZURE_TRANSLATE_LAN_TAG_EN);
        azureTranslateSource_zh.setTarget(azureTranslateSource_zh_targetList);

        LanguageTag azureTranslateSource_en = copyTag(AZURE_TRANSLATE_LAN_TAG_EN);
        List<LanguageTag> azureTranslateSource_en_targetList = new ArrayList<>();
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_en_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_JA));
        setSelected(azureTranslateSource_en_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_en.setTarget(azureTranslateSource_en_targetList);

        LanguageTag azureTranslateSource_ja = copyTag(AZURE_TRANSLATE_LAN_TAG_JA);
        List<LanguageTag> azureTranslateSource_ja_targetList = new ArrayList<>();
        azureTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_ZH));
        azureTranslateSource_ja_targetList.add(copyTag(AZURE_TRANSLATE_LAN_TAG_EN));
        setSelected(azureTranslateSource_ja_targetList, AZURE_TRANSLATE_LAN_TAG_ZH);
        azureTranslateSource_ja.setTarget(azureTranslateSource_ja_targetList);

        azureTranslateList.add(azureTranslateSource_zh);
        azureTranslateList.add(azureTranslateSource_en);
        azureTranslateList.add(azureTranslateSource_ja);
        setSelected(azureTranslateList, azureTranslateSource_zh);

        azureConfig.setName("Azure");
        azureConfig.setTranscribe(azureTranscribeList);
        azureConfig.setTranslate(azureTranslateList);

        configs.add(azureConfig);
        /* Azure END */

        /* IFlyTek START */
        AsrLanguageConfig IFlyTekConfig = new AsrLanguageConfig();

        List<LanguageTag> IFlyTekTranscribeList = new ArrayList<>();
        IFlyTekTranscribeList.add(copyTag(IFLYTEK_TRANSCRIBE_LAN_TAG_ZH));
        IFlyTekTranscribeList.add(copyTag(IFLYTEK_TRANSCRIBE_LAN_TAG_JA));
        setSelected(IFlyTekTranscribeList, IFLYTEK_TRANSCRIBE_LAN_TAG_ZH);

        List<LanguageTag> IFlyTekTranslateList = new ArrayList<>();

        IFlyTekConfig.setName("IFlyTek");
        IFlyTekConfig.setTranscribe(IFlyTekTranscribeList);
        IFlyTekConfig.setTranslate(IFlyTekTranslateList);

        configs.add(IFlyTekConfig);
        /* IFlyTek END */

        /* AiSpeech START */
        AsrLanguageConfig AiSpeechConfig = new AsrLanguageConfig();

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
        AsrLanguageConfig mockConfig = new AsrLanguageConfig();

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