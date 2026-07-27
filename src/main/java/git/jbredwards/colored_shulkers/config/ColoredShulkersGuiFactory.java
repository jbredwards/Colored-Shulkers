package git.jbredwards.colored_shulkers.config;

import git.jbredwards.colored_shulkers.Tags;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.DefaultGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class ColoredShulkersGuiFactory extends DefaultGuiFactory
{
    public ColoredShulkersGuiFactory() {
        super(Tags.MOD_ID, Tags.MOD_NAME);
    }

    @Nonnull
    @Override
    public GuiScreen createConfigGui(@Nonnull final GuiScreen parentScreen) {
        return new GuiConfig(parentScreen, ConfigElement.from(ColoredShulkersCfg.class).getChildElements(), modid, false, false, title) {
            @Override
            public void initGui() {
                if(entryList == null || needsRefresh) {
                    entryList = new GuiConfigEntries(this, mc) {
                        @Override
                        protected void overlayBackground(final int startY, final int endY, final int startAlpha, final int endAlpha) {
                            if(isWorldRunning) drawGradientRect(left, endY, left + width, startY, -1072689136, -804253680);
                            else super.overlayBackground(startY, endY, startAlpha, endAlpha);
                        }

                        @Override
                        protected void drawContainerBackground(@Nonnull final Tessellator tessellator) {
                            if(isWorldRunning) drawGradientRect(right, bottom, left, top, -1072689136, -804253680);
                            else super.drawContainerBackground(tessellator);
                        }

                        @Override
                        protected void drawSelectionBox(final int insideLeft, final int insideTop, final int mouseXIn, final int mouseYIn, final float partialTicks) {
                            final double scaleH = mc.displayHeight / new ScaledResolution(mc).getScaledHeight_double();
                            GL11.glEnable(GL11.GL_SCISSOR_TEST);
                            GL11.glScissor(0, (int)(mc.displayHeight - (bottom * scaleH)), mc.displayWidth, (int)((bottom - top) * scaleH));
                            super.drawSelectionBox(insideLeft, insideTop, mouseXIn, mouseYIn, partialTicks);
                            GL11.glDisable(GL11.GL_SCISSOR_TEST);
                        }
                    };

                    needsRefresh = false;
                }

                super.initGui();
            }
        };
    }
}
