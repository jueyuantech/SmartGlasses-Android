package com.jueyuantech.glasses.stt.aispeech.bean;

public class AudioBean {
    /**
     * audioType : wav
     * sampleRate : 16000
     * sampleBytes : 2
     * channel : 1
     */

    private String audioType;
    private int sampleRate;
    private int sampleBytes;
    private int channel;

    public String getAudioType() {
        return audioType;
    }

    /**
     * 音频类型支持：wav, ogg(speex), ogg_opus, mp3
     * @param audioType
     */
    public void setAudioType(String audioType) {
        this.audioType = audioType;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * 音频采样率
     * @param sampleRate
     */
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getSampleBytes() {
        return sampleBytes;
    }

    /**
     * 取样字节数
     * @param sampleBytes
     */
    public void setSampleBytes(int sampleBytes) {
        this.sampleBytes = sampleBytes;
    }

    public int getChannel() {
        return channel;
    }

    /**
     * 音频通道数
     * @param channel
     */
    public void setChannel(int channel) {
        this.channel = channel;
    }
}
