package network.palace.pictify.renderer;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_11_R1.WorldMap;
import net.minecraft.server.v1_11_R1.WorldServer;
import network.palace.core.Core;
import network.palace.pictify.Pictify;
import network.palace.pictify.utils.FilesystemCache;
import network.palace.pictify.utils.ImageUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
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
//        initialize();
    }

    private void initialize() {
        FilesystemCache.setLoader((short) id, new FilesystemCache.Loader(this) {
            public BufferedImage load() {
                RendererManager manager = Pictify.getInstance().getRendererManager();
                String source = manager.getPrefix() + getSource() + ".png";
                try {
                    image = ImageUtil.loadImage(id, new URL(source));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if (image == null) {
                    Core.logMessage("Pictify Renderer ID " + id, "The source does not contain an image");
                    return null;
                }
//                image = ImageUtil.scale(image, 128, 128);
                xCap = image.getWidth(null);
                yCap = image.getHeight(null);
                return image;
            }
        });
    }

    @Override
    public void render(MapView view, MapCanvas canvas, Player p) {
        if (rendered.contains(p.getUniqueId())) {
            // Already sent this player the map, no need to send it again
            return;
        }
//        byte[] data = getCache();
        System.out.println("LENGTH: " + data.length);
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

    private byte[] getCache() {
        byte[] data = FilesystemCache.getByteData(this);
        if (data == null) {
            return this.data;
        }
        return data;
    }

    public void deactivate() {
        MapView m = getMapView();
        for (MapRenderer mr : m.getRenderers()) m.removeRenderer(mr);
    }

    public void activate() {
        deactivate();
        MapView m = getMapView();
        m.addRenderer(this);
    }

    private MapView getMapView() {
        MapView m = Bukkit.getMap((short) frameId);
        if (m != null) {
            return m;
        }
        WorldServer ws = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        String name = "map_" + frameId;
        WorldMap map = new WorldMap(name);
        map.scale = 3;
        map.a(ws.getWorldData().b(), ws.getWorldData().d(), map.scale);
        map.map = (byte) ws.dimension;
        map.c();
        ws.getServer().getServer().worlds.get(0).a(name, map);
        MapInitializeEvent event = new MapInitializeEvent(map.mapView);
        Bukkit.getPluginManager().callEvent(event);
        return map.mapView;
    }

    public void leave(UUID uuid) {
        rendered.remove(uuid);
    }
}
