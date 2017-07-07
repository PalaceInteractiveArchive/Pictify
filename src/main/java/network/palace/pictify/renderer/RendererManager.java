package network.palace.pictify.renderer;

import network.palace.core.Core;
import network.palace.pictify.utils.ImageUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Marc
 * @since 7/2/17
 */
public class RendererManager {
    private static final String prefix = "https://staff.palace.network/pictify/images/";
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
        List<Integer> ids = idConfig.getIntegerList("ids");
        if (ids.isEmpty()) {
            Core.logMessage("Pictify Loader", "No images to load, finished!");
            return;
        }
        try (Connection connection = Core.getSqlUtil().getConnection()) {
            PreparedStatement sql = connection.prepareStatement("SELECT * FROM pixelator");
            ResultSet result = sql.executeQuery();
            while (result.next()) {
                int id = result.getInt("id");
                if (!ids.contains(id)) {
                    continue;
                }
                try {
                    BufferedImage image = ImageUtil.loadImage(id, new URL(prefix + result.getString("source")));
                    if (image == null) {
                        continue;
                    }
                    ImageRenderer renderer = new ImageRenderer(id, image);
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
}
