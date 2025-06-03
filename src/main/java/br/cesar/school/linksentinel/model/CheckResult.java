package br.cesar.school.linksentinel.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @Column(nullable = false)
    private LocalDateTime checkTimestamp;

    @Column(nullable = false)
    private int statusCode;

    @Column(columnDefinition = "TEXT")
    private String finalUrl;

    @Column(nullable = false)
    private boolean accessible;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String originalUrl;

    private String status;
}