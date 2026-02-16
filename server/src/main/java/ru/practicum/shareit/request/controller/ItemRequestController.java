package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestResponseDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST /requests - Creating item request for user: {}", userId);
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getByRequestor(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /requests - Getting item requests for user: {}", userId);
        return itemRequestService.getByRequestor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAll(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("GET /requests/all - Getting all item requests for user: {}", userId);
        return itemRequestService.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long requestId) {
        log.info("GET /requests/{} - Getting item request by id for user: {}", requestId, userId);
        return itemRequestService.getById(userId, requestId);
    }
}