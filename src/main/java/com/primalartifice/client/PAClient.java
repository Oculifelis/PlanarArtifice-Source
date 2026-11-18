package com.primalartifice.client;

import com.leclowndu93150.thaumaturge.client.hud.AuraHudOverlay;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.primalartifice.PARegistry;
import com.primalartifice.alkimium.PAAlkimium;
import com.primalartifice.mirror.PAMirror;
import com.leclowndu93150.thaumaturge.client.render.blockentity.JarRenderer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.phys.Vec3;
import com.primalartifice.PrimalArtifice;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Client setup: the Aura Meter's gauge (Thaumaturge's own aura HUD, shown without a thaumometer in hand) and the
 * Alkimium Goggles' suction readout, and the connected-glass block model.
 */
public final class PAClient {
    private static AuraHudOverlay gauge;

    private PAClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(PAClient::layers);
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) ->
                e.registerBlockEntityRenderer(PAMirror.MIRRORED_JAR_BE.get(), JarRenderer::new));
        // Connected glass: the nine Glassworking glasses join into one pane (blockstates use "primalartifice:connected").
        modBus.addListener((RegisterBlockStateModels e) ->
                e.registerModel(PrimalArtifice.id("connected"), ConnectedModel.Unbaked.CODEC));
        NeoForge.EVENT_BUS.addListener(PAClient::suspensionBelt);
    }

    private static void layers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, PrimalArtifice.id("aura_meter"), PAClient::auraMeter);
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, PrimalArtifice.id("goggles_suction"), PAClient::gogglesSuction);
    }

    private static void auraMeter(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || !carries(player, PARegistry.AURA_METER.get())) {
            return;
        }
        // Thaumaturge draws the same gauge itself while a thaumometer is held; don't draw it twice.
        if (player.getMainHandItem().getItem() instanceof ThaumometerItem || player.getOffhandItem().getItem() instanceof ThaumometerItem) {
            return;
        }
        if (gauge == null) {
            gauge = new AuraHudOverlay();
        }
        gauge.render(graphics, delta);
    }

    /** Sneaking in Alkimium Goggles: the essentia and suction on the face of the block under the crosshair. */
    private static void gogglesSuction(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.options.hideGui || !player.isShiftKeyDown() || !wearsGoggles(player)
                || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        IEssentiaTransport transport = mc.level.getCapability(EssentiaCapabilities.TRANSPORT, hit.getBlockPos(), hit.getDirection());
        if (transport == null) {
            return;
        }
        Direction face = hit.getDirection();
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2 + 8;
        Holder<IAspect> held = transport.getEssentiaType(face);
        if (held != null && transport.getEssentiaAmount(face) > 0) {
            graphics.text(mc.font, Component.translatable("hud.primalartifice.essentia", aspectName(held), transport.getEssentiaAmount(face)), x, y, 0xFFE0C8FF, true);
            y += 10;
        }
        Holder<IAspect> wanted = transport.getSuctionType(face);
        Component type = wanted == null ? Component.translatable("hud.primalartifice.any") : aspectName(wanted);
        graphics.text(mc.font, Component.translatable("hud.primalartifice.suction", transport.getSuctionAmount(face), type), x, y, 0xFF8EF0B8, true);
    }

    private static Component aspectName(Holder<IAspect> aspect) {
        return Component.translatable("aspect.thaumaturge." + aspect.value().tag());
    }

    private static boolean wearsGoggles(LocalPlayer player) {
        Item goggles = PAAlkimium.GOGGLES.get();
        return player.getItemBySlot(EquipmentSlot.HEAD).is(goggles)
                || CuriosApi.getCuriosInventory(player).map(c -> c.isEquipped(goggles)).orElse(false);
    }

    /**
     * Belt of Suspension, on the wearer's own client (which owns its movement): holding jump stops a fall, holding
     * sneak slows it to a gentle sink. Creative flight is left alone.
     */
    private static void suspensionBelt(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.getAbilities().flying || player.onGround() || player.getDeltaMovement().y >= 0
                || !CuriosApi.getCuriosInventory(player).map(c -> c.isEquipped(PAMirror.SUSPENSION_BELT.get())).orElse(false)) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (player.input.keyPresses.shift()) {
            player.setDeltaMovement(motion.x, Math.max(motion.y, -0.25), motion.z);
        } else if (player.input.keyPresses.jump()) {
            player.setDeltaMovement(motion.x, 0, motion.z);
        }
    }

    static boolean carries(LocalPlayer player, Item item) {
        if (player.getInventory().contains(s -> s.is(item))) {
            return true;
        }
        return CuriosApi.getCuriosInventory(player).map(c -> c.isEquipped(item)).orElse(false);
    }
}
