package com.nbp.weblab.date;

import static org.fest.assertions.api.Assertions.*;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;
import org.joda.time.chrono.GJChronology;
import org.joda.time.chrono.GregorianChronology;
import org.junit.Test;

public class JodaTimeTest {
	
	@Test
	public void shouldGetAfterOneDay() {
		Chronology chrono = GregorianChronology.getInstance();
		LocalDate theDay = new LocalDate(1582, 10, 4, chrono);
		String pattern = "yyyy.MM.dd";
		assertThat(theDay.toString(pattern)).isEqualTo("1582.10.04");
		
		LocalDate nextDay = theDay.plusDays(1);
		assertThat(nextDay.toString(pattern)).isEqualTo("1582.10.05");
	}
	
	@Test
	public void shouldGetAfterOneDayWithGJChronology() {
		Chronology chrono = GJChronology.getInstance();
		LocalDate theDay = new LocalDate(1582, 10, 4, chrono);
		String pattern = "yyyy.MM.dd";
		assertThat(theDay.toString(pattern)).isEqualTo("1582.10.04");
		
		LocalDate nextDay = theDay.plusDays(1);
		assertThat(nextDay.toString(pattern)).isEqualTo("1582.10.15");
	}
	
	@Test
	public void shouldGetAfterOneHour() {
		DateTimeZone seoul = DateTimeZone.forID("Asia/Seoul");
		DateTime theTime = new DateTime(1988,5,7,23,0, seoul);
		String pattern = "yyyy.MM.dd HH:mm";
		assertThat(theTime.toString(pattern)).isEqualTo("1988.05.07 23:00");
		assertThat(seoul.isStandardOffset(theTime.getMillis())).isTrue();
		
		DateTime after1Hour = theTime.plusHours(1);
		assertThat(after1Hour.toString(pattern)).isEqualTo("1988.05.08 01:00");
		assertThat(seoul.isStandardOffset(after1Hour.getMillis())).isFalse();
	}
	
	@Test
	public void shouldGetAfterOneMinute() {
		DateTimeZone seoul = DateTimeZone.forID("Asia/Seoul");
		DateTime theTime = new DateTime(1961, 8, 9, 23, 59, seoul);
		String pattern = "yyyy.MM.dd HH:mm";
		assertThat(theTime.toString(pattern)).isEqualTo("1961.08.09 23:59");
		
		DateTime after1Minute = theTime.plusMinutes(1);
		assertThat(after1Minute.toString(pattern)).isEqualTo("1961.08.10 00:30");
	}

	@Test
	public void shouldGetAfterTwoSecond() {
		DateTimeZone utc = DateTimeZone.forID("UTC");
		DateTime theTime = new DateTime(2012, 6, 30, 23, 59, 59, utc);
		String pattern = "yyyy.MM.dd HH:mm:ss";
		assertThat(theTime.toString(pattern)).isEqualTo("2012.06.30 23:59:59");
		
		DateTime after2Seconds = theTime.plusSeconds(2);
		assertThat(after2Seconds.toString(pattern)).isEqualTo("2012.07.01 00:00:01");
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowExceptionWhenWrongTimeZoneId(){
		DateTimeZone.forID("Seoul/Asia");
	}
	
	@Test
	public void shouldGetDate() {
		LocalDate theDay = new LocalDate(1999, 12, 31);
		
		assertThat(theDay.getYear()).isEqualTo(1999);
		assertThat(theDay.getMonthOfYear()).isEqualTo(12);		
		assertThat(theDay.getDayOfMonth()).isEqualTo(31);		
	}
	
	@Test (expected=IllegalFieldValueException.class)
	public void shouldNotAcceptWrongMonth() {
		new LocalDate(1999, 13, 31);
	}
	
	@Test
	public void shouldGetDayOfWeek() {
		LocalDate theDay = new LocalDate(2014, 1, 1);
		
		int dayOfWeek = theDay.getDayOfWeek();
		assertThat(dayOfWeek).isEqualTo(DateTimeConstants.WEDNESDAY);
		assertThat(dayOfWeek).isEqualTo(3);
	}
}
