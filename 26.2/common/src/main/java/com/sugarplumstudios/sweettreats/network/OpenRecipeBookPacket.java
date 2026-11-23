package com.sugarplumstudios.sweettreats.network;

import com.sugarplumstudios.sweettreats.SweetTreats;
import com.sugarplumstudios.sweettreats.discovery.RecipeBookEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server -> client: open the recipe book with every recipe it shows and the player's discovered recipes.
 */
public record OpenRecipeBookPacket(Set<Identifier> discovered, boolean hideUndiscoveredIngredients,
                                   List<RecipeBookEntry> entries) implements CustomPacketPayload {
    public static final Type<OpenRecipeBookPacket> TYPE = new Type<>(SweetTreats.id("open_recipe_book"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRecipeBookPacket> STREAM_CODEC =
            StreamCodec.of((buffer, packet) -> packet.encode(buffer), OpenRecipeBookPacket::decode);

    private void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(discovered.size());
        discovered.forEach(buffer::writeIdentifier);
        buffer.writeBoolean(hideUndiscoveredIngredients);
        buffer.writeVarInt(entries.size());
        entries.forEach(entry -> entry.write(buffer));
    }

    private static OpenRecipeBookPacket decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Set<Identifier> discovered = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            discovered.add(buffer.readIdentifier());
        }
        boolean hide = buffer.readBoolean();
        int count = buffer.readVarInt();
        List<RecipeBookEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(RecipeBookEntry.read(buffer));
        }
        return new OpenRecipeBookPacket(discovered, hide, entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
