package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
@Entity
@Table(name = "lemma")
public class Lemma
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteDto siteId;
    @NotNull
    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String lemma;
    @NotNull
    private int frequency;

}
