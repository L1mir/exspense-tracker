package org.limir.expensetracker.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.limir.expensetracker.common.ValidationPatterns;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API B (client): transactions of the client's account. */
@Tag(name = "Client: transactions")
@RestController
@RequestMapping("/client/v1/accounts/{account}/transactions")
@RequiredArgsConstructor
public class ClientTransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Operation(summary = "Transactions that exceeded the monthly limit",
            description = "Each transaction comes with the limit it exceeded: amount, date and currency.")
    @GetMapping("/exceeded")
    public List<ExceededTransactionResponse> getExceeded(
            @Parameter(example = "0000000123")
            @PathVariable @Pattern(regexp = ValidationPatterns.ACCOUNT_NUMBER, message = "must be 10 digits")
            String account) {
        return transactionService.getExceeded(account).stream()
                .map(transactionMapper::toExceededResponse)
                .toList();
    }
}
