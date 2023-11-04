package kanade.kill;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FakeEntityDataManager extends EntityDataManager {
    public static final FakeEntityDataManager instance = new FakeEntityDataManager();

    private FakeEntityDataManager() {
        super(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(@Nonnull DataParameter<T> key) {
        T obj = super.get(key);
        if (obj instanceof Integer) {
            return (T) new Integer(0);
        }
        if (obj instanceof Long) {
            return (T) new Long(0);
        }
        if (obj instanceof Short) {
            return (T) new Short((short) 0);
        }
        if (obj instanceof Double) {
            return (T) new Double(0);
        }
        if (obj instanceof Float) {
            return (T) new Float(0);
        }
        if (obj instanceof String) {
            return (T) "";
        }
        if (obj instanceof Boolean) {
            return (T) Boolean.FALSE;
        }
        if (obj instanceof Character) {
            return (T) new Character(' ');
        }
        if (obj instanceof Byte) {
            return (T) new Byte((byte) 0);
        }
        return null;
    }

    @Override
    public <T> void set(@Nullable DataParameter<T> key, @Nullable T value) {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public <T> void setDirty(@Nullable DataParameter<T> key) {
    }

    @Override
    public List<DataEntry<?>> getAll() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void setClean() {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setEntryValues(@Nullable List<EntityDataManager.DataEntry<?>> entriesIn) {
    }
}
