package com.jueyuantech.glasses.stt.aispeech.bean;

public class LasrBean {

    /**
     * command : start
     * params : {"env":{"use_txt_smooth":1,"use_tprocess":1,"use_sensitive_wds_norm":0,"use_alignment":1,"use_aux":1},"audio":{"audioType":"wav","sampleRate":16000,"sampleBytes":2,"channel":1}}
     * lmId : default
     * phraseFileId :
     * sensitiveFileId :
     */

    private String command;
    private ParamsBean params;
    private String lmId;
    private String phraseFileId;
    private String sensitiveFileId;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ParamsBean getParams() {
        return params;
    }

    public void setParams(ParamsBean params) {
        this.params = params;
    }

    public String getLmId() {
        return lmId;
    }

    /**
     * 二路模型ID。当lmId== “default”时，会使用和产品关联绑定的语言模型。
     * @param lmId
     */
    public void setLmId(String lmId) {
        this.lmId = lmId;
    }

    public String getPhraseFileId() {
        return phraseFileId;
    }

    /**
     * 热词ID。 当phraseFileId== “default”时，会使用和产品关联绑定的热词ID。
     * @param phraseFileId
     */
    public void setPhraseFileId(String phraseFileId) {
        this.phraseFileId = phraseFileId;
    }

    public String getSensitiveFileId() {
        return sensitiveFileId;
    }

    /**
     * 敏感词ID。 当phraseFileId== “default”时，会使用和产品关联绑定的敏感词ID。
     * @param sensitiveFileId
     */
    public void setSensitiveFileId(String sensitiveFileId) {
        this.sensitiveFileId = sensitiveFileId;
    }

    public static class ParamsBean {
        /**
         * env : {"use_txt_smooth":1,"use_tprocess":1,"use_sensitive_wds_norm":0,"use_alignment":1,"use_aux":1}
         * audio : {"audioType":"wav","sampleRate":16000,"sampleBytes":2,"channel":1}
         */

        private EnvBean env;
        private AudioBean audio;

        public EnvBean getEnv() {
            return env;
        }

        public void setEnv(EnvBean env) {
            this.env = env;
        }

        public AudioBean getAudio() {
            return audio;
        }

        public void setAudio(AudioBean audio) {
            this.audio = audio;
        }

        public static class EnvBean {
            /**
             * use_txt_smooth : 1
             * use_tprocess : 1
             * use_sensitive_wds_norm : 0
             * use_alignment : 1
             * use_aux : 1
             * use_stream : 1
             * use_stream_rec_words : 1
             * use_stream_sp : 0
             * use_txtpost : 1
             * use_wp_in_rec : 1
             */

            private int use_txt_smooth;
            private int use_tprocess;
            private int use_sensitive_wds_norm;
            private int use_alignment;
            private int use_aux;
            private int use_stream;
            private int use_stream_rec_words;
            private int use_stream_sp;
            private int use_txtpost;
            private int use_wp_in_rec;

            public int getUse_txt_smooth() {
                return use_txt_smooth;
            }

            /**
             * 口语顺滑，0：不使用(默认)；1：使用
             * @param use_txt_smooth
             */
            public void setUse_txt_smooth(int use_txt_smooth) {
                this.use_txt_smooth = use_txt_smooth;
            }

            public int getUse_tprocess() {
                return use_tprocess;
            }

            /**
             * 逆文本，0：不使用，1：使用（默认）
             * @param use_tprocess
             */
            public void setUse_tprocess(int use_tprocess) {
                this.use_tprocess = use_tprocess;
            }

            public int getUse_sensitive_wds_norm() {
                return use_sensitive_wds_norm;
            }

            /**
             * 敏感词开关。0：关闭敏感词（默认）；1：开启敏感词
             * @param use_sensitive_wds_norm
             */
            public void setUse_sensitive_wds_norm(int use_sensitive_wds_norm) {
                this.use_sensitive_wds_norm = use_sensitive_wds_norm;
            }

            public int getUse_alignment() {
                return use_alignment;
            }

            /**
             * 输出词级别时间对齐信息
             * @param use_alignment
             */
            public void setUse_alignment(int use_alignment) {
                this.use_alignment = use_alignment;
            }

            public int getUse_aux() {
                return use_aux;
            }

            /**
             * 是否返回情绪（emotion） / 年龄（age） / 性别（gender）信息。设置为1时，返回值里回增加emotion, age, gender字段。设置为0 时，则不返回
             * @param use_aux
             */
            public void setUse_aux(int use_aux) {
                this.use_aux = use_aux;
            }

            public int getUse_stream() {
                return use_stream;
            }

            public void setUse_stream(int use_stream) {
                this.use_stream = use_stream;
            }

            public int getUse_stream_rec_words() {
                return use_stream_rec_words;
            }

            public void setUse_stream_rec_words(int use_stream_rec_words) {
                this.use_stream_rec_words = use_stream_rec_words;
            }

            public int getUse_stream_sp() {
                return use_stream_sp;
            }

            public void setUse_stream_sp(int use_stream_sp) {
                this.use_stream_sp = use_stream_sp;
            }

            public int getUse_txtpost() {
                return use_txtpost;
            }

            public void setUse_txtpost(int use_txtpost) {
                this.use_txtpost = use_txtpost;
            }

            public int getUse_wp_in_rec() {
                return use_wp_in_rec;
            }

            public void setUse_wp_in_rec(int use_wp_in_rec) {
                this.use_wp_in_rec = use_wp_in_rec;
            }
        }
    }
}
