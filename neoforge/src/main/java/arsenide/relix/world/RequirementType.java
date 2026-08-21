package arsenide.relix.world;

import com.mojang.serialization.Codec;

public enum RequirementType {
    DAY_PHASE("phase"),
    BLOCK_INTERACT("block"),
    ITEM_INTERACT("item"),
    HELD_ITEM_OFFHAND("offhand"),
    HELD_ITEM_HEAD("head"),
    HELD_ITEM_CHEST("chest"),
    HELD_ITEM_PANTS("pants"),
    HELD_ITEM_BOOTS("boots"),
    BIOME("biome");

    private final String prefix;

    RequirementType(String prefix) {
        this.prefix = prefix;
    }

    public static RequirementType fromPrefix(String prefix) {
        for (RequirementType type : values()) {
            if (type.prefix.equals(prefix)) {
                return type;
            }
        }
        throw new IllegalArgumentException(
            "Unknown requirement type: " + prefix
        );
    }

    public String prefix() {
        return prefix;
    }

    public static final Codec<RequirementType> CODEC = 
        Codec.STRING.xmap(
            RequirementType::fromPrefix, 
            RequirementType::prefix
        );
}
