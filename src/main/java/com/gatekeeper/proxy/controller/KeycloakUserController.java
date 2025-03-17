package com.gatekeeper.proxy.controller;

import com.gatekeeper.proxy.config.KeycloakConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UserProfileResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@Slf4j
@Tag(name = "Keycloak 사용자 API", description = "Keycloak 사용자 관리 API")
public class KeycloakUserController {

    @GetMapping("/auth-check/getUsers/{userName}")
    @Operation(summary = "사용자 검색", description = "사용자 이름으로 Keycloak 사용자를 검색합니다.")
    public Mono<List<UserRepresentation>> getUsers(
            @Parameter(description = "검색할 사용자 이름") 
            @PathVariable("userName") String userName) {
        
        log.info("사용자 검색 요청: {}", userName);
        List<UserRepresentation> userRepresentations = KeycloakConfig.getKeycloakInstance()
                .realm(KeycloakConfig.getRealm())
                .users()
                .search(userName, 0, 1000, true);
        
        return Mono.just(userRepresentations);
    }

    @GetMapping("/auth-check/getUserProfile")
    @Operation(summary = "사용자 프로필 조회", description = "Keycloak 사용자 프로필 정보를 조회합니다.")
    public Mono<UserProfileResource> getUserProfile() {
        log.info("사용자 프로필 조회 요청");
        UserProfileResource userProfileResource = KeycloakConfig.getKeycloakInstance()
                .realm(KeycloakConfig.getRealm())
                .users()
                .userProfile();
        
        return Mono.just(userProfileResource);
    }
} 