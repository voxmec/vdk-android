package com.voxmec.vdk.test;

import com.google.gson.Gson;
import com.voxmecanica.vdk.parser.*;
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
        assertEquals(part.getType(), PartType.SPEAK);
        assertEquals(part.getText(), "Hello");
        assertEquals(part.getTitle(), "Speech Title");
    }

    @Test
    public void parseTest_Part_Input() {
        Gson gson = new Gson();
        String json = "{" +
                "\"type\":\"INPUT\", " +
                "\"inputMode\":\"ASR\"" +
                "}";

        Part part = gson.fromJson(json, Part.class);
        assertEquals(part.getType(), PartType.INPUT);
        assertEquals(part.getMode(), InputMode.ASR);
    }

    @Test
    public void parseTest_Part_Directives() {
        Gson gson = new Gson();
        String json = "{" +
                "\"type\":\"DIRECTIVE\", " +
                "\"pause\":2000," +
                "\"rate\":300," +
                "\"pitch\":200," +
                "\"voice\":\"James\"" +
                "}";

        Part part = gson.fromJson(json, Part.class);
        assertEquals(part.getType(), PartType.DIRECTIVE);
        assertEquals(part.getPause(), 2000);
        assertEquals(part.getRate(), 300);
        assertEquals(part.getPitch(), 200);
        assertEquals(part.getVoice(), "James");
    }

    @Test
    public void parseTest_Param() {
        Gson gson = new Gson();
        String json = "{" +
                "\"id\":\"city_name\", " +
                "\"value\":\"Toronto\"" +
               "}";

        DialogParam param = gson.fromJson(json, DialogParam.class);
        assertEquals(param.getId(), "city_name");
        assertEquals(param.getValue(), "Toronto");
    }

    @Test
    public void parseTest_Properties() {
        Gson gson = new Gson();
        String json = "{" +
                "\"originUri\":\"http://vox.io/\", " +
                "\"submitMethod\":\"POST\"" +
               "}";

        Map map = gson.fromJson(json, Map.class);
        assertEquals(map.get("originUri"), "http://vox.io/");
        assertEquals(map.get("submitMethod"), "POST");
    }

    @Test
    public void parseTest_Dialog() {
        Gson gson = new Gson();
        String json = "{" +
                "\"properties\":{" +
                        "\"originUri\":\"http://vox.io/\"," +
                        "\"submitMethod\":\"POST\"" +
                    "}," +
                "\"parts\":[" +
                    "{\"type\":\"INPUT\", \"inputMode\":\"ASR\"},"+
                    "{\"type\":\"SPEAK\", \"text\":\"hello\"}" +
                "]," +
                "\"params\":[" +
                    "{\"id\":\"city_name\", \"value\":\"Toronto\"}," +
                    "{\"id\":\"is_available\", \"value\":\"false\"}" +
                "]" +
               "}";

        Dialog dialog = gson.fromJson(json, Dialog.class);
        assertEquals(dialog.getProperties().get("originUri"), "http://vox.io/");
        assertEquals(dialog.getProperties().get("submitMethod"), "POST");
        assertEquals(dialog.getParts().size(), 2);
        assertEquals(dialog.getParts().get(0).getType(), PartType.INPUT);
        assertEquals(dialog.getParts().get(1).getText(), "hello");
        assertEquals(dialog.getParams().size(), 2);
        assertEquals(dialog.getParams().get(0).getId(), "city_name");
        assertEquals(dialog.getParams().get(1).getValue(), "false");
    }

    @Test
    public void parseTest_DialogParser() {
        DialogParser parser = new DialogParser();
        String json = "{" +
                "\"properties\":{" +
                        "\"originUri\":\"http://vox.io/\"," +
                        "\"submitMethod\":\"POST\"" +
                    "}," +
                "\"parts\":[" +
                    "{\"type\":\"INPUT\", \"inputMode\":\"ASR\"},"+
                    "{\"type\":\"SPEAK\", \"text\":\"hello\"}" +
                "]," +
                "\"params\":[" +
                    "{\"id\":\"city_name\", \"value\":\"Toronto\"}," +
                    "{\"id\":\"is_available\", \"value\":\"false\"}" +
                "]" +
               "}";

        Dialog dialog = parser.parse(json);
        assertEquals(dialog.getProperties().get("originUri"), "http://vox.io/");
        assertEquals(dialog.getProperties().get("submitMethod"), "POST");
        assertEquals(dialog.getParts().size(), 2);
        assertEquals(dialog.getParts().get(0).getType(), PartType.INPUT);
        assertEquals(dialog.getParts().get(1).getText(), "hello");
        assertEquals(dialog.getParams().size(), 2);
        assertEquals(dialog.getParams().get(0).getId(), "city_name");
        assertEquals(dialog.getParams().get(1).getValue(), "false");
    }

}
