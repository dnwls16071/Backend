package rsa;

import com.nimbusds.jose.jwk.RSAKey;
import app.JwtApplication;
import app.TestApiConfig;
import mac.custom.MacTokenVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import rsa.custom.RsaTokenVerifier;
import rsa.signer.RsaSecuritySigner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MacCustomModeTest / MacResourceServerModeTest 와 같은 시나리오를 rsa-custom 모드로 돌린다.
 */
// 테스트가 rsa 패키지에 있어 상위에서 @SpringBootConfiguration 을 찾지 못한다. 명시해 준다.
@SpringBootTest(classes = JwtApplication.class, properties = "jwt.mode=rsa-custom")
@AutoConfigureMockMvc
@Import(TestApiConfig.class)
class RsaCustomModeTest {

    @Autowired MockMvc mockMvc;
    @Autowired RsaSecuritySigner signer;
    @Autowired RSAKey rsaKey;
    @Autowired ApplicationContext context;

    @Test
    @DisplayName("rsa-custom 모드에서는 RSA 검증기만 뜨고 대칭키 쪽은 뜨지 않는다")
    void wiresRsaOnly() {
        assertThat(context.getBeansOfType(RsaTokenVerifier.class)).hasSize(1);
        assertThat(context.getBeansOfType(MacTokenVerifier.class)).isEmpty();
        assertThat(context.getBeansOfType(JwtDecoder.class)).isEmpty();
    }

    @Test
    @DisplayName("생성된 키 쌍은 개인키를 포함하고, 설정한 길이를 따른다")
    void generatesKeyPair() throws Exception {
        assertThat(rsaKey.isPrivate()).isTrue();
        assertThat(rsaKey.toRSAPublicKey().getModulus().bitLength()).isEqualTo(2048);
    }

    @Test
    @DisplayName("유효한 토큰이면 주체와 권한이 그대로 전달된다")
    void authenticatesValidToken() throws Exception {
        String token = signer.sign("woojin", List.of("ROLE_USER"));

        mockMvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("woojin:[ROLE_USER]"));
    }

    @Test
    @DisplayName("토큰이 없으면 401")
    void rejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("서명이 깨진 토큰이면 401")
    void rejectsTamperedToken() throws Exception {
        String tampered = signer.sign("woojin", List.of("ROLE_USER")) + "x";

        mockMvc.perform(get("/api/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("permitAll 경로는 토큰 없이 통과한다")
    void allowsPublicPath() throws Exception {
        mockMvc.perform(get("/api/public/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }
}
