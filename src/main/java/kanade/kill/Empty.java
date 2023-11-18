package kanade.kill;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;

import java.util.ArrayList;
import java.util.List;

public class Empty {
    public List<Entity> field_72996_f;
    public List<Entity> protects;

    public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
        if (this.field_72996_f.getClass() != ArrayList.class) {
            this.field_72996_f = new ArrayList<>(this.field_72996_f);
        }

        for (Entity entity2 : this.protects) {
            if (!this.field_72996_f.contains(entity2)) {
                this.field_72996_f.add(entity2);
            }
        }

        int count = 0;
        for (int x = 0; x < field_72996_f.size(); x++) {
            if (((Entity) field_72996_f.get(x)).isCreatureType(type, forSpawnCount)) {
                count++;
            }
        }
        return count;
    }

    public int func_72907_a(Class<?> entityType) {
        if (this.field_72996_f.getClass() != ArrayList.class) {
            this.field_72996_f = new ArrayList<>(this.field_72996_f);
        }

        for (Entity entity2 : this.protects) {
            if (!this.field_72996_f.contains(entity2)) {
                this.field_72996_f.add(entity2);
            }
        }

        int j2 = 0;

        for (Entity entity4 : this.field_72996_f) {
            if ((!(entity4 instanceof EntityLiving) || !((EntityLiving) entity4).isNoDespawnRequired()) && entityType.isAssignableFrom(entity4.getClass())) {
                ++j2;
            }
        }

        return j2;
    }
}
