package searchengine.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.PageDto;
import searchengine.model.SiteDto;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageDto, Integer>
{
   List<PageDto> findBySiteId(SiteDto siteId);
   Index findByUrl(String url);

   void deleteByUrl(String url);

   int countBySiteId();

   List<PageDto> findByLemma(String lemma);
}
