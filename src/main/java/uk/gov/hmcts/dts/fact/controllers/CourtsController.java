package uk.gov.hmcts.dts.fact.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dts.fact.model.Court;
import uk.gov.hmcts.dts.fact.model.CourtReference;
import uk.gov.hmcts.dts.fact.model.deprecated.OldCourt;
import uk.gov.hmcts.dts.fact.services.CourtService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(
    path = "/courts",
    produces = {MediaType.APPLICATION_JSON_VALUE}
)
public class CourtsController {

    private final CourtService courtService;

    @Autowired
    public CourtsController(final CourtService courtService) {
        this.courtService = courtService;
    }

    /**
     * Find court by name.
     *
     * @deprecated Use {@link #findCourtByName}, path = /{slug}}
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @GetMapping(path = "/{slug}.json")
    @ApiOperation("Find court details by name")
    public ResponseEntity<OldCourt> findCourtByNameDeprecated(@PathVariable String slug) {
        return ok(courtService.getCourtBySlugDeprecated(slug));
    }

    @GetMapping
    @ApiOperation("Find courts by name, address, town or postcode")
    public ResponseEntity<List<CourtReference>> findCourtByNameOrAddressOrPostcodeOrTown(@RequestParam(name = "q") String query) {
        if (query.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ok(courtService.getCourtByNameOrAddressOrPostcodeOrTown(query));
    }

    @GetMapping(path = "/{slug}")
    @ApiOperation("Find court details by slug")
    public ResponseEntity<Court> findCourtByName(@PathVariable String slug) {
        return ok(courtService.getCourtBySlug(slug));
    }
}
