package kz.yandex.intershop.repository;

import kz.yandex.clientshop.IntershopApplication;
import kz.yandex.clientshop.model.Item;
import kz.yandex.clientshop.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@DataR2dbcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = IntershopApplication.class)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testSaveAndFind() {
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(BigDecimal.valueOf(12.34));

        StepVerifier.create(itemRepository.save(item))
                .assertNext(saved -> {
                    assert saved.getId() != null;
                    assert saved.getTitle().equals("Test Item");
                })
                .verifyComplete();

        StepVerifier.create(itemRepository.findAll())
                .expectNextMatches(found -> found.getTitle().equals("Test Item"))
                .verifyComplete();
    }
}
