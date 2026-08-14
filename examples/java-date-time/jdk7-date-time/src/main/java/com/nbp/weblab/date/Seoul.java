package com.nbp.weblab.date;

import java.text.ParseException;
import java.util.TimeZone;

/**
 * Created by benelog on 15. 2. 6.
 */
public class Seoul {


	public static void main(String[] args) throws ParseException {
		TimeZone seoul = TimeZone.getTimeZone("Asia/Seoul");
		TimeZone tokyo = TimeZone.getTimeZone("Asia/Tokyo");
		System.out.println(seoul.getClass());
		System.out.println(tokyo.getClass());
		
		System.out.println(tokyo.hasSameRules(seoul));
		
		System.out.println(hasSameRules(seoul, tokyo));

		System.out.println(seoul.getRawOffset());
		System.out.println(tokyo.getRawOffset());

		System.out.println(seoul.useDaylightTime());
		System.out.println(tokyo.useDaylightTime());

	}


	public static boolean hasSameRules(TimeZone one, TimeZone other) {
		return other != null && one.getRawOffset() == other.getRawOffset() &&
				one.useDaylightTime() == other.useDaylightTime();
	}
}