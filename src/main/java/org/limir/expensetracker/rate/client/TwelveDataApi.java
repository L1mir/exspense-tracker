package org.limir.expensetracker.rate.client;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * HTTP interface of twelvedata.com. Spring generates the implementation (see {@link TwelveDataClientConfig}).
 *
 * @see <a href="https://twelvedata.com/docs#time-series">Time series documentation</a>
 */
@HttpExchange
public interface TwelveDataApi {

    /**
     * Daily candles of a currency pair, newest first. With {@code outputsize=1} and {@code end_date}
     * returns the last close on or before that date, so weekends and holidays fall back to the previous close.
     */
    @GetExchange("/time_series")
    TimeSeriesResponse getTimeSeries(@RequestParam("symbol") String symbol,
                                     @RequestParam("interval") String interval,
                                     // Without an explicit ISO format the date is formatted by the system locale
                                     @RequestParam("end_date") @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
                                     @RequestParam("outputsize") int outputSize);
}
