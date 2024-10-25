package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.List;

public class CheckLemmaService {
    public static void main(String[] args) {
        LuceneMorphology luceneMorph =
                null;
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> wordBaseForms =
                luceneMorph.getNormalForms("леса");
        wordBaseForms.forEach(System.out::println);
    }
}
