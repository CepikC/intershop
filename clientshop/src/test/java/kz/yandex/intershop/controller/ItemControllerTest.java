package kz.yandex.intershop.controller;

import kz.yandex.clientshop.controller.ItemController;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.service.CachedItemService;
import kz.yandex.clientshop.service.CartService;
import kz.yandex.clientshop.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private CachedItemService cachedItemService;

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private ItemController controller;

    @Test
    void shouldReturnItemView() {
        Item item = new Item();
        item.setId(1L);

        when(cachedItemService.getItemById(1L))
                .thenReturn(Mono.just(item));
        when(cartService.getItemCount(1L))
                .thenReturn(Mono.just(3));

        StepVerifier.create(controller.viewItem(1L, model, null))
                .expectNext("item")
                .verifyComplete();

        assertThat(item.getCount()).isEqualTo(3);

        verify(model).addAttribute("isAuthenticated", false);
        verify(model).addAttribute("item", item);
    }

    @Test
    void shouldReturnItemView_WhenUserAuthenticated() {
        Item item = new Item();
        item.setId(1L);

        Principal principal = () -> "user1";

        when(cachedItemService.getItemById(1L))
                .thenReturn(Mono.just(item));
        when(cartService.getItemCount(1L))
                .thenReturn(Mono.just(2));

        StepVerifier.create(controller.viewItem(1L, model, principal))
                .expectNext("item")
                .verifyComplete();

        assertThat(item.getCount()).isEqualTo(2);

        verify(model).addAttribute("isAuthenticated", true);
        verify(model).addAttribute("item", item);
    }

    @Test
    void shouldRedirectAfterChangingItemCount() {
        when(cartService.changeItemCount(1L, "minus"))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.changeItemCountMinus(1L))
                .expectNext("redirect:/items/1")
                .verifyComplete();

        verify(cartService).changeItemCount(1L, "minus");
    }
}


