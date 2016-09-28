package me.pr3a.localweather.Helper;

public class UrlApi {

    private String uri,url,apikey;


    public  void setUri(String url, String apikey){
        this.url = url;
        this.apikey = apikey;
        this.uri = url + apikey;
    }

    public  String getUrl(){
        return uri;
    }
}
