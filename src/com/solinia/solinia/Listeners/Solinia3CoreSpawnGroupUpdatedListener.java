package com.solinia.solinia.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.solinia.solinia.Solinia3CorePlugin;
import com.solinia.solinia.Events.SoliniaNPCUpdatedEvent;
import com.solinia.solinia.Events.SoliniaSpawnGroupUpdatedEvent;
import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Managers.StateManager;

public class Solinia3CoreSpawnGroupUpdatedListener implements Listener {
	Solinia3CorePlugin plugin;

	public Solinia3CoreSpawnGroupUpdatedListener(Solinia3CorePlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void OnSpawnGroupUpdated(SoliniaSpawnGroupUpdatedEvent event) {
		if (event.isCancelled())
			return;
		
		try {
			StateManager.getInstance().getEntityManager().getNPCEntityProvider().updateSpawnGroup(event.getSpawnGroup());
		} catch (CoreStateInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}