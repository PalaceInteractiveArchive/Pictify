package network.palace.pictify.commands.pictify;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.message.FormattedMessage;
import network.palace.pictify.Pictify;
import network.palace.pictify.renderer.ImageRenderer;
import network.palace.pictify.renderer.RendererManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Marc
 * @since 7/7/17
 */
@CommandMeta(description = "List all local images")
public class ListCommand extends CoreCommand {

    public ListCommand() {
        super("list");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "/pictify list [world]");
            return;
        }
        World world = Bukkit.getWorld(args[0]);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "Couldn't find a world by the name " + args[0] + "!");
            return;
        }
        List<ImageRenderer> images = Pictify.getRendererManager().getImages(world);
        sender.sendMessage(ChatColor.GREEN + "There are " + images.size() + " images on this server");
        for (ImageRenderer image : images) {
            if (sender instanceof Player) {
                new FormattedMessage("- ID " + image.getId() + ", Map ID " + image.getFrameId() + ", ")
                        .color(ChatColor.YELLOW).then(image.getSource().replace(RendererManager.getPrefix(), ""))
                        .color(ChatColor.YELLOW).link(image.getSource()).send((Player) sender);
            } else {
                sender.sendMessage(ChatColor.YELLOW + "- " + image.getId() + ", " + image.getSource());
            }
        }
    }
}
