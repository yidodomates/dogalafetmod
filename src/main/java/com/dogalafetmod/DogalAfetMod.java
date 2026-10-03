package com.dogalafetmod;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

@Mod("dogalafetmod")
public class DogalAfetMod {

    private static int ticksElapsed = 0;
    private static final int DISASTER_INTERVAL = 2400; // 2 dakika
    public static int remainingSeconds = 120;
    private static final Random random = new Random();

    public DogalAfetMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ticksElapsed++;
        remainingSeconds = (DISASTER_INTERVAL - ticksElapsed) / 20;

        if (ticksElapsed >= DISASTER_INTERVAL) {
            ticksElapsed = 0;
            if (event.getServer() != null) {
                for (ServerWorld world : event.getServer().getAllLevels()) {
                    if (!world.isClientSide()) {
                        triggerRandomDisaster(world);
                    }
                }
            }
        }
    }

    private static void triggerRandomDisaster(ServerWorld world) {
        int disasterType = random.nextInt(5);

        for (ServerPlayerEntity player : world.players()) {
            BlockPos pos = player.blockPosition();

            switch (disasterType) {
                case 0:
                    player.sendMessage(new StringTextComponent("§c[AFET] Şiddetli Yıldırım Fırtınası Başladı!"), player.getUUID());
                    for (int i = 0; i < 5; i++) {
                        LightningBoltEntity lightning = new LightningBoltEntity(world, pos.getX() + random.nextInt(12) - 6, pos.getY(), pos.getZ() + random.nextInt(12) - 6, true);
                        world.addFreshEntity(lightning);
                    }
                    break;
                case 1:
                    player.sendMessage(new StringTextComponent("§c[AFET] Gökten Meteorlar Düşüyor!"), player.getUUID());
                    for (int i = 0; i < 3; i++) {
                        BlockPos target = pos.offset(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
                        world.explode(null, target.getX(), target.getY(), target.getZ(), 4.0F, ServerWorld.Mode.BREAK);
                    }
                    break;
                case 2:
                    player.sendMessage(new StringTextComponent("§c[AFET] Ani Su Baskını Başladı!"), player.getUUID());
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            BlockPos waterPos = pos.offset(x, 0, z);
                            if (world.isEmptyBlock(waterPos)) {
                                world.setBlockAndUpdate(waterPos, Blocks.WATER.defaultBlockState());
                            }
                        }
                    }
                    break;
                case 3:
                    player.sendMessage(new StringTextComponent("§c[AFET] Yer Altından Lavlar Yükseliyor!"), player.getUUID());
                    BlockPos lavaPos = pos.offset(random.nextInt(4) - 2, 0, random.nextInt(4) - 2);
                    world.setBlockAndUpdate(lavaPos, Blocks.LAVA.defaultBlockState());
                    break;
                case 4:
                    player.sendMessage(new StringTextComponent("§c[AFET] Şiddetli Deprem! Zemin Çöküyor!"), player.getUUID());
                    for (int x = -2; x <= 2; x++) {
                        for (int z = -2; z <= 2; z++) {
                            world.destroyBlock(pos.offset(x, -1, z), false);
                        }
                    }
                    break;
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "dogalafetmod", value = Dist.CLIENT)
    public static class ClientHUD {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onRenderGui(RenderGameOverlayEvent.Post event) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && !mc.options.hideGui) {
                    String text = "Afete Kalan: " + remainingSeconds + " sn";
                    int color = remainingSeconds <= 10 ? 0xFFFF5555 : 0xFFFFFFFF;
                    mc.font.drawShadow(event.getMatrixStack(), text, 10, 10, color);
                }
            }
        }
    }
}
