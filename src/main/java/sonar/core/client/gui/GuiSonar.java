package sonar.core.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.SonarCore;
import sonar.core.api.machines.IPausable;
import sonar.core.api.machines.IProcessMachine;
import sonar.core.helpers.FontHelper;
import sonar.core.network.PacketByteBuf;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.upgrades.UpgradeInventory;
import sonar.core.utils.IWorldPosition;

public abstract class GuiSonar extends GuiContainer {

	public IWorldPosition entity;
	protected List<SonarTextField> fieldList = Lists.<SonarTextField>newArrayList();

	public GuiSonar(Container container, IWorldPosition entity) {
		super(container);
		if (entity != null)
			this.entity = entity;
	}

	public abstract ResourceLocation getBackground();

	public void reset() {
		this.buttonList.clear();
		this.fieldList.clear();
		this.initGui();
	}

	public void initButtons() {
		this.buttonList.clear();
	}

	// public void initGui(boolean pause) {}

	public void setZLevel(float zLevel) {
		this.zLevel = zLevel;
	}

	public void drawNormalToolTip(ItemStack stack, int x, int y) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		this.renderToolTip(stack, x - guiLeft, y - guiTop);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
	}

	public void drawSpecialToolTip(List list, int x, int y, FontRenderer font) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		drawHoveringText(list, x - guiLeft, y - guiTop, (font == null ? fontRendererObj : font));
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
	}

	public static void drawTransparentRect(int left, int top, int right, int bottom, int color) {
		if (left < right) {
			int i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			int j = top;
			top = bottom;
			bottom = j;
		}

		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.color(f, f1, f2, f3);
		GlStateManager.disableTexture2D();
		OpenGlHelper.glBlendFunc(770, 1, 1, 0);
		GlStateManager.color(f, f1, f2, f3);
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
		vertexbuffer.pos((double) left, (double) bottom, 0.0D).endVertex();
		vertexbuffer.pos((double) right, (double) bottom, 0.0D).endVertex();
		vertexbuffer.pos((double) right, (double) top, 0.0D).endVertex();
		vertexbuffer.pos((double) left, (double) top, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	protected void drawGuiContainerForegroundLayer(int x, int y) {
		fieldList.forEach(field -> field.drawTextBox());
		RenderHelper.disableStandardItemLighting();
		Iterator iterator = this.buttonList.iterator();

		while (iterator.hasNext()) {
			GuiButton guibutton = (GuiButton) iterator.next();

			if (guibutton.isMouseOver()) {
				guibutton.drawButtonForegroundLayer(x - this.guiLeft, y - this.guiTop);
				break;
			}
		}
		RenderHelper.enableGUIStandardItemLighting();

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(this.getBackground());
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	public void bindTexture(ResourceLocation resource) {
		mc.getTextureManager().bindTexture(resource);
	}

	public void drawSonarCreativeTabHoveringText(String tabName, int mouseX, int mouseY) {
		drawCreativeTabHoveringText(tabName, mouseX, mouseY);
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		for (SonarTextField field : fieldList) {
			if (field.isFocused()) {
				if (c == 13 || c == 27) {
					field.setFocused(false);
				} else {
					field.textboxKeyTyped(c, i);
					onTextFieldChanged(field);
				}
				return;
			}
		}
		super.keyTyped(c, i);
	}

	public void onTextFieldChanged(SonarTextField field) {}

	public SonarTextField getFocusedField() {
		for (SonarTextField f : fieldList) {
			if (f.isFocused()) {
				return f;
			}
		}
		return null;

	}

	@Override
	public void mouseClicked(int i, int j, int k) throws IOException {
		super.mouseClicked(i, j, k);
		fieldList.forEach(field -> field.mouseClicked(i - guiLeft, j - guiTop, k));
	}

	public int getGuiLeft() {
		return guiLeft;
	}

	public int getGuiTop() {
		return guiTop;
	}

	@SideOnly(Side.CLIENT)
	public static class PauseButton extends SonarButtons.ImageButton {

		boolean paused;
		public int id;
		public IPausable machine;
		public GuiSonar gui;

		public PauseButton(GuiSonar gui, IPausable machine, int id, int x, int y, boolean paused) {
			super(id, x, y, new ResourceLocation("Calculator:textures/gui/buttons/buttons.png"), paused ? 51 : 34, 0, 16, 16);
			this.gui = gui;
			this.paused = paused;
			this.id = id;
			this.machine = machine;
		}

		public void drawButtonForegroundLayer(int x, int y) {
			ArrayList list = Lists.newArrayList();
			list.add(TextFormatting.BLUE + "" + TextFormatting.UNDERLINE + (paused ? FontHelper.translate("buttons.resume") : FontHelper.translate("buttons.pause")));
			if (machine instanceof IProcessMachine) {
				list.add("Current: " + (int) ((double) ((IProcessMachine) machine).getCurrentProcessTime() / ((IProcessMachine) machine).getProcessTime() * 100) + " %");
			}
			gui.drawHoveringText(list, x, y, gui.fontRendererObj);
		}

		@Override
		public void onClicked() {
			SonarCore.network.sendToServer(new PacketByteBuf((IByteBufTile) gui.entity, gui.entity.getCoords().getBlockPos(), id));
			gui.buttonList.clear();
			gui.initGui();
		}
	}

	@SideOnly(Side.CLIENT)
	public class CircuitButton extends SonarButtons.ImageButton {
		public int id;
		public UpgradeInventory upgrades;

		public CircuitButton(UpgradeInventory upgrades, int id, int x, int y) {
			super(id, x, y, new ResourceLocation("Calculator:textures/gui/buttons/buttons.png"), 0, 0, 16, 16);
			this.upgrades = upgrades;
			this.id = id;
		}

		public void drawButtonForegroundLayer(int x, int y) {
			ArrayList list = Lists.newArrayList();
			list.add(TextFormatting.BLUE + "" + TextFormatting.UNDERLINE + FontHelper.translate("buttons.circuits"));
			for (Entry<String, Integer> entry : upgrades.getInstalledUpgrades().entrySet()) {
				int max = upgrades.maxUpgrades.get(entry.getKey());
				list.add(entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().toLowerCase().substring(1) + ": " + entry.getValue() + "/" + max);
			}
			drawHoveringText(list, x, y, fontRendererObj);
		}

		@Override
		public void onClicked() {
			SonarCore.network.sendToServer(new PacketByteBuf((IByteBufTile) entity, entity.getCoords().getBlockPos(), id));
		}
	}

}