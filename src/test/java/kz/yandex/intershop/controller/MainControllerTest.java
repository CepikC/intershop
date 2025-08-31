package kz.yandex.intershop.controller;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.service.CartService;
import kz.yandex.intershop.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

    @Mock
    private WebSession session;

    @InjectMocks
    private MainController controller;

    @Test
    void shouldReturnMainView() {
        Item item = new Item();
        item.setId(1L);
        Page<Item> page = new PageImpl<>(List.of(item));

        when(itemService.findAll(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(Mono.just(page));
        when(cartService.getItems(session)).thenReturn(Flux.empty());

        StepVerifier.create(controller.main("search", "NO", 10, 1, model, session))
                .expectNext("main")
                .verifyComplete();

        verify(itemService).findAll("search", 1, 10, "NO");
        verify(model).addAttribute(eq("items"), any());
    }
}



