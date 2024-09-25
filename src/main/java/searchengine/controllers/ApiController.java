package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.LemmaProcess;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repositorys.PageRepository;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final LemmaProcess lemmaProcess;

    public ApiController(StatisticsService statisticsService, LemmaProcess lemmaProcess)
    {
        this.statisticsService = statisticsService;
        this.lemmaProcess = lemmaProcess;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing()
    {
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
}
