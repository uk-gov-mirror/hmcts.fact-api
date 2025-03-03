package uk.gov.hmcts.dts.fact.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static uk.gov.hmcts.dts.fact.util.Utils.chooseString;

@Getter
@NoArgsConstructor
@JsonPropertyOrder({"name", "slug", "updated_at"})
public class CourtReference {
    private String name;
    private String slug;
    @JsonProperty("updated_at")
    private String updatedAt;

    public CourtReference(uk.gov.hmcts.dts.fact.entity.Court courtEntity) {
        this.name = chooseString(courtEntity.getNameCy(), courtEntity.getName());
        this.slug = courtEntity.getSlug();
        this.updatedAt = courtEntity.getUpdatedAt() == null
            ? null : new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(courtEntity.getUpdatedAt());
    }
}
