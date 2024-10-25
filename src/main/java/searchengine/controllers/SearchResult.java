package searchengine.controllers;

public class SearchResult {
    private String url;

    private String title;

    private String snippet;

    private double relevance;

    public SearchResult(String url, String title, String snippet, double relevance) {
        this.url = url;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }

    public double getRelevance() {
        return relevance;
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }
}
