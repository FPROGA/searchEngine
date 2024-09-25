package searchengine.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer>
{
    boolean findByLemma(String lemma);
    int getLemma (String lemma);

    void deleteByUrl(String pageUrl);

    int countBySiteId();
}
