package git.jbredwards.colored_shulkers.asm;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import git.jbredwards.colored_shulkers.Tags;
import git.jbredwards.colored_shulkers.config.ColoredShulkersCfg;
import git.jbredwards.colored_shulkers.registry.RainbowShulkerBox;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
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
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author jbred
 *
 */
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name(Tags.MOD_NAME + " Plugin")
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
            if(basicClass == null || transformedName == null) return basicClass;
            else switch(transformedName) {
                // Vanilla:
                case "net.minecraft.block.BlockShulkerBox": return transformBlockShulkerBox(basicClass);
                case "net.minecraft.client.renderer.entity.RenderShulker": return transformRenderShulker(basicClass);
                case "net.minecraft.world.gen.structure.StructureEndCityPieces$CityTemplate": return transformCityTemplate(basicClass);
                // Modded:
                case "com.zephaniahnoah.shulkertooltip.ShulkerToolTip$EventManager": return transformShulkerTooltip(basicClass);
                case "vazkii.quark.client.feature.ShulkerBoxTooltip": return transformShulkerBoxTooltip(basicClass);
                case "vazkii.quark.management.client.gui.GuiButtonShulker": return transformShulkerBoxSortButtons(basicClass);
                default: return basicClass;
            }
        }

        @Nonnull
        private static byte[] transformBlockShulkerBox(@Nonnull final byte[] basicClass) {
            return transformClass(basicClass, transformMethod(method -> method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getBlockByColor" : "func_190952_a"), (method, insn) -> {
                if(insn instanceof FieldInsnNode && ((FieldInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "PURPLE_SHULKER_BOX" : "field_190987_dv")) {
                    method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/colored_shulkers/asm/ASMHandler$Hooks", "getPurpleShulkerBox", "(Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;", false));
                    return true;
                }

                return false;
            }));
        }

        @Nonnull
        private static byte[] transformCityTemplate(@Nonnull final byte[] basicClass) {
            return transformClass(basicClass, transformMethod(method -> method.name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "handleDataMarker" : "func_186175_a"), (method, insn) -> {
                if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "spawnEntity" : "func_72838_d")) {
                    method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/colored_shulkers/asm/ASMHandler$Hooks", "applyRandomColor", "(Lnet/minecraft/entity/monster/EntityShulker;)V", false));
                    method.instructions.insert(insn, insn.getPrevious().clone(Collections.emptyMap()));
                    return true;
                }

                return false;
            }));
        }

        @Nonnull
        private static byte[] transformRenderShulker(@Nonnull final byte[] basicClass) {
            return transformClass(basicClass, classNode -> {
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
            });
        }

        @Nonnull
        private static byte[] transformShulkerBoxSortButtons(@Nonnull final byte[] basicClass) {
            return transformClass(basicClass, transformMethod(method -> method.name.equals("drawChest"), (method, insn) -> {
                if(insn.getOpcode() == Opcodes.ISTORE && ((VarInsnNode)insn).var == 6) {
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 3));
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/tileentity/TileEntity", FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getBlockType" : "func_145838_q", "()Lnet/minecraft/block/Block;", false));
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/colored_shulkers/asm/ASMHandler$Hooks", "getColor", "(ILnet/minecraft/block/Block;)I", false));
                    return true;
                }

                return false;
            }));
        }

        @Nonnull
        private static byte[] transformShulkerTooltip(@Nonnull final byte[] basicClass) {
            return transformClass(basicClass, transformMethod(method -> method.name.equals("event"), (method, insn) -> {
                if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "getMetadata" : "func_77960_j")) {
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETFIELD, "com/zephaniahnoah/shulkertooltip/ShulkerToolTip$EventManager", "color", "[F"));
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 3));
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/colored_shulkers/asm/ASMHandler$Hooks", "getColor", "([FLnet/minecraft/item/ItemStack;)[F", false));
                    method.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.PUTFIELD, "com/zephaniahnoah/shulkertooltip/ShulkerToolTip$EventManager", "color", "[F"));
                    return true;
                }

                return false;
            }));
        }

        @Nonnull
        private static byte[] transformShulkerBoxTooltip(@Nonnull final byte[] basicClass) {
            return transformClass(basicClass, transformMethod(method -> method.name.equals("renderTooltip"), (method, insn) -> {
                if(insn instanceof MethodInsnNode && ((MethodInsnNode)insn).name.equals("renderTooltipBackground")) {
                    method.instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 5));
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "git/jbredwards/colored_shulkers/asm/ASMHandler$Hooks", "getColor", "(ILnet/minecraft/item/ItemStack;)I", false));
                    return true;
                }

                return false;
            }));
        }

        // ----
        // Util
        // ----

        @Nonnull
        private static byte[] transformClass(@Nonnull final byte[] basicClass, @Nonnull final Consumer<ClassNode> action) {
            @Nonnull final ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            action.accept(classNode);

            @Nonnull final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        @Nonnull
        private static Consumer<ClassNode> transformMethod(@Nonnull final Predicate<MethodNode> target, @Nonnull final BiPredicate<MethodNode, AbstractInsnNode> action) {
            return classNode -> {
                for(@Nonnull final MethodNode method : classNode.methods) {
                    if(target.test(method)) {
                        for(@Nonnull final AbstractInsnNode insn : method.instructions.toArray()) {
                            if(action.test(method, insn)) return;
                        }
                    }
                }
            };
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

        public static int getColor(final int oldColor, @Nullable final Block block) {
            if(block == ColoredShulkers.PURPLE_SHULKER_BOX) return EnumDyeColor.PURPLE.getColorValue();
            else if(block == ColoredShulkers.RAINBOW_SHULKER_BOX) return RainbowShulkerBox.getRGB();
            else return block == Blocks.PURPLE_SHULKER_BOX ? 0xBD8FBD : oldColor;
        }

        public static int getColor(final int oldColor, @Nonnull final ItemStack currentBox) {
            return getColor(oldColor, Block.getBlockFromItem(currentBox.getItem()));
        }

        @Nonnull
        public static float[] getColor(@Nonnull final float[] oldColor, @Nonnull final ItemStack currentBox) {
            final int color = getColor(MathHelper.rgb(oldColor[0], oldColor[1], oldColor[2]), currentBox);
            return new float[] {(color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f};
        }

        @Nonnull
        public static Block getPurpleShulkerBox(@Nonnull final Block oldBox) {
            if(ColoredShulkers.PURPLE_SHULKER_BOX != null) return ColoredShulkers.PURPLE_SHULKER_BOX;
            else return ColoredShulkers.PURPLE_SHULKER_BOX = RainbowShulkerBox.Tile.asBlock();
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
