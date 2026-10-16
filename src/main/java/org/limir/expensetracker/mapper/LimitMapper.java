package org.limir.expensetracker.mapper;

import org.limir.expensetracker.dto.LimitResponse;
import org.limir.expensetracker.entity.SpendingLimit;
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
