package git.jbredwards.colored_shulkers.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import vazkii.arl.network.NetworkHandler;
import vazkii.arl.network.NetworkMessage;
import vazkii.quark.api.capability.IEnchantColorProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
public final class QuarkHandler
{
    public static void init() {
        MinecraftForge.EVENT_BUS.register(QuarkHandler.class);
        NetworkHandler.register(MessageEnchant.class, Side.CLIENT);
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
                @Nonnull final ModelShulker model = (ModelShulker)renderer.getMainModel();

                final float texPos  = entity.ticksExisted + partialTicks;
                final int rbg = color.getEnchantEffectColor() != -1 ? color.getEnchantEffectColor() : Color.HSBtoRGB(texPos * 0.005f, 1, 0.6f);
                GlStateManager.color((rbg >> 16 & 0xFF) / 255f, (rbg >> 8 & 0xFF) / 255f, (rbg & 0xFF) / 255f, 1);

                model.base.isHidden = true;
                renderGlint(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, texPos);
                model.base.isHidden = false;

                model.lid.isHidden = true;
                renderGlint(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, texPos);
                model.lid.isHidden = false;
            }
        }

        void renderGlint(@Nonnull final EntityLivingBase entity, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale, final float texPos) {
            final float texScale = 1f / 3;

            renderer.bindTexture(ENCHANTED_ITEM_GLINT_RES);
            Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
            GlStateManager.pushMatrix();

            GlStateManager.enableBlend();
            GlStateManager.depthFunc(GL11.GL_EQUAL);
            GlStateManager.depthMask(false);
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

            GlStateManager.popMatrix();
            Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
        }

        @Override
        public boolean shouldCombineTextures() { return false; }

        @Override
        protected void initArmor() {}
    }

    // ---------------
    // Capability Data
    // ---------------

    @SubscribeEvent
    static void attachEnchantCapability(@Nonnull final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof EntityShulker) event.addCapability(new ResourceLocation("quark", "glint"), new EnchantCapability(event.getObject()));
    }

    public static final class EnchantCapability implements IEnchantColorProvider, ICapabilitySerializable<NBTBase>
    {
        int color;

        @Nonnull
        final Entity entity;
        public EnchantCapability(@Nonnull final Entity entityIn) { entity = entityIn; }

        @Override
        public int getEnchantEffectColor() { return color; }
        public void setColor(final int colorIn) {
            NetworkHandler.INSTANCE.sendToAllTracking(new MessageEnchant(colorIn, entity.getEntityId()), entity);
            color = colorIn;
        }

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

    // --------------------
    // Capability Data Sync
    // --------------------

    @SubscribeEvent
    static void syncEnchantCapability(@Nonnull final PlayerEvent.StartTracking event) {
        if(event.getTarget() instanceof EntityShulker) NetworkHandler.INSTANCE.sendTo(new MessageEnchant(event.getTarget()), (EntityPlayerMP)event.getEntityPlayer());
    }

    public static final class MessageEnchant extends NetworkMessage<MessageEnchant>
    {
        public int color, entityId;

        public MessageEnchant() {}
        public MessageEnchant(final int colorIn, final int entityIdIn) {
            color = colorIn;
            entityId = entityIdIn;
        }

        public MessageEnchant(@Nonnull final Entity entity) {
            this(Objects.requireNonNull(entity.getCapability(IEnchantColorProvider.CAPABILITY, null)).getEnchantEffectColor(), entity.getEntityId());
        }

        @Nullable
        @Override
        public IMessage handleMessage(@Nonnull final MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> Optional.ofNullable(Minecraft.getMinecraft().world)
                    .map(world -> world.getEntityByID(entityId))
                    .map(entity -> (EnchantCapability)entity.getCapability(IEnchantColorProvider.CAPABILITY, null))
                    .ifPresent(data -> data.color = color));

            return null;
        }
    }
}
