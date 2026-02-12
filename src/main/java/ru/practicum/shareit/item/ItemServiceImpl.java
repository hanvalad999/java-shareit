package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.InvalidCommentException;
import ru.practicum.shareit.exception.InvalidItemDataException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NoCompletedBookingForCommentException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = findUser(ownerId);
        validateForCreate(itemDto);
        Item savedItem = itemRepository.save(ItemMapper.toItem(itemDto, owner));
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));
        if (!ownerId.equals(existing.getOwner().getId())) {
            // Для чужого пользователя вещь считается недоступной/не найденной.
            throw new ItemNotFoundException("Item does not belong to user");
        }

        if (StringUtils.hasText(itemDto.getName())) {
            existing.setName(itemDto.getName());
        }
        if (StringUtils.hasText(itemDto.getDescription())) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        itemRepository.save(existing);
        return ItemMapper.toItemDto(existing);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        ItemDto dto = ItemMapper.toItemDto(item);

        // комментарии доступны всем
        dto.setComments(item.getComments().stream()
                .sorted(Comparator.comparing(
                        ru.practicum.shareit.item.model.Comment::getCreated).reversed())
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));

        // информация о бронированиях только для владельца вещи
        if (item.getOwner() != null && userId != null && userId.equals(item.getOwner().getId())) {
            var now = java.time.LocalDateTime.now();

            bookingRepository.findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(
                            itemId, now, BookingStatus.APPROVED)
                    .ifPresent(booking -> dto.setLastBooking(
                            new ItemDto.BookingShortDto(
                                    booking.getId(),
                                    booking.getBooker() != null ? booking.getBooker().getId() : null,
                                    booking.getStart(),
                                    booking.getEnd()
                            )));

            bookingRepository.findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
                            itemId, now, BookingStatus.APPROVED)
                    .ifPresent(booking -> dto.setNextBooking(
                            new ItemDto.BookingShortDto(
                                    booking.getId(),
                                    booking.getBooker() != null ? booking.getBooker().getId() : null,
                                    booking.getStart(),
                                    booking.getEnd()
                            )));
        }

        return dto;
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        findUser(ownerId);
        return itemRepository.findAllByOwner_Id(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = findUser(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found"));

        boolean hasFinishedBooking = bookingRepository
                .existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                        itemId,
                        userId,
                        BookingStatus.APPROVED,
                        java.time.LocalDateTime.now()
                );

        if (!hasFinishedBooking) {
            throw new NoCompletedBookingForCommentException("User has no completed bookings for this item");
        }

        if (commentDto == null || !StringUtils.hasText(commentDto.getText())) {
            throw new InvalidCommentException("Comment text is required");
        }

        var comment = CommentMapper.toComment(commentDto, item, author);
        comment.setCreated(java.time.LocalDateTime.now());

        var saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }

    private void validateForCreate(ItemDto itemDto) {
        if (itemDto == null || !StringUtils.hasText(itemDto.getName())
                || !StringUtils.hasText(itemDto.getDescription()) || itemDto.getAvailable() == null) {
            throw new InvalidItemDataException("Name, description and availability are required");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
