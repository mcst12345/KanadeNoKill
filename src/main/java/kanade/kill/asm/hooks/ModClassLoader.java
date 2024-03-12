package kanade.kill.asm.hooks;

import kanade.kill.Launch;
import kanade.kill.classload.KanadeClassLoader;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.ModAPITransformer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import java.util.List;

public class ModClassLoader {
    public static ModAPITransformer addModAPITransformer(net.minecraftforge.fml.common.ModClassLoader classLoader,ASMDataTable dataTable)
    {
        Launch.classLoader.registerTransformer("net.minecraftforge.fml.common.asm.transformers.ModAPITransformer");
        List<IClassTransformer> transformers = KanadeClassLoader.NecessaryTransformers;
        ModAPITransformer modAPI = (ModAPITransformer) transformers.get(transformers.size()-1);
        modAPI.initTable(dataTable);
        return modAPI;
    }
}
