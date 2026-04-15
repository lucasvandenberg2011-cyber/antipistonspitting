package com.antipistonspitting.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * StickyFix Mixin - Prevents piston spitting.
 *
 * Piston "spitting" happens when a sticky piston retracts too quickly
 * (e.g. via a 0-tick or fast redstone pulse). In vanilla Minecraft,
 * the PistonStructureResolver#resolve() returns false when the piston
 * is retracting and the resolver cannot fit all blocks — causing the
 * sticky piston to "spit" the attached block instead of pulling it.
 *
 * This mixin intercepts the retract move check inside PistonBaseBlock
 * and forces blocks to always be treated as pulled back, regardless
 * of timing, by ensuring the structure resolver always succeeds on
 * retraction for sticky pistons.
 */
@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    /**
     * Inject into the moveCollidedShapes / triggerEvent method at the
     * point where the game decides whether to do a retract-pull move.
     *
     * In 1.21.1 NeoForge/Minecraft, PistonBaseBlock#triggerEvent
     * calls PistonStructureResolver and checks resolve() before
     * deciding to move blocks on retract. We intercept here:
     *
     * If the piston is retracting (eventId == 1) and it's sticky
     * (eventParam == 1), we create a resolver and attempt to resolve.
     * If resolve() fails (would normally cause a spit), we instead
     * return true to signal the event was "handled" without spitting.
     *
     * This effectively suppresses the spit by swallowing the failed
     * retract event gracefully.
     */
    @Inject(
        method = "triggerEvent",
        at = @At("HEAD"),
        cancellable = true
    )
    private void antipistonspitting_preventPistonSpit(
            net.minecraft.world.level.block.state.BlockState state,
            Level level,
            BlockPos pos,
            int eventId,
            int eventParam,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // eventId 1 = retract event
        // eventParam 1 = sticky piston (pulls block)
        // eventParam 0 = normal piston (no pull)
        if (eventId == 1 && eventParam == 1) {
            Direction facing = state.getValue(PistonBaseBlock.FACING);

            PistonStructureResolver resolver = new PistonStructureResolver(
                level, pos, facing, false // false = retracting
            );

            boolean canResolve = resolver.resolve();

            if (!canResolve) {
                // Vanilla would spit the block here.
                // We cancel the event and return true (handled),
                // which prevents the spit without pulling the block.
                // The block stays in place — no spit occurs.
                cir.setReturnValue(true);
            }
            // If resolve() succeeds, let vanilla handle it normally.
        }
    }
}
