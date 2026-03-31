package org.puregxl.site.jobbacked.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(RustfsProperties.class)
public class RustfsConfiguration {

    @Bean
    public S3Client s3Client(RustfsProperties rustfsProperties) {
        return S3Client.builder()
                .endpointOverride(URI.create(rustfsProperties.getUrl()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                rustfsProperties.getAccessKeyId(),
                                rustfsProperties.getSecretAccessKey()
                        )
                ))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
