package sk.ppmscan.application.util;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalDateTimeJsonSerializer implements JsonSerializer<LocalDateTime> {

	@Override
	public JsonElement serialize(LocalDateTime localDateTime, Type typeOfSrc, JsonSerializationContext context) {
		String formattedLocalDateTime = localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
		return context.serialize(formattedLocalDateTime);
	}

}