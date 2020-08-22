package mod.torchbowmod;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.jottyfan.minecraft.quickiefabric.blocks.QuickieBlocks;
import de.jottyfan.minecraft.quickiefabric.items.QuickieItems;
import de.jottyfan.minecraft.quickiefabric.tools.QuickieTools;

public class TorchBowMod implements ModInitializer {
    public static final String MODID = "torchbowmod";
    public static Logger LOGGER = LogManager.getLogger("TorchBowMod");
    public static Item torchbow = new TorchBow(Settings.of(Material.WOOD).defaultMaxDamage(384))
        .setIdentifier(new Identifier(MODID, "torchbow"));
    public static final ItemGroup torchBowModTab = FabricItemGroupBuilder.create(new Identifier(MODID, "all")).icon(() -> new ItemStack(torchbow))
  			.appendItems(stacks -> {
  				stacks.add(new ItemStack(torchbow));
  			}).build();


    @ObjectHolder("torchbandolier:torch_bandolier")
    public static Item torchbinder = null;
    @ObjectHolder("storagebox:storagebox")
    public static Item StorageBox = null;
    @ObjectHolder("ceilingtorch:torch")
    public static Block CeilingTorch = null;

    public static Item multiTorch = new Item(new Item.Properties()
            .group(torchBowModTab).maxStackSize(64))
            .setRegistryName(new ResourceLocation(MODID, "multitorch"));
    public static EntityType<EntityTorch> TORCH_ENTITY;

    public TorchBowMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::initClient);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void preInit(final FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void initClient(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(TORCH_ENTITY, RenderTorch::new);

        torchBowModTab.createIcon();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(torchbow, multiTorch);
            LOGGER.info("HELLO from Register Item");
        }

        @SubscribeEvent
        public static void registerEntityTypes(final RegistryEvent.Register<EntityType<?>> event) {
            TORCH_ENTITY = EntityType.Builder.<EntityTorch>create(EntityTorch::new, EntityClassification.MISC)
                    .setCustomClientFactory(EntityTorch::new)
                    .setTrackingRange(60)
                    .setUpdateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .size(0.5F, 0.5F)
                    .build(MODID + ":entitytorch");
            TORCH_ENTITY.setRegistryName(new ResourceLocation(MODID, "entitytorch"));
            event.getRegistry().register(TORCH_ENTITY);
        }
    }

		@Override
		public void onInitialize() {
			
		}

}

