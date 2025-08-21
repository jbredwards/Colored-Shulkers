package git.jbredwards.colored_shulkers.asm;

import git.jbredwards.colored_shulkers.ColoredShulkers;
import git.jbredwards.colored_shulkers.ShulkerUtils;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

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
            if(basicClass != null && "net.minecraft.world.gen.structure.StructureEndCityPieces$CityTemplate".equals(transformedName)) {
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
        @Nonnull
        public static EntityShulker applyRandomColor(@Nonnull final EntityShulker shulker) {
            if(ColoredShulkers.Cfg.enableEndCity) {
                final long rng = MathHelper.getCoordinateRandom((int)shulker.posX, (int)shulker.posY, (int)shulker.posZ);
                final int weight = (int)(rng % WeightedRandom.getTotalWeight(ColoredShulkers.Cfg.WEIGHTS));

                ShulkerUtils.setColor(shulker, WeightedRandom.getRandomItem(ColoredShulkers.Cfg.WEIGHTS, weight).color);
            }

            return shulker;
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
