package com.github.zly2006.clienttweaks;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.mixin.gametest.ArgumentTypesMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class ClientTweaksClient implements ClientModInitializer {
    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("ct")
            .then(ClientCommandManager.literal("reload")
                .executes((commandContext) -> {
                    reload();
                    return 1;
                }))
            .then(ClientCommandManager.literal("game_won_screen")
                .executes(context -> {
                    MinecraftClient client = context.getSource().getClient();
                    //client.getNetworkHandler().onGameStateChange(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.GAME_WON, 1));
                    client.setScreen(new CreditsScreen(true, () -> {
                        client.getMusicTracker().stop();
                    }));
                    return 1;
                }))
            .then(ClientCommandManager.literal("highlight_entities")
                .then(ClientCommandManager.argument("entities", EntityArgumentType.entities())
                    .executes(context -> {
                        MinecraftClient client = context.getSource().getClient();

                        EntitySelector selector = context.getArgument("entities", EntitySelector.class);
                        List<? extends Entity> entities = selector.getEntities(client.player.getCommandSource());
                        if (entities.isEmpty()) {
                            context.getSource().sendFeedback(Text.literal("No entities found!"));
                        }
                        else {
                            entities.forEach(entity -> {
                                entity.setGlowing(true);
                            });
                        }
                        return 1;
                    })))
            .then(ClientCommandManager.literal("highlight_entity")
                .then(ClientCommandManager.argument("entity", UuidArgumentType.uuid())
                    .executes(context -> {
                        MinecraftClient client = context.getSource().getClient();
                        UUID uuid = context.getArgument("entity", UUID.class);
                        assert client.world != null;
                        AtomicBoolean found = new AtomicBoolean(false);
                        client.world.getEntities().forEach(e -> {
                            if (e.getUuid().equals(uuid)) {
                                e.setGlowing(true);
                                e.setFlag(6/*glowing*/, true);
                                found.set(true);
                            }
                        });
                        if (!found.get()) {
                            context.getSource().sendFeedback(Text.literal("No entity found!"));
                        }///ct highlight_entity 0ea66aa2-490d-4f1b-9039-4d8ffc021515
                        return 1;
                    })))
        );
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(ClientTweaksClient::registerCommands);
    }

    private static void reload() {
    }
}
