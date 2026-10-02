package com.nubi;

import com.nubi.init.ModCreativeTabs;
import com.nubi.init.ModEntities;
import com.nubi.init.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NubiMod.MOD_ID)
public class NubiMod {
    public static final String MOD_ID = "nubi";

    public NubiMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModCreativeTabs.TABS.register(bus);
    }
}
