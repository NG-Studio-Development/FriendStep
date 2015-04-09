package com.ngstudio.friendstep;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class CodesMap {
    public static final String ENGLISH_CODE = "English";

    public static TreeMap<String,String> map = new TreeMap<String,String>();

    public static Map<String,String> getLanguageMap() {

        if (map.size() == 0) {
            map.put("Afrikaans","af");
            map.put("Albanian","sq");
            map.put("Arabic","ar");
            map.put("Azerbaijani","az");
            map.put("Basque","eu");
            map.put("Bengali","bn");
            map.put("Belarusian","be");
            map.put("Bulgarian","bg");
            map.put("Catalan","ca");
            map.put("Bulgarian","bg");
            map.put("Chinese Simplified","zh-CN");
            map.put("Chinese Traditional","zh-TW");
            map.put("Croatian","hr");
            map.put("Czech","cs");
            map.put("Danish","da");
            map.put("Dutch","nl");
            map.put("English","en");
            map.put("Esperanto","eo");
            map.put("Estonian","et");
            map.put("Filipino","tl");
            map.put("Finnish","fi");
            map.put("French","fr");
            map.put("Galician","gl");
            map.put("Georgian","ka");
            map.put("German","de");
            map.put("Greek","el");
            map.put("Gujarati","gu");
            map.put("Haitian Creole","ht");
            map.put("Hebrew","iw");
            map.put("Hindi","hi");
            map.put("Hungarian","hu");
            map.put("Icelandic","is");
            map.put("Indonesian","id");
            map.put("Irish","ga");
            map.put("Italian","it");
            map.put("Japanese","ja");
            map.put("Kannada","kn");
            map.put("Korean","ko");
            map.put("Latin","la");
            map.put("Latvian","lv");
            map.put("Lithuanian","lt");
            map.put("Macedonian","mk");
            map.put("Malay","ms");
            map.put("Maltese","mt");
            map.put("Norwegian","no");
            map.put("Persian","fa");
            map.put("Polish","pl");
            map.put("Portuguese","pt");
            map.put("Romanian","ro");
            map.put("Russian","ru");
            map.put("Serbian","sr");
            map.put("Slovak","sk");
            map.put("Slovenian","sl");
            map.put("Spanish","es");
            map.put("Swahili","sw");
            map.put("Swedish","sv");
            map.put("Tamil","ta");
            map.put("Telugu","te");
            map.put("Thai","th");
            map.put("Turkish","tr");
            map.put("Ukrainian","uk");
            map.put("Urdu","ur");
            map.put("Vietnamese","vi");
            map.put("Welsh","cy");
            map.put("Yiddish","yi");
        }
        Set<String> st = map.keySet();

        String ss = (String)map.keySet().toArray()[0];
        return map;
    }

    public static String getDefaultLanguageKey () { return ENGLISH_CODE; }
}
