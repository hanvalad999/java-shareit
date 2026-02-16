package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {

    ItemDto create(Long ownerId, ItemDto itemDto);

    ItemDetailDto getById(Long id, Long userId);

    List<ItemWithBookingsDto> getByOwnerIdWithBookings(Long ownerId);

    ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

    void delete(Long ownerId, Long itemId);

    Collection<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}