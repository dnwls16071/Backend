package mac;

import mac.custom.MacTokenVerifier;
import mac.signer.MacSecuritySigner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.mode=resource-server")
@AutoConfigureMockMvc
@Import(TestApiConfig.class)
class ResourceServerModeTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MacSecuritySigner signer;
    @Autowired
    ApplicationContext context;

    @Test
    @DisplayName("resource-server 모드에서는 JwtDecoder 만 뜨고 직접 만든 검증기는 뜨지 않는다")
    void wiresDecoderOnly() {
        assertThat(context.getBeansOfType(JwtDecoder.class)).hasSize(1);
        assertThat(context.getBeansOfType(MacTokenVerifier.class)).isEmpty();
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
