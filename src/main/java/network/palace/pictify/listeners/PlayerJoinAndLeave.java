package network.palace.pictify.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.pictify.Pictify;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Marc
 * @since 7/7/17
 */
public class PlayerJoinAndLeave implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerLeave(Core.getPlayerManager().getPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        onPlayerLeave(Core.getPlayerManager().getPlayer(event.getPlayer()));
    }

    private void onPlayerLeave(CPlayer player) {
        Pictify.getInstance().getRendererManager().leave(player.getUniqueId());
    }
}
