package kz.yandex.intershop.service;

import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.repository.ItemRepository;
import kz.yandex.clientshop.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private WebSession session;

    @InjectMocks
    private CartService cartService;

    private Map<String, Object> attributes;
    private Map<Long, Integer> cart;

    @BeforeEach
    void setUp() {
        cart = new HashMap<>();
        attributes = new HashMap<>();
        attributes.put("cartItems", cart);

        when(session.getAttributes()).thenReturn(attributes);
    }

    @Test
    void shouldAddItemToCart() {
        StepVerifier.create(cartService.changeItemCount(session, 1L, "plus"))
                .verifyComplete();

        assertThat(cart).containsEntry(1L, 1);
    }

    @Test
    void shouldIncreaseAndDecreaseItemCount() {
        // add twice
        cartService.changeItemCount(session, 1L, "plus").block();
        cartService.changeItemCount(session, 1L, "plus").block();
        assertThat(cart).containsEntry(1L, 2);

        // decrease once
        cartService.changeItemCount(session, 1L, "minus").block();
        assertThat(cart).containsEntry(1L, 1);

        // decrease to remove
        cartService.changeItemCount(session, 1L, "minus").block();
        assertThat(cart).doesNotContainKey(1L);
    }

    @Test
    void shouldDeleteItem() {
        cart.put(2L, 5);

        cartService.changeItemCount(session, 2L, "delete").block();

        assertThat(cart).doesNotContainKey(2L);
    }

    @Test
    void shouldClearCart() {
        cart.put(1L, 2);
        cart.put(2L, 3);

        cartService.clear(session).block();

        assertThat(cart).isEmpty();
    }

    @Test
    void shouldReturnItemCount() {
        cart.put(3L, 7);

        StepVerifier.create(cartService.getItemCount(session, 3L))
                .expectNext(7)
                .verifyComplete();

        StepVerifier.create(cartService.getItemCount(session, 99L))
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

        StepVerifier.create(cartService.getItems(session))
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

        StepVerifier.create(cartService.getTotal(session))
                .expectNext(BigDecimal.valueOf(300))
                .verifyComplete();
    }
}

