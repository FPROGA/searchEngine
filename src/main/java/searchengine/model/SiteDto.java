package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "site")
@Getter
@Setter
public class SiteDto
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL")
    private Status status;
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    @NotNull
    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String url;
    @NotNull
    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String name;
    @NotNull
    @Column(name = "status_time")
    private LocalDateTime statusTime;
    @PrePersist
    public void onCreate() {
        statusTime = LocalDateTime.now();
    }
}
