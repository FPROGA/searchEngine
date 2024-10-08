package searchengine.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.SiteDto;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer>
{

    void deleteByUrl(String pageUrl);
    int getRank(SiteDto siteId, String lemma);
}
