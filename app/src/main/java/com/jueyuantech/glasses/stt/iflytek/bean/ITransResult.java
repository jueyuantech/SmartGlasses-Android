package com.jueyuantech.glasses.stt.iflytek.bean;

public class ITransResult {

    /**
     * trans_result : {"dst":"The minutes of our May monetary policy meeting explain the fifth.","src":"我们的五月货币政策会议纪要解释第五个"}
     * from : cn
     * to : en
     */

    private TransResultBean trans_result;
    private String from;
    private String to;

    public TransResultBean getTrans_result() {
        return trans_result;
    }

    public void setTrans_result(TransResultBean trans_result) {
        this.trans_result = trans_result;
    }

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

    public static class TransResultBean {
        /**
         * dst : The minutes of our May monetary policy meeting explain the fifth.
         * src : 我们的五月货币政策会议纪要解释第五个
         */

        private String dst;
        private String src;

        public String getDst() {
            return dst;
        }

        public void setDst(String dst) {
            this.dst = dst;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }
    }
}
