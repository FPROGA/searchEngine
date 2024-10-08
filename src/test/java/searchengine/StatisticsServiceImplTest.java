package java.searchengine;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import searchengine.StatisticsServiceImpl;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.SiteDto;
import searchengine.model.Status;
import searchengine.repositorys.LemmaRepository;
import searchengine.repositorys.PageRepository;
import searchengine.repositorys.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class StatisticsServiceImplTest
{
        @Mock
        private SitesList sites;

        @Mock
        private SiteRepository siteRepository;

        @Mock
        private PageRepository pageRepository;

        @Mock
        private LemmaRepository lemmaRepository;

        @Mock
        private ForkJoinPool pool;

        @InjectMocks
        private StatisticsServiceImpl statisticsService;

        private static final String SITE_URL = "https://example.com";

        @BeforeEach
        public void setUp() {
            when(sites.getSites()).thenReturn(new ArrayList<>());
        }

        @Test
        public void testGetStatistics() {
            // Arrange
            SiteDto siteDto = new SiteDto();
            siteDto.setName("Example Site");
            siteDto.setUrl(SITE_URL);
            siteDto.setStatus(Status.INDEXED);
            siteDto.setStatusTime(LocalDateTime.now());
            when(siteRepository.findAll()).thenReturn(List.of(siteDto));

            StatisticsResponse response = statisticsService.getStatistics();

            assertNotNull(response);
            assertEquals(true, response.getResult());
            assertEquals(1, response.getStatistics().getTotal().getSites());
            assertEquals(1, response.getStatistics().getDetailed().size());
            assertEquals("Example Site", response.getStatistics().getDetailed().get(0).getName());
        }

        @Test
        public void testDeleteSiteByUrl() {

            SiteDto siteDto = new SiteDto();
            siteDto.setId(1);
            siteDto.setUrl(SITE_URL);
            when(siteRepository.findByUrl(SITE_URL)).thenReturn(siteDto);

            statisticsService.deleteSiteByUrl(SITE_URL);

            verify(siteRepository, times(1)).delete(siteDto);
        }

        @Test
        public void testGetSites() {

            List<String> sites = new ArrayList<>();
            sites.add("https://example.com");


            List<String> result = statisticsService.getSites("path");


            assertEquals(sites, result);
        }

        @Test
        public void testStartIndexing() {

            List<String> sites = new ArrayList<>();
            sites.add(SITE_URL);
            when(siteRepository.findByUrl(SITE_URL)).thenReturn(null);

            Response response = statisticsService.startIndexing();

            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        }

        @Test
        public void testStopIndexing() {

            when(pool.isTerminated()).thenReturn(true);

            Response response = statisticsService.stopIndexing();


            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        }

}

