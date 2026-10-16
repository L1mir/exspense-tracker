package org.limir.expensetracker.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.limir.expensetracker.dto.TransactionRequest;
import org.limir.expensetracker.dto.TransactionResponse;
import org.limir.expensetracker.mapper.TransactionMapper;
import org.limir.expensetracker.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Integration: transactions", description = "Receiving expense transactions from bank systems")
@RestController
@RequestMapping("/integration/v1/transactions")
@RequiredArgsConstructor
public class IntegrationTransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Operation(summary = "Receive an expense transaction",
            description = "Converts the amount to USD by the rate of the transaction day and sets limit_exceeded. "
                    + "Responds 503 if no exchange rate is available; the sender should retry later.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse receive(@Valid @RequestBody TransactionRequest request) {
        return transactionMapper.toResponse(transactionService.process(request));
    }
}
