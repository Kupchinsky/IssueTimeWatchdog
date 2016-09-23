package ru.killer666.issuetimewatchdog;

public interface TrackorType {
    String ID = "TRACKOR_ID";
    String KEY = "TRACKOR_KEY";

    String getTrackorName();
    Long getTrackorId();
    String getTrackorKey();
}
