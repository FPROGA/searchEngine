package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Setter
@Getter
@Table(name = "index")
public class Index
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private PageDto pageId;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemmaId;
    @NotNull
    private float rank;
}
