package app;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 두 모드가 같은 엔드포인트에 대해 같은 결과를 내는지 비교하기 위한 최소 API.
 * 프로덕션 코드에 테스트 전용 컨트롤러를 두지 않으려고 테스트 소스에만 둔다.
 */
@TestConfiguration
public class TestApiConfig {

    @RestController
    public static class ProbeController {

        @GetMapping("/api/me")
        public String me(Authentication authentication) {
            return authentication.getName() + ":" + authentication.getAuthorities();
        }

        @GetMapping("/api/public/ping")
        public String ping() {
            return "pong";
        }
    }
}
