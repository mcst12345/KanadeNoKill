package kanade.kill.asm.injections;

import kanade.kill.Launch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class InventoryPlayer implements Opcodes {
    public static void AddField(ClassNode cn) {
        Launch.LOGGER.info("Adding field.");
        cn.fields.add(new FieldNode(ACC_PUBLIC, "mainInv", "[Lnet/minecraft/item/ItemStack;", null, null));
        cn.fields.add(new FieldNode(ACC_PUBLIC, "armorInv", "[Lnet/minecraft/item/ItemStack;", null, null));
    }

    public static void InjectConstructor(MethodNode mn) {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new IntInsnNode(BIPUSH, 36));
        list.add(new TypeInsnNode(ANEWARRAY, "net/minecraft/item/ItemStack"));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/player/InventoryPlayer", "field_70462_a", "[Lnet/minecraft/item/ItemStack;"));
        list.add(new VarInsnNode(ALOAD, 0));
        list.add(new InsnNode(ICONST_4));
        list.add(new TypeInsnNode(ANEWARRAY, "net/minecraft/item/ItemStack"));
        list.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/player/InventoryPlayer", "field_70460_b", "[Lnet/minecraft/item/ItemStack;"));
        mn.instructions.insert(list);
    }
}
