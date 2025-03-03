package uk.gov.hmcts.dts.fact;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.model.CourtReferenceWithDistance;
import uk.gov.hmcts.dts.fact.model.ServiceAreaWithCourtReferencesWithDistance;
import uk.gov.hmcts.dts.fact.model.deprecated.CourtWithDistance;

import java.util.Comparator;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(SpringExtension.class)
public class SearchEndpointTest {

    private static final String CONTENT_TYPE_VALUE = "application/json";
    private static final String SEARCH_ENDPOINT = "/search/";


    @Value("${TEST_URL:http://localhost:8080}")
    private String testUrl;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = testUrl;
    }

    @Test
    public void shouldRetrieve10CourtDetailsSortedByDistance() {
        final var response = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results.json?postcode=OX1 1RZ")
            .thenReturn();


        assertThat(response.statusCode()).isEqualTo(OK.value());
        final List<CourtWithDistance> courts = response.body().jsonPath().getList(".", CourtWithDistance.class);
        assertThat(courts.size()).isEqualTo(10);
        assertThat(courts).isSortedAccordingTo(Comparator.comparing(CourtWithDistance::getDistance));
    }

    @Test
    public void shouldRetrieve10CourtDetailsByAreaOfLawSortedByDistance() {
        final String aol = "Adoption";
        final var response = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results.json?postcode=OX1 1RZ&aol=" + aol)
            .thenReturn();

        assertThat(response.statusCode()).isEqualTo(OK.value());
        final List<CourtWithDistance> courts = response.body().jsonPath().getList(".", CourtWithDistance.class);
        assertThat(courts.size()).isEqualTo(10);
        assertThat(courts).isSortedAccordingTo(Comparator.comparing(CourtWithDistance::getDistance));
        assertTrue(courts
            .stream()
            .allMatch(c -> c.getAreasOfLaw()
                .stream()
                .anyMatch(a -> a.getName().equals(aol))));
    }

    @Test
    public void postcodeSearchShouldSupportWelsh() {
        final var welshResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT_LANGUAGE, "cy")
            .when()
            .get(SEARCH_ENDPOINT + "results.json?postcode=CF10 1ET")
            .thenReturn();

        assertThat(welshResponse.statusCode()).isEqualTo(OK.value());
        final List<CourtWithDistance> welshCourts = welshResponse.body().jsonPath()
            .getList(".", CourtWithDistance.class
            );
        assertThat(welshCourts.get(0).getAddress().getTownName()).isEqualTo("Caerdydd");

        final var englishResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results.json?postcode=CF10 1ET")
            .thenReturn();

        assertThat(englishResponse.statusCode()).isEqualTo(OK.value());
        final List<CourtWithDistance> englishCourts = englishResponse.body().jsonPath()
            .getList(".", CourtWithDistance.class);
        assertThat(englishCourts.get(0).getAddress().getTownName()).isEqualTo("Cardiff");
    }

    @Test
    public void nameSearchShouldSupportWelsh() {
        final var welshResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT_LANGUAGE, "cy")
            .when()
            .get(SEARCH_ENDPOINT + "results.json?q=caerdydd")
            .thenReturn();

        assertThat(welshResponse.statusCode()).isEqualTo(OK.value());
        final List<CourtWithDistance> welshCourts = welshResponse.body().jsonPath()
            .getList(".", CourtWithDistance.class
            );
        assertThat(welshCourts.get(0).getAddress().getTownName()).isEqualTo("Caerdydd");

        final var englishResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results.json?q=cardiff")
            .thenReturn();

        assertThat(englishResponse.statusCode()).isEqualTo(OK.value());
        final List<CourtWithDistance> englishCourts = englishResponse.body().jsonPath()
            .getList(".", CourtWithDistance.class);
        assertThat(englishCourts.get(0).getAddress().getTownName()).isEqualTo("Cardiff");
    }

    @Test
    public void shouldRetrieve10CourtReferenceByPostcodeAndServiceAreaSortedByDistance() {
        final String serviceArea = "claims-against-employers";
        final var response = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results?postcode=OX1 1RZ&serviceArea=" + serviceArea)
            .thenReturn();

        assertThat(response.statusCode()).isEqualTo(OK.value());
        final ServiceAreaWithCourtReferencesWithDistance serviceAreaWithCourtReferencesWithDistance =
            response.as(ServiceAreaWithCourtReferencesWithDistance.class);

        assertThat(serviceAreaWithCourtReferencesWithDistance.getCourts().size()).isEqualTo(10);
        assertThat(serviceAreaWithCourtReferencesWithDistance.getCourts()).isSortedAccordingTo(Comparator.comparing(
            CourtReferenceWithDistance::getDistance));
    }

    @Test
    public void shouldRetrieve10CourtReferenceByPostcodeAndServiceAreaSupportWelsh() {
        final String serviceArea = "claims-against-employers";
        final var englishResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results?postcode=CF24 0RZ&serviceArea=" + serviceArea)
            .thenReturn();

        assertThat(englishResponse.statusCode()).isEqualTo(OK.value());
        final ServiceAreaWithCourtReferencesWithDistance englishSrviceAreaCourtReferencesWithDistance =
            englishResponse.as(ServiceAreaWithCourtReferencesWithDistance.class);
        assertThat(englishSrviceAreaCourtReferencesWithDistance.getCourts().get(0).getName()).isEqualTo(
            "Cardiff  Magistrates' Court");

        final var welshResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT_LANGUAGE, "cy")
            .when()
            .get(SEARCH_ENDPOINT + "results?postcode=CF24 0RZ&serviceArea=" + serviceArea)
            .thenReturn();

        assertThat(welshResponse.statusCode()).isEqualTo(OK.value());
        final ServiceAreaWithCourtReferencesWithDistance serviceAreaWithCourtReferencesWithDistance =
            welshResponse.as(ServiceAreaWithCourtReferencesWithDistance.class);
        assertThat(serviceAreaWithCourtReferencesWithDistance.getCourts().get(0).getName()).isEqualTo(
            "Caerdydd  - Llys Ynadon");
    }

    @Test
    public void shouldRetrieveRegionalCourtReferenceByPostcodeAndServiceArea() {
        final String serviceArea = "divorce";
        final var response = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results?postcode=IP1 2AG&serviceArea=" + serviceArea)
            .thenReturn();

        assertThat(response.statusCode()).isEqualTo(OK.value());
        final ServiceAreaWithCourtReferencesWithDistance serviceAreaWithCourtReferencesWithDistance =
            response.as(ServiceAreaWithCourtReferencesWithDistance.class);

        assertThat(serviceAreaWithCourtReferencesWithDistance.getCourts().size()).isEqualTo(1);
    }

    @Test
    public void shouldRetrieveClosestRegionalCourt() {
        final String serviceArea = "divorce";
        final var response = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results?postcode=TR11 2PH&serviceArea=" + serviceArea)
            .thenReturn();

        assertThat(response.statusCode()).isEqualTo(OK.value());
        final ServiceAreaWithCourtReferencesWithDistance serviceAreaWithCourtReferencesWithDistance =
            response.as(ServiceAreaWithCourtReferencesWithDistance.class);

        assertThat(serviceAreaWithCourtReferencesWithDistance.getCourts().size()).isEqualTo(1);
    }

    @Test
    public void shouldRetrieveCourtReferenceByCourtPostcodeAndServiceAreaSortedByDistance() {
        final String serviceArea = "money-claims";
        final var response = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "results?postcode=W1U 6PU&serviceArea=" + serviceArea)
            .thenReturn();

        assertThat(response.statusCode()).isEqualTo(OK.value());
        final ServiceAreaWithCourtReferencesWithDistance serviceAreaWithCourtReferencesWithDistance =
            response.as(ServiceAreaWithCourtReferencesWithDistance.class);

        assertThat(serviceAreaWithCourtReferencesWithDistance.getCourts().size()).isEqualTo(1);
        assertThat(serviceAreaWithCourtReferencesWithDistance.getCourts().get(0).getName()).isEqualTo(
            "Central London County Court");
        assertThat(serviceAreaWithCourtReferencesWithDistance.getCourts()).isSortedAccordingTo(Comparator.comparing(
            CourtReferenceWithDistance::getDistance));
    }
}
