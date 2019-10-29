package com.baselibrary;

public class MessageBus {
    private String codeType;
    private boolean succeed;
    private Object message;
    private Object param1;
    private Object param2;
    private Object param3;
    private Object param4;
    private Object param5;

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getParam1() {
        return param1;
    }

    public void setParam1(Object param1) {
        this.param1 = param1;
    }

    public Object getParam2() {
        return param2;
    }

    public void setParam2(Object param2) {
        this.param2 = param2;
    }

    public Object getParam3() {
        return param3;
    }

    public void setParam3(Object param3) {
        this.param3 = param3;
    }

    public Object getParam4() {
        return param4;
    }

    public void setParam4(Object param4) {
        this.param4 = param4;
    }

    public Object getParam5() {
        return param5;
    }

    public void setParam5(Object param5) {
        this.param5 = param5;
    }

    public boolean isSucceed() {
        return succeed;
    }

    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }

    public MessageBus(String codeType, Object message) {
        this.codeType = codeType;
        this.message = message;
    }

    public MessageBus(String codeType, Object message, String param1) {
        this.codeType = codeType;
        this.message = message;
        this.param1 = param1;
    }

    public MessageBus(String codeType, String param1, String param2) {
        this.codeType = codeType;
        this.param1 = param1;
        this.param2 = param2;
    }

    public MessageBus(String codeType, Object param1, Object param2, Object param3) {
        this.codeType = codeType;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
    }

    public MessageBus(String codeType, Object param1, Object param2, Object param3, Object param4) {
        this.codeType = codeType;
        this.param1 = param1;
        this.param2 = param2;
        this.param3 = param3;
        this.param4 = param4;
    }

    public MessageBus(String codeType) {
        this.codeType = codeType;
    }

    public MessageBus(Builder builder) {
        this.codeType = builder.codeType;
        this.succeed = builder.succeed;
        this.message = builder.message;
        this.param1 = builder.param1;
        this.param2 = builder.param2;
        this.param3 = builder.param3;
        this.param4 = builder.param4;
        this.param5 = builder.param5;
    }

    public static class Builder {
        String codeType;
        boolean succeed;
        Object message;
        Object param1;
        Object param2;
        Object param3;
        Object param4;
        Object param5;

        public Builder codeType(String codeType) {
            this.codeType = codeType;
            return this;
        }

        public Builder succeed(boolean succeed) {
            this.succeed = succeed;
            return this;
        }

        public Builder message(Object message) {
            this.message = message;
            return this;
        }

        public Builder param1(Object param1) {
            this.param1 = param1;
            return this;
        }

        public Builder param2(Object param2) {
            this.param2 = param2;
            return this;
        }

        public Builder param3(Object param3) {
            this.param3 = param3;
            return this;
        }

        public Builder param4(Object param4) {
            this.param4 = param4;
            return this;
        }

        public Builder param5(Object param5) {
            this.param5 = param5;
            return this;
        }

        public MessageBus build() {
            return new MessageBus(this);
        }

    }

    //消息ID
    public static final String msgId_playTime = "playTime";
    public static final String msgId_closePlay = "closePlay";
    public static final String msgId_fullScreen = "fullScreen";
    public static final String msgId_download_pending = "download_pending";
    public static final String msgId_download_started = "download_started";
    public static final String msgId_download_connected = "download_connected";
    public static final String msgId_download_progress = "download_progress";
    public static final String msgId_download_completed = "download_completed";
    public static final String msgId_download_paused = "download_paused";
    public static final String msgId_download_error = "download_error";
    public static final String msgId_download_warn = "download_warn";

}
