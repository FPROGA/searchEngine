package searchengine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "page")
public class PageDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteDto siteId;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String path;

    @Column(name = "index_path")
    private Index indexPath;

    @NotNull
    private int code;

    @NotNull
    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String name;

    @NotNull
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    private ArrayList<Lemma> lemmas;

    public List<Lemma> getLemmas() {
        return lemmas;
    }

    public void setLemmas(ArrayList<Lemma> lemmas) {
        this.lemmas = lemmas;
    }
}

