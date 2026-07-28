package melonslise.locks.client.gui.sprite.action;

import melonslise.locks.client.gui.sprite.Sprite;

//暂停动作
@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public class WaitAction<S extends Sprite> extends TimedAction<S>
{
	public static <Z extends Sprite> WaitAction<Z> ticks(int ticks)
	{
		return (WaitAction<Z>) new WaitAction().time(ticks);
	}
}