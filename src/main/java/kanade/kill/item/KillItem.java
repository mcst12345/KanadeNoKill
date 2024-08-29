package kanade.kill.item;

import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.entity.EntityProtected;
import kanade.kill.network.NetworkHandler;
import kanade.kill.network.packets.UpdatePlayerProtectedState;
import kanade.kill.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class KillItem extends ItemSword {

    private static final ToolMaterial TOOL_MATERIAL = EnumHelper.addToolMaterial("KANADE", 32, 9999, 9999F, Float.MAX_VALUE, 200);

    public KillItem(){
        super(Objects.requireNonNull(TOOL_MATERIAL));
        this.setRegistryName("kanade:kill");
        this.setMaxStackSize(1);
        this.setMaxDamage(-1);
    }


    @Override
    @Nonnull
    public EnumAction getItemUseAction(@Nonnull ItemStack stack) {
        if (ClassUtil.isCallerFromFilterClass(new ClassUtil.Filter<Class<?>>() {

            @Override
            public boolean filter(Class<?> clazz) {
                return clazz.isAssignableFrom(ItemRenderer.class);
            }
        }, 2)) {
            Minecraft mc = Minecraft.getMinecraft();
            float equippedProgressMainHand = mc.ItemRenderer.equippedProgressMainHand;
            float prevEquippedProgressMainHand = mc.ItemRenderer.prevEquippedProgressMainHand;
            float f5 = 1.0F - (prevEquippedProgressMainHand
                    + (equippedProgressMainHand - prevEquippedProgressMainHand) * mc.getRenderPartialTicks());
            int horizontal = 1;
            GlStateManager.translate((float) horizontal * 0.56F, -0.52F + f5 * -0.6F,
                    -0.72F);
            GlStateManager.translate(horizontal * -0.14142136F, 0.08F, 0.14142136F);
            GlStateManager.rotate(-102.25F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(horizontal * 13.365F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(horizontal * 78.05F, 0.0F, 0.0F, 1.0F);
        }
        return SWORD;
    }

    public static final EnumAction SWORD = EnumHelper.addAction("SWORD:BLOCK");


    public static boolean inList(Object obj) {
        if (obj instanceof Entity) {
            if (obj instanceof EntityProtected) {
                return true;
            }
            if (Config.allPlayerProtect) {
                if (obj instanceof EntityPlayer) {
                    return true;
                }
            }
            if (Launch.client && obj == Minecraft.getMinecraft().PLAYER && NativeMethods.HaveTag(Minecraft.getMinecraft(), 10)) {
                return true;
            }
            UUID uuid = ((Entity) obj).getUniqueID();
            return list.contains(uuid) || (uuid != null && NativeMethods.ProtectContain(uuid.hashCode())) || (obj instanceof EntityItem && Util.NoRemove(((EntityItem) obj).getItem()));
        } else if (Launch.client) {
            if (obj instanceof Minecraft) {
                if (NativeMethods.HaveTag(Minecraft.getMinecraft(), 10)) {
                    return true;
                }
                EntityPlayer player = ((Minecraft) obj).PLAYER;
                if (player != null) {
                    return list.contains(player.getUniqueID());
                }
            }
        } else if (obj instanceof UUID) {
            UUID uuid = (UUID) obj;
            return list.contains(uuid) || NativeMethods.ProtectContain(uuid.hashCode());
        }
        return false;
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack stack) {
        return 72000;
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull IBlockState state) {
        return Float.MAX_VALUE;
    }

    @Override
    public boolean canHarvestBlock(@Nonnull IBlockState blockIn) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }
    public static int mode;
    public static final Set<UUID> list = new HashSet<>();

    @SideOnly(Side.CLIENT)
    @Nullable
    public net.minecraft.client.gui.FontRenderer getFontRenderer(@Nonnull ItemStack stack) {
        return KanadeFontRender.Get();
    }

    public static void AddToList(Object obj) {
        if (obj instanceof Entity) {
            UUID uuid = ((Entity) obj).getUniqueID();
            if (uuid != null) {
                list.add(uuid);
                NativeMethods.ProtectAdd(uuid.hashCode());
                MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
                if (server != null && server.isCallingFromMinecraftThread()) {
                    NetworkHandler.INSTANCE.sendMessageToAllPlayer(new UpdatePlayerProtectedState(uuid));
                }
            }
        } else if (Launch.client) {
            if (obj instanceof Minecraft) {
                UUID uuid = ((Minecraft) obj).PLAYER.getUniqueID();
                if (uuid != null) {
                    list.add(uuid);
                    NativeMethods.ProtectAdd(uuid.hashCode());
                }
            }
        }
    }

    @Override
    public boolean isDamageable(){
        return false;
    }

    @Override
    @Nonnull
    public Item setFull3D(){
        return this;
    }

    @Override
    @Nonnull
    public String getTranslationKey(@Nullable ItemStack stack){
        return "item.kanade.kill";
    }

    @Override
    @Nonnull
    public String getItemStackDisplayName(@Nullable ItemStack stack){
        return "Kill";
    }

    @Override
    public boolean isEnchantable(@Nullable ItemStack stack){
        return false;
    }

    @Override
    public boolean onLeftClickEntity(@Nullable ItemStack stack,@Nonnull EntityPlayer player,@Nonnull Entity entity){
        EntityUtil.summonThunder(entity, 20);
        EntityUtil.Kill(entity, true);
        return true;
    }

    @Override
    @Nonnull
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull EntityLivingBase entityLiving) {
        if (mode == 2) {
            ChunkUtil.reloadChunk(worldIn, worldIn.getChunk(entityLiving.chunkCoordX, entityLiving.chunkCoordZ));
        }
        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

    @Override
    public void onUpdate(@Nullable ItemStack stack,@Nullable World worldIn,@Nonnull Entity entityIn, int itemSlot, boolean isSelected)
    {
        if(entityIn instanceof EntityPlayer){
            entityIn.WORLD.protects.add(entityIn);
            list.add(entityIn.getUniqueID());
        }
    }

    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn,@Nonnull EnumHand handIn){
        ParticleUtil.drawCircle(playerIn, 0.5, EnumParticleTypes.END_ROD);
        ParticleUtil.drawCircle(playerIn, 0.6, EnumParticleTypes.END_ROD);
        ParticleUtil.drawCircle(playerIn, 0.7, EnumParticleTypes.END_ROD);
        ParticleUtil.drawCircle(playerIn, 0.8, EnumParticleTypes.END_ROD);
        ParticleUtil.drawCircle(playerIn, 0.9, EnumParticleTypes.END_ROD);
        ParticleUtil.drawCircle(playerIn, 1.0, EnumParticleTypes.END_ROD);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
