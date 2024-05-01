package jpabook.jpashop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("test")
@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
//        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit1() {
            Member member = createMember("userA", "서울", "1", "111-111");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);

            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            em.persist(book2);

//            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
//            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);
//
//            Delivery delivery = new Delivery();
//            delivery.setAddress(member.getAddress());
//
//            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
//            em.persist(order);
        }

        private Book createBook(String name, int price, int stockQuantity) {
            Book book1 = new Book();
            book1.setName(name);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }

        private Member createMember(String userA, String city, String street, String zipcode) {
            Member member = new Member();
            member.setName(userA);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }

        public void dbInit2() {
            Member member = createMember("userB", "대구", "2", "222-222");
            em.persist(member);

            Book book1 = createBook("SPRING1 BOOK", 30000, 200);
            em.persist(book1);

            Book book2 = createBook("SPRING2 BOOK", 40000, 200);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);

            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }
    }
}


