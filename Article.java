import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Ignor tot ce nu apare in clasa Article
@JsonIgnoreProperties(ignoreUnknown = true)

public class Article {
    public String uuid;
    public String title;
    public String author;
    public String url;
    public String text;
    public String published;
    public String language;
    public java.util.List<String> categories;

    public Article() {}

    // Constructor
    public Article(String uuid, String title, String author, String url, String text, String published, String language, java.util.List<String> categories) {
        this.uuid = uuid;
        this.title = title;
        this.author = author;
        this.url = url;
        this.text = text;
        this.published = published;
        this.language = language;
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "Article{" +
                "uuid='" + uuid + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", published='" + published + '\'' +
                ", language='" + language + '\'' +
                ", categories=" + categories +
                '}';
    }
    public String toStringAllArticles() {
        return uuid + " " + published;
    }
    public String getUuid() {
        return uuid;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getUrl() {
        return url;
    }
    public String getText() {
        return text;
    }
    public String getPublished() {
        return published;
    }
    public String getLanguage() {
        return language;
    }
    public java.util.List<String> getCategories() {
        return categories;
    }

}