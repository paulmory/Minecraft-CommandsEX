package com.github.zathrus_writer.commandsex.listeners;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.permissions.Permission;

import com.github.zathrus_writer.commandsex.CommandsEX;
import com.github.zathrus_writer.commandsex.helpers.LogHelper;
import static com.github.zathrus_writer.commandsex.Language._;

public class PlayerChatListener implements Listener {

	protected Map<String, String> highPriorityEvents = new HashMap<String, String>();
	protected Map<String, String> normalPriorityEvents = new HashMap<String, String>();
	public static PlayerChatListener plugin;
	
	/***
	 * Initialization routine. Retrieves all event handling functions from handle
	 * classes, so they can be executed on PlayerChatEvent one by one.
	 * @param plugin
	 */
	public PlayerChatListener() {
		plugin = this;
		// load up all existing permissions
		List<Permission> perms = CommandsEX.pdfFile.getPermissions();

		// prepare to initialize all event functions
		Class<?>[] proto = new Class[] {CommandsEX.class};
		Object[] params = new Object[] {CommandsEX.plugin};
		
		for(int i = 0; i <= perms.size() - 1; i++) {
			// call initialization function for each of the event handlers
			String[] s = perms.get(i).getName().split("\\.");
			if ((s.length == 0) || (!s[0].equals("PlayerChatListener"))) continue;
			try {
				Class<?> c = Class.forName("com.github.zathrus_writer.commandsex.handlers.Handler_" + s[1]);
				Method method = c.getDeclaredMethod("init", proto);
				method.invoke(null, params);
			} catch (Throwable e) {}
		}
		
		CommandsEX.plugin.getServer().getPluginManager().registerEvents(this, CommandsEX.plugin);
	}
	
	/***
	 * Executes all chat event functions that were registered during initialization.
	 * We do this by priority - from lowest to highest.
	 * @param e
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(PlayerChatEvent e) {
		LogHelper.logInfo("chat event");
		// the usual stuff :-)
		Class<?>[] proto = new Class[] {PlayerChatEvent.class};
		Object[] params = new Object[] {e};
		
		// iterate over all priorities and call functions one by one
		for (Integer i = 0; i < 3; i++) {
			Map<String, String> currentList = ((i == 0) ? this.highPriorityEvents : this.normalPriorityEvents);
			if (currentList.size() > 0) {
				Iterator<Entry<String, String>> it = currentList.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> pairs = it.next();
					try {
						Class<?> c = Class.forName("com.github.zathrus_writer.commandsex.handlers.Handler_" + pairs.getKey());
						Method method = c.getDeclaredMethod((String) pairs.getValue(), proto);
						Object ret = method.invoke(null, params);
						// if our return value is anything but TRUE, we exit the loop, since this means
						// there should be no more text processing occuring
						if (!Boolean.TRUE.equals(ret)) {
							return;
						}
					} catch (Throwable ex) {
						LogHelper.logWarning("[CommandsEX] " + _("eventListenerFunction", "") + pairs.getKey() + "." + pairs.getValue() + " " + _("eventListenerListeningTo", "") + "PlayerChatEvent " + _("eventListenerExecutionFailed", ""));
						LogHelper.logDebug("Message: " + ex.getMessage() + ", cause: "+ex.getCause());
					}
				}
			}
			currentList = null;
		}
	}
	
	/***
	 * Adds new event function to this listener.
	 * @param priority
	 * @param cName
	 * @param functionName
	 */
	public void addEvent(String priority, String cName, String functionName) {
		if (priority.equals("normal")) {
			this.normalPriorityEvents.put(cName, functionName);
		} else if (priority.equals("high")) {
			this.highPriorityEvents.put(cName, functionName);
		}
	}
}
