package com.example.financeapp.utils;
import java.time.*;
import java.text.SimpleDateFormat;

public class StockDateUtil {
    public static long getLastWorkdayTS() {
        LocalDate utcToday = LocalDate.now(ZoneOffset.UTC);
        LocalDate date = utcToday.minusDays(1); // start from "yesterday" in UTC

        while (isWeekend(date)) {
            date = date.minusDays(1);
        }

        // Convert to timestamp (00:00:00 UTC)
        ZonedDateTime startDayUtc = date.atStartOfDay(ZoneOffset.UTC);
        return startDayUtc.toEpochSecond();
    }

    public static int getNumberOfWorkdays(int days) {
        int numberOfWorkdays = 1;
        LocalDate date = LocalDate.now(ZoneOffset.UTC);

        while (days > 0) {
            if(!isWeekend(date))
                numberOfWorkdays ++;

            date = date.minusDays(1);
            days--;
        }

        return numberOfWorkdays;
    }

    public static String getReadableDateUtc(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        return formatter.format(timestamp*1000);
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
