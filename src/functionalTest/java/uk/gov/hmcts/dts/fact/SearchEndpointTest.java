package uk.gov.hmcts.dts.fact;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.dts.fact.model.deprecated.CourtWithDistance;

import java.util.Comparator;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@ExtendWith(SpringExtension.class)
public class SearchEndpointTest {

    private static final String CONTENT_TYPE_VALUE = "application/json";
    private static final String SEARCH_ENDPOINT = "/search/results.json";


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
            .get(SEARCH_ENDPOINT + "?postcode=OX1 1RZ")
            .thenReturn();


        assertThat(response.statusCode()).isEqualTo(200);
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
            .get(SEARCH_ENDPOINT + "?postcode=OX1 1RZ&aol=" + aol)
            .thenReturn();

        assertThat(response.statusCode()).isEqualTo(200);
        final List<CourtWithDistance> courts = response.body().jsonPath().getList(".", CourtWithDistance.class);
        assertThat(courts.size()).isEqualTo(10);
        assertThat(courts).isSortedAccordingTo(Comparator.comparing(CourtWithDistance::getDistance));
        assertThat(courts.stream().allMatch(c -> c.getAreasOfLaw().contains(aol)));
    }

    @Test
    public void postcodeSearchShouldSupportWelsh() {
        final var welshResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .header(ACCEPT_LANGUAGE, "cy")
            .when()
            .get(SEARCH_ENDPOINT + "?postcode=CF10 1ET")
            .thenReturn();

        assertThat(welshResponse.statusCode()).isEqualTo(200);
        final List<CourtWithDistance> welshCourts = welshResponse.body().jsonPath()
            .getList(".", CourtWithDistance.class
            );
        assertThat(welshCourts.get(0).getAddress().getTownName()).isEqualTo("Caerdydd");

        final var englishResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "?postcode=CF10 1ET")
            .thenReturn();

        assertThat(englishResponse.statusCode()).isEqualTo(200);
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
            .get(SEARCH_ENDPOINT + "?q=cardiff") //TODO should be 'caerdydd'
            .thenReturn();

        assertThat(welshResponse.statusCode()).isEqualTo(200);
        final List<CourtWithDistance> welshCourts = welshResponse.body().jsonPath()
            .getList(".", CourtWithDistance.class
            );
        assertThat(welshCourts.get(0).getAddress().getTownName()).isEqualTo("Caerdydd");

        final var englishResponse = given()
            .relaxedHTTPSValidation()
            .header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
            .when()
            .get(SEARCH_ENDPOINT + "?q=cardiff")
            .thenReturn();

        assertThat(englishResponse.statusCode()).isEqualTo(200);
        final List<CourtWithDistance> englishCourts = englishResponse.body().jsonPath()
            .getList(".", CourtWithDistance.class);
        assertThat(englishCourts.get(0).getAddress().getTownName()).isEqualTo("Cardiff");
    }
}
