package kz.yandex.intershop.controller;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.service.CartService;
import kz.yandex.intershop.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

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

    @InjectMocks
    private ItemController controller;

    @Test
    void shouldReturnItemView() {
        Item item = new Item();
        when(itemService.getItemById(1L)).thenReturn(item);
        when(cartService.getItemCount(1L)).thenReturn(3);

        String view = controller.viewItem(1L, model);

        assertThat(view).isEqualTo("item");
        assertThat(item.getCount()).isEqualTo(3);
        verify(model).addAttribute("item", item);
    }

    @Test
    void shouldRedirectAfterChangingItemCount() {
        String view = controller.changeItemCount(1L, "minus");

        assertThat(view).isEqualTo("redirect:/items/1");
        verify(cartService).changeItemCount(1L, "minus");
    }
}

