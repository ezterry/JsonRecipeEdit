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

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * A bach update tool for common operations across many objects
 *
 * Created by ezterry on 2/21/17.
 */
public class BatchUpdate extends GenericCommand{

    @Override
    public String getCommandName() {
        return "batch update";
    }

    @Override
    public void runCommand(JsonObject command) {
        int from;
        if (!command.has("type") || !command.get("type").isJsonPrimitive()) {
            return;
        }
        String type = command.get("type").getAsString();
        if(command.has("from")) {
            from = command.get("from").getAsInt();
        }
        else{
            from = -1;
        }
        int to = command.get("to").getAsInt();

        switch (type) {
            case "stack size":
                info(String.format("Converting items with tool size (%d) to (%d)", from, to));
                for (Item i : ForgeRegistries.ITEMS.getValues()) {
                    //noinspection deprecation
                    if ((from == -1 && i.getItemStackLimit() > to) || i.getItemStackLimit() == from) {
                        i.setMaxStackSize(to);
                    }
                }
                break;
            case "tool damage":
                info(String.format("Converting tools with max damage (%d) to (%d)", from, to));
                for (Item i : ForgeRegistries.ITEMS.getValues()) {
                    //noinspection deprecation
                    if ((from == -1 && i.getMaxDamage() > to) || i.getMaxDamage() == from) {
                        i.setMaxDamage(to);
                    }
                }
                break;
            default:
                error(String.format("Unknown batch convert: %s", type));
                break;
        }
    }
}
