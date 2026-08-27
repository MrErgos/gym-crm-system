package io.github.mrergos.workinghours.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class YearSummary {

    private int year;
    private List<MonthlySummary> months = new ArrayList<>();

    public YearSummary(int year) {
        this.year = year;
    }
}

