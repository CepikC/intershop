package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Apple");
        item1.setPrice(BigDecimal.valueOf(10));

        item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Banana");
        item2.setPrice(BigDecimal.valueOf(5));
    }

    @Test
    void getItems_shouldReturnListWithCounts() {
        // arrange
        cartService.changeItemCount(1L, "plus");
        cartService.changeItemCount(2L, "plus");
        cartService.changeItemCount(2L, "plus");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        // act
        List<Item> result = cartService.getItems();

        // assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCount()).isEqualTo(1);
        assertThat(result.get(1).getCount()).isEqualTo(2);
    }

    @Test
    void getItemCount_shouldReturnCorrectCount() {
        cartService.changeItemCount(1L, "plus");
        cartService.changeItemCount(1L, "plus");

        assertThat(cartService.getItemCount(1L)).isEqualTo(2);
        assertThat(cartService.getItemCount(99L)).isEqualTo(0);
    }

    @Test
    void getTotal_shouldReturnSumOfItems() {
        cartService.changeItemCount(1L, "plus"); // 10
        cartService.changeItemCount(2L, "plus"); // 5
        cartService.changeItemCount(2L, "plus"); // +5 = 10

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        BigDecimal total = cartService.getTotal();

        assertThat(total).isEqualTo(BigDecimal.valueOf(20));
    }

    @Test
    void changeItemCount_plus_shouldIncreaseCount() {
        cartService.changeItemCount(1L, "plus");
        assertThat(cartService.getItemCount(1L)).isEqualTo(1);
    }

    @Test
    void changeItemCount_minus_shouldDecreaseOrRemove() {
        cartService.changeItemCount(1L, "plus");
        cartService.changeItemCount(1L, "plus");
        cartService.changeItemCount(1L, "minus");

        assertThat(cartService.getItemCount(1L)).isEqualTo(1);

        cartService.changeItemCount(1L, "minus");
        assertThat(cartService.getItemCount(1L)).isEqualTo(0);
    }

    @Test
    void changeItemCount_delete_shouldRemoveItem() {
        cartService.changeItemCount(1L, "plus");
        cartService.changeItemCount(1L, "delete");

        assertThat(cartService.getItemCount(1L)).isEqualTo(0);
    }

    @Test
    void clear_shouldRemoveAllItems() {
        cartService.changeItemCount(1L, "plus");
        cartService.changeItemCount(2L, "plus");

        cartService.clear();

        assertThat(cartService.getItems()).isEmpty();
    }
}

