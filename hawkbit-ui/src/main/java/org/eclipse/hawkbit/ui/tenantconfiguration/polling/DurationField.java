/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.polling;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.validation.constraints.NotNull;

import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;

import com.vaadin.data.Result;
import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This class represents a Field which is optimized to enter a time duration in
 * form HH:mm:ss (see {@link #DURATION_FORMAT_STIRNG}). It uses the vaadin
 * DateField as a basic element, but the format is optimized for the duration
 * input. For a correct view of the popup it is recommended not to display the
 * css-class "v-datefield-calendarpanel-header" and
 * "v-datefield-calendarpanel-body" (see systemconfig.scss}
 */
public class DurationField extends DateTimeField {

    private static final long serialVersionUID = 1L;

    private static final String CSS_STYLE_NAME = "durationfield";

    private static final String ADDITIONAL_DURATION_STRING = "HHmmss";
    private static final String DURATION_FORMAT_STIRNG = "HH:mm:ss";

    private static final ZoneId ZONEID_UTC = ZoneId.of("+0");

    private static final Duration MAXIMUM_DURATION = Duration.ofHours(23).plusMinutes(59).plusSeconds(59);
    SimpleDateFormat durationFormat = new SimpleDateFormat(DURATION_FORMAT_STIRNG);
    SimpleDateFormat additionalFormat = new SimpleDateFormat(ADDITIONAL_DURATION_STRING);

    private LocalDateTime minimumDuration;
    private LocalDateTime maximumDuration;

    /**
     * Creates a DurationField
     */
    protected DurationField() {
        final TimeZone tz = SPDateTimeUtil.getBrowserTimeZone();
        this.setZoneId(SPDateTimeUtil.getTimeZoneId(tz));

        durationFormat.setTimeZone(TimeZone.getTimeZone(ZONEID_UTC));
        additionalFormat.setTimeZone(TimeZone.getTimeZone(ZONEID_UTC));
        durationFormat.setLenient(false);
        additionalFormat.setLenient(false);

        this.setResolution(DateTimeResolution.SECOND);
        this.setDateFormat(DURATION_FORMAT_STIRNG);
        this.addStyleName(CSS_STYLE_NAME);
        this.addStyleName(ValoTheme.TEXTFIELD_TINY);
        this.setWidth("100px");

        // needed that popup shows a 24h clock
        this.setLocale(Locale.GERMANY);

        this.addValueChangeListener(this::changeListener);

    }

    public boolean isValid() {
        if (this.getValue() != null && maximumDuration != null
                && compareTimeOfDates(this.getValue(), maximumDuration) > 0) {
            throw new DateTimeException("value is greater than the allowed maximum value");
        }
        if (this.getValue() != null && minimumDuration != null
                && compareTimeOfDates(minimumDuration, this.getValue()) > 0) {
            throw new DateTimeException("value is smaller than the allowed minimum value");
        }
        return true;
    }

    /**
     * This method is called to handle a non-empty date string from the client
     * if the client could not parse it as a Date. In the current case two
     * different parsing schemas are tried. If parsing is not possible a
     * ConversionException is thrown which marks the DurationField as invalid.
     *
     * @return
     */
    @Override
    protected Result<LocalDateTime> handleUnparsableDateString(final String value) {
        try {
            return Result.ok(LocalDateTime.ofInstant(durationFormat.parse(value).toInstant(), ZONEID_UTC));
        } catch (final ParseException e) {
            try {
                Date parsedDate = additionalFormat.parse("000000".substring(Math.min(value.length(), 6)) + value);
                return Result.ok(LocalDateTime.ofInstant(parsedDate.toInstant(), ZONEID_UTC));
            } catch (final ParseException ex) {
                return Result.error("Input is not in HH:MM:SS format.");
            }
        }
    }

    public void changeListener(final ValueChangeEvent event) {
        // do not delete this method, even when removing the code inside this
        // method. This method overwrites the super method, which is
        // necessary, that parsing works correctly on pressing enter key

        if (!(event.getComponent() instanceof DurationField)) {
            return;
        }
        final LocalDateTime value = (LocalDateTime) event.getValue();

        // setValue() calls valueChanged again, when the minimum is greater
        // than the maximum this can lead to an endless loop
        if (value != null && minimumDuration != null && maximumDuration != null && minimumDuration.isBefore(
                maximumDuration)) {

            if (compareTimeOfDates(value, maximumDuration) > 0) {
                ((DurationField) event.getComponent()).setValue(maximumDuration);
            }

            if (compareTimeOfDates(minimumDuration, value) > 0) {
                ((DurationField) event.getComponent()).setValue(minimumDuration);
            }
        }
    }

    /**
     * Sets the duration value
     *
     * @param duration
     *         duration, only values less then 23:59:59 are excepted
     */
    public void setDuration(
            @NotNull
            final Duration duration) {
        if (duration.compareTo(MAXIMUM_DURATION) > 0) {
            throw new IllegalArgumentException("The duaration has to be smaller than 23:59:59.");
        }
        super.setValue(durationToLocalDateTime(duration));
    }

    /**
     * Gets the duration value of the TextField
     *
     * @return duration which is written in the vaadin Field
     */
    public Duration getDuration() {
        if (this.getValue() == null) {
            return null;
        }
        return localDateTimeToDuration(this.getValue());
    }

    /**
     * Sets the minimal allowed duration value as a String
     *
     * @param minimumDuration
     *         minimum Duration, only values smaller 23:59:59 are excepted
     */
    public void setMinimumDuration(
            @NotNull
            final Duration minimumDuration) {
        if (minimumDuration.compareTo(MAXIMUM_DURATION) > 0) {
            throw new IllegalArgumentException("The minimum duaration has to be smaller than 23:59:59.");
        }
        this.minimumDuration = durationToLocalDateTime(minimumDuration);
    }

    /**
     * Sets the maximum allowed duration value as a String
     *
     * @param maximumDuration
     *         maximumDuration, only values smaller 23:59:59 are excepted
     */
    public void setMaximumDuration(
            @NotNull
            final Duration maximumDuration) {
        if (maximumDuration.compareTo(MAXIMUM_DURATION) > 0) {
            throw new IllegalArgumentException("The maximum duaration has to be smaller than 23:59:59.");
        }
        this.maximumDuration = durationToLocalDateTime(maximumDuration);
    }

    private static LocalDateTime durationToLocalDateTime(final Duration duration) {
        if (duration.compareTo(MAXIMUM_DURATION) > 0) {
            throw new IllegalArgumentException("The duaration has to be smaller than 23:59:59.");
        }

        final LocalTime lt = LocalTime.ofNanoOfDay(duration.toNanos());
        final Date date = Date.from(lt.atDate(LocalDate.now(ZONEID_UTC)).atZone(ZONEID_UTC).toInstant());
        return LocalDateTime.ofInstant(date.toInstant(), ZONEID_UTC);
    }

    private static Duration localDateTimeToDuration(final LocalDateTime date) {
        final LocalTime endExclusive = LocalDateTime.ofInstant(date.toInstant(ZoneOffset.UTC), ZONEID_UTC)
                .toLocalTime();
        return Duration.between(LocalTime.MIDNIGHT, LocalTime.from(endExclusive));
    }

    /**
     * Because parsing done by base class returns a different date than parsing
     * done by the user or converting duration to a date. But for the
     * DurationField comparison only the time is important. This function helps
     * comparing the time and ignores the values for day, month and year.
     *
     * @param d1
     *         date, which time will compared with the time of d2
     * @param d2
     *         date, which time will compared with the time of d1
     *
     * @return the value 0 if the time represented d1 is equal to the time
     * represented by d2; a value less than 0 if the time of d1 is
     * before the time of d2; and a value greater than 0 if the time of
     * d1 is after the time represented by d2.
     */
    private int compareTimeOfDates(final LocalDateTime d1, final LocalDateTime d2) {

        final LocalTime lt1 = LocalDateTime.ofInstant(d1.toInstant(ZoneOffset.UTC), ZONEID_UTC).toLocalTime();
        final LocalTime lt2 = LocalDateTime.ofInstant(d2.toInstant(ZoneOffset.UTC), ZONEID_UTC).toLocalTime();

        return lt1.compareTo(lt2);
    }
}
