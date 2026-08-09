package com.core.fullstack.services;

import com.core.fullstack.records.Pagination;

public interface PaginationService {

    Pagination getPaginationData(int page, int size);

    Pagination getPaginatedDataFromDb(int page, int size);
    
}
