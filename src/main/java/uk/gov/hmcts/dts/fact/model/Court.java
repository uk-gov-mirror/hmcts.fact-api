package uk.gov.hmcts.dts.fact.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.dts.fact.entity.CourtType;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("PMD.TooManyFields")
@JsonPropertyOrder({"name", "slug", "info", "open", "directions", "lat", "lon", "urgent_message",
    "crown_location_code", "county_location_code", "magistrates_location_code", "areas_of_law",
    "types", "emails", "contacts", "opening_times", "facilities", "addresses", "gbs", "dx_number", "service_area"})
public class Court {
    private String name;
    private String slug;
    private String info;
    private Boolean open;
    private String directions;
    private Double lat;
    private Double lon;
    @JsonProperty("urgent_message")
    private String alert;
    @JsonProperty("crown_location_code")
    private Integer crownLocationCode;
    @JsonProperty("county_location_code")
    private Integer countyLocationCode;
    @JsonProperty("magistrates_location_code")
    private Integer magistratesLocationCode;
    @JsonProperty("areas_of_law")
    private List<AreaOfLaw> areasOfLaw;
    @JsonProperty("types")
    private List<String> courtTypes;
    private List<CourtEmail> emails;
    private List<Contact> contacts;
    @JsonProperty("opening_times")
    private List<OpeningTime> openingTimes;
    @JsonProperty("facilities")
    private List<Facility> facilities;
    private List<CourtAddress> addresses;
    private String gbs;
    @JsonProperty("dx_number")
    private String dxNumber;
    @JsonProperty("service_area")
    private String serviceArea;

    public Court(uk.gov.hmcts.dts.fact.entity.Court courtEntity) {
        this.name = courtEntity.getName();
        this.slug = courtEntity.getSlug();
        this.info = courtEntity.getInfo();
        this.open = courtEntity.getDisplayed();
        this.directions = courtEntity.getDirections();
        this.lat = courtEntity.getLat();
        this.lon = courtEntity.getLon();
        this.alert = courtEntity.getAlert();
        this.crownLocationCode = courtEntity.getNumber();
        this.countyLocationCode = courtEntity.getCciCode();
        this.magistratesLocationCode = courtEntity.getMagistrateCode();
        this.areasOfLaw = courtEntity.getAreasOfLaw().stream().map(AreaOfLaw::new).collect(toList());
        this.courtTypes = courtEntity.getCourtTypes().stream().map(CourtType::getName).collect(toList());
        this.emails = courtEntity.getEmails().stream().map(CourtEmail::new).collect(toList());
        this.contacts = courtEntity.getContacts().stream().map(Contact::new).collect(toList());
        this.openingTimes = courtEntity.getOpeningTimes().stream().map(OpeningTime::new).collect(toList());
        this.facilities = courtEntity.getFacilities().stream().map(Facility::new).collect(toList());
        this.addresses = courtEntity.getAddresses().stream().map(CourtAddress::new).collect(toList());
        this.gbs = courtEntity.getGbs();
        this.dxNumber = this.getDxNumber(courtEntity);
        this.serviceArea = this.getServiceArea(courtEntity);
    }

    private String getDxNumber(uk.gov.hmcts.dts.fact.entity.Court courtEntity) {
        return courtEntity
            .getContacts()
            .stream()
            .filter(c -> "DX".equals(c.getName()))
            .map(uk.gov.hmcts.dts.fact.entity.Contact::getNumber)
            .findFirst()
            .orElse(null);
    }

    private String getServiceArea(uk.gov.hmcts.dts.fact.entity.Court courtEntity) {
        if (courtEntity.getName().toLowerCase().contains("divorce")) {
            return "divorce";
        } else if (courtEntity.getName().toLowerCase().contains("money")) {
            return "money claims";
        } else if (courtEntity.getName().toLowerCase().contains("single-justice")) {
            return "minor crimes";
        } else if (courtEntity.getName().toLowerCase().contains("crime")) {
            return "major crimes";
        } else if (courtEntity.getName().toLowerCase().contains("probate")) {
            return "probate";
        } else {
            return null;
        }
    }

}
