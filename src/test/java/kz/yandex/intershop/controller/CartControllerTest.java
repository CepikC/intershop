package kz.yandex.intershop.controller;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private CartController controller;

    @Test
    void shouldReturnCartView() {
        when(cartService.getItems()).thenReturn(List.of(new Item()));
        when(cartService.getTotal()).thenReturn(BigDecimal.TEN);

        String view = controller.viewCart(model);

        assertThat(view).isEqualTo("cart");
        verify(model).addAttribute(eq("items"), any());
        verify(model).addAttribute("total", BigDecimal.TEN);
        verify(model).addAttribute("empty", false);
    }

    @Test
    void shouldRedirectAfterChangingItemCount() {
        String view = controller.changeCartItemCount(1L, "plus");

        assertThat(view).isEqualTo("redirect:/cart/items");
        verify(cartService).changeItemCount(1L, "plus");
    }
}
