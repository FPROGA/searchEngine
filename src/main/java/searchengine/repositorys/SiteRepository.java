package searchengine.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteDto;
import searchengine.model.Status;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<SiteDto, Integer>
{
    SiteDto findByUrl(String url);

    List<SiteDto> findAllByStatus(Status status);
}
