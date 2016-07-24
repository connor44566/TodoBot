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

	@Override
	public String getNote()
	{
		return command.getNote();
	}
}
