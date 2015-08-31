package net.lafortu.tellit;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TellItWebService {
    @GET("/articles")
    List<Article> getArticles(@Query("category") String category);
}
