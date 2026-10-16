package org.limir.expensetracker.transaction;

import org.limir.expensetracker.common.DateTimeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Entity to DTO mapping; MapStruct generates the implementation at compile time. */
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
