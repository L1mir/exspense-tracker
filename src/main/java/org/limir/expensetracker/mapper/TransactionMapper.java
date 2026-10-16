package org.limir.expensetracker.mapper;

import org.limir.expensetracker.dto.ExceededTransactionResponse;
import org.limir.expensetracker.dto.TransactionResponse;
import org.limir.expensetracker.entity.ExpenseTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface TransactionMapper {

    @Mapping(target = "accountFrom", source = "accountFrom.number")
    @Mapping(target = "currencyShortname", source = "currency")
    @Mapping(target = "expenseCategory", source = "category")
    TransactionResponse toResponse(ExpenseTransaction transaction);

    @Mapping(target = "accountFrom", source = "accountFrom.number")
    @Mapping(target = "currencyShortname", source = "currency")
    @Mapping(target = "expenseCategory", source = "category")
    @Mapping(target = "limitSum", source = "spendingLimit.amount")
    @Mapping(target = "limitDatetime", source = "spendingLimit.setAt")
    @Mapping(target = "limitCurrencyShortname", source = "spendingLimit.currency")
    ExceededTransactionResponse toExceededResponse(ExpenseTransaction transaction);
}
