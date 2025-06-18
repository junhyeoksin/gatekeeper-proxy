package com.gatekeeper.proxy.controller;

import com.gatekeeper.proxy.config.KeycloakConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UserProfileResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Keycloak 사용자 API", description = "Keycloak 사용자 관리를 위한 API 엔드포인트")
@SecurityRequirement(name = "OAuth2")
@Slf4j
public class KeycloakUserController {

    @GetMapping("/users/{userName}")
    @Operation(
        summary = "사용자 검색",
        description = "사용자 이름으로 Keycloak 사용자를 검색합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public Mono<List<UserRepresentation>> getUsers(
            @Parameter(description = "검색할 사용자 이름", required = true) 
            @PathVariable("userName") String userName) {
        
        log.info("사용자 검색 요청: {}", userName);
        List<UserRepresentation> userRepresentations = KeycloakConfig.getKeycloakInstance()
                .realm(KeycloakConfig.getRealm())
                .users()
                .search(userName, 0, 1000, true);
        
        return Mono.just(userRepresentations);
    }

    @GetMapping("/users/profile")
    @Operation(
        summary = "사용자 프로필 조회",
        description = "현재 인증된 사용자의 Keycloak 프로필 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public Mono<UserProfileResource> getUserProfile() {
        log.info("사용자 프로필 조회 요청");
        UserProfileResource userProfileResource = KeycloakConfig.getKeycloakInstance()
                .realm(KeycloakConfig.getRealm())
                .users()
                .userProfile();
        
        return Mono.just(userProfileResource);
    }
} 