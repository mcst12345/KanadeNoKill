package kanade.kill.fake;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

@SuppressWarnings("unused")
public class FakeObjects {
    public static Int2ObjectMap<DimensionManager.Dimension> dimensions = Int2ObjectMaps.synchronize(new Int2ObjectLinkedOpenHashMap<>());
    public static IntSet keepLoaded = IntSets.synchronize(new IntOpenHashSet());
    public static IntSet unloadQueue = IntSets.synchronize(new IntLinkedOpenHashSet());
    public static Int2ObjectMap<WorldServer> worlds = Int2ObjectMaps.synchronize(new Int2ObjectLinkedOpenHashMap<>());
}
