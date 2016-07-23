package com.github.minndevelopment.todobot.command;

import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public class Alias extends AbstractCommand
{

	private AbstractCommand command;
	private String alias;

	public Alias(AbstractCommand command, String alias)
	{
		this.command = command;
		this.alias = alias;
	}

	@Override
	public void invoke(GuildMessageReceivedEvent event, String allArgs, String[] args)
	{
		command.invoke(event, allArgs, args);
	}

	@Override
	public String getInfo()
	{
		return command.getInfo();
	}

	@Override
	public String getAttributes()
	{
		return command.getAttributes();
	}

	@Override
	public String getAlias()
	{
		return alias;
	}
}
