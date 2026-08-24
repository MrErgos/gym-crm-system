package io.github.mrergos.workinghours.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.FetchType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "monthly_summary",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trainer_username", "year", "month"})
)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class MonthlySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_username", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Trainer trainer;

    @Column(name = "c_year", nullable = false)
    private int year;

    @Column(name = "c_month", nullable = false)
    private int month;

    @Column(name = "total_duration_minutes", nullable = false)
    private int totalDurationMinutes;

    public MonthlySummary(Trainer trainer, int year, int month, int totalDurationMinutes) {
        this.trainer = trainer;
        this.year = year;
        this.month = month;
        this.totalDurationMinutes = totalDurationMinutes;
    }
}
