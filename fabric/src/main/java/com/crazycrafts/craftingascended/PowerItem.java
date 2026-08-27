package com.crazycrafts.craftingascended;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PowerItem extends Item {
    private final String power;
    private final String tooltipKey;

    public PowerItem(String power, String tooltipKey, Properties properties) {
        super(properties.stacksTo(1));
        this.power = power;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) activate(level, player);
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() != null) {
            BlockPos pos = context.getClickedPos();
            int radius = power.equals("world_breaker") ? 2 : power.equals("chunk_pickaxe") ? 1 : 0;
            if (power.equals("lumber_axe")) radius = 2;
            if (power.equals("vein_pickaxe")) radius = 1;
            if (radius > 0) {
                for (BlockPos target : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
                    if (context.getLevel().getBlockState(target).getDestroySpeed(context.getLevel(), target) >= 0) {
                        context.getLevel().destroyBlock(target, true, context.getPlayer());
                    }
                }
                context.getPlayer().getCooldowns().addCooldown(this, 15);
                return InteractionResult.CONSUME;
            }
        }
        return super.useOn(context);
    }

    private void activate(Level level, Player player) {
        AABB range = player.getBoundingBox().inflate(12);
        switch (power) {
            case "ore_vacuum" -> level.getEntitiesOfClass(ItemEntity.class, range).forEach(entity -> pull(entity, player, 1.3));
            case "mob_magnet" -> level.getEntitiesOfClass(LivingEntity.class, range,
                    entity -> entity != player).forEach(entity -> pull(entity, player, 1.0));
            case "miners_boots" -> {
                effect(player, MobEffects.DIG_SPEED, 20 * 60, 1);
                effect(player, MobEffects.NIGHT_VISION, 20 * 60, 0);
                effect(player, MobEffects.MOVEMENT_SPEED, 20 * 60, 0);
            }
            case "super_shield" -> {
                effect(player, MobEffects.DAMAGE_RESISTANCE, 20 * 20, 2);
                effect(player, MobEffects.ABSORPTION, 20 * 20, 2);
            }
            case "titan_blade" -> effect(player, MobEffects.DAMAGE_BOOST, 20 * 30, 3);
            case "infinity_bow" -> effect(player, MobEffects.DAMAGE_BOOST, 20 * 30, 1);
            case "ultimate_armor" -> {
                effect(player, MobEffects.DAMAGE_RESISTANCE, 20 * 60, 2);
                effect(player, MobEffects.FIRE_RESISTANCE, 20 * 60, 0);
                effect(player, MobEffects.MOVEMENT_SPEED, 20 * 60, 1);
            }
            case "time_staff" -> level.getEntitiesOfClass(LivingEntity.class, range,
                    entity -> entity != player).forEach(entity -> entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 10, 10)));
            case "star_cannon" -> {
                Vec3 hit = player.getEyePosition().add(player.getLookAngle().scale(10));
                level.explode(player, hit.x, hit.y, hit.z, 3.0F, Level.ExplosionInteraction.NONE);
            }
            case "life_totem" -> {
                player.setHealth(player.getMaxHealth());
                effect(player, MobEffects.REGENERATION, 20 * 20, 3);
                effect(player, MobEffects.ABSORPTION, 20 * 60, 4);
            }
            case "admin_apple" -> {
                effect(player, MobEffects.REGENERATION, 20 * 120, 4);
                effect(player, MobEffects.DAMAGE_BOOST, 20 * 120, 4);
                effect(player, MobEffects.DAMAGE_RESISTANCE, 20 * 120, 3);
                effect(player, MobEffects.MOVEMENT_SPEED, 20 * 120, 3);
            }
            case "duplication_core" -> duplicate(player);
            case "void_armor" -> {
                effect(player, MobEffects.REGENERATION, 20 * 120, 2);
                effect(player, MobEffects.DAMAGE_RESISTANCE, 20 * 120, 4);
                effect(player, MobEffects.FIRE_RESISTANCE, 20 * 120, 0);
                effect(player, MobEffects.WATER_BREATHING, 20 * 120, 0);
            }
            case "quick_furnace" -> effect(player, MobEffects.FIRE_RESISTANCE, 20 * 60, 0);
            case "auto_farmer" -> effect(player, MobEffects.LUCK, 20 * 60, 2);
            default -> { }
        }
    }

    private static void pull(Entity entity, Player player, double speed) {
        Vec3 direction = player.position().add(0, 1, 0).subtract(entity.position());
        if (direction.lengthSqr() > 0.01) entity.setDeltaMovement(direction.normalize().scale(speed));
    }

    private static void effect(Player player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                               int duration, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }

    private static void duplicate(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        ItemStack source = player.getOffhandItem();
        if (source.isEmpty() || (!player.isCreative() && player.experienceLevel < 30)) {
            player.displayClientMessage(Component.literal("Hold an item in your offhand and have 30 levels."), true);
            return;
        }
        if (!player.isCreative()) player.giveExperienceLevels(-30);
        ItemStack copy = source.copy();
        if (!player.getInventory().add(copy)) player.drop(copy, false);
        serverPlayer.displayClientMessage(Component.literal("Duplicated offhand item for 30 levels."), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click to activate").withStyle(ChatFormatting.GOLD));
    }
}
