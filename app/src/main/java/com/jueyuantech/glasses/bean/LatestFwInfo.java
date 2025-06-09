package com.jueyuantech.glasses.bean;

import java.io.Serializable;

public class LatestFwInfo implements Serializable {

    /**
     * id : 202
     * verCode : 10926
     * verName : V1.2.2
     * description : temp build
     * fileSize : 0
     * fileMd5 :
     */

    private int id;
    private int verCode;
    private String verName;
    private String description;
    private int fileSize;
    private String fileMd5;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVerCode() {
        return verCode;
    }

    public void setVerCode(int verCode) {
        this.verCode = verCode;
    }

    public String getVerName() {
        return verName;
    }

    public void setVerName(String verName) {
        this.verName = verName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }
}