package com.github.minndevelopment.todobot.command;

import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class HelpCommand extends AbstractCommand
{

	private List<AbstractCommand> commands;

	public HelpCommand(List<AbstractCommand> list)
	{
		commands = list;
	}

	@Override
	public void invoke(GuildMessageReceivedEvent event, String allArgs, String[] args)
	{
		// TODO
	}

	@Override
	public String getAlias()
	{
		return "help";
	}
}
