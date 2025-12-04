package net.george.milk.api;

import com.google.common.collect.ImmutableList;
import net.george.milk.MilkLib;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class Constants {
    public static final List<Identifier> DRIP_HANG = ImmutableList.of(Identifier.ofVanilla("drip_hang"));
    public static final List<Identifier> DRIP_FALL = ImmutableList.of(Identifier.ofVanilla("drip_fall"));
    public static final List<Identifier> SPLASH = ImmutableList.of(
            MilkLib.id("splash_0"), MilkLib.id("splash_1"), MilkLib.id("splash_2"), MilkLib.id("splash_3")
    );

    public static final Map<DripstoneInteractingFluid, ParticleTypeSet> FLUIDS_TO_PARTICLES = new HashMap<>();
    @ApiStatus.Internal
    public static final Set<DripstoneInteractingFluid> TO_REGISTER = new HashSet<>();
}
