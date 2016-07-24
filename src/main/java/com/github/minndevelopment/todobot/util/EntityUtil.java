/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.minndevelopment.todobot.util;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class EntityUtil
{

	public static final String CHANNEL_MENTION = "<#(\\d{16,})>";
	public static final String USER_MENTION = "<@!?(\\d{16,})>";
	public static final String ROLE_MENTION = "<@&(\\d{16,})>";
	public static final String USER_TAG = "\\S.{0,30}\\S#\\d{4}";
	public static final String TODO_CHUNK = "^((?:~~)?\\d+\\) .*\n?)+$";

	public static String extractID(String mention)
	{
		return mention.matches(CHANNEL_MENTION) ? mention.replaceAll(CHANNEL_MENTION, "$1") :
				mention.matches(USER_MENTION) ? mention.replaceAll(USER_MENTION, "$1") :
						mention.matches(ROLE_MENTION) ? mention.replaceAll(ROLE_MENTION, "$1") : null;
	}

	public static User extractUser(String mention, JDA api)
	{
		return mention.matches(USER_MENTION) ? api.getUserById(mention.replaceAll(USER_MENTION, "$1")) :
				mention.matches(USER_TAG) ? getUserFromTag(mention, api) : mention.matches("\\d{16,}") ? api.getUserById(mention) : null;
	}

	public static TextChannel extractChannel(String mention, Guild guild)
	{
		return mention.matches(CHANNEL_MENTION) ? guild.getTextChannels().parallelStream().filter(c -> c.getAsMention().matches(mention)).findFirst().orElse(null) :
				mention.matches("\\d{16,}") ? guild.getTextChannels().parallelStream().filter(c -> c.getId().equals(mention) || c.getName().equals(mention)).findFirst().orElse(null) :
						getChannelsForName(mention, guild).isEmpty() ? null : getChannelsForName(mention, guild).get(0);
	}

	public static List<TextChannel> getChannelsForName(String name, Guild guild)
	{
		return guild.getTextChannels().parallelStream().filter(c -> c.getName().startsWith(name)).collect(Collectors.toList());
	}

	public static User getUserFromTag(String tag, JDA api)
	{
		return !tag.matches(USER_TAG) ? null : api.getUsers().parallelStream().filter(u -> (u.getUsername() + "#" + u.getDiscriminator()).equals(tag)).findFirst().orElse(null);
	}

	public static boolean isTodoChunk(String message)
	{
		return message.matches(TODO_CHUNK);
	}

}
