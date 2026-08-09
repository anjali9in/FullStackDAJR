package com.core.fullstack.services;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.core.fullstack.entitybean.UserEntity;
import com.core.fullstack.records.Pagination;
import com.core.fullstack.repository.UserRepository;

@Service
public class PaginationServiceImpl implements PaginationService {

    @Autowired
    UserRepository userRepository;

    private static final List<Object> DEMO_DATA = IntStream.rangeClosed(1, 100)
            .mapToObj(index -> "item-" + index)
            .map(Object.class::cast)
            .toList();

    @Override
    public Pagination getPaginationData(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        int totalElements = DEMO_DATA.size();
        int totalPages = totalElements == 0 ? 0 : (totalElements + size - 1) / size;
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<Object> pageContent = startIndex >= totalElements
                ? List.of()
                : DEMO_DATA.subList(startIndex, endIndex);

        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;

        return new Pagination(page, size, totalElements, totalPages, first, last, pageContent);
    }

    @Override
    public Pagination getPaginatedDataFromDb(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserEntity> usersPage = userRepository.findAll(pageable);

        List<Object> pageContent = usersPage.getContent().stream()
                .map(Object.class::cast)
                .toList();

        return new Pagination(
                usersPage.getNumber(),
                usersPage.getSize(),
                (int) usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isFirst(),
                usersPage.isLast(),
                pageContent);
    }
}
