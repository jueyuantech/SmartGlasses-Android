package com.jueyuantech.glasses.bean;

import java.io.Serializable;

public class SpeechSession implements Serializable {

    /**
     * id : 0
     * uuid :
     * userId :
     * title :
     * funcType : 0
     * startTime : 1696732040000
     * endTime : 1696732040000
     * duration : 1696732040000
     * latitude :
     * longitude :
     * audioFilePath :
     * audioFileName :
     * audioSource :
     * delete : 0
     * createDate : 1696732040000
     * updateDate : 1696732040000
     */

    private int id;
    private String uuid;
    private String userId;
    private String title;
    private String funcType;
    private long startTime;
    private long endTime;
    private long duration;
    private String latitude;
    private String longitude;
    private String audioFilePath;
    private String audioFileName;
    private String audioSource;
    private int state;
    private int delete;
    private long createDate;
    private long updateDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFuncType() {
        return funcType;
    }

    public void setFuncType(String funcType) {
        this.funcType = funcType;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(String audioSource) {
        this.audioSource = audioSource;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getDelete() {
        return delete;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }
}
