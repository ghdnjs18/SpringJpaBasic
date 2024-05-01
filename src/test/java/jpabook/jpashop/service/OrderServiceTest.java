package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired MemberService memberService;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired EntityManager em;

    @Autowired
    ItemService itemService;

    // 통합 테스트
    @Test
    public void 상품주문() throws Exception {
        // given
        Member member = createMember();
        Book book = getBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1, getOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 한다.");
        assertEquals(10000 * 2, getOrder.getTotalPrice(), "주문 가격은 가격 * 수량이다.");
        assertEquals(8, book.getStockQuantity(), "주문 수량만큼 재고가 줄어야 한다.");
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        // given
        Member member = createMember();
        Book book = getBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        // when
        NotEnoughStockException e = assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), book.getId(), orderCount));

        // then
        assertEquals(e.getMessage(), "need more stock");

    }

    @Test
    public void 주문취소() throws Exception {
        // given
        Member member = createMember();
        Book book = getBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // when
        orderService.cancleOrder(orderId);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태는 CANCEL 이다.");
        assertEquals(10, book.getStockQuantity(), "주문이 취소된 상품은 그만큼 재고가 증가해야 한다.");
    }

//    @BeforeEach
    public void before() {
        Member member = createMember();
        Book book = getBook("시골 JPA", 10000, 101);

        System.out.println("BeforeEach 종료");
    }

    @Test
    @Rollback(value = false)
    @DisplayName("동시성 제어 - 비관적 락")
    public void concurrency() throws InterruptedException {
        System.out.println("test 시작");
        // given
//        Member member1 = createMember();
//        Book book = getBook("시골 JPA", 10000, 101);
        int orderCount = 1;

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    orderService.order(1L, 1L, orderCount);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Item result = itemService.findOne(1L);
        assertEquals(result.getStockQuantity(), 80);
        System.out.println("test 종료");
    }

    @Test
    @Rollback(value = false)
    @DisplayName("동시성 제어 - 낙관적 락")
    public void concurrency2() throws InterruptedException {
        // given
        int orderCount = 1;

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        AtomicInteger retryCount = new AtomicInteger();
        int maxRetries = 10;
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                while (retryCount.get() < maxRetries) {
                    try {
                        orderService.order2(1L, 1L, orderCount);

                        latch.countDown();

                        break;
                    } catch (OptimisticLockingFailureException e) {
                        retryCount.getAndIncrement();
                        if (retryCount.get() >= maxRetries) {
                            System.out.println("재시도 횟수 초과");
                            throw e;
                        }
                        System.out.println("락획득 재시도 합니다.");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Item result = itemService.findOne(1L);
        assertEquals(result.getStockQuantity(), 80);
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
//        memberService.join(member);
        return member;
    }

    private Book getBook(String name, int orderPrice, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(orderPrice);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
//        itemService.saveItem(book);
        return book;
    }

}