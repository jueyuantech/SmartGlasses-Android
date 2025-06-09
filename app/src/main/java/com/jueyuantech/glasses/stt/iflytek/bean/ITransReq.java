package com.jueyuantech.glasses.stt.iflytek.bean;

public class ITransReq {

    /**
     * header : {"app_id":"a33a0ae9","status":3,"res_id":"its_en_cn_word"}
     * parameter : {"its":{"from":"cn","to":"en","result":{}}}
     * payload : {"input_data":{"encoding":"utf8","status":3,"text":"5byA"}}
     */

    private HeaderBean header;
    private ParameterBean parameter;
    private PayloadBean payload;

    public HeaderBean getHeader() {
        return header;
    }

    public void setHeader(HeaderBean header) {
        this.header = header;
    }

    public ParameterBean getParameter() {
        return parameter;
    }

    public void setParameter(ParameterBean parameter) {
        this.parameter = parameter;
    }

    public PayloadBean getPayload() {
        return payload;
    }

    public void setPayload(PayloadBean payload) {
        this.payload = payload;
    }

    public static class HeaderBean {
        /**
         * app_id : a33a0ae9
         * status : 3
         * res_id : its_en_cn_word
         */

        private String app_id;
        private int status;
        private String res_id;

        public String getApp_id() {
            return app_id;
        }

        public void setApp_id(String app_id) {
            this.app_id = app_id;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getRes_id() {
            return res_id;
        }

        public void setRes_id(String res_id) {
            this.res_id = res_id;
        }
    }

    public static class ParameterBean {
        /**
         * its : {"from":"cn","to":"en","result":{}}
         */

        private ItsBean its;

        public ItsBean getIts() {
            return its;
        }

        public void setIts(ItsBean its) {
            this.its = its;
        }

        public static class ItsBean {
            /**
             * from : cn
             * to : en
             * result : {}
             */

            private String from;
            private String to;
            private ResultBean result;

            public String getFrom() {
                return from;
            }

            public void setFrom(String from) {
                this.from = from;
            }

            public String getTo() {
                return to;
            }

            public void setTo(String to) {
                this.to = to;
            }

            public ResultBean getResult() {
                return result;
            }

            public void setResult(ResultBean result) {
                this.result = result;
            }

            public static class ResultBean {
            }
        }
    }

    public static class PayloadBean {
        /**
         * input_data : {"encoding":"utf8","status":3,"text":"5byA"}
         */

        private InputDataBean input_data;

        public InputDataBean getInput_data() {
            return input_data;
        }

        public void setInput_data(InputDataBean input_data) {
            this.input_data = input_data;
        }

        public static class InputDataBean {
            /**
             * encoding : utf8
             * status : 3
             * text : 5byA
             */

            private String encoding;
            private int status;
            private String text;

            public String getEncoding() {
                return encoding;
            }

            public void setEncoding(String encoding) {
                this.encoding = encoding;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
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
