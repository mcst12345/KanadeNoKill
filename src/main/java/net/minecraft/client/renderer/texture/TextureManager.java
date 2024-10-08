package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SideOnly(Side.CLIENT)
public class TextureManager implements ITickable, IResourceManagerReloadListener {
    public static final ResourceLocation RESOURCE_LOCATION_EMPTY = new ResourceLocation("");
    private static final Logger LOGGER = LogManager.getLogger();
    public final Map<ResourceLocation, ITextureObject> mapTextureObjects = Maps.newHashMap();
    public final IResourceManager resourceManager;
    private final List<ITickable> listTickables = Lists.newArrayList();
    private final Map<String, Integer> mapTextureCounters = Maps.newHashMap();

    public TextureManager(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void bindTexture(ResourceLocation resource) {
        ITextureObject itextureobject = this.mapTextureObjects.get(resource);

        if (itextureobject == null) {
            itextureobject = new SimpleTexture(resource);
            this.loadTexture(resource, itextureobject);
        }

        TextureUtil.bindTexture(itextureobject.getGlTextureId());
    }

    public boolean loadTickableTexture(ResourceLocation textureLocation, ITickableTextureObject textureObj) {
        if (this.loadTexture(textureLocation, textureObj)) {
            this.listTickables.add(textureObj);
            return true;
        } else {
            return false;
        }
    }

    public boolean loadTexture(ResourceLocation textureLocation, ITextureObject textureObj) {
        boolean flag = true;

        try {
            textureObj.loadTexture(this.resourceManager);
        } catch (IOException ioexception) {
            if (textureLocation != RESOURCE_LOCATION_EMPTY) {
                LOGGER.warn("Failed to load texture: {}", textureLocation, ioexception);
            }

            textureObj = TextureUtil.MISSING_TEXTURE;
            this.mapTextureObjects.put(textureLocation, textureObj);
            flag = false;
        } catch (Throwable throwable) {
            final ITextureObject textureObjf = textureObj;
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Registering texture");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Resource location being registered");
            crashreportcategory.addCrashSection("Resource location", textureLocation);
            crashreportcategory.addDetail("Texture object class", () -> textureObjf.getClass().getName());
            throw new ReportedException(crashreport);
        }

        this.mapTextureObjects.put(textureLocation, textureObj);
        return flag;
    }

    public ITextureObject getTexture(ResourceLocation textureLocation) {
        return this.mapTextureObjects.get(textureLocation);
    }

    public ResourceLocation getDynamicTextureLocation(String name, DynamicTexture texture) {
        Integer integer = this.mapTextureCounters.get(name);

        if (integer == null) {
            integer = 1;
        } else {
            integer = integer + 1;
        }

        this.mapTextureCounters.put(name, integer);
        ResourceLocation resourcelocation = new ResourceLocation(String.format("dynamic/%s_%d", name, integer));
        this.loadTexture(resourcelocation, texture);
        return resourcelocation;
    }

    public void tick() {
        for (ITickable itickable : this.listTickables) {
            itickable.tick();
        }
    }

    public void deleteTexture(ResourceLocation textureLocation) {
        ITextureObject itextureobject = this.getTexture(textureLocation);

        if (itextureobject != null) {
            this.mapTextureObjects.remove(textureLocation); // Forge: fix MC-98707
            TextureUtil.deleteTexture(itextureobject.getGlTextureId());
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        net.minecraftforge.fml.common.ProgressManager.ProgressBar bar = net.minecraftforge.fml.common.ProgressManager.push("Reloading Texture Manager", this.mapTextureObjects.keySet().size(), true);
        Iterator<Entry<ResourceLocation, ITextureObject>> iterator = this.mapTextureObjects.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<ResourceLocation, ITextureObject> entry = iterator.next();
            bar.step(entry.getKey().toString());
            ITextureObject itextureobject = entry.getValue();

            if (itextureobject == TextureUtil.MISSING_TEXTURE) {
                iterator.remove();
            } else {
                this.loadTexture(entry.getKey(), itextureobject);
            }
        }
        net.minecraftforge.fml.common.ProgressManager.pop(bar);
    }
}
