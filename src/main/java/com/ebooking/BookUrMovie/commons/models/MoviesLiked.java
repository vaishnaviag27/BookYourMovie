package com.ebooking.BookUrMovie.commons.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "movies-liked")
public class MoviesLiked {

  @Id
  private int userId;

  @Field(type = FieldType.Text, fielddata = true)
  private List<String> likedMovies;

  public MoviesLiked(int userId, List<String> likedMovies) {
    this.userId = userId;
    this.likedMovies = likedMovies;
  }
}
