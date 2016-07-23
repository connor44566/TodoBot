package com.github.minndevelopment.todobot;

import net.dv8tion.jda.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Launcher
{

	public static final SimpleLog LOG = SimpleLog.getLog("Launcher");

	public static void main(String... a) throws IOException, LoginException
	{
		new Bot("Config.json");
	}

}
