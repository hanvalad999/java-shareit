package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemCreateDto itemCreateDto);

    ItemDto update(Long userId, Long itemId, ItemCreateDto itemCreateDto);

    ItemDto getById(Long userId, Long itemId);

    List<ItemDto> getByOwnerId(Long ownerId);

    List<ItemDto> search(Long userId, String text);

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto);
}
