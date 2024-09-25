package searchengine;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Response;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageDto;
import searchengine.model.SiteDto;
import searchengine.repositorys.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import searchengine.repositorys.PageRepository;
import searchengine.services.ParsePageTask;

@Slf4j
@Getter
@Setter
public class LemmaProcess
{
    @Autowired
    private static PageRepository pageRepository;
    @Autowired
    private static  SiteRepository siteRepository;
    @Autowired
    private static LemmaRepository lemmaRepository;
    @Autowired
    private static IndexRepository indexRepository;
    private static String pageUrl;



    public LemmaProcess(PageRepository pageRepository, SiteRepository siteRepository, String pageUrl) {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.pageUrl = pageUrl;
    }

    public static Response lemprocess() {
    //получение текста с сайта и очищение его от тегов
        // сохрание текста в Page
        // Создание и сохранение лемм и индексов
        Index index = pageRepository.findByUrl(pageUrl);
        if (index!=null)
        {
            pageRepository.deleteByUrl(pageUrl);
            indexRepository.deleteByUrl(pageUrl);
            lemmaRepository.deleteByUrl(pageUrl);
        }
        String text = clearHtmlTextFromTegs(pageUrl);
        try {
            saveHtmlTextToDataBase(text, pageUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
        HashMap <String, Integer> lemmas= new HashMap<>();
        try {
            lemmas =  getLemmas(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        saveLemmasAndIndex(lemmas);

        return new Response(HttpServletResponse.SC_OK);

    }

    private static void saveLemmasAndIndex(HashMap<String, Integer> lemmas)
    {
        for (String key : lemmas.keySet())
        {
            Lemma lemma = new Lemma();
            lemma.setLemma(key);
            if (lemmaRepository.findByLemma(key))
            {
                int freq = lemmaRepository.getLemma(key);
                lemma.setFrequency(freq++);
            }
            else {
                lemma.setFrequency(1);
            }
            lemmaRepository.save(lemma);
            Index index = new Index();
            index.setLemmaId(lemma);
            index.setRank(lemma.getFrequency());
        }
    }


    public static HashMap<String, Integer> getLemmas(String text) throws IOException {
         HashSet<String> STOP_WORDS = new HashSet<>(Arrays.asList(
                "и", "в", "не", "на", "с", "что", "как", "по", "из", "за", "то", "так", "это", "вот", "ли", "же", "бы", "да", "или", "между", "для", "о", "об", "над", "под", "при", "более", "менее"
                // Добавьте другие слова, которые хотите игнорировать
        ));
        HashMap<String, Integer> lemmas = new HashMap<>();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            String sanitizedWord = word.replaceAll("[^а-яА-ЯёЁ]", "").toLowerCase();

            if (sanitizedWord.isEmpty() || STOP_WORDS.contains(sanitizedWord)) {
                continue;
            }

            List<String> normalForms = luceneMorph.getNormalForms(sanitizedWord);
            if (normalForms.isEmpty()) {
                continue; // d
            }

            String lemma = normalForms.get(0);
            lemmas.put(lemma, lemmas.getOrDefault(lemma, 0) + 1); // Update count
        }
        return lemmas;
    }

    public static String clearHtmlTextFromTegs(String urlText) {
        String text = "";
        try {
            URL url = new URL(urlText);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    text += line.replaceAll("<[^>]*>", " ").replace("&nbsp;", " ").trim() + " ";
                }
            }
        } catch (MalformedURLException e) {
            System.err.println("Ошибка: Некорректный URL");
        } catch (IOException e) {
            System.err.println("Ошибка: Не удалось прочитать содержимое URL");
        }
        return text.trim();
    }
    public static void saveHtmlTextToDataBase(String text, String urlText) throws IOException {
        ForkJoinPool pool = new ForkJoinPool();
        SiteDto siteId = siteRepository.findByUrl(urlText);
        ParsePageTask parsePageTask = new ParsePageTask(urlText, siteId ,pageRepository, pool);

    }
}