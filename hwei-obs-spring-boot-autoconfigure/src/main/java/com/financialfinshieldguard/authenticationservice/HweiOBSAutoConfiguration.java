package com.financialfinshieldguard.authenticationservice;


import com.obs.services.ObsClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ObsConfig.class)
public class HweiOBSAutoConfiguration {

    @Bean
    public HweiOBSUtil HweiOBSUtil(ObsConfig obsConfig) {
        HweiOBSUtil hweiOBSUtil = new HweiOBSUtil();
        hweiOBSUtil.setObsConfig(obsConfig);
        hweiOBSUtil.setObsClient(new ObsClient(obsConfig.getAccessKey(), obsConfig.getSecretKey(), obsConfig.getEndPoint()));
        return hweiOBSUtil;
    }
}
