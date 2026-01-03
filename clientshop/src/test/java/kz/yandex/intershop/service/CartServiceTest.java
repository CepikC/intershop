package kz.yandex.intershop.service;

import kz.yandex.clientshop.config.security.SecurityUtils;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.repository.ItemRepository;
import kz.yandex.clientshop.service.CartService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames.USERNAME;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    private Map<Long, Integer> cart;

    private static final String USERNAME = "user1";

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() throws Exception {
        cart = new ConcurrentHashMap<>();

        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::currentUsername)
                .thenReturn(Mono.just(USERNAME));

        Field cartsField = CartService.class.getDeclaredField("carts");
        cartsField.setAccessible(true);
        Map<String, Map<Long, Integer>> carts =
                (Map<String, Map<Long, Integer>>) cartsField.get(cartService);
        carts.put(USERNAME, cart);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void shouldAddItemToCart() {
        StepVerifier.create(cartService.changeItemCount(1L, "plus"))
                .verifyComplete();

        assertThat(cart).containsEntry(1L, 1);
    }

    @Test
    void shouldIncreaseAndDecreaseItemCount() {
        cartService.changeItemCount(1L, "plus").block();
        cartService.changeItemCount(1L, "plus").block();
        assertThat(cart).containsEntry(1L, 2);

        cartService.changeItemCount(1L, "minus").block();
        assertThat(cart).containsEntry(1L, 1);

        cartService.changeItemCount(1L, "minus").block();
        assertThat(cart).doesNotContainKey(1L);
    }

    @Test
    void shouldDeleteItem() {
        cart.put(2L, 5);

        cartService.changeItemCount(2L, "delete").block();

        assertThat(cart).doesNotContainKey(2L);
    }

    @Test
    void shouldClearCart() {
        cart.put(1L, 2);
        cart.put(2L, 3);

        cartService.clear().block();

        assertThat(cart).isEmpty();
    }

    @Test
    void shouldReturnItemCount() {
        cart.put(3L, 7);

        StepVerifier.create(cartService.getItemCount(3L))
                .expectNext(7)
                .verifyComplete();

        StepVerifier.create(cartService.getItemCount(99L))
                .expectNext(0)
                .verifyComplete();
    }

    @Test
    void shouldReturnItemsFromRepository() {
        Item item = new Item();
        item.setId(10L);
        item.setPrice(BigDecimal.TEN);

        cart.put(10L, 2);
        when(itemRepository.findById(10L)).thenReturn(Mono.just(item));

        StepVerifier.create(cartService.getItems())
                .assertNext(i -> {
                    assertThat(i.getId()).isEqualTo(10L);
                    assertThat(i.getCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void shouldCalculateTotal() {
        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.valueOf(100));

        cart.put(1L, 3);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));

        StepVerifier.create(cartService.getTotal())
                .expectNext(BigDecimal.valueOf(300))
                .verifyComplete();
    }
}

