package kz.yandex.clientshop.config.security;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

public class SecurityUtils {

    public static Mono<String> currentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }
}
