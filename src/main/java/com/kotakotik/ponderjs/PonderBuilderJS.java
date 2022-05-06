package com.kotakotik.ponderjs;

import com.kotakotik.ponderjs.api.AbstractPonderBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderStoryBoardEntry;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.repack.registrate.util.entry.ItemProviderEntry;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class PonderBuilderJS extends AbstractPonderBuilder<PonderBuilderJS> {
    public static final String BASIC_STRUCTURE = "ponderjs:basic";

    public PonderBuilderJS(String name, Set<Item> items) {
        super(PonderJS.appendKubeToId(name), items);
        String namespace = this.name.getNamespace();
        if (!PonderJS.namespaces.contains(namespace)) {
            PonderJS.namespaces.add(namespace);
        }
    }

    public static HashMap<String, PonderStoryBoardEntry.PonderStoryBoard> scenes = new HashMap<>();


    public PonderBuilderJS scene(String name, String title, PonderStoryBoardEntry.PonderStoryBoard scene) {
        return scene(name, title, BASIC_STRUCTURE, scene);
    }

    public PonderBuilderJS scene(String name, String title, String structureName, PonderStoryBoardEntry.PonderStoryBoard scene) {
        String fullName = createSceneId(name);
        if (PonderJS.isInitialized() && !scenes.containsKey(fullName)) {
            ScriptType.CLIENT.console.error("Tried to register ponder scene " + fullName + " in a reload, you'll have to restart!");
            return this;
        }
        scenes.put(fullName, scene);
        if (!PonderJS.isInitialized()) {
            String pathOnlyName = getPathOnlyName(name);
            Couple<String> sceneId = Couple.create(this.name.getNamespace(), pathOnlyName);
            if (!PonderJS.scenes.contains(sceneId)) {
                PonderJS.scenes.add(sceneId);
            }
            for (var id : items)
                addNamedStoryBoard(pathOnlyName, title, id, PonderJS.appendKubeToId(structureName), (b, u) -> invokeScene(fullName, b, u));
        }
        return this;
    }

    protected void invokeScene(String name, SceneBuilder builder, SceneBuildingUtil util) {
            try {
                scenes.get(name).program(builder, util);
            } catch (Exception e) {
                ConsoleJS.CLIENT.error("Failed to invoke scene " + name + ": " + e.getMessage());
                e.printStackTrace();
            }
    }

    @Override
    public PonderBuilderJS getSelf() {
        return this;
    }

    @Override
    protected ItemProviderEntry<?> getItemProviderEntry(Item item) {
        return new ItemProviderEntry<>(Create.registrate(), RegistryObject.create(item.getRegistryName(), ForgeRegistries.ITEMS));
    }
}
