package com.home.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@Slf4j
@Getter
@Setter
@PropertySource("classpath:telegram.properties")
public class PingTask {
    @Value("${pings.url}")
    private String url;

    @Scheduled(fixedRateString = "1200000")
    public void pingMe() {
        try {
            URL url = new URL(getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            log.info("Ping {}, OK: response code {}", url.getHost(), connection.getResponseCode());
            connection.disconnect();
        } catch (IOException e) {
            log.error("Ping FAILED");
            e.printStackTrace();
        }

    }

}
