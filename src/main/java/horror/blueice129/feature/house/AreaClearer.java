package horror.blueice129.feature.house;

import horror.blueice129.utils.SurfaceFinder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class AreaClearer {

    private static final int CLEAR_RADIUS = 20;
    private static final int TORCH_RADIUS = 22;

    public static void clearArea(ServerWorld world, BlockPos center) {
        BlockPos[] trees = SurfaceFinder.findTreePositions(world, center, CLEAR_RADIUS);
        for (BlockPos treeBase : trees) {
            for (BlockPos logPos : SurfaceFinder.getTreeLogPositions(world, treeBase)) {
                world.setBlockState(logPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
        }

        // TODO: scan each column within CLEAR_RADIUS for leaves, tall grass, and flowers and remove them
    }

    public static void placeTorches(ServerWorld world, BlockPos center) {
        // TODO: walk the perimeter at TORCH_RADIUS, find solid surface positions, place torches on top
    }
}
