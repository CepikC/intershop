package kz.yandex.intershop.controller;

import kz.yandex.clientshop.controller.CartController;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.ui.Model;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @Mock
    private WebSession session;

    @InjectMocks
    private CartController controller;

    @Test
    void shouldReturnCartView() {
        Item item = new Item();
        item.setId(1L);

        when(cartService.getItems()).thenReturn(Flux.just(item));
        when(cartService.getTotal()).thenReturn(Mono.just(BigDecimal.TEN));

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "user1",
                        "password",
                        List.of(new SimpleGrantedAuthority("USER"))
                );

        SecurityContext securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(
                        controller.viewCart(model)
                                .contextWrite(
                                        ReactiveSecurityContextHolder.withSecurityContext(
                                                Mono.just(securityContext)
                                        )
                                )
                )
                .expectNext("cart")
                .verifyComplete();

        verify(model).addAttribute("items", List.of(item));
        verify(model).addAttribute("total", BigDecimal.TEN);
        verify(model).addAttribute("empty", false);
    }

}
