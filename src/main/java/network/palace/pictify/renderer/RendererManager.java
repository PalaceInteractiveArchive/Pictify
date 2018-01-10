package network.palace.pictify.renderer;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.pictify.utils.ImageUtil;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.map.MapPalette;

import java.awt.image.BufferedImage;
import java.io.*;
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
@SuppressWarnings("deprecation")
public class RendererManager {
    @Getter private static final String prefix = "https://staff.palace.network/pictify/images/";
    private HashMap<Integer, ImageRenderer> images = new HashMap<>();
    private boolean running = false;

    public RendererManager() {
        load();
    }

    public void load() {
        images.clear();
        File dir = new File("plugins/Pictify");
        if (!dir.exists()) dir.mkdir();
        File cacheDir = new File("plugins/Pictify/cache");
        if (!cacheDir.exists()) cacheDir.mkdir();
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
            PreparedStatement sql = connection.prepareStatement("SELECT id,source FROM pictify");
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
                    ImageRenderer renderer;
                    File cacheFile = new File("plugins/Pictify/cache/" + id + ".cache");
                    if (cacheFile.exists()) {
                        byte[] data = IOUtils.toByteArray(new FileInputStream(cacheFile));
                        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
                        int xCap = inputStream.readInt();
                        int yCap = inputStream.readInt();
                        byte[] out = new byte[data.length - 8];
                        inputStream.readFully(out);
                        inputStream.close();
                        renderer = new ImageRenderer(id, frameId, out, xCap, yCap, source);
                    } else {
                        BufferedImage image = ImageUtil.loadImage(id, new URL(source));
                        image = ImageUtil.scale(image, 128, 128);
                        if (image == null) continue;
                        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(cacheFile));
                        dataOutputStream.writeInt(image.getWidth(null));
                        dataOutputStream.writeInt(image.getHeight(null));
                        byte[] data = MapPalette.imageToBytes(image);
                        dataOutputStream.write(data);
                        System.out.println(dataOutputStream.size());
                        dataOutputStream.close();
                        renderer = new ImageRenderer(id, frameId, data, image.getWidth(null),
                                image.getHeight(null), source);
                    }
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
        if (uuid == null)
            return;
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
        int frameId = 0;

        boolean checking = true;

        while (checking) {
            frameId++;
            if (frameIds.contains(frameId)) continue;
            if (Bukkit.getMap((short) frameId) != null) continue;
            checking = false;
        }

//        for (int i : frameIds) {
//            if (i == frameId) {
//                frameId++;
//            } else {
//                break;
//            }
//        }
        if (frameIds.contains(frameId)) {
            player.sendMessage("Didn't find the smallest number " + frameId);
            running = false;
            return false;
        }

        ImageRenderer image;
        try {
            URL url = new URL(getPrefix() + source + ".png");
            BufferedImage bufferedImage = ImageUtil.loadImage(id, url);
            if (bufferedImage == null) {
                player.sendMessage(ChatColor.RED + "Error creating image object with URL " + ChatColor.GREEN +
                        url.toString());
                running = false;
                return false;
            }
            bufferedImage = ImageUtil.scale(bufferedImage, 128, 128);
            byte[] data = MapPalette.imageToBytes(bufferedImage);
            image = new ImageRenderer(id, frameId, data, bufferedImage.getWidth(null), bufferedImage.getHeight(null));
        } catch (MalformedURLException e) {
            player.sendMessage(ChatColor.RED + "Error requesting image with source '" + source + "'!");
            e.printStackTrace();
            running = false;
            return false;
        }
        image.setSource(getPrefix() + source + ".png");
        try {
            addImage(image);
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Error saving ID to server file ids.yml");
            e.printStackTrace();
            running = false;
            return false;
        }
        player.sendMessage(ChatColor.GREEN + "Renderer with ID " + id + " created and added to this server");
        running = false;
        return true;
    }
}
