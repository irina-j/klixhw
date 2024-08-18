package org.klix.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "offers-api.fast-bank")
public class FastBankProperties {

    private int timeout;
    private Retry retry;

    @Getter
    @Setter
    public static class Retry {
        private int count;
        private int delaySec;
    }
}
