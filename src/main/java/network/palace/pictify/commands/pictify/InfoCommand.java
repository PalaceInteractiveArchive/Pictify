package network.palace.pictify.commands.pictify;

import network.palace.core.command.CommandException;
import network.palace.core.command.CoreCommand;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.ImageRenderer;
import network.palace.pictify.renderer.RendererManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Marc
 * @since 9/8/17
 */
public class InfoCommand extends CoreCommand {

    public InfoCommand() {
        super("info");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "/pictify info [Local/Remote] [ID]");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            sender.sendMessage(ChatColor.RED + args[1] + " isn't a number!");
            return;
        }
        RendererManager manager = Pictify.getInstance().getRendererManager();
        ImageRenderer image;
        if (args[0].toLowerCase().equals("local")) {
            image = manager.getLocalImage(id);
        } else {
            image = manager.getImage(id);
        }
        if (image == null) {
            sender.sendMessage(ChatColor.RED + "There is no local image with ID " + id + "!");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Image ID: " + image.getId());
        sender.sendMessage(ChatColor.GREEN + "Frame ID: " + image.getFrameId());
        sender.sendMessage(ChatColor.GREEN + "Source: " + image.getSource());
    }
}
