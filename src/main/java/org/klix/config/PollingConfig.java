package org.klix.config;

import lombok.Getter;

@Getter
public class PollingConfig {

    private int retryCount;
    private int timeoutSecs;
    private int retryDelaySeconds;

    public PollingConfig(int retryCount, int timeoutSecs, int retryDelaySeconds) {
        this.retryCount = retryCount;
        this.timeoutSecs = timeoutSecs;
        this.retryDelaySeconds = retryDelaySeconds;
    }
}
