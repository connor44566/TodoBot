package com.github.minndevelopment.todobot.util;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONException;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PersistenceUtil
{

	public static final String BASE_URI = "Objects/";
	public static final SimpleLog LOG = SimpleLog.getLog("PersistenceUtil");

	public static synchronized void save(Serializable object, String name)
	{
		assert name != null && !name.isEmpty() && object != null;
		ensureDir();
		try
		{
			File f = new File(BASE_URI + name);
			if (f.exists()) // To make sure it's clean
				f.delete();
			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(BASE_URI + name)));
			out.writeObject(object);
			out.close();
			LOG.debug("Saved '" + name + "' [" + object.toString() + "]");
		} catch (IOException e)
		{
			LOG.warn(e);
		}
	}

	public static Object retrieve(String name)
	{
		assert name != null && !name.isEmpty();
		ensureDir();
		try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(BASE_URI + name))))
		{
			Object object = in.readObject();
			in.close();
			return object;
		} catch (IOException | ClassNotFoundException e)
		{
			LOG.warn(e);
			return null;
		}
	}

	public static String hastebin(String input) throws InterruptedException, ExecutionException, TimeoutException, JSONException
	{
		return "http://hastebin.com/" + Unirest.post("http://hastebin.com/documents").body(input).asJsonAsync().get(3, TimeUnit.SECONDS).getBody().getObject().getString("key");
	}

	private static void ensureDir()
	{
		File dir = new File(BASE_URI);
		dir.mkdirs();
	}

	public static void main(String... a)
	{
		try
		{
			System.out.println(hastebin("Test"));
		} catch (InterruptedException | ExecutionException | TimeoutException e)
		{
			e.printStackTrace();
		}
	}


}
