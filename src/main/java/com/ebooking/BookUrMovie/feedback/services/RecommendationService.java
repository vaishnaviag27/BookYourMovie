package com.ebooking.BookUrMovie.feedback.services;

import com.ebooking.BookUrMovie.commons.configurations.ElasticSearchConfiguration;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Booking;
import com.ebooking.BookUrMovie.commons.models.Movie;
import com.ebooking.BookUrMovie.commons.models.MoviesLiked;
import com.ebooking.BookUrMovie.commons.models.User;
import com.ebooking.BookUrMovie.commons.services.CommunicationService;
import com.ebooking.BookUrMovie.feedback.dto.MovieDTO;
import com.ebooking.BookUrMovie.feedback.repository.MoviesLikedRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.mail.iap.ConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.terms.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RecommendationService {

    @Autowired
    private MoviesLikedRepository repository;

    @Value("${endpoint}")
    private String endPoint;

    @Autowired
    ElasticSearchConfiguration elasticConfiguration;


    public String createIndex(HttpServletRequest request, HttpMethod method) throws URISyntaxException,
        NotFoundException, ConnectionException, IOException {


        RestHighLevelClient client = elasticConfiguration.client();

        // Deleting the index if it already exists
        GetIndexRequest indexRequest = new GetIndexRequest("movies-liked");
        boolean exists = client.indices().exists(indexRequest, RequestOptions.DEFAULT);
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("movies-liked");
        if (exists)
            client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);

        ObjectMapper mapper = new ObjectMapper();

        // Fetching list of users
        List<User> users = new ArrayList<User>();
        try {
            users = mapper.convertValue(CommunicationService.RequestMirror(
                null,
                HttpMethod.GET,
                request,
                "/users/"),
                List.class);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new ConnectionException("Unable to fetch users");
        } catch (HttpServerErrorException e) {
            throw new NotFoundException("No users found");
        }
        List<User> usersList = mapper.convertValue(
            users,
            new TypeReference<List<User>>() {
            }
        );
        for (int i = 0; i < usersList.size(); i++) {
            User user = usersList.get(i);
            List<String> likedMovies = new ArrayList<>();
            // Fetching bookings of each user
            for (int j = 0; j < user.getBookings().size(); j++) {
                Booking booking = user.getBookings().get(j);

                // If a user has given a rating of 3 and more than it then that movie is liked by the user
                if (booking.getMovieRating() != null) {
                    if (booking.getMovieRating().getRating() >= 3) {
                        likedMovies.add(booking.getMovieName());
                    }
                }
            }

            // Saving the liked movies for a given user in the elastic search repository
            MoviesLiked moviesLiked = new MoviesLiked(user.getUserId(), likedMovies);
            repository.save(moviesLiked);
        }
        return "Index created";
    }

    /**
     * It will recommend movies for users who like the given movie based on all users likings.
     *
     * @return
     */
    public List<MovieDTO> recommendMovies(String movieName, HttpServletRequest request, HttpMethod method) throws IOException, URISyntaxException {

        RestHighLevelClient client = elasticConfiguration.client();
        ElasticsearchRestTemplate template = (ElasticsearchRestTemplate) elasticConfiguration
            .elasticsearchTemplate();
        template.putMapping(MoviesLiked.class);

        List<MovieDTO> movies = new ArrayList<>();

        /**
         * The Significant term aggregation tries to find terms that are significant for specific group
         * of documents within the whole set.
         * It uses JLH score.
         * JLH score = (foreground percentage / background percentage) * (foreground percentage
         * -background percentage)
         * Minimum Document Count : It is used to set minimum number of hits to only return terms
         * that match more than a configured number of hits.
         */
        SearchRequest searchRequest = new SearchRequest("movies-liked");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("likedMovies", movieName);
        SignificantTermsAggregationBuilder aggregationBuilder = AggregationBuilders
            .significantTerms("movies_like_" + movieName)
            .field("likedMovies")
            .minDocCount(1);
        /**
         * Minimum Document count is configured 1 over here due to less amount of data. By default it's
         * value is 3.
         */
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.aggregation(aggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();
        SignificantTerms agg = aggregations.get("movies_like_" + movieName);

        ObjectMapper mapper = new ObjectMapper();
        for (SignificantTerms.Bucket entry : agg.getBuckets()) {
            if (!movieName.equalsIgnoreCase((String) entry.getKey())) {
                Movie movie = mapper.convertValue(CommunicationService.RequestMirror(
                    null,
                    HttpMethod.GET,
                    request,
                    "/movies/" + entry.getKey().toString() + "/details"),
                    Movie.class);
                MovieDTO movieDTO = new MovieDTO(movie, entry.getSignificanceScore());
                movies.add(movieDTO);
            }
        }
        if(movies.isEmpty())
            throw new IOException("No recommendations for " + movieName);
        return movies;
    }
}
