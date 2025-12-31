package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    @Override
    public synchronized Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idSequence.incrementAndGet());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        List<Item> ownedItems = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner() != null && ownerId.equals(item.getOwner().getId())) {
                ownedItems.add(item);
            }
        }
        return ownedItems;
    }

    @Override
    public List<Item> search(String text) {
        List<Item> result = new ArrayList<>();
        String lowerText = text.toLowerCase();
        for (Item item : items.values()) {
            if ((item.getName() != null && item.getName().toLowerCase().contains(lowerText))
                    || (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerText))) {
                result.add(item);
            }
        }
        return result;
    }
}
