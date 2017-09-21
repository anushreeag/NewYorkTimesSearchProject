package com.cosmic.newyorktimessearch.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by anushree on 9/19/2017.
 */

public class Article implements Serializable {

    String web_url;
    String thumbnail;
    String title;

    public String getWeb_url() {
        return web_url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }



    public Article(JSONObject object) throws JSONException {
        this.web_url = object.getString("web_url");
        this.title = object.getJSONObject("headline").getString("main");
        JSONArray multimedia = object.getJSONArray("multimedia");
        if(multimedia.length()>0)
            this.thumbnail = "http://www.nytimes.com/"+multimedia.getJSONObject(0).getString("url");
        else
            this.thumbnail = "";
    }


    public static ArrayList<Article> getArticleList(JSONArray array){
        ArrayList<Article> articlelist =  new ArrayList<>();

        for(int i=0;i<array.length();i++){
            try {
                articlelist.add(new Article(array.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return articlelist;
    }
}
