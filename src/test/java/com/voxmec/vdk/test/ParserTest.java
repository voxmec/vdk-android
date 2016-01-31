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
                "\"speak\":{\"text\":\"Hello\", \"title\":\"Speech Title\"}" +
                "}";

        Part part = gson.fromJson(json, Part.class);
        assertEquals(part.getType().name(), "SPEAK");
       assertNotNull(part.getSpeak());
        assertEquals(part.getSpeak().getText(), "Hello");
        assertEquals(part.getSpeak().getTitle(), "Speech Title");
    }

}
