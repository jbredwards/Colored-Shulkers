package git.jbredwards.colored_shulkers.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import vazkii.quark.api.capability.IEnchantColorProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 *
 * @author jbred
 *
 */
public final class QuarkHandler
{
    public static void init() {
        MinecraftForge.EVENT_BUS.register(QuarkHandler.class);
        if(FMLCommonHandler.instance().getSide().isClient()) initClient();
    }

    // ---------
    // Rendering
    // ---------

    @SideOnly(Side.CLIENT)
    static void initClient() {
        @Nullable final Render<EntityShulker> renderer = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(EntityShulker.class);
        if(renderer instanceof RenderLivingBase) ((RenderLivingBase<EntityShulker>)renderer).addLayer(new EnchantLayer((RenderLivingBase<EntityShulker>)renderer));
    }

    @SideOnly(Side.CLIENT)
    public static class EnchantLayer extends LayerBipedArmor
    {
        @Nonnull
        protected final RenderLivingBase<?> renderer;
        public EnchantLayer(@Nonnull final RenderLivingBase<?> rendererIn) {
            super(rendererIn);
            renderer = rendererIn;
        }

        @Override
        public void doRenderLayer(@Nonnull final EntityLivingBase entity, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
            @Nullable final IEnchantColorProvider color = entity.getCapability(IEnchantColorProvider.CAPABILITY, null);
            if(color != null && color.getEnchantEffectColor() != 0) {
                final float texPos = entity.ticksExisted + partialTicks;
                final float texScale = 1f / 3;

                renderer.bindTexture(ENCHANTED_ITEM_GLINT_RES);
                Minecraft.getMinecraft().entityRenderer.setupFogColor(true);

                GlStateManager.enableBlend();
                GlStateManager.depthFunc(GL11.GL_EQUAL);
                GlStateManager.depthMask(false);

                final int rbg = color.getEnchantEffectColor() != -1 ? color.getEnchantEffectColor() : Color.HSBtoRGB(texPos * 0.005f, 1, 0.6f);
                GlStateManager.color((rbg >> 16 & 0xFF) / 255f, (rbg >> 8 & 0xFF) / 255f, (rbg & 0xFF) / 255f, 1);
                GlStateManager.disableLighting();

                for(int layer = 0; layer < 2; layer++) {
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
                    GlStateManager.matrixMode(GL11.GL_TEXTURE);
                    GlStateManager.loadIdentity();

                    GlStateManager.scale(texScale, texScale, texScale);
                    GlStateManager.rotate(30 - layer * 60, 0, 0, 1);
                    GlStateManager.translate(0, texPos * (0.001f + layer * 0.003f) * 20, 0);

                    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                    renderer.getMainModel().render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

                    GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                }

                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.enableLighting();

                GlStateManager.depthMask(true);
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
                GlStateManager.disableBlend();

                Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
            }
        }

        @Override
        public boolean shouldCombineTextures() { return true; }

        @Override
        protected void initArmor() {}
    }

    // ---------------
    // Capability Data
    // ---------------

    @SubscribeEvent
    static void attackEnchantCapability(@Nonnull final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof EntityShulker) event.addCapability(new ResourceLocation("quark", "glint"), new EnchantCapability());
    }

    public static final class EnchantCapability implements IEnchantColorProvider, ICapabilitySerializable<NBTBase>
    {
        public int color;

        @Override
        public int getEnchantEffectColor() { return color; }

        @Override
        public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
            return capability == CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
            return capability == CAPABILITY ? CAPABILITY.cast(this) : null;
        }

        @Nonnull
        @Override
        public NBTBase serializeNBT() {
            @Nonnull final NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("Color", color);
            return nbt;
        }

        @Override
        public void deserializeNBT(@Nonnull final NBTBase nbt) { color = ((NBTTagCompound)nbt).getInteger("Color"); }
    }
}
