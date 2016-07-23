package com.github.minndevelopment.todobot;

import com.github.minndevelopment.todobot.command.*;
import com.github.minndevelopment.todobot.util.Config;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Bot implements EventListener
{

	public static final SimpleLog LOG = SimpleLog.getLog("CommandExecutor");
	public final Config cfg;
	public final JDA api;
	public final List<AbstractCommand> commands = new LinkedList<>();
	public final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r ->
	{
		final Thread t = new Thread(r, "Executor");
		t.setDaemon(true);
		t.setUncaughtExceptionHandler((t1, e) -> LOG.fatal(e));
		return t;
	});

	public Bot(String configLocation) throws IOException, LoginException
	{
		cfg = new Config(configLocation, true);
		if (cfg.shardAmount > 1)
		{
			api = new JDABuilder().setBotToken(cfg.token).useSharding(cfg.shardID, cfg.shardAmount).setBulkDeleteSplittingEnabled(false).setAudioEnabled(false).addListener(this).buildAsync();
		} else
		{
			api = new JDABuilder().setBotToken(cfg.token).setBulkDeleteSplittingEnabled(false).setAudioEnabled(false).addListener(this).buildAsync();
		}
	}


	private void init(ReadyEvent event)
	{
		commands.add(new HelpCommand(commands)); // Help Command!
		commands.add(new ListCommand(cfg.prefix));
		commands.add(new Alias(new ListCommand(cfg.prefix), "todo"));
		commands.add(new ToggleListCommand());
	}

	@Override
	public void onEvent(Event event)
	{
		if (event instanceof GuildMessageReceivedEvent)
			handle((GuildMessageReceivedEvent) event);
		else if (event instanceof ReadyEvent)
			init((ReadyEvent) event);
	}

	public void handle(GuildMessageReceivedEvent event)
	{
		Message message = event.getMessage();
		if (!message.getRawContent().startsWith(cfg.prefix) || message.getRawContent().length() <= cfg.prefix.length())
			return;
		if (!event.getChannel().checkPermission(event.getAuthor(), Permission.MANAGE_SERVER) && !event.getAuthor().getId().equals(cfg.owner))
		{
			AbstractCommand.send(event, "You are unable to operate this bot.");
			return;
		}
		String command = message.getRawContent().substring(cfg.prefix.length()).split("\\s+")[0].toLowerCase();
		String allArgs = message.getRawContent().substring(message.getRawContent().indexOf(command) + command.length()).trim();
		String[] args = allArgs.split("\\s+");

		for (AbstractCommand c : commands)
		{
			if (c.getAlias().equals(command))
			{
				executor.submit(() -> c.invoke(event, allArgs, args));
				return;
			}
		}

	}
}
