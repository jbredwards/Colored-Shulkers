package git.jbredwards.colored_shulkers.asm;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.registry.RainbowShulkerBox;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author jbred
 *
 */
public final class ASMHandler implements IFMLLoadingPlugin
{
    @Nonnull
    @Override
    public String[] getASMTransformerClass() { return new String[] {"git.jbredwards.colored_shulkers.asm.ASMHandler$Transformer"}; }

    @SuppressWarnings("unused")
    public static final class Transformer implements IClassTransformer
    {
        @Nullable
        @Override
        public byte[] transform(@Nullable final String name, @Nullable final String transformedName, @Nullable final byte[] basicClass) {
            if(basicClass == null) return null;
            else if("net.minecraft.block.BlockShulkerBox".equals(transformedName)) {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                methods:
                for(@Nonnull final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getBlockByColor" : "func_190952_a")) {
                        for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn instanceof FieldInsnNode && ((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "PURPLE_SHULKER_BOX" : "field_190987_dv")) {
                                method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/colored_shulkers/asm/ASMHandler$Hooks", "getPurpleShulkerBox", "(Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;", false));
                                break methods;
                            }
                        }
                    }
                }

                @Nonnull final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }
            else if("net.minecraft.client.renderer.entity.RenderShulker".equals(transformedName)) {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                classNode.methods.removeIf(method -> method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "renderModel" : "func_77036_a"));

                @Nonnull final MethodNode generic = new MethodNode(Opcodes.ACC_PROTECTED | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, FMLLaunchHandler.isDeobfuscatedEnvironment() ? "renderModel" : "func_77036_a", "(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V", null, null);
                generic.visitVarInsn(Opcodes.ALOAD, 0);
                generic.visitVarInsn(Opcodes.ALOAD, 1);
                generic.visitTypeInsn(Opcodes.CHECKCAST, "net/minecraft/entity/monster/EntityShulker");
                generic.visitVarInsn(Opcodes.FLOAD, 2);
                generic.visitVarInsn(Opcodes.FLOAD, 3);
                generic.visitVarInsn(Opcodes.FLOAD, 4);
                generic.visitVarInsn(Opcodes.FLOAD, 5);
                generic.visitVarInsn(Opcodes.FLOAD, 6);
                generic.visitVarInsn(Opcodes.FLOAD, 7);
                generic.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/entity/RenderShulker", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "renderModel" : "func_77036_a", "(Lnet/minecraft/entity/monster/EntityShulker;FFFFFF)V", false);
                generic.visitInsn(Opcodes.RETURN);
                classNode.methods.add(generic);

                @Nonnull final MethodNode typed = new MethodNode(Opcodes.ACC_PROTECTED, FMLLaunchHandler.isDeobfuscatedEnvironment() ? "renderModel" : "func_77036_a", "(Lnet/minecraft/entity/monster/EntityShulker;FFFFFF)V", null, null);
                typed.visitVarInsn(Opcodes.ALOAD, 1);
                typed.visitMethodInsn(Opcodes.INVOKESTATIC, "git/jbredwards/colored_shulkers/asm/ASMHandler$Hooks", "applyRainbowColor", "(Lnet/minecraft/entity/monster/EntityShulker;)V", false);
                typed.visitVarInsn(Opcodes.ALOAD, 0);
                typed.visitVarInsn(Opcodes.ALOAD, 1);
                typed.visitVarInsn(Opcodes.FLOAD, 2);
                typed.visitVarInsn(Opcodes.FLOAD, 3);
                typed.visitVarInsn(Opcodes.FLOAD, 4);
                typed.visitVarInsn(Opcodes.FLOAD, 5);
                typed.visitVarInsn(Opcodes.FLOAD, 6);
                typed.visitVarInsn(Opcodes.FLOAD, 7);
                typed.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/client/renderer/entity/RenderLivingBase", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "renderModel" : "func_77036_a", "(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V", false);
                typed.visitInsn(Opcodes.FCONST_1);
                typed.visitInsn(Opcodes.FCONST_1);
                typed.visitInsn(Opcodes.FCONST_1);
                typed.visitMethodInsn(Opcodes.INVOKESTATIC, "net/minecraft/client/renderer/GlStateManager", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "color" : "func_179124_c", "(FFF)V", false);
                typed.visitInsn(Opcodes.RETURN);
                classNode.methods.add(typed);

                @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                return writer.toByteArray();
            }
            else if("net.minecraft.world.gen.structure.StructureEndCityPieces$CityTemplate".equals(transformedName)) {
                @Nonnull final ClassNode classNode = new ClassNode();
                new ClassReader(basicClass).accept(classNode, 0);
                methods:
                for(@Nonnull final MethodNode method : classNode.methods) {
                    if(method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "handleDataMarker" : "func_186175_a")) {
                        for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "spawnEntity" : "func_72838_d")) {
                                method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/colored_shulkers/asm/ASMHandler$Hooks", "applyRandomColor", "(Lnet/minecraft/entity/monster/EntityShulker;)V", false));
                                method.instructions.insert(insn, insn.getPrevious().clone(Collections.emptyMap()));
                                break methods;
                            }
                        }
                    }
                }

                @Nonnull final ClassWriter writer = new ClassWriter(0);
                classNode.accept(writer);
                return writer.toByteArray();
            }

            return basicClass;
        }
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static void applyRainbowColor(@Nonnull final EntityShulker shulker) {
            if(ShulkerUtils.isRainbow(shulker)) {
                final int rgb = RainbowShulkerBox.getRGB();
                GlStateManager.color((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f);
            }
            else if(shulker.getEntityData().getBoolean(ShulkerUtils.PURPLE_TAG)) {
                final int rgb = EnumDyeColor.PURPLE.getColorValue();
                GlStateManager.color((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f);
            }
        }

        public static void applyRandomColor(@Nonnull final EntityShulker shulker) {
            final long posSeed = MathHelper.getCoordinateRandom((int)shulker.posX, (int)shulker.posY, (int)shulker.posZ);
            ShulkerUtils.setRandomColor(shulker, new Random(posSeed ^ shulker.world.getSeed()), ColoredShulkersCfg.enableEndCity);
        }

        @Nonnull
        public static Block getPurpleShulkerBox(@Nonnull final Block oldBox) {
            try { if(ColoredShulkers.PURPLE_SHULKER_BOX != null) return ColoredShulkers.PURPLE_SHULKER_BOX; }
            catch(@Nonnull final Throwable ignored) {} // Should never error, but let's be safe.
            return oldBox;
        }
    }

    // ------
    // UNUSED
    // ------

    @Nullable
    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }

    @Override
    public void injectData(@Nonnull final Map<String, Object> data) {}
}
