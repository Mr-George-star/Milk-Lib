package net.george.milk;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.george.milk.api.DrippableFluidManager;

public class MilkLibDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider((output, registriesFuture) ->
                DrippableFluidManager.getInstance().createProvider(MilkLib.MOD_ID, output)
        );
    }
}
