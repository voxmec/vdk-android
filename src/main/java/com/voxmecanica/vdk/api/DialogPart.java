package com.voxmecanica.vdk.api;

public interface DialogPart {
    public static enum MetaPart {
        DIALOG("dialog", 0),

        DISPLAYED("displayed", 10),
        TITLE("title", 11),
        CONTENT("content", 12),

        SPEAK ("speak", 100),
        SPEAK_PITCH ("pitch", 101),
        SPEAK_RATE ("rate", 102),

        PLAY("play", 200),

        PAUSE("pause", 300),

        INPUT("input", 400),
        INPUT_PROMPT("prompt", 401),

        PARAM("param", 500),
        PARAM_VALUE("value", 510),

        SUBMIT("submit", 600),
        SUBMIT_TO("to", 601),
        SUBMIT_METHOD("method", 602),

        TERMINATION ("termination", 1000);

        private int partId;
        private String partLabel;

        MetaPart(String label, int id){
            partLabel = label;
            partId = id;
        }

        public String label() {
            return partLabel;
        }

        public int id() {
            return partId;
        }
    }

	public MetaPart getMetaPart();
}
