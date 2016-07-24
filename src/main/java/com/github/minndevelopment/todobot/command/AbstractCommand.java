package com.github.minndevelopment.todobot.command;

import com.github.minndevelopment.todobot.Bot;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.exceptions.PermissionException;

import java.util.function.Consumer;
import java.util.stream.IntStream;

public abstract class AbstractCommand implements CharSequence
{

	public abstract void invoke(GuildMessageReceivedEvent event, String allArgs, String[] args);

	public String getInfo()
	{
		return getAlias() + " " + getAttributes();
	}

	public String getNote()
	{
		return "";
	}

	public abstract String getAlias();

	public String getAttributes()
	{
		return "";
	}

	public static void send(GuildMessageReceivedEvent event, String content)
	{
		send(event, content, null);
	}

	public static void send(GuildMessageReceivedEvent event, String content, Consumer<Message> consumer)
	{
		try
		{
			if (!content.isEmpty())
				event.getChannel().sendMessageAsync(content, (consumer == null ? null : (consumer)));
		} catch (PermissionException e)
		{
			Bot.LOG.warn("A message was not sent due to " + e.getClass().getName() + ": " + e.getMessage());
		}
	}

	public void delete(Message m)
	{
		try
		{
			m.deleteMessage();
		} catch (PermissionException e)
		{
			Bot.LOG.warn("Message was not deleted due to " + e.getClass().getName() + ": " + e.getMessage());
		}
	}

	// Don't ask why...

	@Override
	public String toString()
	{
		return getAlias() + " " + getAttributes();
	}

	@Override
	public IntStream chars()
	{
		return toString().chars();
	}

	@Override
	public IntStream codePoints()
	{
		return toString().codePoints();
	}

	@Override
	public int length()
	{
		return toString().length();
	}

	@Override
	public char charAt(int index)
	{
		return toString().charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		return toString().subSequence(start, end);
	}
}
