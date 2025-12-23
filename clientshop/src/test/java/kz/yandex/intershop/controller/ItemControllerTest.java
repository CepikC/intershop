package kz.yandex.intershop.controller;

import kz.yandex.clientshop.controller.ItemController;
import kz.yandex.clientshop.model.Item;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @Mock
    private WebSession session;

    @InjectMocks
    private ItemController controller;

    @Test
    void shouldReturnItemView() {
        Item item = new Item();
        item.setId(1L);

        when(itemService.getItemById(1L)).thenReturn(Mono.just(item));
        when(cartService.getItemCount(session, 1L)).thenReturn(Mono.just(3));

        StepVerifier.create(controller.viewItem(1L, model, session))
                .expectNext("item")
                .verifyComplete();

        assertThat(item.getCount()).isEqualTo(3);
        verify(model).addAttribute("item", item);
    }

    @Test
    void shouldRedirectAfterChangingItemCount() {
        when(cartService.changeItemCount(session, 1L, "minus"))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.changeItemCountMinus(1L,  session))
                .expectNext("redirect:/items/1")
                .verifyComplete();

        verify(cartService).changeItemCount(session, 1L, "minus");
    }
}


