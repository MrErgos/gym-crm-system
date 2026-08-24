package io.github.mrergos.workinghours.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "trainer_summaries")
@CompoundIndex(name = "trainer_full_name_idx", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class TrainerWorkloadSummaryDocument {

    @Id
    private String trainerUsername;

    @Field("trainerFirstName")
    private String trainerFirstName;

    @Field("trainerLastName")
    private String trainerLastName;

    @Field("trainerStatus")
    private boolean trainerStatus;

    @Field("years")
    private List<YearSummary> years = new ArrayList<>();

    public TrainerWorkloadSummaryDocument(String trainerUsername, String trainerFirstName,
                                          String trainerLastName, boolean trainerStatus) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.trainerStatus = trainerStatus;
    }
}

