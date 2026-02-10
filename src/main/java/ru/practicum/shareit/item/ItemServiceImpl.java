package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemCreateDto itemCreateDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Item item = Item.builder()
                .name(itemCreateDto.getName())
                .description(itemCreateDto.getDescription())
                .available(itemCreateDto.getAvailable())
                .owner(owner)
                .build();
        item = itemRepository.save(item);
        return toDto(item, userId, false);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemCreateDto itemCreateDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemId));
        if (!item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not the owner of the item");
        }
        if (itemCreateDto.getName() != null) {
            item.setName(itemCreateDto.getName());
        }
        if (itemCreateDto.getDescription() != null) {
            item.setDescription(itemCreateDto.getDescription());
        }
        if (itemCreateDto.getAvailable() != null) {
            item.setAvailable(itemCreateDto.getAvailable());
        }
        item = itemRepository.save(item);
        return toDto(item, userId, true);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemId));
        boolean isOwner = item.getOwner().getId().equals(userId);
        return toDto(item, userId, isOwner);
    }

    @Override
    public List<ItemDto> getByOwnerId(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);
        return items.stream()
                .map(item -> toDto(item, ownerId, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemId));

        boolean hasRented = !bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now()).isEmpty();

        if (!hasRented) {
            throw new BadRequestException("User has not rented this item");
        }

        Comment comment = Comment.builder()
                .text(commentCreateDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        comment = commentRepository.save(comment);

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    private ItemDto toDto(Item item, Long requestUserId, boolean isOwner) {
        ItemDto.ItemDtoBuilder builder = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(null)
                .comments(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()).stream()
                        .map(this::toCommentDto)
                        .collect(Collectors.toList()));

        if (isOwner) {
            LocalDateTime now = LocalDateTime.now();
            bookingRepository.findByItemIdAndEndBeforeOrderByEndDesc(item.getId(), now, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .ifPresent(b -> builder.lastBooking(BookingShortDto.builder()
                            .id(b.getId())
                            .bookerId(b.getBooker().getId())
                            .start(b.getStart())
                            .end(b.getEnd())
                            .build()));

            bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(item.getId(), now, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .ifPresent(b -> builder.nextBooking(BookingShortDto.builder()
                            .id(b.getId())
                            .bookerId(b.getBooker().getId())
                            .start(b.getStart())
                            .end(b.getEnd())
                            .build()));
        }

        return builder.build();
    }

    private CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    @Override
    public List<ItemDto> search(Long userId, String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailable(text).stream()
                .map(i -> ItemDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .description(i.getDescription())
                        .available(i.getAvailable())
                        .requestId(null)
                        .comments(List.of())   // в поиске обычно комментарии не требуют
                        .build())
                .toList();
    }
}
