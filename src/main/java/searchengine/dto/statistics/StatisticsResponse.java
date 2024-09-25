package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class StatisticsResponse {

    private boolean result;
    private StatisticsData statistics;



}
