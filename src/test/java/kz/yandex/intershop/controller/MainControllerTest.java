package kz.yandex.intershop.controller;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.service.CartService;
import kz.yandex.intershop.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private MainController controller;

    @Test
    void shouldReturnMainView() {
        Item item = new Item();
        item.setId(1L);

        when(itemService.findAll(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(new PageImpl<>(List.of(item)));
        when(cartService.getItems()).thenReturn(List.of());

        String view = controller.main("search", "NO", 10, 1, model);

        assertThat(view).isEqualTo("main");
        verify(itemService).findAll("search", 1, 10, "NO");
        verify(model).addAttribute(eq("items"), any());
    }
}



