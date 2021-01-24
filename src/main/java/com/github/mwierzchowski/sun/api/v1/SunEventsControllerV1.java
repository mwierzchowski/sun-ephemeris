package com.github.mwierzchowski.sun.api.v1;

import com.github.mwierzchowski.sun.core.SunEphemerisProvider;
import com.github.mwierzchowski.sun.core.SunEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/v1/events")
@Tag(name = "Sun events", description = "Sun events resource")
public class SunEventsControllerV1 {
    private final SunEphemerisProvider provider;
    private final Clock clock;

    @GetMapping(path = "/")
    @Operation(summary = "All sun events on a date")
    @ApiResponse(responseCode = "200", description = "List of sun events")
    public List<SunEvent> getEvents(
            @Parameter(description = "Date ISO 8601", example = "2021-01-23", schema = @Schema(defaultValue = "today"))
            @RequestParam(required = false) LocalDate date) {
        if (date == null) {
            date = LocalDate.now(clock);
            LOG.debug("No date provided, assuming today");
        }
        LOG.debug("Request for {} sun events", date);
        return provider.sunEphemerisFor(date).stream()
                .collect(toList());
    }

    @GetMapping(path = "/next")
    @Operation(summary = "Next sun event to happen from now")
    @ApiResponse(responseCode = "200", description = "Sun event")
    public SunEvent getNextEvent() {
        LOG.debug("Request for next sun event after now");
        var today = LocalDate.now(clock);
        var now = Instant.now(clock);
        return provider.sunEphemerisFor(today)
                .firstEventAfter(now)
                .orElseGet(() -> provider.sunEphemerisFor(today.plusDays(1)).firstEvent());
    }
}
