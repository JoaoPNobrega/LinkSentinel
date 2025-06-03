package br.cesar.school.linksentinel.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "links")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime firstChecked;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastChecked;

    @Column(nullable = false)
    private boolean monitored = false;

    @OneToMany(
            mappedBy = "link",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<CheckResult> checkResults = new ArrayList<>();


    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int consecutiveDownCount = 0;

    @Column(length = 50) 
    private String internalMonitoringStatus = "UNKNOWN";

    public Link(String url) {
        this.url = url;

    }

    public void addCheckResult(CheckResult result) {
        checkResults.add(result);
        result.setLink(this);
    }


}