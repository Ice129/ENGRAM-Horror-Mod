package horror.blueice129.feature.house;

import horror.blueice129.utils.SurfaceFinder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
// import net.minecraft.block.Block;

public class HousePlacer {

    public static void placeHouse(int stage, BlockPos startPos, ServerWorld world) {
        String woodType = getWoodType(world, startPos);

        Identifier resourceId = new Identifier("horror-mod-129", "entityBase/house" + stage);
        // Load the NBT file and parse it to get block data

        
    }

    private static String getWoodType(ServerWorld world, BlockPos pos) {
        BlockPos[] trees = SurfaceFinder.findTreePositions(world, pos, 50);
        if (trees.length == 0) {
            return "oak"; // Default to oak if no trees are found
        }
        // dictionary to count types:
        java.util.Map<String, Integer> woodTypeCounts = new java.util.HashMap<>();
        for (BlockPos treePos : trees) {
            // log type:
            String block = world.getBlockState(treePos).getBlock().getTranslationKey();
            // count types and return the most common one
            String type = block.replace("block.minecraft.", "").replace("_log", "");
            woodTypeCounts.put(type, woodTypeCounts.getOrDefault(type, 0) + 1);
        }
        return woodTypeCounts.entrySet().stream().max(java.util.Map.Entry.comparingByValue()).get().getKey();
    }
}
