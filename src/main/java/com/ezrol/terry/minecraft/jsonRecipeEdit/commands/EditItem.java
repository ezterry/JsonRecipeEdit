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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Allows editing of a block data
 * Created by ezterry on 2/21/17.
 */
public class EditItem extends GenericCommand{

    @Override
    public String getCommandName() {
        return "edit item";
    }

    @Override
    public void runCommand(JsonObject command) {
        Item item;
        String itemName = command.get("itemname").getAsString();

        ResourceLocation key = new ResourceLocation(itemName);
        if (!ForgeRegistries.ITEMS.containsKey(key)) {
            error(String.format("Cant find item: %s", itemName));
            return;
        }

        item = ForgeRegistries.ITEMS.getValue(key);

        if (item == null) {
            error(String.format("unable to find item, canceling edit: %s", command.toString()));
            return;
        }

        info(String.format("Editing stats for %s", item.toString()));

        if (command.has("harvestlevel")) {
            JsonArray ar = command.get("harvestlevel").getAsJsonArray();
            if (ar.size() != 2) {
                error(String.format("invalid harvest level array, must be [\"class\",level]: %s", ar.toString()));
                return;
            }
            item.setHarvestLevel(ar.get(0).getAsString(), ar.get(1).getAsInt());
        }
        if (command.has("unlocalizedname")) {
            item.setUnlocalizedName(command.get("unlocalizedname").getAsString());
        }
        if (command.has("maxdamage")) {
            item.setMaxDamage(command.get("maxdamage").getAsInt());
        }
        if (command.has("norepair")) {
            if (command.get("norepair").getAsBoolean()) {
                item.setNoRepair();
            }
        }
        if (command.has("maxstack")) {
            item.setMaxStackSize(command.get("maxstack").getAsInt());
        }
    }
}
