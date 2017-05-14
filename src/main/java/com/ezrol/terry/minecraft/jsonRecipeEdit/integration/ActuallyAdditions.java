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
import com.ezrol.terry.minecraft.jsonRecipeEdit.api.CommandRegistry;
import com.ezrol.terry.minecraft.jsonRecipeEdit.commands.GenericCommand;
import com.ezrol.terry.minecraft.jsonRecipeEdit.tools.DataDumps;
import com.ezrol.terry.minecraft.jsonRecipeEdit.tools.JsonHelpers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.recipe.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import org.apache.logging.log4j.Level;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * The Actually Additions integration logic
 * Created by ezterry on 5/13/17.
 */
public class ActuallyAdditions extends GenericIntegratedMod{
    @SuppressWarnings("WeakerAccess")
    static final public int MIN_API_VERSION_SUPPORTED = 33;
    @SuppressWarnings("WeakerAccess")
    static final public int MAX_API_VERSION_SUPPORTED = 33;
    private int apiVersion = 0;

    @Override
    void init(FMLInitializationEvent event) {
        //First lets check the API version
        try {
            Field f = ActuallyAdditionsAPI.class.getDeclaredField("API_VERSION");
            apiVersion=Integer.parseInt(f.get(null).toString());
        }
        catch(NoSuchFieldException | IllegalAccessException e) {
            JSONRecipeEdit.log(Level.ERROR,"Unable to access Actually Additions API VERSION number");
        }
        if(apiVersion < MIN_API_VERSION_SUPPORTED){
            JSONRecipeEdit.log(Level.FATAL,String.format(
                    "Actually additions API version %d found, we require at least version %d!",
                    apiVersion, MIN_API_VERSION_SUPPORTED));
            JSONRecipeEdit.log(Level.FATAL,"Actually Addition Commands will not be registered!");
            return;
        }
        if(apiVersion > MAX_API_VERSION_SUPPORTED){
            JSONRecipeEdit.log(Level.WARN, String.format(
                    "Actually additions API version %d found, this is newer than the version we are validated for.",
                    apiVersion));
            JSONRecipeEdit.log(Level.INFO, "Attempting to load Actually Additions commands despite API " +
                    "mismatch");
        }

        CommandRegistry r = CommandRegistry.getInstance();

        r.register(new aaRemoveCrusher());
        r.register(new aaAddCrusher());
        r.register(new aaRemoveBallOfFur());
        r.register(new aaAddBallOfFur());
        r.register(new aaRemoveTreasureChest());
        r.register(new aaAddTreasureChest());
        r.register(new aaRemoveAtomicReconstructor());
        r.register(new aaAddAtomicReconstructor());
        r.register(new aaRemoveEmpowerer());
        r.register(new aaAddEmpowerer());
    }

    @Override
    void postJson(FMLLoadCompleteEvent event) {
        //nop
    }

    private class aaRemoveCrusher extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa remove crusher";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"input");
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;

            ItemStack itm= getItemFromArray(command.getAsJsonArray("input"),1);
            if(itm == null){
                error("Unable to find item from command " + command.toString());
                return;
            }

            for(CrusherRecipe r : ActuallyAdditionsAPI.CRUSHER_RECIPES){
                if(r.inputStack.isItemEqual(itm) && itm.getCount() == r.inputStack.getCount()){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                ActuallyAdditionsAPI.CRUSHER_RECIPES.remove(i);
            }
            info(String.format("Removed %d aa crusher recipes for %s",todelete.size(),itm.toString()));
        }
    }

    private class aaAddCrusher extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa add crusher";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"input");
            JsonHelpers.assertArray(command,"outputone");
            int chance = JsonHelpers.getInt(command,"chance",5);

            ItemStack input= getItemFromArray(command.getAsJsonArray("input"),1);
            if(input == null){
                error("Unable to find item from command " + command.toString());
                return;
            }
            ItemStack outputOne= getItemFromArray(command.getAsJsonArray("outputone"),
                    JsonHelpers.getInt(command,"outputonecount",1));
            if(outputOne == null){
                error("Unable to find item from command " + command.toString());
                return;
            }
            ItemStack outputTwo;
            if(command.has("outputtwo")) {
                outputTwo = getItemFromArray(command.getAsJsonArray("outputtwo"),
                        JsonHelpers.getInt(command, "outputtwocount", 1));
                if (outputTwo == null) {
                    error("Unable to find item from command " + command.toString());
                    return;
                }
            }
            else{
                outputTwo = ItemStack.EMPTY;
                chance = 0;
            }

            if(chance < 0){
                info("chance must be >=0 forcing value in range");
                chance = 0;
            }
            if(chance > 100){
                info("chance must be <=100 forcing value in range");
                chance = 100;
            }
            ActuallyAdditionsAPI.addCrusherRecipe(input, outputOne, outputTwo, chance);
            info(String.format("Added aa crushing recipe %s -> %s",input.toString(),outputOne.toString()));
        }
    }

    private class aaRemoveBallOfFur extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa remove ball of fur";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"item");
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;

            ItemStack itm= getItemFromArray(command.getAsJsonArray("item"),1);
            if(itm == null){
                error("Unable to find item from command " + command.toString());
                return;
            }

            for(BallOfFurReturn r : ActuallyAdditionsAPI.BALL_OF_FUR_RETURN_ITEMS){
                if(r.returnItem.isItemEqual(itm)){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                ActuallyAdditionsAPI.BALL_OF_FUR_RETURN_ITEMS.remove(i);
            }
            info(String.format("Removed %d aa ball of fur return items for %s",todelete.size(),itm.toString()));
        }
    }

    private class aaAddBallOfFur extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa add ball of fur";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"item");
            JsonHelpers.assertExists(command,"chance");

            ItemStack itm= getItemFromArray(command.getAsJsonArray("item"),
                    JsonHelpers.getInt(command, "count", 1));
            if(itm == null){
                error("Unable to find item from command " + command.toString());
                return;
            }
            int chance = JsonHelpers.getInt(command, "chance");

            ActuallyAdditionsAPI.addBallOfFurReturnItem(itm,chance);

            info(String.format("Added ball of Fur return item: %s with chance %d",itm.toString(),chance ));
        }

    }
    private class aaRemoveTreasureChest extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa remove treasure loot";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"item");
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;

            ItemStack itm= getItemFromArray(command.getAsJsonArray("item"),1);
            if(itm == null){
                error("Unable to find item from command " + command.toString());
                return;
            }

            for(TreasureChestLoot r : ActuallyAdditionsAPI.TREASURE_CHEST_LOOT){
                if(r.returnItem.isItemEqual(itm)){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                ActuallyAdditionsAPI.TREASURE_CHEST_LOOT.remove(i);
            }
            info(String.format("Removed %d aa treasure chest loot items for %s",todelete.size(),itm.toString()));
        }
    }

    private class aaAddTreasureChest extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa add treasure loot";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command, "item");
            JsonHelpers.assertExists(command, "chance");

            int min = JsonHelpers.getInt(command,"min",1);
            int max = JsonHelpers.getInt(command,"max",1);

            ItemStack itm = getItemFromArray(command.getAsJsonArray("item"),1);
            if (itm == null) {
                error("Unable to find item from command " + command.toString());
                return;
            }
            int chance = JsonHelpers.getInt(command, "chance");

            ActuallyAdditionsAPI.addTreasureChestLoot(itm, chance, min, max);

            info(String.format("Added treasure loot item: %s with chance %d", itm.toString(), chance));
        }
    }

    private class aaRemoveAtomicReconstructor extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa remove atomic reconstructor";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"item");
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;

            ItemStack itm= getItemFromArray(command.getAsJsonArray("item"),1);
            if(itm == null){
                error("Unable to find item from command " + command.toString());
                return;
            }

            for(LensConversionRecipe r : ActuallyAdditionsAPI.RECONSTRUCTOR_LENS_CONVERSION_RECIPES){
                if(r.outputStack.isItemEqual(itm)){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                ActuallyAdditionsAPI.RECONSTRUCTOR_LENS_CONVERSION_RECIPES.remove(i);
            }
            info(String.format("Removed %d atomic reconstructor recipes for %s",todelete.size(),itm.toString()));
        }
    }

    private class aaAddAtomicReconstructor extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa add atomic reconstructor";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"input");
            JsonHelpers.assertArray(command,"output");

            ItemStack in= getItemFromArray(command.getAsJsonArray("input"),1);
            if(in == null){
                error("Unable to find input for command " + command.toString());
                return;
            }
            ItemStack out= getItemFromArray(command.getAsJsonArray("output"),1);
            if(out == null){
                error("Unable to find input for command " + command.toString());
                return;
            }
            int energy = JsonHelpers.getInt(command,"energy");

            ActuallyAdditionsAPI.addReconstructorLensConversionRecipe(in,out,energy);
            info(String.format("Added atomic reconstructore recipe from %s to %s.",in.toString(),out.toString()));
        }
    }

    private class aaRemoveEmpowerer extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa remove empowerer";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"item");
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;

            ItemStack itm= getItemFromArray(command.getAsJsonArray("item"),1);
            if(itm == null){
                error("Unable to find item from command " + command.toString());
                return;
            }

            for(EmpowererRecipe r : ActuallyAdditionsAPI.EMPOWERER_RECIPES){
                if(r.output.isItemEqual(itm)){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                ActuallyAdditionsAPI.EMPOWERER_RECIPES.remove(i);
            }
            info(String.format("Removed %d empowerer recipes for %s",todelete.size(),itm.toString()));
        }
    }

    private class aaAddEmpowerer extends GenericCommand {

        @Override
        public String getCommandName() {
            return "aa add empowerer";
        }

        @Override
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"input");
            JsonHelpers.assertArray(command,"output");
            JsonHelpers.assertArray(command,"modifiers");
            JsonArray modifiers = command.getAsJsonArray("modifiers");

            if(modifiers.size() != 4){
                error(String.format("4 modifies are required, found %d for command %s",
                        modifiers.size(),command.toString()));
                return;
            }
            if(!(modifiers.get(0).isJsonArray() && modifiers.get(1).isJsonArray() &&
                    modifiers.get(2).isJsonArray() && modifiers.get(3).isJsonArray())){
                error(String.format("One of the modifies is not a valid item stack in command %s",
                        command.toString()));
                return;
            }

            ItemStack in= getItemFromArray(command.getAsJsonArray("input"),1);
            if(in == null){
                error("Unable to find input for command " + command.toString());
                return;
            }
            ItemStack out= getItemFromArray(command.getAsJsonArray("output"),1);
            if(out == null){
                error("Unable to find input for command " + command.toString());
                return;
            }
            ItemStack mod1= getItemFromArray(modifiers.get(0).getAsJsonArray(),1);
            if(mod1 == null){
                error("Unable to find input for command " + command.toString());
                return;
            }
            ItemStack mod2= getItemFromArray(modifiers.get(1).getAsJsonArray(),1);
            if(mod2 == null){
                error("Unable to find input for command " + command.toString());
                return;
            }
            ItemStack mod3= getItemFromArray(modifiers.get(2).getAsJsonArray(),1);
            if(mod3 == null){
                error("Unable to find input for command " + command.toString());
                return;
            }
            ItemStack mod4= getItemFromArray(modifiers.get(3).getAsJsonArray(),1);
            if(mod4 == null){
                error("Unable to find input for command " + command.toString());
                return;
            }
            int energy = JsonHelpers.getInt(command,"energy");
            int time = JsonHelpers.getInt(command,"time");
            float color[] = new float[3];

            if(!command.has("color")){
                color[0]=((float)255)/255.0f;
                color[1]=((float)105)/255.0f;
                color[2]=((float)180)/255.0f;
            }
            else if(!command.get("color").isJsonArray()){
                error("particle color must be an array of 3 integers between 0 and 255");
                error("Invalid command parameter: " + command.toString());
                return;
            }
            else {
                color[0] = ((float) command.getAsJsonArray("color").get(0).getAsInt()) / 255.0f;
                color[1] = ((float) command.getAsJsonArray("color").get(1).getAsInt()) / 255.0f;
                color[2] = ((float) command.getAsJsonArray("color").get(2).getAsInt()) / 255.0f;
            }

            ActuallyAdditionsAPI.addEmpowererRecipe(
                    in,out,mod1,mod2,mod3,mod4,energy,time, color);
            info(String.format("Added empowerer recipe from %s to %s.",in.toString(),out.toString()));
        }
    }

    @Override
    public boolean runDataDump(String name){
        if(name.equals("balloffur")){
            BufferedWriter output = DataDumps.dumpFile("balloffur");
            if(output == null){
                return true;
            }

            try{
                output.write(DataDumps.generateCSVLine("Item", "Count", "Chance"));
                for(BallOfFurReturn a : ActuallyAdditionsAPI.BALL_OF_FUR_RETURN_ITEMS){
                    output.write(DataDumps.generateCSVLine(a.returnItem.getUnlocalizedName(),
                            a.returnItem.getCount(),a.itemWeight));
                }
                output.flush();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return(true);

        }
        else if(name.equals("aatreasure")){
            BufferedWriter output = DataDumps.dumpFile("aatreasure");
            if(output == null){
                return true;
            }

            try{
                output.write(DataDumps.generateCSVLine("Item", "Min Amount","Max Amount", "Chance"));
                for(TreasureChestLoot a : ActuallyAdditionsAPI.TREASURE_CHEST_LOOT){
                    output.write(DataDumps.generateCSVLine(a.returnItem.getUnlocalizedName(),
                            a.minAmount,a.maxAmount,a.itemWeight));
                }
                output.flush();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return(true);
        }
        return(false);
    }

}
