package mac;

import app.JwtApplication;
import app.TestApiConfig;
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

/**
 * {@link MacResourceServerModeTest} 와 같은 시나리오를 custom 모드로 돌린다.
 * 기대값이 동일해야 두 방식이 맞바꿔 쓸 수 있다는 뜻이다.
 */
// JwtApplication 이 app 패키지로 옮겨져 상위 탐색으로는 찾지 못한다. 명시해 준다.
@SpringBootTest(classes = JwtApplication.class, properties = "jwt.mode=mac-custom")
@AutoConfigureMockMvc
@Import(TestApiConfig.class)
class MacCustomModeTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MacSecuritySigner signer;
    @Autowired
    ApplicationContext context;

    @Test
    @DisplayName("mac-custom 모드에서는 직접 만든 검증기만 뜨고 JwtDecoder 는 뜨지 않는다")
    void wiresCustomVerifierOnly() {
        assertThat(context.getBeansOfType(MacTokenVerifier.class)).hasSize(1);
        assertThat(context.getBeansOfType(JwtDecoder.class)).isEmpty();
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
