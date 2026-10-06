package net.onelitefeather.butterfly.minestom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.onelitefeather.butterfly.api.LuckPermsAPI;
import net.onelitefeather.butterfly.api.chat.ChatLine;
import net.onelitefeather.butterfly.api.chat.ChatMessageParser;
import net.onelitefeather.butterfly.api.chat.PlayerHeads;
import net.onelitefeather.butterfly.api.config.ButterflySettings;
import net.onelitefeather.butterfly.api.config.SettingsFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Butterfly {

    private static final Logger LOGGER = LoggerFactory.getLogger(Butterfly.class);

    private final EventNode<Event> parent;
    private final ButterflySettings settings;
    private @Nullable EventNode<Event> node;
    private @Nullable MinestomLuckPermsService service;

    private Butterfly(@NotNull EventNode<Event> parent, @NotNull ButterflySettings settings) {
        this.parent = parent;
        this.settings = settings;
    }

    /**
     * Creates an instance with the default settings, overridden only by JVM system properties
     * ({@code butterfly.teams.sort-format}, {@code butterfly.teams.collision}). No file is read or written.
     */
    public static Butterfly create() {
        return create(SettingsFile.fromSystemProperties(LOGGER));
    }

    /**
     * Creates an instance with the settings supplied by the host. No file is read or written.
     */
    public static Butterfly create(@NotNull ButterflySettings settings) {
        return create(MinecraftServer.getGlobalEventHandler(), settings);
    }

    /**
     * Creates an instance whose listeners live on a child node of the given parent.
     */
    static Butterfly create(@NotNull EventNode<Event> parent, @NotNull ButterflySettings settings) {
        return new Butterfly(parent, settings);
    }

    public void load() {
        service = new MinestomLuckPermsService(settings);
        LuckPermsAPI.setLuckPermsService(service);
        LuckPermsAPI.luckPermsAPI().subscribeEvents();

        node = EventNode.all("butterfly");
        node.addListener(PlayerChatEvent.class, this::playerChat);
        node.addListener(PlayerSpawnEvent.class, this::playerSpawn);
        parent.addChild(node);
    }

    private void playerSpawn(PlayerSpawnEvent playerSpawnEvent) {
        Player player = playerSpawnEvent.getPlayer();
        LuckPermsAPI.luckPermsAPI().setDisplayName(LuckPermsAPI.luckPermsAPI().getUser(player.getUuid()));
    }

    private void playerChat(PlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();

        var prefixOptional = LuckPermsAPI.luckPermsAPI().getPlayerPrefix(player.getUuid());
        if(prefixOptional.isEmpty()) return;
        var prefix = prefixOptional.get();

        String displayName = prefix + player.getUsername();
        PlayerSkin skin = player.getSkin();
        Component head = settings.chatHeadEnabled()
                ? PlayerHeads.of(player.getUuid(), player.getUsername(), skin == null ? null : skin.textures(), skin == null ? null : skin.signature())
                : null;
        Component message = ChatMessageParser.parse(playerChatEvent.getRawMessage(), node -> LuckPermsAPI.luckPermsAPI().hasPermission(player.getUuid(), node));
        playerChatEvent.setFormattedMessage(ChatLine.compose(head, MiniMessage.miniMessage().deserialize(displayName), message));
    }

    public void terminate() {
        LuckPermsAPI.luckPermsAPI().unsubscribeEvents();
        if (node != null) {
            parent.removeChild(node);
            node = null;
        }
        if (service != null) {
            service.removeCreatedTeams();
            service = null;
        }
    }
}
