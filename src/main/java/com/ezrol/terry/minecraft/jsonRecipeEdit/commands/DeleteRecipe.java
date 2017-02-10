package com.ezrol.terry.minecraft.jsonRecipeEdit.commands;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;

/**
 * Command to delete a recipe
 *
 * Created by ezterry on 2/9/17.
 */
public class DeleteRecipe extends GenericCommand{
    @Override
    public String getCommandName() {
        return "delete recipe";
    }

    @Override
    public void runCommand(JsonObject command) {
        ResourceLocation itemname;
        int meta=0;

        itemname = new ResourceLocation(command.get("item").getAsString());
        if(command.has("meta")){
            meta = command.get("meta").getAsNumber().intValue();
        }

        //get the item stack item+meta
        Item item = ForgeRegistries.ITEMS.getValue(itemname);
        if(item == null){
            error(String.format("Unable to load item %s in command: %s",itemname.toString(),command.toString()));
            return;
        }

        @SuppressWarnings("ConstantConditions")
        ItemStack testitem = new ItemStack(ForgeRegistries.ITEMS.getValue(itemname), 1, meta);
        List<IRecipe> recipeList=CraftingManager.getInstance().getRecipeList();
        int index = 0;
        LinkedList<Integer> todelete = new LinkedList<>();

        for(IRecipe cur : recipeList){
            if(cur.getRecipeOutput().isItemEqual(testitem)){
                todelete.addFirst(index);
            }
            index ++;
        }
        info(String.format("Removing %d recipes for %s.",todelete.size(),testitem.toString()));
        for(int i : todelete){
            recipeList.remove(i);
        }
    }
}
