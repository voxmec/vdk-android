package com.voxmec.vdk.test;

import com.google.gson.Gson;
import com.voxmecanica.vdk.core.Part;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ParserTest {
    @Test
    public void parseTest_Part_Speak() {
        Gson gson = new Gson();
        String json = "{" +
                "\"type\":\"SPEAK\", " +
                "\"title\":\"speak test\"," +
                "\"text\":\"spoken part\"" +
                "}";

        Part part = gson.fromJson(json, Part.class);
        assertEquals(part.getType().name(), "SPEAK");
        assertEquals(part.getTitle(), "speak test");
        assertEquals(part.getText(), "spoken part");
    }

}
