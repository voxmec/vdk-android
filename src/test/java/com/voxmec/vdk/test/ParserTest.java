package com.voxmec.vdk.test;

import com.google.gson.Gson;
import com.voxmecanica.vdk.parser.Part;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ParserTest {
    @Test
    public void parseTest_Part_Speak() {
        Gson gson = new Gson();
        String json = "{" +
                "\"type\":\"SPEAK\", " +
                "\"text\":\"Hello\", \"title\":\"Speech Title\"" +
                "}";

        Part part = gson.fromJson(json, Part.class);
        assertEquals(part.getType().name(), "SPEAK");
        assertEquals(part.getText(), "Hello");
        assertEquals(part.getTitle(), "Speech Title");
    }

    @Test
    public void parseTest_Part_Input() {
        Gson gson = new Gson();
        String json = "{" +
                "\"type\":\"INPUT\", " +
                "\"input-mode\":\"ASR\"" +
                "}";

        Part part = gson.fromJson(json, Part.class);
        assertEquals(part.getType().name(), "INPUT");
        assertEquals(part.getMode().name(), "ASR");
    }

    //TODO - Test directives {rate, pitch, pause, etc}

}
