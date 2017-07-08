package network.palace.pictify.renderer;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.utility.MinecraftReflection;
import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Marc
 * @since 7/2/17
 */
@SuppressWarnings("deprecation")
public class ImageRenderer extends MapRenderer {
    @Getter private final int id;
    @Getter private final int frameId;
    @Getter private BufferedImage image;
    @Getter public int xCap = 0;
    @Getter public int yCap = 0;
    @Getter private String source;
    @Getter private byte[] data;
    @Getter @Setter private boolean restored = false;
    private List<UUID> rendered = new ArrayList<>();

    public ImageRenderer(int id, int frameId, BufferedImage image) {
        this(id, frameId, image, "unknown");
    }

    public ImageRenderer(int id, int frameId, BufferedImage image, String source) {
        this.id = id;
        this.frameId = frameId;
        this.image = image;
        this.data = MapPalette.imageToBytes(image);
        this.xCap = image.getWidth(null);
        this.yCap = image.getHeight(null);
        this.source = source;
        activate();
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player p) {
        if (rendered.contains(p.getUniqueId())) {
            // Already sent this player the map, no need to send it again
            return;
        }
        for (int x2 = 0; x2 < this.xCap; x2++) {
            for (int y2 = 0; y2 < this.yCap; y2++) {
                try {
                    canvas.setPixel(x2, y2, data[(y2 * this.yCap + x2)]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Core.logMessage("Renderer " + id, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        rendered.add(p.getUniqueId());
        p.sendMap(view);
    }

    public void deactivate() {
        MapView m = getMapView();
        if (m == null) return;
        for (MapRenderer mr : m.getRenderers()) m.removeRenderer(mr);
    }

    public void activate() {
        deactivate();
        MapView m = getMapView();
        if (m == null) return;
        m.addRenderer(this);
    }

    private MapView getMapView() {
        MapView m = Bukkit.getMap((short) frameId);
        if (m != null) {
            return m;
        }
        return createNewMap("map_" + frameId);
    }

    public MapView createNewMap(String name) {
        try {
            // Create instance
            Object map = MinecraftReflection.getMinecraftClass("WorldMap").getDeclaredConstructor(String.class).newInstance(name);
            // Set Scale
            Field scale = map.getClass().getDeclaredField("scale");
            scale.setByte(map, (byte) 3);
            // Get server
            Object worldServer = new BukkitUnwrapper().unwrapItem(Bukkit.getWorlds().get(0));
            // Get World data
            Object worldData = worldServer.getClass().getMethod("getWorldData").invoke(worldServer);
            int spawnX = (int) worldData.getClass().getMethod("b").invoke(worldData);
            int spawnY = (int) worldData.getClass().getMethod("d").invoke(worldData);
            int dimension = (int) worldServer.getClass().getDeclaredField("dimension").get(worldServer);
            // Calculate map center
            map.getClass().getMethod("a", double.class, double.class, int.class).invoke(map, spawnX, spawnY, scale.get(map));
            // Set dimension
            Field mapDimension = map.getClass().getDeclaredField("map");
            mapDimension.setByte(map, (byte) dimension);
            // Mark dirty
            map.getClass().getMethod("c").invoke(map);
            // Create map for world
            Object craftServer = worldServer.getClass().getMethod("getServer").invoke(worldServer);
            Object minecraftServer = craftServer.getClass().getMethod("getServer").invoke(craftServer);
            List worlds = (List) minecraftServer.getClass().getDeclaredField("worlds").get(minecraftServer);
            Object worldServerFromWorlds = worlds.get(0);
            worldServerFromWorlds.getClass().getMethod("a").invoke(worldServerFromWorlds, name, map);
            // Get mapView
            MapView mapView = (MapView) map.getClass().getField("mapView").get(map);
            Bukkit.getPluginManager().callEvent(new MapInitializeEvent(mapView));
            return mapView;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void leave(UUID uuid) {
        rendered.remove(uuid);
    }
}
