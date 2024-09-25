package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class StatisticsData {
    private TotalStatistics total;
    private List<DetailedStatisticsItem> detailed;



}
