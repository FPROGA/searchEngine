package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.LemmaProcess;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.PageDto;
import searchengine.repositorys.IndexRepository;
import searchengine.repositorys.LemmaRepository;
import searchengine.repositorys.PageRepository;
import searchengine.StatisticsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

    private final LemmaProcess lemmaProcess;

    private LemmaRepository lemmaRepository;

    private PageRepository pageRepository;

    private IndexRepository indexRepository;

    public ApiController(StatisticsService statisticsService, LemmaProcess lemmaProcess) {
        this.statisticsService = statisticsService;
        this.lemmaProcess = lemmaProcess;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        return ResponseEntity.ok(statisticsService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing()
    {
        return ResponseEntity.ok(statisticsService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public static ResponseEntity indexPage()
    {
         return ResponseEntity.ok(LemmaProcess.lemprocess());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SearchResult>> search(@RequestParam String query,
                                                     @RequestParam(required = false) String site, @RequestParam(required = false)
        Integer offset, @RequestParam(required = false) Integer limit) {
        List<SearchResult> results = new ArrayList<>();
        int count = 0;
        List<String> lemmas = null;
        try {
            lemmas = (List<String>) getLemmas(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lemmas = filterLemmas(lemmas, 0.5);
        lemmas = sortLemmas(lemmas);
        List<PageDto> pages = findPages(lemmas.get(0));

        for (int i = 1; i < lemmas.size(); i++) {
            pages = filterPages(pages, lemmas.get(i));
        }

        for (PageDto page : pages) {
            double relevance = calculateRelevance(page, lemmas);
            SearchResult result = new SearchResult(Integer.toString(page.getId()), page.getName(), getSnippet(page, lemmas), relevance);
            results.add(result);
        }

        results.sort((a, b) -> Double.compare(b.getRelevance(), a.getRelevance()));

        int startIndex = offset != null ? offset : 0;
        int endIndex = startIndex + (limit != null ? limit : 20);
        results = results.subList(startIndex, endIndex);

        count = pages.size();

        return ResponseEntity.ok((List<SearchResult>) new SearchResponse(results, count));
    }

    private HashMap<String, Integer> getLemmas(String query) throws IOException {
        return LemmaProcess.getLemmas(query);
    }

    private List<String> filterLemmas(List<String> lemmas, double percentage) {
        List<String> filteredLemmas = new ArrayList<>();
        for (String lemma : lemmas) {
            int frequency = lemmaRepository.getFrequency(lemma);
            if (frequency < percentage * pageRepository.count()) {
                filteredLemmas.add(lemma);
            }
        }
        return filteredLemmas;
    }

    private List<String> sortLemmas(List<String> lemmas) {
        lemmas.sort((a, b) -> Integer.compare(lemmaRepository.getFrequency(b), lemmaRepository.getFrequency(a)));
        return lemmas;
    }

    private List<PageDto> findPages(String lemma) {
        // Find pages that match the lemma
        List<PageDto> pages = pageRepository.findByLemma(lemma);
        return pages;
    }

    private List<PageDto> filterPages(List<PageDto> pages, String lemma) {
        List<PageDto> filteredPages = new ArrayList<>();
        for (PageDto page : pages) {
            if (page.getLemmas().contains(lemma)) {
                filteredPages.add(page);
            }
        }
        return filteredPages;
    }

    private double calculateRelevance(PageDto page, List<String> lemmas) {
        double relevance = 0;
        for (String lemma : lemmas) {
            int rank = indexRepository.getRank(page.getSiteId(), lemma);
            relevance += rank;
        }
        return relevance / lemmas.size();
    }

    private String getSnippet(PageDto page, List<String> lemmas) {
        // Generate a snippet for the page
        String snippet = "";
        for (String lemma : lemmas) {
            snippet += "<b>" + lemma + "</b> ";
        }
        return snippet;
    }

}
