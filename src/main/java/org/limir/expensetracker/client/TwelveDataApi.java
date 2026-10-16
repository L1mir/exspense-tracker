package org.limir.expensetracker.client;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface TwelveDataApi {

    /**
     * With outputsize=1 returns the last close on or before end_date, i.e. the previous close on weekends.
     * The ISO date format is explicit, otherwise the date is formatted by the system locale (03.01.2022).
     */
    @GetExchange("/time_series")
    TimeSeriesResponse getTimeSeries(@RequestParam("symbol") String symbol,
                                     @RequestParam("interval") String interval,
                                     @RequestParam("end_date") @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
                                     @RequestParam("outputsize") int outputSize);
}
