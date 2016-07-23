package com.github.minndevelopment.todobot.command;

import com.github.minndevelopment.todobot.manager.ChannelManager;
import com.github.minndevelopment.todobot.manager.ListManager;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class ListCommand extends AbstractCommand
{

	private String prefix;

	public ListCommand(String fix)
	{
		prefix = fix;
	}

	@Override
	public void invoke(GuildMessageReceivedEvent event, String allArgs, String[] args)
	{
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
		}
	}

	@Override
	public String getInfo()
	{
		return "test"; // TODO
	}

	@Override
	public String getAlias()
	{
		return "list";
	}

	@Override
	public String getAttributes()
	{
		return "<method> [input]";
	}

}
