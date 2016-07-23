package com.github.minndevelopment.todobot.manager;

import com.github.minndevelopment.todobot.util.PersistenceUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelManager
{

	public static final SimpleLog LOG;
	private static HashMap<String, LinkedList<String>> todoMap;

	static
	{
		LOG = SimpleLog.getLog("ChannelManager");
		todoMap = (HashMap<String, LinkedList<String>>) PersistenceUtil.retrieve("todoMap.todo");
		if (todoMap == null) todoMap = new HashMap<>();
		Thread t = new Thread(() ->
		{
			while (!Thread.currentThread().isInterrupted())
			{
				try
				{
					Thread.sleep(60000);
				} catch (InterruptedException e)
				{
					return;
				}
				PersistenceUtil.save(todoMap, "todoMap.todo");
			}
		}, "TodoMap-AutoSave");
		t.setDaemon(true);
		t.start();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> PersistenceUtil.save(todoMap, "todoMap.todo")));
	}

	public static boolean has(String channel)
	{
		AtomicBoolean atomicBoolean = new AtomicBoolean(false);
		todoMap.forEach((g, list) ->
		{
			if (list.contains(channel)) atomicBoolean.set(true);
		});
		return atomicBoolean.get();
	}

	public static boolean toggle(TextChannel channel, Guild guild)
	{
		if (guild.getPublicChannel() == channel) return false;
		if (has(channel.getId()))
		{
			todoMap.get(guild.getId()).remove(channel.getId());
			return false;
		} else if (todoMap.containsKey(guild.getId()))
		{
			todoMap.get(guild.getId()).add(channel.getId());
			return true;
		} else
		{
			LinkedList<String> list = new LinkedList<>();
			list.add(channel.getId());
			todoMap.put(guild.getId(), list);
			return true;
		}
	}

	public static void remove(Guild guild)
	{
		if (todoMap.containsKey(guild.getId())) todoMap.remove(guild.getId());
	}

}
