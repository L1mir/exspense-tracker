package org.limir.expensetracker.limit;

import org.limir.expensetracker.common.DateTimeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface LimitMapper {

    @Mapping(target = "expenseCategory", source = "category")
    @Mapping(target = "limitSum", source = "amount")
    @Mapping(target = "limitDatetime", source = "setAt")
    @Mapping(target = "limitCurrencyShortname", source = "currency")
    LimitResponse toResponse(SpendingLimit limit);
}
