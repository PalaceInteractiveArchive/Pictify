package network.palace.pictify.renderer;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.pictify.utils.ImageUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Marc
 * @since 7/2/17
 */
public class RendererManager {
    @Getter private static final String prefix = "https://staff.palace.network/pictify/images/";
    private HashMap<Integer, ImageRenderer> images = new HashMap<>();

    public RendererManager() {
        load();
    }

    public void load() {
        images.clear();
        File dir = new File("plugins/Pictify");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File idFile = new File("plugins/Pictify/ids.yml");
        if (!idFile.exists()) {
            try {
                idFile.createNewFile();
            } catch (IOException e) {
                Core.logMessage("Pictify Loader", "Error creating file at " + idFile.getPath());
            }
            return;
        }
        YamlConfiguration idConfig = YamlConfiguration.loadConfiguration(idFile);
        List<String> ids = idConfig.getStringList("ids");
        if (ids.isEmpty()) {
            Core.logMessage("Pictify Loader", "No images to load, finished!");
            return;
        }
        Connection connection = Core.getSqlUtil().getConnection();
        if (connection == null) {
            Core.logMessage("Pictify Loader", "Could not establish an SQL connection!");
            return;
        }
        try {
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM pictify");
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                int id = result.getInt("id");
                int frameId = 0;
                boolean contains = false;
                for (String s : ids) {
                    try {
                        int dbId = Integer.parseInt(s.split(":")[0]);
                        if (dbId == id) {
                            contains = true;
                            frameId = Integer.parseInt(s.split(":")[1]);
                            break;
                        }
                    } catch (Exception e) {
                        Core.logMessage("Pictify Loader Error", "Error parsing config value '" + s +
                                "' Cause: " + e.getMessage());
                        break;
                    }
                }
                if (!contains) {
                    continue;
                }
                try {
                    String source = prefix + result.getString("source") + ".png";
                    BufferedImage image = ImageUtil.loadImage(id, new URL(source));
                    if (image == null) {
                        continue;
                    }
                    ImageRenderer renderer = new ImageRenderer(id, frameId, image, source);
                    Core.logMessage("Pictify Loader", "Loaded renderer with id " + renderer.getId());
                    images.put(renderer.getId(), renderer);
                } catch (Exception e) {
                    Core.logMessage("Pictify Loader Error", "Ignoring renderer with id '" + result.getInt("id") + "'. Cause: " + e.getMessage());
                }
            }
            result.close();
            sql.close();
            Core.logMessage("Pictify Loader", "Finished loading all images!");
        } catch (SQLException e) {
            Core.logMessage("Pictify Loader Error", "Error with loader SQL query!");
            e.printStackTrace();
        }
    }

    public void leave(UUID uuid) {
        for (ImageRenderer renderer : images.values())
            renderer.leave(uuid);
    }

    public List<ImageRenderer> getImages() {
        return new ArrayList<>(images.values());
    }

    public List<Integer> getIds() {
        return new ArrayList<>(images.keySet());
    }

    public ImageRenderer getImage(int id) {
        return images.get(id);
    }

    public void addImage(ImageRenderer image) throws IOException {
        images.put(image.getId(), image);
        File idFile = new File("plugins/Pictify/ids.yml");
        YamlConfiguration idConfig = YamlConfiguration.loadConfiguration(idFile);
        List<String> ids = idConfig.getStringList("ids");
        if (ids.add(image.getId() + ":" + image.getFrameId())) {
            idConfig.set("ids", ids);
            idConfig.save(idFile);
        }
    }

    public void removeImage(int id) throws IOException {
        ImageRenderer image = images.remove(id);
        File idFile = new File("plugins/Pictify/ids.yml");
        YamlConfiguration idConfig = YamlConfiguration.loadConfiguration(idFile);
        List<String> ids = idConfig.getStringList("ids");
        ids.remove(id + ":" + image.getFrameId());
        idConfig.set("ids", ids);
        idConfig.save(idFile);
    }

    public boolean importFromDatabase(int id, CPlayer player) {
        Connection connection = Core.getSqlUtil().getConnection();
        if (connection == null) {
            player.sendMessage(ChatColor.RED + "Database connection is null, can't proceed!");
            return false;
        }
        String source;
        try {
            PreparedStatement getImage = connection.prepareStatement("SELECT * FROM pictify WHERE id=?");
            getImage.setInt(1, id);
            ResultSet result = getImage.executeQuery();
            if (!result.next()) {
                result.close();
                getImage.close();
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "No image exists with ID " + id + "!");
                return false;
            }
            source = result.getString("source");
            result.close();
            getImage.close();
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        player.sendMessage(ChatColor.GREEN + "Found image source, creating renderer now...");

        File idFile = new File("plugins/Pictify/ids.yml");
        YamlConfiguration idConfig = YamlConfiguration.loadConfiguration(idFile);
        List<String> ids = idConfig.getStringList("ids");
        List<Integer> frameIds = new ArrayList<>();
        for (String s : ids) {
            try {
                int frameId = Integer.parseInt(s.split(":")[1]);
                frameIds.add(frameId);
            } catch (Exception e) {
                Core.logMessage("Pictify Loader Error", "Error parsing config value '" + s +
                        "' Cause: " + e.getMessage());
            }
        }
        Collections.sort(frameIds);
        int frameId = 1;
        for (int i : frameIds) {
            if (i == frameId) {
                frameId++;
            } else {
                break;
            }
        }
        if (frameIds.contains(frameId)) {
            player.sendMessage("Didn't find the smallest number " + frameId);
            return false;
        }

        ImageRenderer image;
        try {
            URL url = new URL(getPrefix() + source + ".png");
            BufferedImage bufferedImage = ImageUtil.loadImage(id, url);
            if (bufferedImage == null) {
                player.sendMessage(ChatColor.RED + "Error creating image object with URL " + ChatColor.GREEN +
                        url.toString());
                return false;
            }
            image = new ImageRenderer(id, frameId, bufferedImage, url.toString());
        } catch (MalformedURLException e) {
            player.sendMessage(ChatColor.RED + "Error requesting image with source '" + source + "'!");
            e.printStackTrace();
            return false;
        }
        try {
            addImage(image);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Error saving ID to server file ids.yml");
            e.printStackTrace();
            return false;
        }
        player.sendMessage(ChatColor.GREEN + "Renderer with ID " + id + " created and added to this server");
        return true;
    }
}
