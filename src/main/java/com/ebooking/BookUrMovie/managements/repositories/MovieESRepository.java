package com.ebooking.BookUrMovie.managements.repositories;

import com.ebooking.BookUrMovie.commons.models.MovieES;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MovieESRepository extends ElasticsearchRepository<MovieES, Integer> {
}
