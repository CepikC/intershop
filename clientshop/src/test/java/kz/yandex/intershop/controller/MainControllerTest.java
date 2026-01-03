package kz.yandex.intershop.controller;

import kz.yandex.clientshop.controller.MainController;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.service.CachedItemService;
import kz.yandex.clientshop.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private CachedItemService itemService;

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

        Page<Item> page = new PageImpl<>(List.of(item));

        when(itemService.findAll(anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(Mono.just(page));

        when(cartService.getItems())
                .thenReturn(Flux.empty());

        StepVerifier.create(
                        controller.main(
                                "search",
                                "NO",
                                10,
                                1,
                                model,
                                null // principal = null (anonymous)
                        )
                )
                .expectNext("main")
                .verifyComplete();

        verify(itemService).findAll("search", 1, 10, "NO");
        verify(cartService).getItems();

        verify(model).addAttribute(eq("items"), any());
        verify(model).addAttribute("search", "search");
        verify(model).addAttribute("sort", "NO");
        verify(model).addAttribute(eq("paging"), any());
        verify(model).addAttribute("isAuthenticated", false);
    }
}



