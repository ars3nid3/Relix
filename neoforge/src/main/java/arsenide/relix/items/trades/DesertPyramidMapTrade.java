package arsenide.relix.items.trades;

import java.util.Optional;

import javax.annotation.Nullable;

import arsenide.relix.Relix;
import arsenide.relix.world.RelixMapDecorationTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class DesertPyramidMapTrade implements ItemListing {

    public static final TagKey<Structure> DESTINATION = TagKey.create(
        Registries.STRUCTURE,
        ResourceLocation.fromNamespaceAndPath(Relix.MODID, "on_desert_pyramid_explorer_maps")
    );

    private static final int EMERALD_COST = 8;
    private static final int MAX_USES = 12;
    private static final int VILLAGER_XP = 5;
    private static final float PRICE_MULTIPLIER = 0.2F;
    private static final int SEARCH_RADIUS = 100;
    private static final byte ZOOM = 2;

    @Override
    public @Nullable MerchantOffer getOffer(Entity trader, RandomSource random) {
        if (!(trader.level() instanceof ServerLevel level)) return null;
        BlockPos structurePos = level.findNearestMapStructure(
            DESTINATION, 
            trader.blockPosition(),
            SEARCH_RADIUS,
            true
        );
        // No pyramid found: don't offer the trade
        if (structurePos == null) return null;
        ItemStack map = MapItem.create(
            level,
            structurePos.getX(),
            structurePos.getY(),
            ZOOM,
            true,
            true
        );
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(
            map, 
            structurePos, 
            "+", 
            RelixMapDecorationTypes.DESERT_PYRAMID
        );
        map.set(
            DataComponents.ITEM_NAME,
            Component.translatable("filled_map.explorer_desert")
        );
        CompoundTag customData = new CompoundTag();
        customData.putBoolean("relix_treasure_map", true);
        map.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));

        return new MerchantOffer(
            new ItemCost(Items.EMERALD, EMERALD_COST),
            Optional.of(new ItemCost(Items.COMPASS)),
            map,
            MAX_USES,
            VILLAGER_XP,
            PRICE_MULTIPLIER
        );
    }
    
}
