package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.lock.timeout", 10000);

//        return em.find(Item.class, id);

        return em.find(Item.class, id, LockModeType.PESSIMISTIC_WRITE, properties);

//        return em.find(Item.class, id, LockModeType.OPTIMISTIC);


//        return em.createQuery("select i from Item i where i.id = :id", Item.class)
//                .setParameter("id", id)
//                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
//                .getSingleResult();
    }

    public Item findOne2(Long id) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.lock.timeout", 10000);

//        return em.find(Item.class, id);
        return em.find(Item.class, id, LockModeType.OPTIMISTIC);
    }

    public List<Item> findAll() {
        return em.createQuery("SELECT i FROM Item i", Item.class)
                .getResultList();
    }
}
