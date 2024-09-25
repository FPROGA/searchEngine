package searchengine.services;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.PageDto;
import searchengine.model.SiteDto;
import searchengine.model.Status;
import searchengine.repositorys.LemmaRepository;
import searchengine.repositorys.PageRepository;
import searchengine.repositorys.SiteRepository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;


@Service
public class StatisticsServiceImpl implements StatisticsService{

    private final Random random = new Random();
    private final SitesList sites;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    private ForkJoinPool pool = new ForkJoinPool();
    private final ArrayList<String> indexingTasks = new ArrayList<>();

    public StatisticsServiceImpl(SitesList sites) {
        this.sites = sites;
    }

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<SiteDto> sitesList = siteRepository.findAll();
        for(SiteDto site : sitesList)
        {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            item.setStatus(site.getStatus().toString());
            item.setStatusTime(site.getStatusTime().toEpochSecond(ZoneOffset.UTC));
            item.setError(site.getLastError());
            item.setPages(pageRepository.countBySiteId());
            item.setLemmas(lemmaRepository.countBySiteId());

            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
    public void deleteSiteByUrl(String url)
    {
        SiteDto site = siteRepository.findByUrl(url);
        if (site != null)
        {
            List<PageDto> pages = pageRepository.findBySiteId(site);
            pageRepository.deleteAll(pages);
            siteRepository.delete(site);
        }
    }

    public List<String> getSites(String path)
    {
        List<String> sites = new ArrayList<>();
        try
        {
            List<String> lines = Files.readAllLines(Paths.get(path));
            for (String line : lines)
            {
                if (line.trim().startsWith("- url: "))
                    sites.add(line.substring(7));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return sites;
    }

    public Response startIndexing()
    {
        String path = "application.yaml";
        List<String> sites  = getSites(path);
        for (String siteUrl:sites)
        {
            if (indexingTasks.contains(siteUrl)) {
                // Возвращаем ошибку, если индексация уже запущена
                Response response = new Response();
                response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
                response.setContentType("Индексация уже запущена");
                return response;
            }
            deleteSiteByUrl(siteUrl);

            SiteDto siteDto = new SiteDto();
            siteDto.setUrl(siteUrl);
            siteDto.setStatus(Status.INDEXING);
            siteDto.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteDto);

            SiteParser parser = new SiteParser(siteDto, pageRepository, pool);
            indexingTasks.add(siteUrl);
            pool.invoke(parser);
            if (parser.getError() != null) {
                siteDto.setStatus(Status.FAILED);
                siteDto.setLastError(parser.getError().getMessage());

            }
            else {
                siteDto.setStatus(Status.INDEXED);
            }
            siteDto.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteDto);
            indexingTasks.remove(siteUrl);
        }
        Response response = new Response();
        response.setStatus(HttpServletResponse.SC_OK);
        return response;
    }
    @Override
    public Response stopIndexing()
    {
        if (pool != null) {
            pool.shutdownNow();
            while (!pool.isTerminated()) {
                try {
                    Thread.sleep(100); // Ждём немного, чтобы пул завершил свои задачи
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
                }
            }
            List<SiteDto> sitesToUpdate = siteRepository.findAllByStatus(Status.INDEXING);
            for (SiteDto site : sitesToUpdate) {
                site.setStatus(Status.FAILED);
                site.setLastError("Индексация остановлена пользователем");
                siteRepository.save(site);

            }

            Response response = new Response();
            response.setStatus(HttpServletResponse.SC_OK);
            return response;
        }
        else{
            Response response = new Response();
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            response.setContentType("Индексация не запущена");
            return response;
        }


    }
}
