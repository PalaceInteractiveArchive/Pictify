package network.palace.pictify.utils;

import network.palace.core.player.CPlayer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Marc
 * @since 7/7/17
 */
public class RestoreUtil {
    private static List<UUID> restoring = new ArrayList<>();

    public static boolean isRestoring(UUID uuid) {
        return restoring.contains(uuid);
    }

    public static void toggle(CPlayer player) {
        if (restoring.contains(player.getUniqueId())) {
            restoring.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You are no longer in restore mode!");
            return;
        }
        restoring.add(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You are now in restore mode! Use an Iron Hoe to restore images.");
    }
}
