/*
 * Copyright (c) 2017, Terrence Ezrol (ezterry)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.ezrol.terry.minecraft.jsonRecipeEdit.commands;

import com.ezrol.terry.minecraft.jsonRecipeEdit.JSONRecipeEdit;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Provides a special shapeless recipe, where a tools durability is used in the craft
 * (This tool need not be intended for such crafting before hand to work, such as a pickaxe)
 *
 * Created by ezterry on 3/15/17.
 */
public class ToolCrafting extends GenericCommand {
    static private final String validref="abcdefghijklmnopqrstuvwxyz";

    static private boolean CheckRef(String r) {
        return r.length() == 1 && (validref.contains(r));
    }

    @Override
    public String getCommandName() {
        return "tool crafting";
    }

    @Override
    public void runCommand(JsonObject command) {
        List<Object> params = new LinkedList<>();
        int stacksize = 1;

        //first read in (non tool) ingredients
        JsonObject jsonIngredients = command.get("ingredients").getAsJsonObject();
        for(Map.Entry<String,JsonElement> e : jsonIngredients.entrySet()){
            if(!CheckRef(e.getKey())){
                error(String.format("invalid ingredient reference '%s' in command: %s",e.getKey(),command));
                return;
            }
            if(e.getValue().isJsonPrimitive()){
                //assume its an oredict entry
                params.add(e.getValue().getAsString());
            }
            else if(e.getValue().isJsonArray()){
                ItemStack itm = getItemFromArray(e.getValue().getAsJsonArray(),1);
                if(itm == null){
                    error(String.format("unable to find item %s",e.getValue().toString()));
                    return;
                }
                params.add(itm);
            }
        }
        ItemStack tool = getItemFromArray(command.get("tool").getAsJsonArray(),1);
        if(tool == null){
            error(String.format("Unable to find tool item: %s",command.toString()));
            return;
        }
        if(command.has("count") && command.get("count").isJsonPrimitive()) {
            try{
                stacksize = command.get("count").getAsJsonPrimitive().getAsInt();
            }
            catch (Exception e){
                error(String.format("Unable to parse count (stack size) assuming 1 for command: %s",command.toString()));
            }
        }
        ItemStack result = getItemFromArray(command.get("result").getAsJsonArray(),stacksize);
        if(result == null){
            error(String.format("unable to find Item in result while running command %s",command.toString()));
            return;
        }

        ShapelessToolCrafting shapelessRecipe = new ShapelessToolCrafting(result,tool,params.toArray());

        info(String.format("Add tool crafting recipe for %s.",result));
        GameRegistry.addRecipe(shapelessRecipe);
    }

    public static class ShapelessToolCrafting implements IRecipe {
        private ItemStack output;
        private ItemStack tool;
        private Object[] input;
        private static Random rand = new Random();

        ShapelessToolCrafting(@Nonnull ItemStack result,
                              @Nonnull ItemStack tool,
                              Object... recipe)
        {
            output = result.copy();
            this.tool = tool.copy();
            input = new Object[recipe.length];
            for (int i=0; i<recipe.length; i++)
            {
                if (recipe[i] instanceof ItemStack)
                {
                    input[i] = ((ItemStack)recipe[i]).copy();
                }
                else if (recipe[i] instanceof String)
                {
                    try {
                        input[i] = (OreDictionary.getOres(((String) recipe[i])));
                    }
                    catch(ArrayStoreException e){
                        JSONRecipeEdit.log(Level.ERROR,"Unable to find ore dictionary entry: " + recipe[i]);
                        throw(e);
                    }
                }
                else
                {
                    String ret = "Invalid tool crafting ore recipe: ";
                    for (Object tmp :  recipe)
                    {
                        ret += tmp + ", ";
                    }
                    ret += output;
                    throw new RuntimeException(ret);
                }
            }
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public boolean matches(InventoryCrafting inv, World worldIn) {
            int sz = inv.getSizeInventory();
            LinkedList<ItemStack> itms;
            boolean foundTool = false;

            if(sz < input.length + 1){
                return false;
            }
            itms = new LinkedList<>();
            for(int i=0;i<sz;i++){
                ItemStack itm=inv.getStackInSlot(i);
                if(itm.isEmpty()){
                    continue;
                }
                if(itm.getItem() == tool.getItem()){
                    //we have the right tool
                    if(!tool.getItem().isDamageable()){
                        if(OreDictionary.itemMatches(tool,itm,false)){
                            if(foundTool){
                                return false;
                            }
                            foundTool=true;
                            continue;
                        }
                    }
                    else{
                        if(foundTool){
                            return false;
                        }
                        foundTool=true;
                        continue;
                    }
                }
                //item is not the tool
                itms.addLast(itm);
            }
            if(!foundTool){
                return false;
            }
            for(Object ingredient : input){
                boolean found=false;
                for(Iterator<ItemStack> itr=itms.iterator();itr.hasNext();){
                    ItemStack in = itr.next();
                    if(ingredient instanceof ItemStack) {
                        if(OreDictionary.itemMatches((ItemStack)ingredient,in,false)){
                            itr.remove();
                            found=true;
                            break;
                        }
                    }
                    if(ingredient instanceof List) {
                        boolean done=false;
                        for(Object e : ((List) ingredient)){
                            if(!(e instanceof ItemStack)){
                                JSONRecipeEdit.log(Level.WARN,"Item in ore dictionary not an ItemStack");
                                continue;
                            }
                            if(OreDictionary.itemMatches((ItemStack)e,in,false)){
                                itr.remove();
                                found=true;
                                done=true;
                                break;
                            }
                        }
                        if(done){
                            break;
                        }
                    }
                }
                if(!found){
                    return false;
                }
            }
            return itms.size() == 0;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            return output.copy();
        }

        @Override
        public int getRecipeSize() {
            return(input.length + 1);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public ItemStack getRecipeOutput() {
            return output;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
            int index=0;
            ItemStack updatedTool=ItemStack.EMPTY;

            //find the updated version of the tool
            for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                ItemStack itm = inv.getStackInSlot(i);
                if(itm.isEmpty()){
                    continue;
                }
                if(itm.getItem() == tool.getItem()){
                    //we have the right tool
                    if(!tool.getItem().isDamageable()){
                        if(OreDictionary.itemMatches(tool,itm,false)){
                            updatedTool=itm.copy();
                            index=i;
                            break;
                        }
                    }
                    else{
                        updatedTool=itm.copy();
                        if(updatedTool.attemptDamageItem(1, rand)){
                            updatedTool=ItemStack.EMPTY;
                        }
                        index=i;
                        break;
                    }
                }
            }
            //return forge defaults for the non-tool items
            NonNullList<ItemStack> r = ForgeHooks.defaultRecipeGetRemainingItems(inv);
            //re add the updated tool
            r.set(index,updatedTool);

            return r;
        }
        public List jeiGetInputs(){
            List<Object> r = new LinkedList<>();
            r.add(tool);
            Collections.addAll(r, input);
            return r;
        }
        public boolean jeiIsValid(){
            if(output == null){
                JSONRecipeEdit.log(Level.ERROR,"JEI Invalid: No Recipe Output");
                return false;
            }
            if(tool == null){
                JSONRecipeEdit.log(Level.ERROR,"JEI Invalid: Tool not defined for " + output.toString());
                return false;
            }
            if(input.length > 8){
                JSONRecipeEdit.log(Level.ERROR,"JEI Invalid: too many items in recipe " + output.toString());
                return false;
            }
            for(Object i : input){
                if(i == null){
                    JSONRecipeEdit.log(Level.ERROR,"JEI Invalid: null input for " + output.toString());
                    return false;
                }
                if(i instanceof List && ((List)i).size() == 0){
                    JSONRecipeEdit.log(Level.ERROR,"JEI Invalid: Empty Ore Dictionary " + output.toString());
                    return false;
                }
            }
            return true;
        }
    }
}
