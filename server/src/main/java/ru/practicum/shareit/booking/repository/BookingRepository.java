package ru.practicum.shareit.booking.repository;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Все бронирования пользователя (как арендатора)
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Текущие бронирования пользователя
    List<Booking> findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(
            Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // Прошедшие бронирования пользователя
    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    // Будущие бронирования пользователя
    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    // Бронирования пользователя по статусу
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    // Все бронирования для вещей владельца
    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    // Текущие бронирования для вещей владельца
    List<Booking> findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(
            Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // Прошедшие бронирования для вещей владельца
    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Sort sort);

    // Будущие бронирования для вещей владельца
    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Sort sort);

    // Бронирования для вещей владельца по статусу
    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    // Последнее и следующее бронирование для вещи
    @Query("select b from Booking b " +
            "where b.item.id = :itemId " +
            "and b.status = :status " +
            "and b.start <= :now " +
            "order by b.start desc " +
            "limit 1")
    Booking findFirstLastBooking(@Param("itemId") Long itemId,
                                 @Param("status") BookingStatus status,
                                 @Param("now") LocalDateTime now);

    @Query("select b from Booking b " +
            "where b.item.id = :itemId " +
            "and b.status = :status " +
            "and b.start > :now " +
            "order by b.start asc " +
            "limit 1")
    Booking findFirstNextBooking(@Param("itemId") Long itemId,
                                 @Param("status") BookingStatus status,
                                 @Param("now") LocalDateTime now);

    // Проверка возможности оставить комментарий
    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, BookingStatus status, LocalDateTime now);
}