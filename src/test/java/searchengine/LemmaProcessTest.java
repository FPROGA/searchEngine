package java.searchengine;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Response;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import searchengine.LemmaProcess;
import searchengine.model.SiteDto;
import searchengine.repositorys.IndexRepository;
import searchengine.repositorys.LemmaRepository;
import searchengine.repositorys.PageRepository;
import searchengine.repositorys.SiteRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LemmaProcessTest {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private LemmaRepository lemmaRepository;

    @Mock
    private IndexRepository indexRepository;

    @InjectMocks
    private LemmaProcess lemmaProcess;

    private static final String PAGE_URL = "https://example.com";

    @BeforeEach
    public void setUp() {
        lemmaProcess = new LemmaProcess(pageRepository, siteRepository, PAGE_URL);
    }

    @Test
    public void testLemprocess() throws IOException {
        // Arrange
        when(pageRepository.findByUrl(PAGE_URL)).thenReturn(null);
        when(siteRepository.findByUrl(PAGE_URL)).thenReturn(new SiteDto());
        when(lemmaRepository.findByLemma(any())).thenReturn(false);
        when(lemmaRepository.getLemma(any())).thenReturn(1);

        // Act
        Response response = lemmaProcess.lemprocess();

        // Assert
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        verify(pageRepository, times(1)).deleteByUrl(PAGE_URL);
        verify(indexRepository, times(1)).deleteByUrl(PAGE_URL);
        verify(lemmaRepository, times(1)).deleteByUrl(PAGE_URL);
        verify(lemmaRepository, times(1)).save(any());
        verify(indexRepository, times(1)).save(any());
    }

    @Test
    public void testGetLemmas() throws IOException {
        // Arrange
        String text = "Привет мир!";
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        List<String> normalForms = luceneMorph.getNormalForms("мир");
        when(luceneMorph.getNormalForms(any())).thenReturn(normalForms);

        // Act
        HashMap<String, Integer> lemmas = lemmaProcess.getLemmas(text);

        // Assert
        assertNotNull(lemmas);
        assertEquals(1, lemmas.size());
        assertEquals("мир", lemmas.keySet().iterator().next());
    }

    @Test
    public void testClearHtmlTextFromTegs() throws MalformedURLException, IOException {
        // Arrange
        URL url = new URL("https://example.com");
        String text = "<html><body>Привет мир!</body></html>";

        // Act
        String clearedText = lemmaProcess.clearHtmlTextFromTegs(url.toString());

        // Assert
        assertEquals("Привет мир!", clearedText);
    }

    @Test
    public void testSaveHtmlTextToDataBase() throws IOException {
        // Arrange
        String text = "Привет мир!";
        SiteDto siteId = new SiteDto();
        when(siteRepository.findByUrl(PAGE_URL)).thenReturn(siteId);

        // Act
        lemmaProcess.saveHtmlTextToDataBase(text, PAGE_URL);

        // Assert
        verify(pageRepository, times(1)).save(any());
    }

    @Test
    public void testSaveLemmasAndIndex() {
        // Arrange
        HashMap<String, Integer> lemmas = new HashMap<>();
        lemmas.put("мир", 1);
        when(lemmaRepository.findByLemma(any())).thenReturn(false);
        when(lemmaRepository.getLemma(any())).thenReturn(1);

        // Act
        lemmaProcess.saveLemmasAndIndex(lemmas);

        // Assert
        verify(lemmaRepository, times(1)).save(any());
        verify(indexRepository, times(1)).save(any());
    }
}

