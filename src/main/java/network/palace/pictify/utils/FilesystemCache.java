package network.palace.pictify.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import network.palace.pictify.renderer.ImageRenderer;
import org.apache.commons.io.IOUtils;
import org.bukkit.map.MapPalette;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author Marc
 * @since 7/8/17
 */
public class FilesystemCache {
    private static final Cache<Short, Loader> LOADERS = CacheBuilder.newBuilder().build();
    private static final LoadingCache<ImageRenderer, byte[]> GENERATED_PIXELS = CacheBuilder.newBuilder().build(new CacheLoader<ImageRenderer, byte[]>() {
        @Override
        public byte[] load(ImageRenderer renderer) throws Exception {
            File file = new File("plugins/Pictify/cache/" + renderer.getId() + ".cache");
            if (file.exists()) {
                byte[] data = IOUtils.toByteArray(new FileInputStream(file));
                DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
                renderer.xCap = inputStream.readInt();
                renderer.yCap = inputStream.readInt();
                byte[] out = new byte[data.length - 16];
                inputStream.readFully(out);
                inputStream.close();
                return out;
            }
            Loader loader = FilesystemCache.LOADERS.getIfPresent(renderer.getId());
            if (loader == null) {
                throw new RuntimeException("Null loader for ID (" + renderer.getId() + ")");
            }
            BufferedImage image = loader.load();
            if (image == null) return new byte[16];
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
            dataOutputStream.writeInt(renderer.xCap);
            dataOutputStream.writeInt(renderer.yCap);
            byte[] data = MapPalette.imageToBytes(image);
            dataOutputStream.write(data);
            dataOutputStream.close();
            return data;
        }
    });

    public static void setLoader(short id, Loader loader) {
        LOADERS.put(id, loader);
        getByteData(loader.renderer);
    }

    public static byte[] getByteData(ImageRenderer renderer) {
        try {
            return GENERATED_PIXELS.getUnchecked(renderer);
        } catch (Exception e) {
            return new byte[16];
        }
    }

    public static abstract class Loader {
        protected final ImageRenderer renderer;

        public Loader(ImageRenderer renderer) {
            this.renderer = renderer;
        }

        public abstract BufferedImage load();
    }
}
