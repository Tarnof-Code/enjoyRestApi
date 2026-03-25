package com.tarnof.enjoyrestapi.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateFormatHelper {

    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateFormatHelper() {
    }

    /**
     * Formate une date au format jour/mois/année (23/10/2025).
     */
    public static String formatDdMmYyyy(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date");
        }
        return date.format(DD_MM_YYYY);
    }
}
