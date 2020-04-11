package network.palace.pictify.commands;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.core.player.Rank;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.MapData;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@CommandMeta(description = "Testing", rank = Rank.DEVELOPER)
public class TestinCommand extends CoreCommand {
    private List<String> completed = new ArrayList<>();

    public TestinCommand() {
        super("testin");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        int radius = Integer.parseInt(args[0]);
        Location loc = player.getLocation();

        HashMap<World, List<MapData>> conversionMap = Pictify.getRendererManager().getConversions();
        if (!conversionMap.containsKey(loc.getWorld())) {
            player.sendMessage(ChatColor.RED + "No conversion data!");
            return;
        }
        List<MapData> list = conversionMap.get(player.getWorld());
        if (list == null || list.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Empty conversion data!");
            return;
        }
        Chunk locChunk = loc.getChunk();
        int minX = locChunk.getX() - radius;
        int minZ = locChunk.getZ() - radius;
        int maxX = locChunk.getX() + radius;
        int maxZ = locChunk.getZ() + radius;
        int count = 0, skip = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (completed.contains(loc.getWorld().getName() + ":" + x + ":" + z)) {
                    skip++;
                    continue;
                }
                Chunk c = loc.getWorld().getChunkAt(x, z);
                for (Entity e : c.getEntities()) {
                    if (!e.getType().equals(EntityType.ITEM_FRAME)) continue;
                    ItemFrame frame = (ItemFrame) e;
                    ItemStack item = frame.getItem();
                    if (item == null || !item.getType().equals(Material.MAP)) return;
                    short durability = item.getDurability();
                    Optional<MapData> change = list.stream().filter(data -> durability == data.getPreviousId()).findFirst();
                    if (!change.isPresent()) return;
                    item.setDurability((short) change.get().getNextId());
                    frame.setItem(item);
                    count++;
                    if (count % 100 == 0) {
                        player.sendMessage(ChatColor.GREEN + "Processed " + count + ", skipped " + skip + "...");
                    }
                    completed.add(loc.getWorld().getName() + ":" + x + ":" + z);
                }
            }
        }
        player.sendMessage(ChatColor.GREEN + "Finished! Processed " + count + " and skipped " + skip + " in total.");
    }
}
