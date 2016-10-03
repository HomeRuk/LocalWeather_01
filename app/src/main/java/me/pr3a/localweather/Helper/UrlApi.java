package me.pr3a.localweather.Helper;

public class UrlApi {

    private String uri;
    private String url;
    private String apiKey;

    public void setUri(String url, String apiKey) {
        this.url = url;
        this.apiKey = apiKey;
        this.uri = url + apiKey;
    }

    public String getUrl() {
        return this.uri;
    }
}
