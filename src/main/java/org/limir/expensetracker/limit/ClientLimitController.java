package org.limir.expensetracker.limit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.limir.expensetracker.common.ValidationPatterns;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** API B (client): monthly limits of the client's account. */
@Tag(name = "Client: limits")
@RestController
@RequestMapping("/client/v1/accounts/{account}/limits")
@RequiredArgsConstructor
public class ClientLimitController {

    private final LimitService limitService;
    private final LimitMapper limitMapper;

    @Operation(summary = "Set a new monthly limit",
            description = "The limit date is always the current time. Existing limits are never changed.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LimitResponse setLimit(
            @Parameter(example = "0000000123")
            @PathVariable @Pattern(regexp = ValidationPatterns.ACCOUNT_NUMBER, message = "must be 10 digits")
            String account,
            @Valid @RequestBody LimitRequest request) {
        return limitMapper.toResponse(limitService.setLimit(account, request.expenseCategory(), request.limitSum()));
    }

    @Operation(summary = "All limits of the account, newest first")
    @GetMapping
    public List<LimitResponse> getLimits(
            @Parameter(example = "0000000123")
            @PathVariable @Pattern(regexp = ValidationPatterns.ACCOUNT_NUMBER, message = "must be 10 digits")
            String account) {
        return limitService.getLimits(account).stream()
                .map(limitMapper::toResponse)
                .toList();
    }
}
