package com.almostreliable.ponderjs;

import com.almostreliable.ponderjs.mixin.PonderTagRegistryAccessor;
import com.google.common.collect.Multimap;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderTag;
import com.simibubi.create.foundation.ponder.PonderTagRegistry;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class PonderItemTagEventJS extends EventJS {
    public void createTag(String id, ItemStackJS displayItem, String title, String description, boolean useAsMainItem, IngredientJS ingredient) {
        PonderJS.getTagByName(id).ifPresent(tag -> {
            throw new IllegalArgumentException("Tag " + id + " already exists");
        });

        ResourceLocation idWithNamespace = PonderJS.appendKubeToId(id);
        PonderTag ponderTag = new PonderTag(idWithNamespace)
                .item(displayItem.getItem(), true, useAsMainItem)
                .defaultLang(title, description);
        PonderRegistry.TAGS.listTag(ponderTag);
        add(ponderTag, ingredient);
        PonderJS.NAMESPACES.add(idWithNamespace.getNamespace());
    }

    public void createTag(String id, ItemStackJS displayItem, String title, String description, boolean useAsMainItem) {
        createTag(id, displayItem, title, description, useAsMainItem, ItemStackJS.EMPTY);
    }

    public void removeTag(PonderTag... tags) {
        for (PonderTag tag : tags) {
            Set<ResourceLocation> items = PonderRegistry.TAGS.getItems(tag);
            PonderRegistry.TAGS.getListedTags().remove(tag);
            remove(tag, items);
        }
    }

    public void add(PonderTag tag, IngredientJS ingredient) {
        if (ingredient.isEmpty()) return;
        PonderTagRegistry.TagBuilder tagBuilder = PonderRegistry.TAGS.forTag(tag);
        ingredient.getStacks().forEach(stack -> tagBuilder.add(stack.getItem()));
    }

    public void remove(PonderTag tag, IngredientJS ingredient) {
        if (ingredient.isEmpty()) return;
        Set<ResourceLocation> ids = ingredient.getStacks()
                .stream()
                .map(ItemStackJS::getId)
                .map(ResourceLocation::new)
                .collect(Collectors.toSet());
        remove(tag, ids);
    }

    private void remove(PonderTag tag, Set<ResourceLocation> items) {
        Multimap<ResourceLocation, PonderTag> tagMap = ((PonderTagRegistryAccessor) PonderRegistry.TAGS).getTags();
        for (ResourceLocation item : items) {
            Collection<PonderTag> tagsForItem = tagMap.get(item);
            if (tagsForItem.remove(tag)) {
                ConsoleJS.CLIENT.info("Removed ponder tag " + tag.getId() + " from item " + item);
            }
        }
    }
}
