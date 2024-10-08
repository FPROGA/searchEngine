package searchengine;

import org.apache.catalina.connector.Response;
import searchengine.dto.statistics.StatisticsResponse;

public interface StatisticsService {
    StatisticsResponse getStatistics();
    Response startIndexing();
    Response stopIndexing();
}
