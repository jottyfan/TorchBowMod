package mod.torchbowmod;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.Vanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.function.Predicate;

import static mod.torchbowmod.TorchBowMod.multiTorch;
import static net.minecraft.item.BowItem.getArrowVelocity;

public class TorchBow extends RangedWeaponItem implements Vanishable {
    private ItemStack torchbinder;
    private ItemStack sitemstack;
    private boolean sitem;
    private boolean storageid;

    private boolean binder;
    public static final Predicate<ItemStack> TORCH = (itemStack) -> {
        return itemStack.getItem() == Blocks.TORCH.asItem() ||
                itemStack.getItem() == multiTorch ||
                (itemStack.getItem() == TorchBowMod.torchbinder && itemStack.getOrCreateSubTag("TorchBandolier").getInt("Count") > 0) ||
                (itemStack.getItem() == TorchBowMod.StorageBox &&
                        (ItemStack.fromTag(itemStack.getTag().getCompound("StorageItemData")).getItem() == Blocks.TORCH.asItem() ||
                                ItemStack.fromTag(itemStack.getTag().getCompound("StorageItemData")).getItem() == multiTorch));
    };

    public TorchBow(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
    return ingredient.getItem() == Items.FLINT_AND_STEEL || super.canRepair(stack, ingredient);
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
    	return TORCH;
    }
    
    @Override
    public int getRange() {
        return 15;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity) entityLiving;
            boolean flag = playerentity.abilities.creativeMode || EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack itemstack = playerentity.getArrowType(stack);

            int i = this.getUseDuration(stack) - timeLeft;
            if (i < 0) return;

            if (!itemstack.isEmpty() || flag) {
                if (itemstack.isEmpty()) {
                    itemstack = new ItemStack(Blocks.TORCH);
                }

                float f = getArrowVelocity(i);
                if ((double) f >= 0.1D) {
                    boolean flag1 = playerentity.abilities.creativeMode || (itemstack.getItem() instanceof ArrowItem && ((ArrowItem) itemstack.getItem()).isInfinite(itemstack, stack, playerentity));
                    if (!worldIn.isClient) {
                        float size = 10;
                        shootTorch(playerentity.pitch, playerentity.yaw, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                        if (itemstack.getItem() == multiTorch || (itemstack.getItem() == TorchBowMod.StorageBox && ItemStack.fromTag(itemstack.getTag().getCompound("StorageItemData")).getItem() == multiTorch)) {
                            shootTorch(playerentity.pitch - size, playerentity.yaw + size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.pitch - size, playerentity.yaw, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.pitch - size, playerentity.yaw - size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.pitch + size, playerentity.yaw + size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.pitch + size, playerentity.yaw, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.pitch + size, playerentity.yaw - size, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.pitch, playerentity.yaw + size * 1.2f, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                            shootTorch(playerentity.pitch, playerentity.yaw - size * 1.2f, playerentity, entityLiving, worldIn, itemstack, stack, flag1, f);
                        }
                    }

                    worldIn.playSound((PlayerEntity) entityLiving, playerentity.prevX, playerentity.prevY, playerentity.prevZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                    if (!playerentity.abilities.creativeMode) {
                        if (itemstack.getItem() == Blocks.TORCH.asItem() || itemstack.getItem() == multiTorch) {
                        		itemstack.decrement(1);
                            if (itemstack.isEmpty()) {
                            		playerentity.inventory.removeOne(itemstack);
                            }
                        } else if (sitem) {//StorageBoxだった場合の処理
                            if (!worldIn.isClient) {
                                if (storageid) {
                                    int Size = sitemstack.getTag().getInt("StorageSize");//今のアイテムの数取得
                                    int retrun_size = --Size;
                                    if (retrun_size != 0) {
                                        sitemstack.getTag().putInt("StorageSize", retrun_size);//ストレージBoxの中のアイテムの数減少させる。
                                    } else {
                                        sitemstack.getTag().remove("StorageItemData");
                                    }
                                }
                            }
                        } else if (binder) {//TorchBandolierだった場合の処理
                            if (!worldIn.isClient) {
                                int Size = torchbinder.getOrCreateSubTag("TorchBandolier").getInt("Count");//今のアイテムの数取得
                                int retrun_size = --Size;
                                torchbinder.getOrCreateSubTag("TorchBandolier").putInt("Count", retrun_size);//TorchBandolierのアイテムの数減少させる。
                            }
                        }
                    }

                    playerentity.addStat(Stat.ITEM_USED.get(this));
                }
            }
        }
    }

    private void shootTorch(float offsetPitch, float offsetYaw, PlayerEntity entitle, LivingEntity livingEntity, World worldIn, ItemStack itemstack, ItemStack stack, boolean flag1, float f) {
        EntityTorch abstractedly = new EntityTorch(worldIn, livingEntity);
        float fs = -MathHelper.sin(offsetYaw * ((float) Math.PI / 180F)) * MathHelper.cos(offsetPitch * ((float) Math.PI / 180F));
        float f1 = -MathHelper.sin(offsetPitch * ((float) Math.PI / 180F));
        float f2 = MathHelper.cos(offsetYaw * ((float) Math.PI / 180F)) * MathHelper.cos(offsetPitch * ((float) Math.PI / 180F));
        abstractedly.shoot(fs, f1, f2, f * 3.0F, 1.0F);
        if (f == 1.0F) {
            abstractedly.setIsCritical(true);
        }

        int j = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
        if (j > 0) {
            abstractedly.setDamage(abstractedly.getDamage() + (double) j * 0.5D + 0.5D);
        }

        int k = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
        if (k > 0) {
            abstractedly.setKnockbackStrength(k);
        }

        if (EnchantmentHelper.getLevel(Enchantments.FLAME, stack) > 0) {
            abstractedly.setOnFireFor(100);
        }

        stack.damage(1, entitle, (p_220009_1_) -> {
            p_220009_1_.sendBreakAnimation(entitle.getActiveHand());
        });
        if (flag1 || entitle.abilities.creativeMode && (itemstack.getItem() == Blocks.TORCH.asItem())) {
            abstractedly.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
        }
        worldIn.spawnEntity(abstractedly);
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getStackInHand(handIn);
        binder = getSilentsMod(playerIn);
        storageid = getStorageMod(playerIn);
        boolean flag = !playerIn.findAmmo(itemstack).isEmpty();

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null) return ret;

        if (!playerIn.abilities.creativeMode && !flag && !binder && !storageid) {
            return flag ? new ActionResult<>(ActionResultType.PASS, itemstack) : new ActionResult<>(ActionResultType.FAIL, itemstack);
        } else {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
        }
    }

    /***
     * アイテムからアイテムスタック取得。
     * @param player
     * @param item
     * @return　ItemStack
     */
    private ItemStack getStack(PlayerEntity player, Item item) {
        for (int i = 0; i < player.inventory.main.size(); ++i) {
            if (player.inventory.main.get(i) != null && player.inventory.main.get(i).getItem() == item/*TorchBowMod.StorageBox*/) {
                ItemStack itemstack = player.inventory.main.get(i);
                if (itemstack != null) {//アイテムスタックがからじゃなかったら
                    if (itemstack.getTag() == null) {//NBTがNullだったら
                        itemstack.setTag(new CompoundTag());//新しい空のNBTを書き込む
                    }
                }
                return itemstack;
            }
        }
        ItemStack stack = new ItemStack(Items.BONE);//取得できなかったら適当に骨入れる
        return stack;
    }

    /***
     *  Modのアイテムが有効かどうか、松明が切れてないかどうか
     *  StorageBoxMod用処理
     * @param player
     * @return Modお問い合わせ
     */
    private boolean getStorageMod(PlayerEntity player) {
        sitemstack = getStack(player, TorchBowMod.StorageBox);//ItemStack取得
        boolean as = sitemstack.getItem() == TorchBowMod.StorageBox;//正しいかどうかチェック
        boolean storageid = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        int ssize = 0;
        if (as) {//ただしかったら
            CompoundTag a = sitemstack.getTag().getCompound("StorageItemData");//StrageBoxに入ってるItemStackを取得
            if (a != null) {
                Item itemname = ItemStack.fromTag(a).getItem();//スロトレージBoxのなかのID取得
                Item itemid = new ItemStack(Blocks.TORCH).getItem();//対象のID取得
                Item itemid2 = new ItemStack(multiTorch).getItem();
                sitem = itemname == itemid || itemname == itemid2;
                if (sitem) {//同じ場合
                    ssize = sitemstack.getTag().getInt("StorageSize");
                    storageid = true;//有効に
                    if (ssize == 0) {
                        storageid = false;//無効に
                    }
                }
            } else {
                sitem = false;
            }
        }
        return storageid;
    }

    /**
     * Modのアイテムが有効かどうか、松明が切れてないかどうか
     * TorchBandolier用処理
     *
     * @param player 　プレイヤー
     * @return Modお問い合わせ
     */
    private boolean getSilentsMod(PlayerEntity player) {
        torchbinder = getStack(player, TorchBowMod.torchbinder);//ItemStack取得
        boolean mitem = torchbinder.getItem() == TorchBowMod.torchbinder;//正しいかどうかチェック
        boolean myes = false;//ストレージBoxに入ってるItemのIDチェック用の変数初期化　初期値：無効
        if (mitem) {
            int ssize = 0;
            ssize = torchbinder.getOrCreateSubTag("TorchBandolier").getInt("Count");
            myes = true;//有効に
            if (ssize == 0) {
                myes = false;//無効に
            }
        }
        return myes;
    }
}
