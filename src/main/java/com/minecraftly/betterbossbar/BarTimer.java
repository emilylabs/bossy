/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.betterbossbar;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class BarTimer extends BukkitRunnable {

	private final BetterBossBarPlugin plugin;

	@Setter
	private long currentTickCount = 0;

	@Override
	public void run() {

		if ( currentTickCount >= (plugin.getChangeInterval()) ) {

			currentTickCount = 0;
			Bar oldBar = plugin.getCurrentBar();
			Bar newBar = plugin.getBarQueue().poll();

			plugin.setCurrentBar( newBar );
			if ( oldBar != null )
				plugin.getBarQueue().add( oldBar );

			Bukkit.getScheduler().runTask( plugin, () -> {

				if ( newBar != null && oldBar != null ) {
					Bukkit.getOnlinePlayers().forEach( player -> {
						newBar.sendBar( player );
						oldBar.removeBar( player );
					} );
				} else if ( newBar == null && oldBar != null ) {
					Bukkit.getOnlinePlayers().forEach( oldBar::removeBar );
				} else if ( newBar != null ) {
					Bukkit.getOnlinePlayers().forEach( newBar::sendBar );
				}

			} );


		} else {
			currentTickCount++;
		}

	}

}
