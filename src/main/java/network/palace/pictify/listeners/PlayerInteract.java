package network.palace.pictify.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.player.Rank;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.ImageRenderer;
import network.palace.pictify.renderer.RendererManager;
import network.palace.pictify.utils.RestoreUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author Marc
 * @since 7/7/17
 */
public class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null) {
            return;
        }
        if (player.getRank().getRankId() < Rank.KNIGHT.getRankId() || !RestoreUtil.isRestoring(player.getUniqueId())) {
            return;
        }
        Entity e = event.getRightClicked();
        if (!e.getType().equals(EntityType.ITEM_FRAME)) {
            return;
        }
        PlayerInventory inv = player.getInventory();
        boolean wand = false;
        switch (event.getHand()) {
            case HAND:
                wand = inv.getItemInMainHand().getType().equals(Material.IRON_HOE);
                break;
            case OFF_HAND:
                wand = inv.getItemInOffHand().getType().equals(Material.IRON_HOE);
                break;
        }
        ItemFrame frame = (ItemFrame) e;
        ItemStack map = frame.getItem();
        if (!wand || !map.getType().equals(Material.MAP)) {
            return;
        }
        event.setCancelled(true);
        int id = map.getDurability();
        RendererManager manager = Pictify.getInstance().getRendererManager();
        ImageRenderer image = manager.getImage(id);
        if (image != null && image.isRestored()) {
            player.sendMessage(ChatColor.GREEN + "This image has already been restored!");
            return;
        }
        if (!manager.importFromDatabase(id, player)) {
            return;
        }
        image = manager.getImage(id);
        if (image == null) {
            player.sendMessage(ChatColor.RED + "Image is still null... not converting world maps");
            return;
        }
        image.setRestored(true);
        player.sendMessage(ChatColor.GREEN + "Updating world maps from old ID (" + id + ") to new ID (" + image.getFrameId() + ")");
        int count = 0;
        for (ItemFrame itemFrame : frame.getWorld().getEntitiesByClass(ItemFrame.class)) {
            ItemStack item = itemFrame.getItem();
            if (item == null || !item.getType().equals(Material.MAP)) {
                continue;
            }
            int oldFrameId = item.getDurability();
            if (oldFrameId != id) {
                continue;
            }
            item.setDurability((short) image.getFrameId());
            itemFrame.setItem(item);
            count++;
        }
        player.sendMessage(ChatColor.GOLD + "Updated " + count + " world maps!");
    }
}