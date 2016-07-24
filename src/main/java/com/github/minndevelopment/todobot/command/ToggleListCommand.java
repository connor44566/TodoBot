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

package com.github.minndevelopment.todobot.command;

import com.github.minndevelopment.todobot.manager.ChannelManager;
import com.github.minndevelopment.todobot.util.EntityUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public class ToggleListCommand extends AbstractCommand
{

	private String owner;

	public ToggleListCommand(String owner)
	{
		this.owner = owner;
	}

	@Override
	public void invoke(GuildMessageReceivedEvent event, String allArgs, String[] args)
	{
		if (!event.getChannel().checkPermission(event.getAuthor(), Permission.MANAGE_SERVER) && !event.getAuthor().getId().equals(owner))
		{
			AbstractCommand.send(event, "You are unable to operate this bot.");
			return;
		}
		if (allArgs.isEmpty())
		{
			send(event, "Usage: `" + getInfo() + "`");
			return;
		}
		TextChannel channel = EntityUtil.extractChannel(allArgs, event.getGuild());
		if (channel != null)
		{
			boolean enabled = ChannelManager.toggle(channel, event.getGuild());
			send(event, channel.getAsMention() + " " + (enabled ? "is now a todo list." : "is **not** a todo list (anymore)."));
		} else send(event, "No such channel: **" + allArgs + "**!");
	}

	@Override
	public String getAlias()
	{
		return "toggle";
	}

	@Override
	public String getAttributes()
	{
		return "<channel>";
	}
}
