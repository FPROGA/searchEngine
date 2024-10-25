package searchengine;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Response;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.PageDto;
import searchengine.model.SiteDto;
import searchengine.repositorys.PageRepository;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParsePageTask extends RecursiveAction {
    private final String url;

    private final SiteDto siteId;

    private final PageRepository pageRepository;

    private final ForkJoinPool pool;

    private Throwable error;

    public Throwable getError() {
        return error;
    }

    public ParsePageTask(String url, SiteDto siteId, PageRepository pageRepository, ForkJoinPool pool) {
        this.url = url;
        this.siteId = siteId;
        this.pageRepository = pageRepository;
        this.pool = pool;
    }

    @Override
    protected void compute() {
        try {
            parsePage(url, siteId);
        } catch (Throwable t) {
            error = t;
        }
    }

    private void  parsePage(String url, SiteDto siteId) throws IOException {
        Connection.Response response = Jsoup.connect(url).execute();
        if (response.statusCode() != HttpServletResponse.SC_OK) {
            throw new IOException("Error connecting to " + url);
        }
        Document doc = Jsoup.connect("https://www.facebook.com/")
                .userAgent("Search/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer(url)
                .get();
        PageDto pageDto = new PageDto();
        pageDto.setSiteId(siteId);
        pageDto.setPath(url);
        pageDto.setContent(doc.html());
        int code = Integer.parseInt(doc.selectFirst("meta[name=viewport]").attr("content").split("=")[1].split(",")[0].trim());
        Connection.Response response2 = Jsoup.connect(url).execute();
        pageDto.setCode(response2.statusCode());
        pageRepository.save(pageDto);

    }

}
