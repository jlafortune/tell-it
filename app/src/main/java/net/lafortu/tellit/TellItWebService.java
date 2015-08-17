package net.lafortu.tellit;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

public interface TellItWebService {
    @GET("/articles")
    List<Article> getArticles();

}
