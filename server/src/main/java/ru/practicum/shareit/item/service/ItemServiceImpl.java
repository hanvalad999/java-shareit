package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userService.findUserEntityById(ownerId);

        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Item request not found with id: " + itemDto.getRequestId()));
        }

        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);
        Item savedItem = itemRepository.save(item);

        log.info("Created item with id: {} for owner: {}", savedItem.getId(), ownerId);
        return ItemMapper.toDto(savedItem);
    }

    @Override
    public ItemDetailDto getById(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + id));

        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = null;
        Booking nextBooking = null;

        // Показываем бронирования только владельцу
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = bookingRepository.findFirstLastBooking(
                    item.getId(), BookingStatus.APPROVED, now);

            nextBooking = bookingRepository.findFirstNextBooking(
                    item.getId(), BookingStatus.APPROVED, now);
        }

        // Загружаем комментарии
        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(id)
                .stream()
                .map(CommentMapper::toDto)
                .toList();

        return ItemMapper.toDetailDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemWithBookingsDto> getByOwnerIdWithBookings(Long ownerId) {
        userService.findUserEntityById(ownerId); // validates owner exists

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    Booking lastBooking = bookingRepository.findFirstLastBooking(
                            item.getId(), BookingStatus.APPROVED, now);

                    Booking nextBooking = bookingRepository.findFirstNextBooking(
                            item.getId(), BookingStatus.APPROVED, now);

                    return ItemMapper.toWithBookingsDto(item, lastBooking, nextBooking);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        userService.findUserEntityById(ownerId); // validates owner exists

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        // Check if the user is the owner
        if (existingItem.getOwner() == null || !existingItem.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only the owner can edit the item");
        }

        // Partial update - only update non-null fields
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Updated item with id: {}", itemId);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public void delete(Long ownerId, Long itemId) {
        userService.findUserEntityById(ownerId); // validates owner exists

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        if (existingItem.getOwner() == null || !existingItem.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only the owner can delete the item");
        }

        itemRepository.deleteById(itemId);
        log.info("Deleted item with id: {}", itemId);
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return java.util.Collections.emptyList();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userService.findUserEntityById(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        // Check if user has completed booking for this item
        LocalDateTime now = LocalDateTime.now();
        boolean hasCompletedBooking = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, now);

        if (!hasCompletedBooking) {
            throw new ValidationException("User must complete booking before commenting");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author);
        Comment savedComment = commentRepository.save(comment);

        log.info("User {} added comment to item {}", userId, itemId);
        return CommentMapper.toDto(savedComment);
    }
}