package com.solinia.solinia.Timers;

import org.bukkit.scheduler.BukkitRunnable;

import com.solinia.solinia.Exceptions.CoreStateInitException;
import com.solinia.solinia.Managers.StateManager;

public class NPCCheckForEnemiesTimer extends BukkitRunnable {
	@Override
	public void run() {

		try {
			StateManager.getInstance().getEntityManager().doNPCCheckForEnemies();
		} catch (CoreStateInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
