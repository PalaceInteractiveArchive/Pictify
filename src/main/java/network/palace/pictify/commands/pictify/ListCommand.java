package network.palace.pictify.commands.pictify;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.message.FormattedMessage;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.ImageRenderer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Marc
 * @since 7/7/17
 */
@CommandMeta(description = "List all pictify images")
public class ListCommand extends CoreCommand {

    public ListCommand() {
        super("list");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        List<ImageRenderer> images = Pictify.getInstance().getRendererManager().getImages();
        sender.sendMessage(ChatColor.GREEN + "There are " + images.size() + " images on this server");
        for (ImageRenderer image : images) {
            if (sender instanceof Player) {
                new FormattedMessage("- " + image.getId() + ", ").color(ChatColor.YELLOW)
                        .then(image.getSource()).color(ChatColor.YELLOW).link(image.getSource()).send((Player) sender);
            } else {
                sender.sendMessage(ChatColor.YELLOW + "- " + image.getId() + ", " + image.getSource());
            }
        }
    }
}
