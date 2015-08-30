package net.lafortu.tellit;

import java.io.Serializable;

public class Article implements Serializable {
    private static long serialVersionUID = 0L;

    private int articleId;
    private String title;
    private String text;

    public int getArticleIdId() {
        return articleId;
    }

    public void setArticleId(int articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return title;
    }
}
