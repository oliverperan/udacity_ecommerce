package com.example.demo.services;

import com.example.demo.bean.SplunkLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

@Service
public class SplunkLoggingServiceImpl implements  SplunkLoggingService {

    @Value("${splunk_hec_url}")
    private String splunkHecUrl;

    @Value("${splunk_token}")
    private String slpunkToken;


    @Override
    public Boolean logToSplunk(SplunkLog info) {
        try {
            URL url = new URL(splunkHecUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", slpunkToken);

            try(OutputStream os = con.getOutputStream()) {
                byte[] input = info.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int HttpResult = con.getResponseCode();
            System.out.println("http result->"+HttpResult);
            return (HttpResult == HttpURLConnection.HTTP_OK);

        } catch (Exception e) {
            System.out.println("exception+>"+e.getMessage());
            return false;
        }
    }
}
