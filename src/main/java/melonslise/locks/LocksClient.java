package melonslise.locks;

import melonslise.locks.client.init.LocksItemModelsProperties;
import melonslise.locks.client.init.LocksNetworkClient;
import melonslise.locks.client.init.LocksScreens;
import melonslise.locks.common.config.LocksClientConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Locks.ID, dist = Dist.CLIENT)
public class LocksClient {
    public LocksClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::onClientSetup);
        LocksNetworkClient.register(modBus);
        container.registerConfig(ModConfig.Type.CLIENT, LocksClientConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(melonslise.locks.client.util.LocksClient::onScreenKeyPressed);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(LocksScreens::register);
        event.enqueueWork(LocksItemModelsProperties::register);
    }
}
