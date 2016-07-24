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

package com.github.minndevelopment.todobot.manager;

import com.github.minndevelopment.todobot.util.EntityUtil;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.impl.MessageImpl;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.*;
import java.util.stream.Collectors;

public class ListManager
{

	public static final SimpleLog LOG = SimpleLog.getLog("ListManager");
	public static final Comparator<Message> COMPARATOR_MESSAGE = (o1, o2) ->
			Integer.parseInt(o1.getContent().replaceAll("^(?:~~)?(\\d+)(?:.+\n?)+$", "$1")) - Integer.parseInt(o2.getContent().replaceAll("^(?:~~)?(\\d+)(?:.+\n?)+$", "$1"));
	public static final Comparator<String> COMPARATOR_STRING = (o1, o2) ->
			Integer.parseInt(o1.replaceAll("^(?:~~)?(\\d+).+$", "$1")) - Integer.parseInt(o2.replaceAll("^(?:~~)?(\\d+).+$", "$1"));
	public static final Comparator<String[]> COMPARATOR_STRING_ARR = (o1, o2) ->
	{
		if (o1.length == 0) return -1;
		else if (o2.length == 0) return 1;
		return Integer.parseInt(o1[0].replaceAll("^(?:~~)?(\\d+).+$", "$1")) - Integer.parseInt(o2[0].replaceAll("^(?:~~)?(\\d+).+$", "$1"));
	};

	// UTIL

	public static List<String> getList(TextChannel channel)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		List<String> lines = new LinkedList<>();
		List<Message> history = channel.getHistory().retrieve(10).parallelStream().filter(m -> EntityUtil.isTodoChunk(m.getRawContent())).collect(Collectors.toList());
		if (history == null) return null;
		List<String[]> chunks = getChunks(history, channel);
		for (String[] chunk : chunks)
			Collections.addAll(lines, chunk);
		return lines;
	}

	public static List<ListTuple<MessageImpl, String[]>> getAssosiatedChunks(TextChannel channel)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		List<ListTuple<MessageImpl, String[]>> chunks = new LinkedList<>();
		List<Message> history = channel.getHistory().retrieve(10).parallelStream().filter(m -> EntityUtil.isTodoChunk(m.getRawContent())).collect(Collectors.toList());
		if (history == null) return null;
		history = history.parallelStream()
				.filter(m -> m.getAuthor() == channel.getJDA().getSelfInfo())
				.sorted(COMPARATOR_MESSAGE).collect(Collectors.toList());
		for (Message message : history)
			chunks.add(new ListTuple<>((MessageImpl) message, message.getRawContent().split("\n")));
		return chunks;
	}

	public static List<String> getList(List<Message> history, TextChannel channel)
	{
		List<String> lines = new LinkedList<>();
		if (history == null) return null;
		List<String[]> chunks = getChunks(history, channel);
		for (String[] chunk : chunks)
			Collections.addAll(lines, chunk);
		return lines;
	}

	public static List<ListTuple<MessageImpl, String[]>> getAssosiatedChunks(List<Message> history, TextChannel channel)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		List<ListTuple<MessageImpl, String[]>> chunks = new LinkedList<>();
		if (history == null) return null;
		history = history.parallelStream()
				.filter(m -> m.getAuthor() == channel.getJDA().getSelfInfo())
				.sorted(COMPARATOR_MESSAGE).collect(Collectors.toList());
		for (Message message : history)
			chunks.add(new ListTuple<>((MessageImpl) message, message.getRawContent().split("\n")));
		return chunks;
	}


	public static List<String[]> getChunks(List<Message> hist, TextChannel channel)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		return hist.parallelStream()
				.filter(m -> m.getAuthor() == channel.getJDA().getSelfInfo())
				.map(m ->
				{
					try
					{
						String[] arr = m.getRawContent().split("\n");
						Arrays.sort(m.getRawContent().split("\n"), COMPARATOR_STRING);
						return arr;
					} catch (Exception e)
					{
						e.printStackTrace();
						return new String[0];
					}
				})
				.sorted(COMPARATOR_STRING_ARR).collect(Collectors.toList());
	}


	// TERMINAL

	public static boolean edit(TextChannel channel, int index, String newContent)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		if (newContent.isEmpty()) return remove(channel, index);
		newContent = newContent.replaceAll("(~~|\n)", "");
		List<ListTuple<MessageImpl, String[]>> chunks = getAssosiatedChunks(channel);
		if (chunks == null || chunks.isEmpty() || chunks.size() * 10 < index || index < 1)
			return false; // No such index
		int chunkIndex = index / 10;
		Message message = chunks.get(chunkIndex).message;
		CharSequence[] lines = chunks.get(chunkIndex).chunk;

		try
		{
			lines[(index % 10) - 1] = ((String) lines[(index % 10) - 1]).replaceAll("^((?:~~)?\\d+\\)) .*", "$1 " + newContent);
			message.updateMessageAsync(String.join("\n", lines), null);
			return true;
		} catch (ArrayIndexOutOfBoundsException e)
		{
			return false;
		}
	}

	public static boolean add(String newLine, TextChannel channel)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		newLine = newLine.replaceAll("(~~|\n)", "");
		List<Message> history = channel.getHistory().retrieve(10);
		if (history == null)
		{
			channel.sendMessageAsync("1) " + newLine, null);
			return true;
		}
		history = history.parallelStream().filter(m -> m.getAuthor() == channel.getJDA().getSelfInfo()).collect(Collectors.toList());
		List<ListTuple<MessageImpl, String[]>> chunks = getAssosiatedChunks(history, channel);
		if (chunks == null || chunks.isEmpty())
		{
			channel.sendMessageAsync("1) " + newLine, null);
			return true;
		}
		List<String> lines = getList(history, channel);
		int index = (lines == null ? 1 : lines.size() + 1);
		Message m = chunks.get(chunks.size() - 1).message;
		CharSequence[] chunk = chunks.get(chunks.size() - 1).chunk;
		if (chunk.length == 10)
		{
			channel.sendMessageAsync(index + ") " + newLine, null);
			return true;
		}

		CharSequence[] newChunk = new CharSequence[chunk.length + 1];

		for (int i = 0; i < chunk.length; i++)
			newChunk[i] = chunk[i];
		newChunk[newChunk.length - 1] = index + ") " + newLine;
		m.updateMessageAsync(String.join("\n", newChunk), null);
		return true; //TODO: TEST
	}

	public static boolean mark(TextChannel channel, int index)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		List<ListTuple<MessageImpl, String[]>> chunks = getAssosiatedChunks(channel);
		if (chunks == null || chunks.isEmpty() || chunks.size() * 10 < index || index < 1)
			return false; // No such index
		int chunkIndex = (index - 1) / 10;
		Message message = chunks.get(chunkIndex).message;
		String[] lines = chunks.get(chunkIndex).chunk;

		try
		{
			lines[((index - 1) % 10)] = lines[((index - 1) % 10)].matches("~~.*~~") ? lines[((index - 1) % 10)].replaceAll("~~", "") : String.format("~~%s~~", lines[((index - 1) % 10)]);
			message.updateMessageAsync(String.join("\n", (CharSequence[]) lines), null);
			return true;
		} catch (ArrayIndexOutOfBoundsException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static boolean remove(TextChannel channel, int index)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		List<Message> history = channel.getHistory().retrieve(10);
		if (history == null)
			return false;
		history = history.parallelStream().filter(m -> m.getAuthor() == channel.getJDA().getSelfInfo()).collect(Collectors.toList());
		List<ListTuple<MessageImpl, String[]>> chunks = getAssosiatedChunks(history, channel);
		List<String> lines = getList(history, channel);
		if (chunks == null || lines == null) return false;
		int chunkIndex = (index - 1) / 10;
		if (chunkIndex >= chunks.size()) return false;
		chunks = chunks.subList(chunkIndex, chunks.size()); // Only grab affected chunks
		lines.remove(index - 1);
		lines = lines.subList(chunkIndex * 10, lines.size());
		try
		{
			for (ListTuple<MessageImpl, String[]> chunk : chunks)
			{
				if (lines.isEmpty())
				{
					chunk.message.deleteMessage();
					return true;
				}
				Object[] sub = lines.subList(0, Math.min(lines.size(), 10)).toArray();
				String[] arr = new String[sub.length];
				for (int i = 0; i < sub.length; i++)
				{
					int lineIndex = Integer.parseInt(((String) sub[i]).replaceAll("^(?:~~)?(\\d+).+$", "$1")) - 1;
					arr[i] = ((String) sub[i]).replaceAll("^(~~)?(\\d+)", (((String) sub[i]).startsWith("~~") ? "$1" : "") + lineIndex);
				}
				chunk.message.updateMessageAsync(String.join("\n", (CharSequence[]) arr), null);
				lines = lines.subList(Math.min(sub.length, lines.size()), lines.size());
			}
		} catch (Exception e)
		{
			LOG.log(e);
		}
		return true; // TODO: TEST
	}

	public static boolean promote(TextChannel channel, int index)
	{
		assert channel != null && ChannelManager.has(channel.getId()) : "No such channel.";
		try
		{
			List<ListTuple<MessageImpl, String[]>> chunks = getAssosiatedChunks(channel);
			if (chunks == null || chunks.isEmpty()) return false;
			String first, promoted;
			String[] firstChunk, promotedChunk;
			int chunkIndex = (index - 1) / 10;

			firstChunk = chunks.get(0).chunk;
			promotedChunk = chunks.get(chunkIndex).chunk;

			first = firstChunk[0].replaceAll("^(?:~~)?\\d+\\) (.+)$", "$1");
			promoted = promotedChunk[(index - 1) % 10].replaceAll("^(?:~~)?\\d+\\) (.+)$", "$1");

			firstChunk[0] = firstChunk[0].replaceAll("^((?:~~)?\\d+\\) ).+$", "$1" + promoted);
			promotedChunk[(index - 1) % 10] = promotedChunk[(index - 1) % 10].replaceAll("^((?:~~)?\\d+\\) ).+$", "$1" + first);

			chunks.get(0).message.updateMessageAsync(String.join("\n", (CharSequence[]) firstChunk), null);
			if (chunkIndex != 0)
				chunks.get(chunkIndex).message.updateMessageAsync(String.join("\n", (CharSequence[]) promotedChunk), null);
			return true;

		} catch (IndexOutOfBoundsException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static boolean fix(TextChannel channel)
	{
		List<ListTuple<MessageImpl, String[]>> chunks = getAssosiatedChunks(channel);
		if (chunks == null || chunks.isEmpty()) return false;
		int i = 1;

		for (ListTuple<MessageImpl, String[]> chunk : chunks)
		{
			for (int j = 0; j < chunk.chunk.length; j++)
			{
				if (chunk.chunk[j].startsWith("~~"))
					chunk.chunk[j] = chunk.chunk[j].replaceAll("^~~\\d+\\) (.*)", "~~" + i++ + ") $1");
				else
					chunk.chunk[j] = chunk.chunk[j].replaceAll("^\\d+\\) (.*)", i++ + ") $1");
			}
			chunk.message.updateMessageAsync(String.join("\n", (CharSequence[]) chunk.chunk), null);
		}
		return true;
	}

	// ENTRY

	private static class ListTuple<M extends MessageImpl, C>
	{
		private Message message;
		private String[] chunk;

		public ListTuple(M message, C chunk)
		{
			this.message = message;
			this.chunk = (String[]) chunk;
		}
	}

}
