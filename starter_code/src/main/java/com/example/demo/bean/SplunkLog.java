package com.example.demo.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SplunkLog {
    private HashMap<String,String> data;

    public SplunkLog() {
        data = new HashMap<String, String>();
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }


    public void addField(String key, String value) {
        data.put(key, value);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"event\":\"");

        Set entrySet = data.entrySet();

        Iterator it = entrySet.iterator();

        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();

            String str = me.getKey()+"="+me.getValue()+",";
            sb.append(str);
        }


        String strRes = sb.toString();
        strRes = strRes.substring(0, strRes.length()-1);

        strRes +="\", \"index\":\"ecommerce_udacity\"}";

        return strRes;
    }
}
