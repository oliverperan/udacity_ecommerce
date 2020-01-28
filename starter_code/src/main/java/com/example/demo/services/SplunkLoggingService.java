package com.example.demo.services;

import com.example.demo.bean.SplunkLog;

public interface SplunkLoggingService {
    public Boolean logToSplunk(SplunkLog info);
}
