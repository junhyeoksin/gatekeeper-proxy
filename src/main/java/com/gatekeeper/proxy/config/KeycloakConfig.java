package com.gatekeeper.proxy.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KeycloakConfig {

    private static String issuerUri;
    private static String clientId;
    private static String clientSecret;
    private static Keycloak keycloak;

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    public void setIssuerUri(String issuerUri) {
        KeycloakConfig.issuerUri = issuerUri;
    }

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    public void setClientId(String clientId) {
        KeycloakConfig.clientId = clientId;
    }

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    public void setClientSecret(String clientSecret) {
        KeycloakConfig.clientSecret = clientSecret;
    }

    @Bean
    public static Keycloak getKeycloakInstance() {
        if (keycloak == null) {
            String realm = issuerUri.substring(issuerUri.lastIndexOf("/") + 1);
            String serverUrl = issuerUri.replace("/realms/" + realm, "");

            log.info("Keycloak 인스턴스를 초기화합니다. serverUrl: {}, realm: {}, clientId: {}", serverUrl, realm, clientId);

            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
        }
        return keycloak;
    }

    public static String getRealm() {
        return issuerUri.substring(issuerUri.lastIndexOf("/") + 1);
    }
}
