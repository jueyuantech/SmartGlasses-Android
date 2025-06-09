package com.jueyuantech.glasses.stt.aispeech.bean;

public class StartTransCmd {

    /**
     * vpr : {"asrPlus":{"domain":"comm"},"audio":{"audioType":"wav","channel":1,"sampleBytes":2,"sampleRate":16000},"env":{"debug":true,"enableVad":false,"isDependRecWord":true,"asrErrorRate":50,"minSpeechlength":0.4},"requestId":"h5iujav06kn0cpuagxuvb1ha2000oa3d0","action":"mock_vpr_user"}
     * lasr : {"command":"start","params":{"audio":{"audioType":"wav","channel":1,"sampleBytes":2,"sampleRate":16000},"env":{"use_alignment":1,"use_stream":1,"use_stream_rec_words":1,"use_stream_sp":0,"use_txtpost":1,"use_wp_in_rec":1,"use_txt_smooth":1}}}
     * itrans : {"topic":"itrans.start","its":{"useNer":0,"domain":"comm","from":"Chinese","to":"English"}}
     */

    private VprBean vpr;
    private LasrBean lasr;
    private ItransBean itrans;

    public VprBean getVpr() {
        return vpr;
    }

    public void setVpr(VprBean vpr) {
        this.vpr = vpr;
    }

    public LasrBean getLasr() {
        return lasr;
    }

    public void setLasr(LasrBean lasr) {
        this.lasr = lasr;
    }

    public ItransBean getItrans() {
        return itrans;
    }

    public void setItrans(ItransBean itrans) {
        this.itrans = itrans;
    }

    public static class VprBean {
        /**
         * asrPlus : {"domain":"comm"}
         * audio : {"audioType":"wav","channel":1,"sampleBytes":2,"sampleRate":16000}
         * env : {"debug":true,"enableVad":false,"isDependRecWord":true,"asrErrorRate":50,"minSpeechlength":0.4}
         * requestId : h5iujav06kn0cpuagxuvb1ha2000oa3d0
         * action : mock_vpr_user
         */

        private AsrPlusBean asrPlus;
        private AudioBean audio;
        private EnvBean env;
        private String requestId;
        private String action;

        public AsrPlusBean getAsrPlus() {
            return asrPlus;
        }

        public void setAsrPlus(AsrPlusBean asrPlus) {
            this.asrPlus = asrPlus;
        }

        public AudioBean getAudio() {
            return audio;
        }

        public void setAudio(AudioBean audio) {
            this.audio = audio;
        }

        public EnvBean getEnv() {
            return env;
        }

        public void setEnv(EnvBean env) {
            this.env = env;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public static class AsrPlusBean {
            /**
             * domain : comm
             */

            private String domain;

            public String getDomain() {
                return domain;
            }

            public void setDomain(String domain) {
                this.domain = domain;
            }
        }

        public static class EnvBean {
            /**
             * debug : true
             * enableVad : false
             * isDependRecWord : true
             * asrErrorRate : 50
             * minSpeechlength : 0.4
             */

            private boolean debug;
            private boolean enableVad;
            private boolean isDependRecWord;
            private double asrErrorRate;
            private double minSpeechlength;

            public boolean isDebug() {
                return debug;
            }

            public void setDebug(boolean debug) {
                this.debug = debug;
            }

            public boolean isEnableVad() {
                return enableVad;
            }

            public void setEnableVad(boolean enableVad) {
                this.enableVad = enableVad;
            }

            public boolean isIsDependRecWord() {
                return isDependRecWord;
            }

            public void setIsDependRecWord(boolean isDependRecWord) {
                this.isDependRecWord = isDependRecWord;
            }

            public double getAsrErrorRate() {
                return asrErrorRate;
            }

            public void setAsrErrorRate(double asrErrorRate) {
                this.asrErrorRate = asrErrorRate;
            }

            public double getMinSpeechlength() {
                return minSpeechlength;
            }

            public void setMinSpeechlength(double minSpeechlength) {
                this.minSpeechlength = minSpeechlength;
            }
        }
    }

    public static class ItransBean {
        /**
         * topic : itrans.start
         * its : {"useNer":0,"domain":"comm","from":"Chinese","to":"English"}
         */

        private String topic;
        private ItsBean its;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public ItsBean getIts() {
            return its;
        }

        public void setIts(ItsBean its) {
            this.its = its;
        }

        public static class ItsBean {
            /**
             * useNer : 0
             * domain : comm
             * from : Chinese
             * to : English
             */

            private int useNer;
            private String domain;
            private String from;
            private String to;

            public int getUseNer() {
                return useNer;
            }

            public void setUseNer(int useNer) {
                this.useNer = useNer;
            }

            public String getDomain() {
                return domain;
            }

            public void setDomain(String domain) {
                this.domain = domain;
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
        }
    }
}
