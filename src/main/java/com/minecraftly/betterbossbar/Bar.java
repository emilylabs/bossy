/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.betterbossbar;

import lombok.Data;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@Data
public class Bar {

	private static Method convertMethod;

	protected static Bar deserialise( final HashMap<String, Object> barMap ) throws Exception {

		YamlConfiguration configuration = new YamlConfiguration();
		doLoad( barMap, configuration );

		String text = ChatColor.translateAlternateColorCodes( '&', configuration.getString( "Title", "&3&lTotally awesome boss bar." ) );

		BossBattle.BarStyle style = BossBattle.BarStyle.valueOf( configuration.getString( "Style", "PROGRESS" ) );
		BossBattle.BarColor color = BossBattle.BarColor.valueOf( configuration.getString( "Color", "PURPLE" ) );
		BossBattleServer server = new BossBattleServer( IChatBaseComponent.ChatSerializer.a( "{\"text\": \"" + text + "\"}" ), color, style );

		server.setProgress( (float) configuration.getDouble( "Progress" ) );
		server.setVisible( false );
		server.setCreateFog( configuration.getBoolean( "CreateFog", false ) );
		server.setDarkenSky( configuration.getBoolean( "DarkenSky", false ) );
		server.setPlayMusic( configuration.getBoolean( "PlayMusic", false ) );

		PacketPlayOutBoss add = new PacketPlayOutBoss( PacketPlayOutBoss.Action.ADD, server );
		PacketPlayOutBoss destroy = new PacketPlayOutBoss( PacketPlayOutBoss.Action.REMOVE, server );

		return new Bar( add, destroy );

	}

	public static void doLoad( Map<?, ?> inMap, YamlConfiguration instance ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		if ( convertMethod == null ) {
			convertMethod = YamlConfiguration.class.getDeclaredMethod( "convertMapsToSections", Map.class, ConfigurationSection.class );
			convertMethod.setAccessible( true );
		}

		convertMethod.invoke( instance, inMap, instance );

	}
	private final Packet spawnPacket;
	private final Packet destroyPacket;

	public boolean sendBar( Player player ) {
		if ( player == null || !(player instanceof CraftPlayer) ) return false;
		((CraftPlayer) player).getHandle().playerConnection.sendPacket( spawnPacket );
		return true;
	}

	public boolean removeBar( Player player ) {
		if ( player == null || !(player instanceof CraftPlayer) ) return false;
		((CraftPlayer) player).getHandle().playerConnection.sendPacket( destroyPacket );
		return true;
	}

}
