package com.gatekeeper.proxy.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KeycloakConfig {

    private static String serverUrl;
    private static String realm;
    private static String clientId;
    private static String clientSecret;
    private static Keycloak keycloak;

    @Value("${keycloak.auth-server-url}")
    public void setServerUrl(String serverUrl) {
        KeycloakConfig.serverUrl = serverUrl;
    }

    @Value("${keycloak.realm}")
    public void setRealm(String realm) {
        KeycloakConfig.realm = realm;
    }

    @Value("${keycloak.resource}")
    public void setClientId(String clientId) {
        KeycloakConfig.clientId = clientId;
    }

    @Value("${keycloak.credentials.secret}")
    public void setClientSecret(String clientSecret) {
        KeycloakConfig.clientSecret = clientSecret;
    }

    @Bean
    public static Keycloak getKeycloakInstance() {
        if (keycloak == null) {
            log.info("Keycloak 인스턴스를 초기화하는 중입니다 serverUrl: {}, realm: {}, clientId: {}",
                    serverUrl, realm, clientId);
            
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
} 