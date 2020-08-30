package mod.torchbowmod;

import static mod.torchbowmod.TorchBowMod.CeilingTorch;
import static mod.torchbowmod.TorchBowMod.TORCH_ENTITY;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class EntityTorch extends ArrowEntity {

	public EntityTorch(EntityType<? extends EntityTorch> entityType, World world) {
		super(entityType, world);
	}

	public EntityTorch(World world, double x, double y, double z) {
		this(TORCH_ENTITY, world);
		this.updatePosition(x, y, z);
	}

	public EntityTorch(World worldIn, LivingEntity owner) {
		this(TORCH_ENTITY, worldIn);
		this.updatePosition(owner.getX(), owner.getEyeY() - 0.10000000149011612D, owner.getZ());
		this.setOwner(owner);
		if (owner instanceof PlayerEntity) {
			this.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		entity.setOnFireFor(100);
	}

	@Override
	protected void onBlockHit(BlockHitResult blockHitResult) {
		super.onBlockHit(blockHitResult);
		BlockHitResult.Type raytraceresultType = blockHitResult.getType();
		if (raytraceresultType == BlockHitResult.Type.BLOCK) {
			BlockState blockstate = this.world.getBlockState(blockHitResult.getBlockPos());
			setTorch(blockHitResult, blockstate, raytraceresultType);
		}
	}

	@Override
	public Packet<?> createSpawnPacket() {
		Entity entity = this.getOwner();
		return new EntitySpawnS2CPacket(this, entity == null ? 0 : entity.getEntityId());
	}

	private void setTorch(BlockHitResult blockHitResult, BlockState blockstate, BlockHitResult.Type raytraceResultIn) {
		BlockPos blockpos = blockHitResult.getBlockPos();
		if (!blockstate.isAir()) {
			if (!world.isClient) {
				Direction face = blockHitResult.getSide();
				BlockState torchState = Blocks.WALL_TORCH.getDefaultState();
				BlockPos setBlockPos = getPosOfFace(blockpos, face);
				if (isBlockAIR(setBlockPos)) {
					if (face == Direction.UP) {
						torchState = Blocks.TORCH.getDefaultState();
						world.setBlockState(setBlockPos, torchState);
					} else if (face == Direction.DOWN && CeilingTorch != null) {
						BlockState ceilingTorch = CeilingTorch.getDefaultState();
						world.setBlockState(setBlockPos, ceilingTorch);
					} else if (face != Direction.DOWN) {
//						world.setBlockState(setBlockPos, torch_state.with(Property.HORIZONTAL_FACING, face));
						world.setBlockState(setBlockPos, torchState);
					}
					this.setDead();
				}
			}
		}
	}

	private BlockPos getPosOfFace(BlockPos blockPos, Direction face) {
		switch (face) {
		case UP:
			return blockPos.up();
		case EAST:
			return blockPos.east();
		case WEST:
			return blockPos.west();
		case SOUTH:
			return blockPos.south();
		case NORTH:
			return blockPos.north();
		case DOWN:
			return blockPos.down();
		}
		return blockPos;
	}

	private void setDead() {
		this.remove();
	}

	@Override
	protected ItemStack asItemStack() {
		return new ItemStack(Blocks.TORCH);
	}

	private boolean isBlockAIR(BlockPos pos) {
		Block block = this.world.getBlockState(pos).getBlock();
		Set<Block> airLike = Set.of(Blocks.CAVE_AIR, Blocks.AIR, Blocks.SNOW, Blocks.VINE, Blocks.DEAD_BUSH,
				Blocks.POTTED_DEAD_BUSH, Blocks.ROSE_BUSH, Blocks.SWEET_BERRY_BUSH);
		return airLike.contains(block);
	}
}