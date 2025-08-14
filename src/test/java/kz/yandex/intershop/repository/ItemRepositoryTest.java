package kz.yandex.intershop.repository;

import kz.yandex.intershop.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
    }


    @Test
    @DisplayName("findByTitleContainingIgnoreCase должен находить товары по части названия, без учета регистра")
    void testFindByTitleContainingIgnoreCase() {
        // given
        Item iphone = new Item();
        iphone.setTitle("Apple iPhone 15 Pro");
        iphone.setPrice(BigDecimal.valueOf(699990));
        iphone.setDescription("Флагманский смартфон");
        itemRepository.save(iphone);

        Item samsung = new Item();
        samsung.setTitle("Samsung Galaxy S23");
        samsung.setPrice(BigDecimal.valueOf(499990));
        samsung.setDescription("Флагман Samsung");
        itemRepository.save(samsung);

        // when
        Page<Item> result = itemRepository.findByTitleContainingIgnoreCase(
                "iphone",
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .first()
                .extracting(Item::getTitle)
                .isEqualTo("Apple iPhone 15 Pro");
    }
}
