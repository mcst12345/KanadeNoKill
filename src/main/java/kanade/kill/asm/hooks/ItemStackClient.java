package kanade.kill.asm.hooks;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static kanade.kill.ModMain.tooltip;
import static net.minecraft.item.ItemStack.DECIMALFORMAT;

public class ItemStackClient {
    @SideOnly(Side.CLIENT)
    public static List<String> getTooltip(ItemStack stack, @Nullable EntityPlayer playerIn, ITooltipFlag advanced) {
        List<String> list = Lists.newArrayList();
        String s = stack.getDisplayName();

        if (stack.hasDisplayName()) {
            s = TextFormatting.ITALIC + s;
        }

        s = s + TextFormatting.RESET;

        if (advanced.isAdvanced()) {
            String s1 = "";

            if (!s.isEmpty()) {
                s = s + " (";
                s1 = ")";
            }

            int i = Item.getIdFromItem(stack.item);

            if (stack.getHasSubtypes()) {
                s = s + String.format("#%04d/%d%s", i, stack.itemDamage, s1);
            } else {
                s = s + String.format("#%04d%s", i, s1);
            }
        } else if (!stack.hasDisplayName() && stack.item == Items.FILLED_MAP) {
            s = s + " #" + stack.itemDamage;
        }

        list.add(s);
        int i1 = 0;

        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("HideFlags", 99)) {
            i1 = stack.stackTagCompound.getInteger("HideFlags");
        }

        if ((i1 & 32) == 0) {
            stack.getItem().addInformation(stack, playerIn == null ? null : playerIn.world, list, advanced);
        }

        if (stack.hasTagCompound()) {
            if ((i1 & 1) == 0) {
                NBTTagList nbttaglist = stack.getEnchantmentTagList();

                for (int j = 0; j < nbttaglist.tagCount(); ++j) {
                    NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(j);
                    int k = nbttagcompound.getShort("id");
                    int l = nbttagcompound.getShort("lvl");
                    Enchantment enchantment = Enchantment.getEnchantmentByID(k);

                    if (enchantment != null) {
                        list.add(enchantment.getTranslatedName(l));
                    }
                }
            }

            if (stack.stackTagCompound.hasKey("display", 10)) {
                NBTTagCompound nbttagcompound1 = stack.stackTagCompound.getCompoundTag("display");

                if (nbttagcompound1.hasKey("color", 3)) {
                    if (advanced.isAdvanced()) {
                        list.add(I18n.translateToLocalFormatted("item.color", String.format("#%06X", nbttagcompound1.getInteger("color"))));
                    } else {
                        list.add(TextFormatting.ITALIC + I18n.translateToLocal("item.dyed"));
                    }
                }

                if (nbttagcompound1.getTagId("Lore") == 9) {
                    NBTTagList nbttaglist3 = nbttagcompound1.getTagList("Lore", 8);

                    if (!nbttaglist3.isEmpty()) {
                        for (int l1 = 0; l1 < nbttaglist3.tagCount(); ++l1) {
                            list.add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + nbttaglist3.getStringTagAt(l1));
                        }
                    }
                }
            }
        }
        for (EntityEquipmentSlot entityequipmentslot : EntityEquipmentSlot.values()) {
            Multimap<String, AttributeModifier> multimap = stack.getAttributeModifiers(entityequipmentslot);

            if (!multimap.isEmpty() && (i1 & 2) == 0) {
                list.add("");
                list.add(I18n.translateToLocal("item.modifiers." + entityequipmentslot.getName()));

                for (Map.Entry<String, AttributeModifier> entry : multimap.entries()) {
                    AttributeModifier attributemodifier = entry.getValue();
                    double d0 = attributemodifier.getAmount();
                    boolean flag = false;

                    if (playerIn != null) {
                        if (attributemodifier.getID() == Item.ATTACK_DAMAGE_MODIFIER) {
                            d0 = d0 + playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                            d0 = d0 + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
                            flag = true;
                        } else if (attributemodifier.getID() == Item.ATTACK_SPEED_MODIFIER) {
                            d0 += playerIn.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                            flag = true;
                        }
                    }

                    double d1;

                    if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2) {
                        d1 = d0;
                    } else {
                        d1 = d0 * 100.0D;
                    }

                    if (flag) {
                        list.add(" " + I18n.translateToLocalFormatted("attribute.modifier.equals." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
                    } else if (d0 > 0.0D) {
                        list.add(TextFormatting.BLUE + " " + I18n.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
                    } else if (d0 < 0.0D) {
                        d1 = d1 * -1.0D;
                        list.add(TextFormatting.RED + " " + I18n.translateToLocalFormatted("attribute.modifier.take." + attributemodifier.getOperation(), DECIMALFORMAT.format(d1), I18n.translateToLocal("attribute.name." + entry.getKey())));
                    }
                }
            }
        }

        if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Unbreakable") && (i1 & 4) == 0) {
            list.add(TextFormatting.BLUE + I18n.translateToLocal("item.unbreakable"));
        }

        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("CanDestroy", 9) && (i1 & 8) == 0) {
            NBTTagList nbttaglist1 = stack.stackTagCompound.getTagList("CanDestroy", 8);

            if (!nbttaglist1.isEmpty()) {
                list.add("");
                list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canBreak"));

                for (int j1 = 0; j1 < nbttaglist1.tagCount(); ++j1) {
                    Block block = Block.getBlockFromName(nbttaglist1.getStringTagAt(j1));

                    if (block != null) {
                        list.add(TextFormatting.DARK_GRAY + block.getLocalizedName());
                    } else {
                        list.add(TextFormatting.DARK_GRAY + "missingno");
                    }
                }
            }
        }

        if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("CanPlaceOn", 9) && (i1 & 16) == 0) {
            NBTTagList nbttaglist2 = stack.stackTagCompound.getTagList("CanPlaceOn", 8);

            if (!nbttaglist2.isEmpty()) {
                list.add("");
                list.add(TextFormatting.GRAY + I18n.translateToLocal("item.canPlace"));

                for (int k1 = 0; k1 < nbttaglist2.tagCount(); ++k1) {
                    Block block1 = Block.getBlockFromName(nbttaglist2.getStringTagAt(k1));

                    if (block1 != null) {
                        list.add(TextFormatting.DARK_GRAY + block1.getLocalizedName());
                    } else {
                        list.add(TextFormatting.DARK_GRAY + "missingno");
                    }
                }
            }
        }

        if (advanced.isAdvanced()) {
            if (stack.isItemDamaged()) {
                list.add(I18n.translateToLocalFormatted("item.durability", stack.getMaxDamage() - stack.getItemDamage(), stack.getMaxDamage()));
            }

            list.add(TextFormatting.DARK_GRAY + Item.REGISTRY.getNameForObject(stack.item).toString());

            if (stack.hasTagCompound()) {
                list.add(TextFormatting.DARK_GRAY + I18n.translateToLocalFormatted("item.nbt_tags", stack.getTagCompound().getKeySet().size()));
            }
        }

        net.minecraftforge.event.ForgeEventFactory.onItemTooltip(stack, playerIn, list, advanced);

        switch (tooltip) {
            case 0: {
                list.add("§f僕らは命に嫌われている。");
                break;
            }
            case 1: {
                list.add("§fもう一回、もう一回。「私は今日も転がります。」と");
                break;
            }
            case 2: {
                list.add("§fアイデンティティ 唸れ 君一人のせい");
                break;
            }
            case 3: {
                list.add("§f君は今日もステイ");
                break;
            }
            case 4: {
                list.add("§f乙女解剖であそぼうよ");
                break;
            }
            case 5: {
                list.add("§fだから妄想感傷代償連盟");
                break;
            }
            case 6: {
                list.add("§fロキロキのロックンロックンロール");
                break;
            }
            case 7: {
                list.add("§fWelcome to the メルティランド");
                break;
            }
            case 8: {
                list.add("§f溶けていく    命が溶けていく");
                break;
            }
            case 9: {
                list.add("§f冷たい第三の心臓が  たしたちを見つめていた");
                break;
            }
            case 10: {
                list.add("§f感度良好 5-2-4");
                break;
            }
            case 11: {
                list.add("§f今後千年草も生えない 砂の惑星さ");
                break;
            }
            case 12: {
                list.add("§fらい らい 羅刹と骸");
                break;
            }
            case 13: {
                list.add("§f残弾、既に無くなった 此処で一度引き返そうか");
                break;
            }
            case 14: {
                list.add("§f一瞬だけ忘れないでよね");
                break;
            }
            case 15: {
                list.add("§f真夜中に告ぐ 音の警告");
                break;
            }
            case 16: {
                list.add("§f二人きりこの儘愛し合えるさ―。");
                break;
            }
            case 17: {
                list.add("§fフラッシュバック・蝉の声・");
                break;
            }
            case 18: {
                list.add("§fそう 君は友達");
                break;
            }
            case 19: {
                list.add("§fあの夜から");
                break;
            }
            case 20: {
                list.add("§f“Gott ist tot”");
                break;
            }
            case 21: {
                list.add("§f愛や厭 愛や厭");
                break;
            }
            case 22: {
                list.add("§fあなたには僕が見えるか？");
                break;
            }
        }

        return list;
    }

}
