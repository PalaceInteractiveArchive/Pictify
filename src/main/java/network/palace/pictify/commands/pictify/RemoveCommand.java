package network.palace.pictify.commands.pictify;

import network.palace.core.command.CommandException;
import network.palace.core.command.CoreCommand;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.ImageRenderer;
import network.palace.pictify.renderer.RendererManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;

/**
 * @author Marc
 * @since 7/7/17
 */
public class RemoveCommand extends CoreCommand {

    public RemoveCommand() {
        super("remove");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "/pictify give [ID]");
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
        ImageRenderer image = manager.getImage(id);
        if (image == null) {
            sender.sendMessage(ChatColor.RED + "There is no local image with ID " + id + "!");
            return;
        }
        image.deactivate();
        try {
            manager.removeImage(id);
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error removing ID from server file ids.yml");
            e.printStackTrace();
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Successfully removed all local data for ID " + id +
                ". To permanently delete an image, you must use the Pictify Website.");
    }
}
