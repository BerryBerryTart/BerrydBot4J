package com.berry.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import kong.unirest.Unirest;

public class Bot {
	private static Logger logger = LoggerFactory.getLogger(Bot.class);
	private static String regexPattern = "https:\\/\\/twitter\\.com\\/[a-zA-Z0-9\\-\\_]*\\/status\\/([0-9]*)";
	private static Pattern pattern = Pattern.compile(regexPattern);
	
	private static Command twitterReact = event -> {
		final String content = event.getMessage().getContent();
		String id = "";
		Matcher matcher = pattern.matcher(content);
		
		if (matcher.find()) {
            id = matcher.group(1);  
			String moji;
			try {
				moji = FetchTwitterRegex.fetchTwitterEmoji(id);
				if (moji != null) {
					event.getMessage().addReaction(ReactionEmoji.unicode(moji)).block();
				}
			} catch (BadRequestException e) {
				logger.error(e.getMessage());
			}	
		}
	};
	
	public static void main(String[] args) {
		Unirest.config()
			.socketTimeout(3000)
	        .connectTimeout(5000)
	        .automaticRetries(true)
	        .enableCookieManagement(false);
		
		String secret = System.getenv("discordSecret");
		final GatewayDiscordClient client = DiscordClientBuilder.create(secret).build().login().block();

		client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
			final User self = event.getSelf();
			logger.info(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()) );
		});
		
		client.getEventDispatcher().on(MessageCreateEvent.class)
	    .subscribe(event -> {
	        twitterReact.execute(event);
	    });

		client.onDisconnect().block();
	}
}
