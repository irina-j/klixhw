package org.klix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PollingConfigBeans {

    @Bean
    public PollingConfig fastBankPollingConfig(FastBankProperties properties) {
        FastBankProperties.Retry retry = properties.getRetry();
        return new PollingConfig(retry.getCount(), properties.getTimeout(), retry.getDelaySec());
    }

    @Bean
    public PollingConfig solidBankPollingConfig(SolidBankProperties properties) {
        SolidBankProperties.Retry retry = properties.getRetry();
        return new PollingConfig(retry.getCount(), properties.getTimeout(), retry.getDelaySec());
    }
}
