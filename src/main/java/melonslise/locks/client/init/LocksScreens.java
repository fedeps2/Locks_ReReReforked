package melonslise.locks.client.init;

import melonslise.locks.client.gui.KeyRingScreen;
import melonslise.locks.client.gui.LockPickingScreen;
import melonslise.locks.common.init.LocksContainerTypes;
import net.minecraft.client.gui.screens.MenuScreens;

@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public final class LocksScreens
{
	private LocksScreens() {}

	public static void register()
	{
		MenuScreens.register(LocksContainerTypes.LOCK_PICKING_TYPE.get(), LockPickingScreen::new);
		MenuScreens.register(LocksContainerTypes.KEY_RING_TYPE.get(), KeyRingScreen::new);
	}
}