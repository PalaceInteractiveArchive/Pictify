package network.palace.pictify.renderer;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.TextUtil;
import network.palace.pictify.utils.ImageUtil;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Marc
 * @since 7/2/17
 */
public class RendererManager {
    @Getter private static final String prefix = "https://staff.palace.network/pictify/images/";
    private final HashMap<World, HashMap<Integer, ImageRenderer>> worlds = new HashMap<>();
    private boolean running = false;

    public RendererManager() {
        initialize();
    }

    public void initialize() {
        worlds.clear();
        File worldDir = new File("plugins/Pictify/worlds");
        if (!worldDir.exists() || !worldDir.isDirectory()) worldDir.mkdir();

        File originalFile = new File("plugins/Pictify/ids.yml");
        if (originalFile.exists()) {
            originalFile.renameTo(new File("plugins/Pictify/worlds/" + Bukkit.getWorlds().get(0).getName() + "_ids.yml"));
        }

        for (World world : Bukkit.getWorlds()) {
            loadWorld(world);
        }
    }

    @SuppressWarnings("deprecation")
    public void loadWorld(World world) {
        File cacheDir = new File("plugins/Pictify/cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        File idFile = new File("plugins/Pictify/worlds/" + world.getName() + "_ids.yml");
        if (!idFile.exists()) {
            try {
                idFile.createNewFile();
            } catch (IOException e) {
                Core.logMessage("Pictify Loader", "Error creating file at " + idFile.getPath());
                return;
            }
        }
        YamlConfiguration idConfig = YamlConfiguration.loadConfiguration(idFile);
        List<String> ids = idConfig.getStringList("ids");
        if (ids.isEmpty()) {
            Core.logMessage("Pictify Loader", "No images to load for world '" + world.getName() + "'!");
            return;
        }
        Connection connection = Core.getSqlUtil().getConnection();
        if (connection == null) {
            Core.logMessage("Pictify Loader", "Could not establish an SQL connection!");
            return;
        }
        HashMap<Integer, ImageRenderer> worldMaps = new HashMap<>();
        Core.logMessage("Pictify Loader", "Loading " + ids.size() + " image" + TextUtil.pluralize(ids.size()) + " for '" + world.getName() + "'...");
        try {
            StringBuilder idList = new StringBuilder();
            for (int i = 0; i < ids.size(); i++) {
                idList.append(ids.get(i));
                if (i < (ids.size() - 1)) {
                    idList.append(",");
                }
            }
            PreparedStatement sql = connection.prepareStatement("SELECT id,source FROM pictify WHERE id IN (" + idList.toString() + ")");
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                int id = result.getInt("id");
                int frameId = 0;
                String source = prefix + result.getString("source") + ".png";
                boolean contains = false;
                for (String s : ids) {
                    try {
                        String[] split = s.split(":");
                        if (split[0].equals(String.valueOf(id))) {
                            contains = true;
                            frameId = Integer.parseInt(split[1]);
                            break;
                        }
                    } catch (Exception e) {
                        Core.logMessage("Pictify Loader Error", "Error parsing config value '" + s + "': " + e.getMessage());
                        break;
                    }
                }
                if (!contains) continue;

                try {
                    ImageRenderer renderer;
                    File cacheFile = new File("plugins/Pictify/cache/" + id + ".cache");
                    byte[] imageData;
                    int xCap, yCap;
                    if (cacheFile.exists()) {
                        byte[] data;
                        data = IOUtils.toByteArray(new FileInputStream(cacheFile));
                        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
                        xCap = inputStream.readInt();
                        yCap = inputStream.readInt();
                        imageData = new byte[data.length - 8];
                        inputStream.readFully(imageData);
                        inputStream.close();
                    } else {
                        Core.logMessage("Pictify Loader", "Saving " + id + " to cache...");
                        BufferedImage image = ImageUtil.loadImage(id, new URL(source));
                        image = ImageUtil.scale(image, 128, 128);
                        if (image == null) continue;
                        imageData = MapPalette.imageToBytes(image);
                        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(cacheFile));
                        dataOutputStream.writeInt(xCap = image.getWidth(null));
                        dataOutputStream.writeInt(yCap = image.getHeight(null));
                        dataOutputStream.write(imageData);
                        dataOutputStream.close();
                    }
                    renderer = new ImageRenderer(world, id, frameId, imageData, xCap, yCap, source);
                    worldMaps.put(renderer.getId(), renderer);
                } catch (Exception e) {
                    e.printStackTrace();
                    Core.logMessage("Pictify Loader Error", "Error loading image id '" + id + "': " + e.getMessage());
                }
            }
            result.close();
            sql.close();
            Core.logMessage("Pictify Loader", "Finished loading " + worldMaps.size() + " image" +
                    TextUtil.pluralize(worldMaps.size()) + " for '" + world.getName() + "'!");
        } catch (SQLException e) {
            Core.logMessage("Pictify Loader Error", "Error with loader SQL query!");
            e.printStackTrace();
        }
        worlds.put(world, worldMaps);
    }

    public void leave(UUID uuid) {
        if (uuid == null) return;
        worlds.values().forEach(worldMaps -> worldMaps.values().forEach(renderer -> renderer.leave(uuid)));
    }

    public List<ImageRenderer> getAllImages() {
        List<ImageRenderer> list = new ArrayList<>();
        worlds.values().forEach(worldMaps -> list.addAll(worldMaps.values()));
        return list;
    }

    public List<ImageRenderer> getImages(World world) {
        if (!worlds.containsKey(world)) return new ArrayList<>();
        return new ArrayList<>(worlds.get(world).values());
    }

    public List<Integer> getIds(World world) {
        if (!worlds.containsKey(world)) return new ArrayList<>();
        return new ArrayList<>(worlds.get(world).keySet());
    }

    public ImageRenderer getImage(World world, int id) {
        if (!worlds.containsKey(world)) return null;
        return worlds.get(world).get(id);
    }

    public ImageRenderer getLocalImage(int id) {
        for (ImageRenderer image : getImages()) {
            if (image.getFrameId() == id) {
                return image;
            }
        }
        return null;
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

    public void removeImage(int id, boolean frameID) throws IOException {
        ImageRenderer image = null;
        if (frameID) {
            for (ImageRenderer i : getImages()) {
                if (i.getFrameId() == id) {
                    image = i;
                    break;
                }
            }
        } else {
            image = images.remove(id);
        }
        if (image == null) return;
        File idFile = new File("plugins/Pictify/ids.yml");
        YamlConfiguration idConfig = YamlConfiguration.loadConfiguration(idFile);
        List<String> ids = idConfig.getStringList("ids");
        ids.remove(image.getId() + ":" + image.getFrameId());
        idConfig.set("ids", ids);
        idConfig.save(idFile);
    }

    public boolean importFromDatabase(int id, CPlayer player) {
        if (running) {
            player.sendMessage(ChatColor.RED + "Pictify is currently downloading another image, try again soon!");
            return false;
        }
        running = true;
        Connection connection = Core.getSqlUtil().getConnection();
        if (connection == null) {
            player.sendMessage(ChatColor.RED + "Database connection is null, can't proceed!");
            running = false;
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
                running = false;
                return false;
            }
            source = result.getString("source");
            result.close();
            getImage.close();
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "SQL Error: " + e.getMessage());
            e.printStackTrace();
            running = false;
            return false;
        }
        player.sendMessage(ChatColor.GREEN + "Found image source, creating renderer now...");

        MapView newView = Bukkit.createMap(player.getWorld());
        int frameId = newView.getId();

        ImageRenderer imageRenderer;
        try {
            URL url = new URL(prefix + source + ".png");
            BufferedImage image = ImageUtil.loadImage(id, url);
            if (image == null) {
                player.sendMessage(ChatColor.RED + "Error creating image object with URL " + ChatColor.GREEN +
                        url.toString());
                running = false;
                return false;
            }
            image = ImageUtil.scale(image, 128, 128);
            @SuppressWarnings("deprecation") byte[] imageData = MapPalette.imageToBytes(image);
            imageRenderer = new ImageRenderer(player.getWorld(), id, frameId, imageData, image.getWidth(null), image.getHeight(null));
        } catch (MalformedURLException e) {
            player.sendMessage(ChatColor.RED + "Error requesting image with source '" + source + "'!");
            e.printStackTrace();
            running = false;
            return false;
        }
        imageRenderer.setSource(prefix + source + ".png");
        try {
            addImage(imageRenderer);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Error saving ID to server file ids.yml");
            e.printStackTrace();
            running = false;
            return false;
        }
        Core.callEvent(new MapInitializeEvent(newView));
        player.sendMessage(ChatColor.GREEN + "Renderer with ID " + id + " created and added to this server");
        running = false;
        return true;
    }
}
