package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findCurrentByBookerId(Long bookerId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastByBookerId(Long bookerId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureByBookerId(Long bookerId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findByOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findCurrentByOwnerId(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastByOwnerId(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureByOwnerId(Long ownerId, LocalDateTime now, Pageable pageable);

    List<Booking> findByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId, Long bookerId,
                                                               BookingStatus status, LocalDateTime now);

    List<Booking> findByItemIdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime now, Pageable pageable);

    List<Booking> findByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now, Pageable pageable);
}
