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

package com.ezrol.terry.minecraft.jsonRecipeEdit.integration;

import com.ezrol.terry.minecraft.jsonRecipeEdit.JSONRecipeEdit;
import com.ezrol.terry.minecraft.jsonRecipeEdit.commands.AnvilCraft;
import com.ezrol.terry.minecraft.jsonRecipeEdit.commands.HideInJEI;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Our integration with the JEI Plugin
 * Created by ezterry on 2/12/17.
 */
@SuppressWarnings({"NullableProblems","unused"})
@JEIPlugin
public class JEIIntegration implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {

    }

    private void loadAnvilRecipes(IModRegistry registry){
        List<AnvilCraft.CraftEntry> entries = AnvilCraft.getEntries();

        for(AnvilCraft.CraftEntry e : entries) {
            List<ItemStack> right = new ArrayList<>();
            List<ItemStack> output = new ArrayList<>();
            right.add(e.modifier);
            output.add(e.result);
            registry.addAnvilRecipe(e.originalItem,right,output);
        }
    }

    @Override
    public void register(IModRegistry registry) {
        JSONRecipeEdit.log(Level.INFO,"(JEI): Hiding items");

        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();

        for(ItemStack i : HideInJEI.getItemList()){
            blacklist.addIngredientToBlacklist(i);
        }

        JSONRecipeEdit.log(Level.INFO,"(JEI) Adding any anvil recipes");
        loadAnvilRecipes(registry);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }
}
