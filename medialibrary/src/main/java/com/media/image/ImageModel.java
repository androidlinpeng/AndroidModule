package com.media.image;

import java.io.Serializable;

public class ImageModel implements Serializable {

    public static final String TYPE_IMAGE = "img";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_ALL = "all";
    public static final String TYPE_CAMERA = "camera";

    public String id;//图片id
    public String path;//路径
    public String thumb;//缩列图
    public String fileName;
    public int size;
    public int duration;
    public int pisNum;
    public String type;
    public Boolean isChecked = false;//是否被选中
    public Boolean isCurrent = false;//是否是当前
    public String sendPath;
    public int width;
    public int height;

    public ImageModel() {
    }

    public ImageModel(String id, String path, String fileName, String type) {
        this.id = id;
        this.path = path;
        this.fileName = fileName;
        this.type = type;
    }

    public ImageModel(String id, String path, String type) {
        this.id = id;
        this.path = path;
        this.type = type;
    }

    public ImageModel(String id, String path, int size, int duration, String type) {
        this.id = id;
        this.path = path;
        this.size = size;
        this.duration = duration;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPisNum() {
        return pisNum;
    }

    public void setPisNum(int pisNum) {
        this.pisNum = pisNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(Boolean isChecked) {
        this.isChecked = isChecked;
    }

    public Boolean getCurrent() {
        return isCurrent;
    }

    public void setCurrent(Boolean current) {
        isCurrent = current;
    }

    public String getSendPath() {
        return sendPath;
    }

    public void setSendPath(String sendPath) {
        this.sendPath = sendPath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ImageModel(Builder builder) {
        this.id = builder.id;
        this.path = builder.path;
        this.thumb = builder.thumb;
        this.fileName = builder.fileName;
        this.size = builder.size;
        this.duration = builder.duration;
        this.pisNum = builder.pisNum;
        this.type = builder.type;
        this.isChecked = builder.isChecked;
        this.isCurrent = builder.isCurrent;
        this.sendPath = builder.sendPath;
        this.width = builder.width;
        this.height = builder.height;
    }

    public static class Builder {

        private String id;//图片id
        private String path;//路径
        private String thumb;//缩列图
        private String fileName;
        private int size;
        private int duration;
        private int pisNum;
        private String type;
        private Boolean isChecked = false;//是否被选中
        private Boolean isCurrent = false;//是否是当前
        private String sendPath;
        private int width;
        private int height;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder thumb(String thumb) {
            this.thumb = thumb;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder pisNum(int pisNum) {
            this.pisNum = pisNum;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder isChecked(boolean isChecked) {
            this.isChecked = isChecked;
            return this;
        }

        public Builder isCurrent(boolean isCurrent) {
            this.isCurrent = isCurrent;
            return this;
        }

        public Builder sendPath(String sendPath) {
            this.sendPath = sendPath;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public ImageModel build() {
            return new ImageModel(this);
        }

    }
}
