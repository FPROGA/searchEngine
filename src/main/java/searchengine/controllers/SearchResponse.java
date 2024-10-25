package searchengine.controllers;

import java.util.List;

public class SearchResponse {
    private boolean result;

    private int count;

    private List<SearchResult> data;

    public SearchResponse(List<SearchResult> results, int count) {
        this.result = true;
        this.count = count;
        this.data = results;
    }
}