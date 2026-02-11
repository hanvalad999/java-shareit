package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBooker_Id(Long bookerId);

    List<Booking> findAllByItem_Owner_Id(Long ownerId);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(Long itemId,
                                                             Long bookerId,
                                                             BookingStatus status,
                                                             LocalDateTime end);

    Optional<Booking> findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(Long itemId,
                                                                                LocalDateTime start,
                                                                                BookingStatus status);

    Optional<Booking> findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(Long itemId,
                                                                              LocalDateTime start,
                                                                              BookingStatus status);
}

