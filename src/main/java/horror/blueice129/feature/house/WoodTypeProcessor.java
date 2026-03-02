package horror.blueice129.feature.house;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.HashMap;
import java.util.Map;

public class WoodTypeProcessor extends StructureProcessor {

    private final Map<Block, Block> replacements;

    public WoodTypeProcessor(String woodType) {
        this.replacements = woodType.equals("birch") ? Map.of() : buildReplacementMap(woodType);
    }

    @Override
    public StructureTemplate.StructureBlockInfo process(
            WorldView world,
            BlockPos pos,
            BlockPos pivot,
            StructureTemplate.StructureBlockInfo original,
            StructureTemplate.StructureBlockInfo current,
            StructurePlacementData data) {

        Block replacement = replacements.get(current.state().getBlock());
        if (replacement == null) return current;

        BlockState newState = copyMatchingProperties(replacement.getDefaultState(), current.state());
        return new StructureTemplate.StructureBlockInfo(current.pos(), newState, current.nbt());
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.NOP;
    }

    private static BlockState copyMatchingProperties(BlockState target, BlockState source) {
        for (Property<?> prop : source.getProperties()) {
            if (target.contains(prop)) {
                target = copyProperty(target, source, prop);
            }
        }
        return target;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState target, BlockState source, Property<T> prop) {
        return target.with(prop, source.get(prop));
    }

    private static Map<Block, Block> buildReplacementMap(String type) {
        Map<Block, Block> map = new HashMap<>();

        String[] simpleSuffixes = {
            "log", "wood", "planks", "slab", "stairs", "fence", "fence_gate",
            "door", "trapdoor", "sign", "wall_sign", "pressure_plate", "button"
        };
        for (String suffix : simpleSuffixes) {
            addReplacement(map, "birch_" + suffix, type + "_" + suffix);
        }

        addReplacement(map, "stripped_birch_log", "stripped_" + type + "_log");
        addReplacement(map, "stripped_birch_wood", "stripped_" + type + "_wood");

        return map;
    }

    private static void addReplacement(Map<Block, Block> map, String from, String to) {
        Block fromBlock = Registries.BLOCK.get(new Identifier(from));
        Block toBlock = Registries.BLOCK.get(new Identifier(to));
        // Registries.BLOCK.get() returns air for unknown identifiers
        if (fromBlock != toBlock) {
            map.put(fromBlock, toBlock);
        }
    }
}
