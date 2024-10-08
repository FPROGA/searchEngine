package searchengine;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageDto;
import searchengine.model.SiteDto;
import searchengine.repositorys.PageRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class SiteParser extends RecursiveAction {
    private static final Set<String> visitedUrls = new HashSet<>();
    private final SiteDto siteDto;
    private final PageRepository pageRepository;
    private final ForkJoinPool pool;
    private Throwable error;

    public SiteParser(SiteDto siteDto, PageRepository pageRepository, ForkJoinPool pool) {
        this.siteDto = siteDto;
        this.pageRepository = pageRepository;
        this.pool = pool;

    }

    public Throwable getError() {
        return error;
    }

    @Override
    protected void compute() {
        try {
            parseSite(siteDto);
        } catch (Throwable t) {
            error = t;
        }

    }

    private void parseSite(SiteDto siteDto) throws IOException, InterruptedException {
        List<PageDto> pages = new ArrayList<>();
        Set<String> visitedUrls = new HashSet<>();

        Document doc = Jsoup.connect("https://www.facebook.com/")
                .userAgent("Search/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer(siteDto.getUrl())
                .get();
        parsePage(doc, siteDto, pages, visitedUrls, siteDto.getUrl());

        pageRepository.saveAll(pages);
        siteDto.setStatusTime(LocalDateTime.now());
    }

    private void parsePage(Document doc, SiteDto siteId, List<PageDto> pages, Set<String> visitedUrls, String url) throws IOException, InterruptedException {
        PageDto pageDto = new PageDto();
        pageDto.setSiteId(siteId);
        pageDto.setPath(doc.location());
        pageDto.setContent(doc.html());
        int code = Integer.parseInt(doc.selectFirst("meta[name=viewport]").attr("content").split("=")[1].split(",")[0].trim());
        Connection.Response response = Jsoup.connect(url).execute();
        pageDto.setCode(response.statusCode());
        pages.add(pageDto);

        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String childUrl = link.attr("abs:href");
            if (childUrl.startsWith(siteDto.getUrl()) && !childUrl.contains("#") && !visitedUrls.contains(childUrl)) {
                visitedUrls.add(childUrl);
                ParsePageTask task = new ParsePageTask(childUrl, siteId, pageRepository, pool);
                pool.invoke(task);
                if (task.getError() != null) {
                    try {
                        throw task.getError();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        Thread.sleep(1000); // Simulate some processing time
    }

}
