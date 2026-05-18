package com.stickersafari.util;

import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for building standardized pagination response maps.
 */
public class PaginationResponseBuilder {

    /**
     * Builds a standardized pagination response map.
     *
     * @param items The list of items for the current page
     * @param page The Page object containing pagination metadata
     * @param itemsKey The key to use for the items in the response map (e.g., "collectors", "stickers")
     * @param <T> The type of items in the list
     * @return A Map containing items and pagination metadata
     */
    public static <T> Map<String, Object> build(List<T> items, Page<?> page, String itemsKey) {
        Map<String, Object> response = new HashMap<>();
        response.put(itemsKey, items);
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }
}
