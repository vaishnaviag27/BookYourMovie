package com.ebooking.BookUrMovie.commons.services;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

public class CommunicationService {

    private static HttpClient createHttpClient(String username, String password) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);

        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        return client;
    }

    private static ClientHttpRequestFactory createRequestFactory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient);
        return clientHttpRequestFactory;
    }

    public static RestTemplate securityRestTemplateBuilder(String username, String password) {
        HttpClient httpClient = createHttpClient(username, password);
        ClientHttpRequestFactory requestFactory = createRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    public static Object RequestMirror(Object body,
                                       HttpMethod method,
                                       HttpServletRequest request,
                                       String urlEndPoint) throws URISyntaxException {
        String requestUrl = request.getRequestURI();
        URI uri = new URI("http", null, "localhost", 8080, urlEndPoint, null, null);
        uri = UriComponentsBuilder.fromUri(uri)
            .query(request.getQueryString())
            .build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(uri, method, httpEntity, Object.class).getBody();
    }
}
