package com.ebooking.BookUrMovie.feedback.repository;

import com.ebooking.BookUrMovie.commons.models.MoviesLiked;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MoviesLikedRepository extends ElasticsearchRepository<MoviesLiked, Integer> {

}
