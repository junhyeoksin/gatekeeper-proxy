package com.gatekeeper.proxy.util;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;

public class JwtUtils {

    /**
     * JWT 토큰에서 사용자 ID (sub 클레임)를 추출합니다.
     *
     * @param jwtToken - Authorization 헤더에서 추출한 JWT Access Token
     * @return userId (Keycloak의 sub)
     * @throws ParseException - JWT 파싱 실패 시
     */
    public static String extractUserId(String jwtToken) throws ParseException {
        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(jwtToken);
        return signedJWT.getJWTClaimsSet().getSubject();  // "sub" 클레임 값 반환
    }
}
