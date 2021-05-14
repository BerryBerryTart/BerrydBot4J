package com.berry.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kong.unirest.Unirest;

public class FetchTwitterRegex {
	private static transient String bearerToken = System.getenv("twitter_bearer_token");
	private static Logger logger = LoggerFactory.getLogger(Bot.class);
	
	public static String fetchTwitterEmoji(String id) throws BadRequestException {
		int count = 0;
		String type = "";
		String moji = null;
		String res = null;
		try {
			res = Unirest.get("https://api.twitter.com/2/tweets/"
				+ id
				+ "?expansions=attachments.media_keys&media.fields=type")
				.header("Authorization", "Bearer " + bearerToken)
				.asString().getBody();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode obj = mapper.readTree(res);
			
			count = obj.get("includes").get("media").size();
			type = obj.get("includes").get("media").get(0).get("type").toString();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			return null;
		};
		
		type = type.replace("\"", "");
		
		if (type == null) {
			throw new BadRequestException("Uh Oh.");
		} else if (count == 1 && type.equals("video")) {
			moji = "\uD83D\uDCF7";
		} else {
			switch (count) {
				case 1: {
					moji = "\u0031\u20E3";
					break;
				} case 2: {
					moji = "\u0032\u20E3";
					break;
				} case 3: {
					moji = "\u0033\u20E3";
					break;
				} case 4: {
					moji = "\u0034\u20E3";
					break;
				}
			}
		}

		return moji;
	}
}
