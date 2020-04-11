package network.palace.pictify.utils;

public class MapUtil {

//    public static MapView getMap(World world, int id) {
//        try {
//            Object craftWorld = MinecraftReflection.getCraftWorldClass().cast(world);
//            System.out.println(craftWorld.toString());
//            Field worldField = craftWorld.getClass().getDeclaredField("world");
//            worldField.setAccessible(true);
//            Object worldServer = worldField.get(craftWorld);
//            WorldServer server = (WorldServer) worldServer;
//            PersistentCollection col = server.worldMaps;
//            WorldMap map = (WorldMap) col.get(WorldMap.class, "map_" + id);
//            System.out.println(map.toString());
//            System.out.println(map.mapView.toString());
//            System.out.println(map.mapView.getWorld().getName());
//            return map.mapView;
////            System.out.println(worldServer.toString());
////            Field worldMapsField = worldServer.getClass().getField("worldMaps");
////            worldMapsField.setAccessible(true);
////            Object worldMaps = worldMapsField.get(worldServer);
////
////            Method getMethod = worldMaps.getClass().getMethod("get", Class.class, String.class);
////            getMethod.setAccessible(true);
////            Object map = getMethod.invoke(worldMaps, MinecraftReflection.getMinecraftClass("WorldMap"), "map_" + id);
////
////            Field mapViewField = map.getClass().getDeclaredField("mapView");
////            MapView mapView = (MapView) mapViewField.get(map);
////            System.out.println(mapView.getWorld().getName().equals(world.getName()));
////            return mapView;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
