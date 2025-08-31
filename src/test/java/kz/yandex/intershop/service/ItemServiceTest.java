package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.repository.ItemCustomRepository;
import kz.yandex.intershop.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemCustomRepository itemCustomRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setTitle("Test Item");
        item.setPrice(BigDecimal.TEN);
    }

    @Test
    void shouldFindAllItemsWithSearch() {
        when(itemCustomRepository.findByTitleContainingIgnoreCase(eq("Test"), any(Pageable.class)))
                .thenReturn(Flux.just(item));
        when(itemCustomRepository.countByTitleContainingIgnoreCase("Test"))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(itemService.findAll("Test", 1, 10, "ALPHA"))
                .assertNext(page -> {
                    assertThat(page).isInstanceOf(Page.class);
                    assertThat(page.getContent()).hasSize(1);
                    assertThat(page.getContent().get(0).getTitle()).isEqualTo("Test Item");
                    assertThat(page.getTotalElements()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllItemsWithoutSearch() {
        when(itemCustomRepository.findAll(any(Pageable.class)))
                .thenReturn(Flux.just(item));
        when(itemCustomRepository.countAll())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(itemService.findAll(null, 1, 10, "PRICE"))
                .assertNext(page -> {
                    assertThat(page.getContent()).hasSize(1);
                    assertThat(page.getContent().get(0).getId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void shouldFindItemById() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));

        StepVerifier.create(itemService.getItemById(1L))
                .expectNextMatches(foundItem -> foundItem.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.getItemById(99L))
                .verifyComplete();
    }
}

