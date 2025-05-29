package br.cesar.school.linksentinel.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode; // <-- IMPORTAR
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // <-- ADICIONAR ESTA LINHA
@Entity
@Table(name = "check_results")
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include // <-- ADICIONAR ESTA LINHA (Indica que o ID entra no equals/hashCode)
    private UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime checkTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer httpStatusCode;
    private Boolean reachable;
    private String finalUrl;
    @Column(length = 1024)
    private String redirectChain;
    private Long responseTimeMs;
    private Boolean safeBrowseOk;
    @Column(length = 1024)
    private String safeBrowseThreats;
    @Column(columnDefinition = "TEXT")
    private String geminiAnalysisResult;
    @Column(length = 1024)
    private String errorMessage;

    public CheckResult(Link link, User user) {
        this.link = link;
        this.user = user;
    }
}