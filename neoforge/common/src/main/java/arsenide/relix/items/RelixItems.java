package arsenide.relix.items;

import java.util.EnumMap;
import java.util.List;

import arsenide.relix.Relix;
import arsenide.relix.sounds.RelixSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelixItems {
    // Create a Deferred Register to hold Items which will all be registered under the "relix" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Relix.MODID);

    public static final DeferredItem<Item> HIEROGLYPH_TABLET = ITEMS.registerSimpleItem("hieroglyph_tablet");

    public static final DeferredItem<Item> MUSIC_DISC_PHARAOH = ITEMS.registerSimpleItem(
        "pharaoh_disc",
        props -> props.stacksTo(1).rarity(Rarity.UNCOMMON).jukeboxPlayable(RelixSounds.PHARAOH_SONG)
    );

    public static final ResourceKey<EquipmentAsset> PHARAOH_HEADDRESS_ASSET = 
        ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Relix.MODID, "pharaoh_headdress")
        );

    public static final ArmorMaterial PHARAOH_HEADDRESS_MATERIAL = new ArmorMaterial(
        8,
        Util.make(new EnumMap<>(ArmorType.class), map -> {
            map.put(ArmorType.HELMET, 2);
        }),
        25,
        SoundEvents.ARMOR_EQUIP_GOLD,
        0,
        0,
        null,
        PHARAOH_HEADDRESS_ASSET
    );

    public static final ResourceKey<EquipmentAsset> SACRED_GOLD_ASSET = 
        ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Relix.MODID, "sacred_gold")
    );

    public static final ArmorMaterial SACRED_GOLD_MATERIAL = new ArmorMaterial(
        // Durability multiplier (gold = 7, iron = 15, diamond = 33)
        24,
        // Defense value (number of half-armors on bar)
        Util.make(new EnumMap<>(ArmorType.class), map -> {
            map.put(ArmorType.BOOTS, 2);
            map.put(ArmorType.LEGGINGS, 4);
            map.put(ArmorType.CHESTPLATE, 8);
            map.put(ArmorType.HELMET, 3);
            // Horse armor | wolf armor etc
            map.put(ArmorType.BODY, 6);
        }),
        // Enchantability (gold = 25)
        25,
        SoundEvents.ARMOR_EQUIP_GOLD,
        // Toughness
        0,
        // Knockback resistance
        0,
        // Tag for items to repair the armor with
        null,
        SACRED_GOLD_ASSET
    );

    public static final DeferredItem<Item> PHARAOH_HEADDRESS = 
        ITEMS.registerSimpleItem(
            "pharaoh_headdress",
            props -> props
                .humanoidArmor(PHARAOH_HEADDRESS_MATERIAL, ArmorType.HELMET)
                .rarity(Rarity.UNCOMMON)
        );

    public static final DeferredItem<Item> SACRED_GOLD_HELMET = 
        ITEMS.registerSimpleItem(
            "sacred_gold_helmet",
            props -> props.humanoidArmor(SACRED_GOLD_MATERIAL, ArmorType.HELMET)
        );

    public static final DeferredItem<Item> SACRED_GOLD_CHESTPLATE = 
        ITEMS.registerSimpleItem(
            "sacred_gold_chestplate",
            props -> props.humanoidArmor(
                SACRED_GOLD_MATERIAL, 
                ArmorType.CHESTPLATE
            )
        );

    public static final DeferredItem<Item> SACRED_GOLD_LEGGINGS =
        ITEMS.registerSimpleItem(
            "sacred_gold_pants",
            props -> props.humanoidArmor(
                SACRED_GOLD_MATERIAL,
                ArmorType.LEGGINGS
            )
        );

    public static final DeferredItem<Item> SACRED_GOLD_BOOTS =
        ITEMS.registerSimpleItem(
            "sacred_gold_boots",
            props -> props.humanoidArmor(
                SACRED_GOLD_MATERIAL,
                ArmorType.BOOTS
            )
        );

    public static final String SACRED_GOLD_TEMPLATE_LANG_KEY = 
        "item.relix.smithing_template.sacred_gold_upgrade";
    public static final DeferredItem<Item> SACRED_GOLD_UPGRADE_SMITHING_TEMPLATE =
        ITEMS.registerItem(
            "sacred_gold_upgrade_smithing_template",
            props -> new SmithingTemplateItem(
                Component.translatable(
                    SACRED_GOLD_TEMPLATE_LANG_KEY + ".applies_to"
                ).withStyle(ChatFormatting.BLUE),
                Component.translatable(
                    SACRED_GOLD_TEMPLATE_LANG_KEY + ".ingredients"
                ).withStyle(ChatFormatting.BLUE),
                Component.translatable(
                    SACRED_GOLD_TEMPLATE_LANG_KEY + ".base_slot_description"
                ), 
                Component.translatable(
                    SACRED_GOLD_TEMPLATE_LANG_KEY + ".additions_slot_description"
                ), 
                List.of(
                    Identifier.withDefaultNamespace("container/slot/helmet"),
                    Identifier.withDefaultNamespace("container/slot/chestplate"),
                    Identifier.withDefaultNamespace("container/slot/leggings"),
                    Identifier.withDefaultNamespace("container/slot/boots")
                ), 
                List.of(
                    Identifier.fromNamespaceAndPath(
                        Relix.MODID, 
                        "container/slot/sacred_gemstone"
                    )
                ),
                props
            ),
            props -> props.rarity(Rarity.UNCOMMON)
        );

    public static final DeferredItem<Item> SACRED_GEMSTONE = 
        ITEMS.registerSimpleItem(
            "sacred_gemstone",
            props -> props.rarity(Rarity.RARE)
        );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
