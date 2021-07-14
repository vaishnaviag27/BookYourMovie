package com.ebooking.BookUrMovie.commons.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "elasticsearch-movies")
@Data
@NoArgsConstructor
@Setting(settingPath = "esanalyzer.json")
public class MovieES {
    @Id
    private int movieId;

    @Field(type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard")
    private String movieName;

    public MovieES(int movieId, String movieName) {
        this.movieId = movieId;
        this.movieName = movieName;
    }
}
