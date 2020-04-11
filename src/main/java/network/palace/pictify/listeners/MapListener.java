package network.palace.pictify.listeners;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.MapData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MapListener implements Listener {

//    @EventHandler
//    public void onMapInitialize(MapInitializeEvent event) {
//        MapView view = event.getMap();
//        if (view.isVirtual()) return;
//        ImageRenderer imageRenderer = Pictify.getRendererManager().getLocalImage(view.getId());
//        if (imageRenderer == null) return;
//        Core.runTask(Pictify.getInstance(), () -> {
//            for (MapRenderer renderer : view.getRenderers()) {
//                view.removeRenderer(renderer);
//            }
//            imageRenderer.setMapView(view);
//            view.addRenderer(imageRenderer);
//        });
//    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk c = event.getChunk();

        HashMap<World, List<MapData>> conversionMap = Pictify.getRendererManager().getConversions();
        if (!conversionMap.containsKey(c.getWorld())) return;
        List<MapData> list = conversionMap.get(c.getWorld());
        if (list == null || list.isEmpty()) return;

        for (Entity e : c.getEntities()) {
            if (e == null || !e.getType().equals(EntityType.ITEM_FRAME)) continue;
            ItemFrame frame = (ItemFrame) e;
            ItemStack item = frame.getItem();
            if (item == null || !item.getType().equals(Material.MAP)) continue;
            short durability = item.getDurability();
            Optional<MapData> change = list.stream().filter(data -> durability == data.getPreviousId()).findFirst();
            if (!change.isPresent()) continue;
            Bukkit.broadcastMessage("Converted map " + durability + " to " + change.get().getNextId());
            item.setDurability((short) change.get().getNextId());
            frame.setItem(item);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
        if (player == null || (player.getItemInMainHand() == null || !player.getItemInMainHand().getType().equals(Material.GOLD_HOE)) || !event.getRightClicked().getType().equals(EntityType.ITEM_FRAME))
            return;
        HashMap<World, List<MapData>> conversionMap = Pictify.getRendererManager().getConversions();
        player.sendMessage("A");
        if (!conversionMap.containsKey(player.getWorld())) return;
        List<MapData> list = conversionMap.get(player.getWorld());
        player.sendMessage("B");
        if (list == null || list.isEmpty()) return;

        ItemFrame frame = (ItemFrame) event.getRightClicked();
        ItemStack item = frame.getItem();
        player.sendMessage("C");
        if (item == null || !item.getType().equals(Material.MAP)) return;
        short durability = item.getDurability();
        Optional<MapData> change = list.stream().filter(data -> durability == data.getPreviousId()).findFirst();
        player.sendMessage("D");
        if (!change.isPresent()) return;
        event.setCancelled(true);
        item.setDurability((short) change.get().getNextId());
        frame.setItem(item);
        player.sendMessage("E");
    }
}
