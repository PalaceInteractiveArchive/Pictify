package network.palace.pictify.commands.pictify;

import network.palace.core.Core;
import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.ItemUtil;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.ImageRenderer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

/**
 * @author Marc
 * @since 7/7/17
 */
@CommandMeta(description = "Give yourself an image map")
public class GiveCommand extends CoreCommand {

    public GiveCommand() {
        super("give");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/pictify give [ID]");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            player.sendMessage(ChatColor.RED + args[0] + " isn't a number!");
            return;
        }
        Core.runTaskAsynchronously(Pictify.getInstance(), () -> {
            if (!Pictify.getRendererManager().getIds().contains(id)) {
                player.sendMessage(ChatColor.YELLOW + "ID " + id + " isn't added to this server, checking database...");
                if (!Pictify.getRendererManager().importFromDatabase(id, player)) {
                    return;
                }
            }
            Core.runTask(Pictify.getInstance(), () -> {
                ImageRenderer image = Pictify.getRendererManager().getImage(id);
                if (image == null || image.getMapView() == null) {
                    player.sendMessage(ChatColor.RED + "Error creating map item!");
                    return;
                }
                ItemStack map = ItemUtil.create(Material.MAP, 1, (short) image.getFrameId());
                if (map == null) {
                    player.sendMessage(ChatColor.RED + "Error creating map item!");
                    return;
                }
                player.getInventory().addItem(map);
                player.sendMessage(ChatColor.GREEN + "Gave you map for image ID " + id + " (map ID " + image.getMapView().getId() + ")");
            });
        });
    }
}
