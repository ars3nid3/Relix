package arsenide.relix.items;

import java.util.EnumMap;
import java.util.List;

import arsenide.relix.Relix;
import arsenide.relix.sounds.RelixSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelixItems {
    // Create a Deferred Register to hold Items which will all be registered under the "relix" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Relix.MODID);

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = 
        DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, Relix.MODID);

    public static final DeferredItem<Item> HIEROGLYPH_TABLET = ITEMS.registerSimpleItem("hieroglyph_tablet");

    public static final DeferredItem<Item> MUSIC_DISC_PHARAOH = ITEMS.registerSimpleItem(
        "pharaoh_disc",
        new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).jukeboxPlayable(RelixSounds.PHARAOH_SONG)
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PHARAOH_HEADDRESS_MATERIAL = ARMOR_MATERIALS.register(
        "pharaoh_headdress",
        () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 2);
            }),
            25,
            SoundEvents.ARMOR_EQUIP_GOLD,
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(Relix.MODID, "pharaoh_headdress")
            )),
            0.0F,
            0.0F
        ));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> SACRED_GOLD_MATERIAL = ARMOR_MATERIALS.register(
        "sacred_gold",
        () -> new ArmorMaterial(
            // Defense value (number of half-armors on bar)
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
                // Horse armor | wolf armor etc
                map.put(ArmorItem.Type.BODY, 6);
            }),
            // Enchantability (gold = 25)
            25,
            SoundEvents.ARMOR_EQUIP_GOLD,
            // Toughness
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(Relix.MODID, "sacred_gold")
            )),
            // Knockback resistance
            0.0F,
            0.0F
        ));

    public static final DeferredItem<Item> PHARAOH_HEADDRESS = 
        ITEMS.registerItem(
            "pharaoh_headdress",
            props -> new ArmorItem(
                PHARAOH_HEADDRESS_MATERIAL,
                ArmorItem.Type.HELMET,
                props
            ),
            new Item.Properties().rarity(Rarity.UNCOMMON)
        );

    public static final DeferredItem<Item> SACRED_GOLD_HELMET = 
        ITEMS.registerItem(
            "sacred_gold_helmet",
            props -> new ArmorItem(
                SACRED_GOLD_MATERIAL,
                ArmorItem.Type.HELMET,
                props
            ),
            new Item.Properties()
        );

    public static final DeferredItem<Item> SACRED_GOLD_CHESTPLATE = 
        ITEMS.registerItem(
            "sacred_gold_chestplate",
            props -> new ArmorItem(
                SACRED_GOLD_MATERIAL,
                ArmorItem.Type.CHESTPLATE,
                props
            ),
            new Item.Properties()
        );

    public static final DeferredItem<Item> SACRED_GOLD_LEGGINGS =
        ITEMS.registerItem(
            "sacred_gold_pants",
            props -> new ArmorItem(
                SACRED_GOLD_MATERIAL,
                ArmorItem.Type.LEGGINGS,
                props
            ),
            new Item.Properties()
        );

    public static final DeferredItem<Item> SACRED_GOLD_BOOTS =
        ITEMS.registerItem(
            "sacred_gold_boots",
            props -> new ArmorItem(
                SACRED_GOLD_MATERIAL,
                ArmorItem.Type.BOOTS,
                props
            ),
            new Item.Properties()
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
                    SACRED_GOLD_TEMPLATE_LANG_KEY + ".upgrade_description"
                ),
                Component.translatable(
                    SACRED_GOLD_TEMPLATE_LANG_KEY + ".base_slot_description"
                ), 
                Component.translatable(
                    SACRED_GOLD_TEMPLATE_LANG_KEY + ".additions_slot_description"
                ), 
                List.of(
                    ResourceLocation.withDefaultNamespace("container/slot/helmet"),
                    ResourceLocation.withDefaultNamespace("container/slot/chestplate"),
                    ResourceLocation.withDefaultNamespace("container/slot/leggings"),
                    ResourceLocation.withDefaultNamespace("container/slot/boots")
                ), 
                List.of(
                    ResourceLocation.fromNamespaceAndPath(
                        Relix.MODID, 
                        "container/slot/sacred_gemstone"
                    )
                )
            ),
            new Item.Properties().rarity(Rarity.UNCOMMON)
        );

    public static final DeferredItem<Item> SACRED_GEMSTONE = 
        ITEMS.registerSimpleItem(
            "sacred_gemstone",
            new Item.Properties().rarity(Rarity.RARE)
        );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
