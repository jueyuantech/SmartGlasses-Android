package com.jueyuantech.glasses.stt.iflytek.bean;

public class ITransRsp {

    /**
     * header : {"code":0,"message":"success","sid":"its000eee2d@dx1971f94a0386f19882"}
     * payload : {"result":{"seq":"0","status":"3","text":"eyJmcm9tIjoiY24iLCJ0byI6ImVuIiwidHJhbnNfcmVzdWx0Ijp7InNyYyI6IuaIkeS7rOeahOS6lOaciOi0p+W4geaUv+etluS8muiurue6quimgeino+mHiuesrOS6lOS4qiIsImRzdCI6IlRoZSBtaW51dGVzIG9mIG91ciBNYXkgbW9uZXRhcnkgcG9saWN5IG1lZXRpbmcgZXhwbGFpbiB0aGUgZmlmdGguIn19"}}
     */

    private HeaderBean header;
    private PayloadBean payload;

    public HeaderBean getHeader() {
        return header;
    }

    public void setHeader(HeaderBean header) {
        this.header = header;
    }

    public PayloadBean getPayload() {
        return payload;
    }

    public void setPayload(PayloadBean payload) {
        this.payload = payload;
    }

    public static class HeaderBean {
        /**
         * code : 0
         * message : success
         * sid : its000eee2d@dx1971f94a0386f19882
         */

        private int code;
        private String message;
        private String sid;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }
    }

    public static class PayloadBean {
        /**
         * result : {"seq":"0","status":"3","text":"eyJmcm9tIjoiY24iLCJ0byI6ImVuIiwidHJhbnNfcmVzdWx0Ijp7InNyYyI6IuaIkeS7rOeahOS6lOaciOi0p+W4geaUv+etluS8muiurue6quimgeino+mHiuesrOS6lOS4qiIsImRzdCI6IlRoZSBtaW51dGVzIG9mIG91ciBNYXkgbW9uZXRhcnkgcG9saWN5IG1lZXRpbmcgZXhwbGFpbiB0aGUgZmlmdGguIn19"}
         */

        private ResultBean result;

        public ResultBean getResult() {
            return result;
        }

        public void setResult(ResultBean result) {
            this.result = result;
        }

        public static class ResultBean {
            /**
             * seq : 0
             * status : 3
             * text : eyJmcm9tIjoiY24iLCJ0byI6ImVuIiwidHJhbnNfcmVzdWx0Ijp7InNyYyI6IuaIkeS7rOeahOS6lOaciOi0p+W4geaUv+etluS8muiurue6quimgeino+mHiuesrOS6lOS4qiIsImRzdCI6IlRoZSBtaW51dGVzIG9mIG91ciBNYXkgbW9uZXRhcnkgcG9saWN5IG1lZXRpbmcgZXhwbGFpbiB0aGUgZmlmdGguIn19
             */

            private String seq;
            private String status;
            private String text;

            public String getSeq() {
                return seq;
            }

            public void setSeq(String seq) {
                this.seq = seq;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }
        }
    }
}
