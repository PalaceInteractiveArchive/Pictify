package network.palace.pictify.commands.pictify;

import network.palace.core.Core;
import network.palace.core.command.CommandException;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.RendererManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author Marc
 * @since 7/7/17
 */
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
        RendererManager manager = Pictify.getInstance().getRendererManager();
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            player.sendMessage(ChatColor.RED + args[0] + " isn't a number!");
            return;
        }
        Core.runTaskAsynchronously(() -> {
            if (!manager.getIds().contains(id)) {
                player.sendMessage(ChatColor.YELLOW + "ID " + id + " isn't added to this server, checking database...");
                if (!manager.importFromDatabase(id, player)) {
                    return;
                }
            }
            Core.runTask(() -> {
                int frameId = manager.getImage(id).getFrameId();
                ItemStack map = new ItemStack(Material.MAP, 1, (short) frameId);
                if (map == null) {
                    player.sendMessage(ChatColor.RED + "Error creating map item!");
                    return;
                }
                player.getInventory().addItem(map);
                player.sendMessage(ChatColor.GREEN + "Gave you map for image ID " + id + " (map ID " + frameId + ")");
            });
        });
    }
}
