package com.example.acadroidquizadmin.Model;

import java.util.List;

public class CategoryModel {
    //String imageUrl, title;
    public String name, url, key;
    public List<String> sets;

    public CategoryModel() {
    }

    public CategoryModel(String name, String url, String key, List<String> sets) {
        this.name = name;
        this.url = url;
        this.key = key;
        this.sets = sets;
    }

    public String getNamee() {
        return name;
    }

    public void setNamee(String name) {
        this.name = name;
    }

    public String getUrll() {
        return url;
    }

    public void setUrll(String url) {
        this.url = url;
    }

    public String getKeyy() {
        return key;
    }

    public void setKeyy(String key) {
        this.key = key;
    }

    public List<String> getSetss() {
        return sets;
    }

    public void setSetss(List<String> sets) {
        this.sets = sets;
    }
}
