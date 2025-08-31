package kz.yandex.intershop.repository;

import kz.yandex.intershop.model.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ItemCustomRepository {

    private final R2dbcEntityTemplate template;

    public ItemCustomRepository(R2dbcEntityTemplate template) {
        this.template = template;
    }

    public Flux<Item> findAll(Pageable pageable) {
        return template.select(Item.class)
                .matching(
                        Query.empty()
                                .sort(pageable.getSort())
                                .limit(pageable.getPageSize())
                                .offset(pageable.getOffset())
                )
                .all();
    }

    public Flux<Item> findByTitleContainingIgnoreCase(String title, Pageable pageable) {
        return template.select(Item.class)
                .matching(
                        Query.query(Criteria.where("title").like("%" + title + "%").ignoreCase(true))
                                .sort(pageable.getSort())
                                .limit(pageable.getPageSize())
                                .offset(pageable.getOffset())
                )
                .all();
    }

    public Mono<Long> countAll() {
        return template.count(Query.empty(), Item.class);
    }

    public Mono<Long> countByTitleContainingIgnoreCase(String title) {
        return template.count(
                Query.query(Criteria.where("title").like("%" + title + "%").ignoreCase(true)),
                Item.class
        );
    }
}

