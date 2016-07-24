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

import com.github.minndevelopment.todobot.Bot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand
{

	private String owner;
	private List<AbstractCommand> commands;

	public HelpCommand(List<AbstractCommand> list, String owner)
	{
		commands = list;
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
		if (allArgs.isEmpty()) // List commands
		{
			try
			{
				Object[] filtered = commands.parallelStream()
						.filter(c -> !(c instanceof Alias))
						.map(c -> "`" + c.toString() + "`" + (c.getNote().isEmpty() ? "" : " - " + c.getNote()))
						.collect(Collectors.toList()).toArray();
				CharSequence[] arr = new CharSequence[filtered.length];
				System.arraycopy(filtered, 0, arr, 0, arr.length);
				send(event, "__Commands__:\n\n" + String.join("\n", arr));
			} catch (Exception e)
			{
				Bot.LOG.log(e);
			}
		} else // Search for command
		{
			String alias = args[0];
			AbstractCommand command = null;
			for (AbstractCommand c : new LinkedList<>(commands))
			{
				if (!c.getAlias().equalsIgnoreCase(alias)) continue;
				command = c;
				break;
			}
			if (command != null) send(event, "Info on **" + command.getAlias() + "**: " + command.getInfo());
			else send(event, "No command found named **" + alias + "**.");
		}
	}

	@Override
	public String getInfo()
	{
		return "Used to retrieve help for a command **or** show a list of available commands.";
	}

	@Override
	public String getAlias()
	{
		return "help";
	}

	@Override
	public String getAttributes()
	{
		return "[command]";
	}
}
