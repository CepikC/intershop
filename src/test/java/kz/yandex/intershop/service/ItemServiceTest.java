package kz.yandex.intershop.service;

import kz.yandex.intershop.model.Item;
import kz.yandex.intershop.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

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

    @InjectMocks
    private ItemService itemService;

    @Test
    void findAll_withSearch_shouldCallFindByTitleContainingIgnoreCase() {
        Item item = new Item();
        Page<Item> page = new PageImpl<>(List.of(item));

        when(itemRepository.findByTitleContainingIgnoreCase(eq("phone"), any(Pageable.class)))
                .thenReturn(page);

        Page<Item> result = itemService.findAll("phone", 1, 10, "ALPHA");

        assertThat(result.getContent()).containsExactly(item);
        verify(itemRepository).findByTitleContainingIgnoreCase(eq("phone"), any(Pageable.class));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void findAll_withoutSearch_shouldCallFindAll() {
        Item item = new Item();
        Page<Item> page = new PageImpl<>(List.of(item));

        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Item> result = itemService.findAll(null, 2, 5, "PRICE");

        assertThat(result.getContent()).containsExactly(item);
        verify(itemRepository).findAll(any(Pageable.class));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void getItemById_whenItemExists_shouldReturnItem() {
        Item item = new Item();
        item.setId(42L);

        when(itemRepository.findById(42L)).thenReturn(Optional.of(item));

        Item result = itemService.getItemById(42L);

        assertThat(result).isEqualTo(item);
        verify(itemRepository).findById(42L);
    }

    @Test
    void getItemById_whenItemDoesNotExist_shouldThrowException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> itemService.getItemById(99L));

        verify(itemRepository).findById(99L);
    }
}

