package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto create(Long userId, ItemRequestDto itemRequestDto) {
        User user = getUserOrThrow(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto.getDescription(), user);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Created item request with id: {} for user: {}", savedRequest.getId(), userId);
        return ItemRequestMapper.toItemRequestResponseDto(savedRequest, List.of());
    }

    @Override
    public List<ItemRequestResponseDto> getByRequestor(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findAllByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestResponseDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAll(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findAllByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestResponseDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .toList();
    }

    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item request with id " + requestId + " not found"));
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        return ItemRequestMapper.toItemRequestResponseDto(request, items);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }
}