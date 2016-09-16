/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.betterbossbar;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class BetterBossBarPlugin extends JavaPlugin implements Listener {

	@Getter
	private final ConcurrentLinkedQueue<Bar> barQueue = new ConcurrentLinkedQueue<>();
	@Getter
	@Setter
	private long changeInterval = 5;
	@Getter
	@Setter
	private Bar currentBar;

	@Getter
	private BarTimer barTimer;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( command.getName().equalsIgnoreCase( "bossreload" ) && sender.hasPermission( "boss.reload" ) ) {

			try {
				load();
				sender.sendMessage( "Configuration reloaded! <3" );
			} catch ( Exception ex ) {
				ex.printStackTrace();
				sender.sendMessage( ChatColor.RED + "Unable to reload configuration! Error: " + ex.getMessage() );
				sender.sendMessage( ChatColor.RED + " " + ex.getClass().getName() );
			}

		} else {
			sender.sendMessage( ChatColor.RED + "Yeah, don't try it.." );
		}

		return true;

	}

	@Override
	public void onLoad() {
		load();
	}

	@Override
	public void onDisable() {

		if ( this.barTimer != null )
			this.barTimer.cancel();

		barQueue.clear();

	}

	@Override
	public void onEnable() {

		this.barTimer = new BarTimer( this );
		this.barTimer.runTaskTimerAsynchronously( this, 20L, 20L );

	}

	public final void load() {

		if ( currentBar != null )
			getServer().getOnlinePlayers().forEach( currentBar::removeBar );

		File file = new File( getDataFolder(), "config.yml" );
		if ( !file.exists() ) {
			try {
				getDataFolder().mkdirs();
				ByteStreams.copy( getResource( "config.yml" ), new FileOutputStream( file ) );
			} catch ( IOException e ) {
				getLogger().log( Level.SEVERE, "Unable to save the default configuration!", e );
			}
			getLogger().info( "Saved default config!" );
		}

		reloadConfig();
		changeInterval = getConfig().getLong( "change-interval-seconds", 5 );

		barQueue.clear();
		setCurrentBar( null );

		List<HashMap<String, Object>> barList = (List<HashMap<String, Object>>) getConfig().getList( "bars", new ArrayList<HashMap<String, Object>>() );
		for ( HashMap<String, Object> barMap : barList ) {
			try {
				barQueue.add( Bar.deserialise( barMap ) );
			} catch ( Exception ex ) {
				ex.printStackTrace();
			}
		}

		if ( this.barTimer != null )
			this.barTimer.setCurrentTickCount( Long.MAX_VALUE );

	}

	@EventHandler
	public void onPlayerJoin( PlayerJoinEvent event ) {
		if ( currentBar != null ) currentBar.sendBar( event.getPlayer() );
	}

}
