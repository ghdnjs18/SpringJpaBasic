package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    /**
     * 상품 등록
     *
     * @param item
     * @return
     */
    @Transactional
    public Long saveItem(Item item) {
        itemRepository.save(item);
        return item.getId();
    }

    /**
     * 상품 전체 조회
     * @return
     */
    public List<Item> findItem() {
        return itemRepository.findAll();
    }

    /**
     * 상품 조회
     * @param itemId
     * @return
     */
    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
