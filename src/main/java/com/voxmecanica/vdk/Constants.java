package com.voxmecanica.vdk;

public class Constants {
    public static class Intents {
        public static class Codes {
            public static final int REQUEST_SPEECH_INPUT 	= 100;
            public static final int REQUEST_TTS_VOICE_DATA  = 200;
        }

        public static class Actions{
            public static final String LAUNCH_DIALOG = "com.voxmecanica.intent.action.LAUNCH_DIALOG";
        }
    }

    public static class ParamKeys {
        public static final String DIALOG_ID = "id";
        public static final String DIALOG_URL = "url";
        public static final String DIALOG_PARAMS = "params";
        public static final String DIALOG_NAME = "name";
        public static final String DIALOG_DESC = "desc";
    }

}
