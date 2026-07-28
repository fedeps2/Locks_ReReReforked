package melonslise.locks.client.gui;

import melonslise.locks.common.network.toserver.RenameKeyPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class RenameKeyScreen extends Screen {

	private static final Component TITLE = Component.translatable("locks.gui.rename_key");
	private static final int WIDTH = 200;
	private static final int HEIGHT = 60;

	private final ItemStack keyStack;
	private EditBox nameField;

	public RenameKeyScreen(ItemStack keyStack) {
		super(TITLE);
		this.keyStack = keyStack;
	}

	@Override
	protected void init() {
		super.init();
		int centerX = this.width / 2;
		int centerY = this.height / 2;

		this.nameField = new EditBox(this.font, centerX - WIDTH / 2 + 5, centerY - 10, WIDTH - 10, 20, Component.empty());
		this.nameField.setMaxLength(50);
		this.nameField.setFocused(true);

		String currentName = null;
		var customName = keyStack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
		if (customName != null)
			currentName = customName.getString();
		if (currentName != null)
			this.nameField.setValue(currentName);

		this.addRenderableWidget(this.nameField);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			sendName();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
			return true;
		}
		return this.nameField.keyPressed(keyCode, scanCode, modifiers);
	}

	private void sendName() {
		String name = this.nameField.getValue().trim();
		PacketDistributor.sendToServer(new RenameKeyPacket(name));
		this.onClose();
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		int centerX = this.width / 2;
		int centerY = this.height / 2;

		guiGraphics.fill(centerX - WIDTH / 2, centerY - 20, centerX + WIDTH / 2, centerY + 30, 0x88000000);
		guiGraphics.drawString(this.font, TITLE, centerX - WIDTH / 2 + 5, centerY - 30, 0xFFFFFF);
	}
}
