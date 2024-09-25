package searchengine.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Getter
@RequiredArgsConstructor
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<Site> sites;
    public List<Site> getSites() {
        return sites;
    }

}
