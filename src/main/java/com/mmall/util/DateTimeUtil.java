package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * ClassName: DateTimeUtil
 * 时间格式处理类
 *
 * @author HoleLin
 * @version 1.0
 * @date 2019/5/2
 */

public class DateTimeUtil {

	public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static Date strToDate(String dateTimeStr, String formatStr) {
		DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(formatStr);
		DateTime dateTime = dateTimeFormat.parseDateTime(dateTimeStr);
		return dateTime.toDate();
	}

	public static String dateToStr(Date date, String formatStr) {
		if (date == null) {
			return StringUtils.EMPTY;
		}
		DateTime dateTime = new DateTime(date);
		return dateTime.toString(formatStr);
	}

	public static Date strToDate(String dateTimeStr) {
		DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(STANDARD_FORMAT);
		DateTime dateTime = dateTimeFormat.parseDateTime(dateTimeStr);
		return dateTime.toDate();
	}

	public static String dateToStr(Date date) {
		if (date == null) {
			return StringUtils.EMPTY;
		}
		DateTime dateTime = new DateTime(date);
		return dateTime.toString(STANDARD_FORMAT);
	}

	public static void main(String[] args) {
		System.out.println(DateTimeUtil.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
		System.out.println(DateTimeUtil.strToDate("2018-01-04 11:11:11", "yyyy-MM-dd HH:mm:ss"));
	}
}