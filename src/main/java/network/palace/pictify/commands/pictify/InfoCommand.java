package network.palace.pictify.commands.pictify;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.ImageRenderer;
import org.bukkit.ChatColor;

/**
 * @author Marc
 * @since 9/8/17
 */
@CommandMeta(description = "Get info about a local or remote image")
public class InfoCommand extends CoreCommand {

    public InfoCommand() {
        super("info");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "/pictify info [Local/Remote] [ID]");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            player.sendMessage(ChatColor.RED + args[1] + " isn't a number!");
            return;
        }
        ImageRenderer image;
        if (args[0].toLowerCase().equals("local")) {
            image = Pictify.getRendererManager().getLocalImage(id);
        } else {
            image = Pictify.getRendererManager().getImage(id);
        }
        if (image == null) {
            player.sendMessage(ChatColor.RED + "There is no local image on this world with ID " + id + "!");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Image ID: " + image.getId());
        player.sendMessage(ChatColor.GREEN + "Frame ID: " + image.getFrameId());
        player.sendMessage(ChatColor.GREEN + "Source: " + image.getSource());
    }
}
