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
import com.github.minndevelopment.todobot.manager.ListManager;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class ListCommand extends AbstractCommand
{

	private String prefix;
	private String owner;

	public ListCommand(String fix, String owner)
	{
		prefix = fix;
		this.owner = owner;
	}

	@Override
	public synchronized void invoke(GuildMessageReceivedEvent event, String allArgs, String[] args)
	{
		if (!event.getChannel().checkPermission(event.getAuthor(), Permission.MANAGE_SERVER) && !event.getAuthor().getId().equals(owner))
		{
			AbstractCommand.send(event, "You are unable to operate this bot.");
			return;
		}
		if (allArgs.isEmpty())
		{
			send(event, getInfo());
			return;
		}
		boolean targeting = true;
		TextChannel target;
		List<TextChannel> mentions = event.getMessage().getMentionedChannels();
		if (mentions.isEmpty() || !mentions.get(0).getAsMention().equals(args[0]))
		{
			targeting = false;
			target = event.getChannel();
		} else
			target = mentions.get(0);
		if (!ChannelManager.has(target.getId()))
		{
			send(event, "**" + target.getAsMention() + "** is not listed. Use `" + prefix + "toggle <mention>` to register it. (Public channel excluded)");
			return;
		}
		if (args.length < (targeting ? 2 : 1))
		{
			send(event, "Missing <method> parameter.");
			return;
		}
		String method = args[(targeting ? 1 : 0)].toLowerCase();
		switch (method)
		{
			default:
			{
				send(event, "No such method (**" + method + "**)");
				return;
			}
			case "insert":
			case "add":
			{
				if (args.length < (targeting ? 3 : 2))
				{
					send(event, "Missing input: **<content>**");
				} else
				{
					if (ListManager.add(allArgs.split("\\s+", (targeting ? 3 : 2))[(targeting ? 2 : 1)], target))
						delete(event.getMessage());
					else send(event, "Something went wrong trying to add given message.");
				}
				break;
			}
			case "update":
			case "alter":
			case "edit":
			{
				if (args.length < (targeting ? 3 : 2))
				{
					send(event, "Missing input: **<index>**");
				} else
				{
					if (!args[(targeting ? 2 : 1)].matches("\\d+"))
					{
						send(event, "Invalid index. (" + args[(targeting ? 2 : 1)] + ")");
						break;
					}
					int index = Integer.parseInt(args[(targeting ? 2 : 1)]);
					String newContent = "";
					if (args.length > (targeting ? 3 : 2))
						newContent = allArgs.split("\\s+", (targeting ? 4 : 3))[(targeting ? 3 : 2)];
					if (ListManager.edit(target, index, newContent)) delete(event.getMessage());
					else send(event, "No such index!");
				}
				break;
			}
			case "strike":
			case "unmark":
			case "mark":
			{
				if (args.length < (targeting ? 3 : 2))
				{
					send(event, "Missing input: **<index>**");
				} else
				{
					if (!args[(targeting ? 2 : 1)].matches("\\d+"))
					{
						send(event, "Invalid index. (" + args[(targeting ? 2 : 1)] + ")");
						break;
					}
					int index = Integer.parseInt(args[(targeting ? 2 : 1)]);
					if (ListManager.mark(target, index)) delete(event.getMessage());
					else send(event, "No such index. (" + index + ")");
				}
				break;
			}
			case "delete":
			case "remove":
			{
				if (args.length < (targeting ? 3 : 2))
				{
					send(event, "Missing input: **<index>**");
				} else
				{
					if (!args[(targeting ? 2 : 1)].matches("\\d+"))
					{
						send(event, "Invalid index. (" + args[(targeting ? 2 : 1)] + ")");
						break;
					}
					int index = Integer.parseInt(args[(targeting ? 2 : 1)]);
					if (ListManager.remove(target, index)) delete(event.getMessage());
					else send(event, "No such index. (" + index + ")");
				}
				break;
			}
			case "clean":
			case "clear":
			{
				target.getHistory().retrieve(10).parallelStream().filter(m -> m.getAuthor() == event.getJDA().getSelfInfo()).forEach(Message::deleteMessage);
				delete(event.getMessage());
				break;
			}
			case "promote":
			{
				if (!args[(targeting ? 2 : 1)].matches("\\d+"))
				{
					send(event, "Invalid index. (" + args[(targeting ? 2 : 1)] + ")");
					break;
				}
				int index = Integer.parseInt(args[(targeting ? 2 : 1)]);
				if (ListManager.promote(target, index)) delete(event.getMessage());
				else send(event, "No such index. (" + index + ")");
				break;
			}
			case "fix":
			{
				if (ListManager.fix(target)) delete(event.getMessage());
				else send(event, "No list to fix.");
			}
		}
	}

	@Override
	public String getInfo()
	{
		return "`list " + getAttributes() + "`\n" +
				"Used to edit a todo list.\n" +
				"__Methods:__\n" +
				">Add/Insert <content> - Adds given *content* to the list\n" +
				">Edit/Update/Alter <index> <content> - Updates the todo entry listed at given *index* with the provided *content*\n" +
				">Remove/Delete <index> - Removes entry located at provided *index*\n" +
				">Strike/Mark/Unmark <index> - Toggles the strike effect for the entry located at *index*\n" +
				">Promote <index> - Switches entry for *index* with the first element in the list\n" +
				">Clear/Clean - Clears the list\n" +
				">Fix - Fixes the list's indices\n\n" +
				"Hint: Make sure the bot's messages are all containing chunks of the todo list. **No extra messages!** Max: 100 entries (no line breaks)";
	}

	@Override
	public String getNote()
	{
		return "Alias: `todo`";
	}

	@Override
	public String getAlias()
	{
		return "list";
	}

	@Override
	public String getAttributes()
	{
		return "[channel] <method> [input]";
	}

}
