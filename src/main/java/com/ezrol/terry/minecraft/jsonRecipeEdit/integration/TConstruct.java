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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import org.apache.logging.log4j.Level;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.DryingRecipe;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.AlloyRecipe;
import slimeknights.tconstruct.library.smeltery.CastingRecipe;
import slimeknights.tconstruct.library.smeltery.ICastingRecipe;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

/**
 * Add integration to TConstruct recipes
 *
 * Created by ezterry on 4/30/17.
 */
public class TConstruct extends GenericIntegratedMod{
    // References to TinkersRegistery drying rack recipes/recipe constructor
    private List<DryingRecipe> dryingRecipes;
    private Constructor<DryingRecipe> dryingRecipeConstructor;

    // References to TinkersRegistery melting Rec
    private List<MeltingRecipe> meltingRecipes;

    // References to tableCastRegistry
    private List<ICastingRecipe> tableCastRecipes;

    // References to basinCastRegistry
    private List<ICastingRecipe> basinCastRecipes;

    // Reference to Alloy Recipes
    private List<AlloyRecipe> alloyRecipes;

    public TConstruct() {
        super();
    }

    private <LS> List<LS> getRegistry(String name){
        Field tmp;
        try {
            tmp = TinkerRegistry.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            JSONRecipeEdit.log(Level.ERROR,"Unable to find TConstruct registry: " + name);
            return null;
        }
        tmp.setAccessible(true);
        try {
            //noinspection unchecked
            return((List<LS>) tmp.get(null));
        } catch (IllegalAccessException e) {
            JSONRecipeEdit.log(Level.ERROR,"Unable to access TConstruct registry: " + name);
            return null;
        }
    }

    @Override
    void init(FMLInitializationEvent event) {
        CommandRegistry r = CommandRegistry.getInstance();

        //get drying recipe registry
        dryingRecipes = getRegistry("dryingRegistry");
        if(dryingRecipes == null){
            return; //not found
        }
        //get melting recipe registry
        meltingRecipes = getRegistry("meltingRegistry");
        if(meltingRecipes == null){
            return; //not found
        }
        //get melting recipe registry
        tableCastRecipes = getRegistry("tableCastRegistry");
        if(tableCastRecipes == null){
            return; //not found
        }
        //get melting recipe registry
        basinCastRecipes = getRegistry("basinCastRegistry");
        if(basinCastRecipes == null){
            return; //not found
        }
        //get melting recipe registry
        alloyRecipes = getRegistry("alloyRegistry");
        if(alloyRecipes == null){
            return; //not found
        }

        //get access to the internal drying recipe list/constructor
        try {
            dryingRecipeConstructor = DryingRecipe.class.getDeclaredConstructor(RecipeMatch.class,ItemStack.class,int.class);
            dryingRecipeConstructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            JSONRecipeEdit.log(Level.ERROR,"Unable to get TConstruct drying recipe constructor");
            return;
        }

        //register the jsonRE commands

        r.register(new ticRemoveDrying());
        r.register(new ticAddDrying());
        r.register(new ticRemoveMelting());
        r.register(new ticAddMelting());
        r.register(new ticRemoveTableCasting());
        r.register(new ticAddTableCasting());
        r.register(new ticRemoveBasinCasting());
        r.register(new ticAddBasinCasting());
        r.register(new ticRemoveAlloy());
        r.register(new ticAddAlloy());
    }

    @Override
    void postJson(FMLLoadCompleteEvent event) {

    }

    private class ticRemoveDrying extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic remove drying";
        }

        @Override
        public void runCommand(JsonObject command) {
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;

            JsonHelpers.assertArray(command,"item");
            ItemStack itm = getItemFromArray(command.get("item").getAsJsonArray(),1);

            if(itm == null){
                error("could not find the item to remove");
                return;
            }
            for(DryingRecipe r : dryingRecipes){
                if(r.getResult().isItemEqual(itm)){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                dryingRecipes.remove(i);
            }
            info(String.format("Removed TiC Drying Recipe(s) for %s",itm.toString()));
        }
    }

    private class ticAddDrying extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic add drying";
        }

        @Override
        public void runCommand(JsonObject command) {
            ItemStack output;
            RecipeMatch recipe;

            int ticks = 20 * 60 * 5;

            JsonHelpers.assertArrayOrString(command,"input");
            JsonHelpers.assertArray(command,"output");

            output = getItemFromArray(command.get("output").getAsJsonArray(),1);
            if(output == null){
                error(String.format("Unable to find drying output item: %s",command.get("output").toString()));
                return;
            }
            ticks = JsonHelpers.getInt(command,"time",ticks);

            if(command.get("input").isJsonArray()) {
                ItemStack in = getItemFromArray(command.get("input").getAsJsonArray(), 1);
                if(in == null){
                    error(String.format("unable to find item %s",command.get("input").toString()));
                    return;
                }
                recipe = RecipeMatch.of(in);

            }
            else{
                String input = command.get("input").getAsString();
                if(input == null){
                    error("Received null string as input item for drying recipe");
                    return;
                }
                recipe = RecipeMatch.of(input);
            }
            try {
                dryingRecipes.add(dryingRecipeConstructor.newInstance(recipe,output,ticks));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                error(String.format("Fatal error constructing recipe %s",command.toString()));
                error(e.toString());
            }
            info(String.format("Add drying recipe for %s",output.toString()));
        }
    }
    private class ticRemoveMelting extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic remove melting";
        }

        @Override
        public void runCommand(JsonObject command) {
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;

            JsonHelpers.assertArray(command,"fluid");
            FluidStack itm = getFluidFromArray(command.get("fluid").getAsJsonArray());

            if(itm == null){
                error("could not find the item to remove");
                return;
            }
            for(MeltingRecipe r : meltingRecipes){
                if(r.getResult().isFluidStackIdentical(itm)){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                meltingRecipes.remove(i);
            }
            info(String.format("Removed TiC Melting Recipe(s) for %s",itm.getFluid().getUnlocalizedName()));
        }
    }

    private class ticAddMelting extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic add melting";
        }

        @Override
        public void runCommand(JsonObject command) {
            FluidStack output;
            RecipeMatch recipe;

            JsonHelpers.assertArrayOrString(command,"input");
            JsonHelpers.assertArray(command,"output");

            output = getFluidFromArray(command.get("output").getAsJsonArray());
            if(output == null){
                error(String.format("Unable to find drying output itme: %s",command.get("output").toString()));
                return;
            }

            if(command.get("input").isJsonArray()) {
                ItemStack in = getItemFromArray(command.get("input").getAsJsonArray(), 1);
                if(in == null){
                    error(String.format("unable to find item %s",command.get("input").toString()));
                    return;
                }
                recipe = RecipeMatch.of(in,output.amount);

            }
            else{
                String input = command.get("input").getAsString();
                if(input == null){
                    error("Received null string as input item for drying recipe");
                    return;
                }
                recipe = RecipeMatch.of(input,output.amount);
            }
            meltingRecipes.add(new MeltingRecipe(recipe,output));
            info(String.format("Add melting recipe for %s",output.getFluid().getUnlocalizedName()));
        }
    }

    private class ticRemoveTableCasting extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic remove table casting";
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void runCommand(JsonObject command) {
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;
            ItemStack cast;
            String ifo="";

            JsonHelpers.assertArray(command,"fluid");
            JsonHelpers.assertExists(command,"cast");

            FluidStack fluid = getFluidFromArray(command.get("fluid").getAsJsonArray());
            if(!command.get("cast").isJsonNull()) {
                cast = getItemFromArray(command.get("cast").getAsJsonArray(),1);
                if (cast == null) {
                    error("unable to locate cast item in command " + command.toString());
                }
            }
            else{
                cast = ItemStack.EMPTY;
            }

            if(fluid == null){
                error("unable to locate input fluid in: " + command.toString());
                return;
            }
            for(ICastingRecipe r : tableCastRecipes){
                if(r.matches(
                        cast,fluid.getFluid())){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                ifo=tableCastRecipes.get(i).getResult(cast,fluid.getFluid()).toString();
                tableCastRecipes.remove(i);
            }
            info(String.format("Removed TiC Table Casting Recipe(s) for %s",ifo));
        }
    }

    private class ticAddTableCasting extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic add table casting";
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"output");
            JsonHelpers.assertArray(command,"fluid");
            JsonHelpers.assertExists(command,"cast");

            boolean consumeCast = JsonHelpers.getBool(command,"consumecast",true);
            boolean switchOutput = JsonHelpers.getBool(command,"switchoutput",false);
            FluidStack fluid = getFluidFromArray(command.get("fluid").getAsJsonArray());
            ItemStack output = getItemFromArray(command.get("output").getAsJsonArray(),1);
            RecipeMatch cast;

            if(command.get("cast").isJsonNull()){
                cast=null;
            }
            else if(command.get("cast").isJsonArray()){
                ItemStack in = getItemFromArray(command.get("cast").getAsJsonArray(), 1);
                if(in == null){
                    error(String.format("unable to find item %s",command.get("cast").toString()));
                    return;
                }
                cast = RecipeMatch.of(in);
            }
            else{
                String in = command.get("cast").getAsString();
                if(in == null){
                    error("Received null string as input item for drying recipe");
                    return;
                }
                cast = RecipeMatch.of(in);
            }

            if(cast == null){
                tableCastRecipes.add(new CastingRecipe(output, fluid.getFluid(), fluid.amount,
                        CastingRecipe.calcCooldownTime(fluid.getFluid(), fluid.amount)));
            }
            else {
                tableCastRecipes.add(new CastingRecipe(output, cast, fluid.getFluid(), fluid.amount, consumeCast, switchOutput));
            }
            info(String.format("Added TiC table Casting recipe for: %s",output.toString()));
        }
    }
    private class ticRemoveBasinCasting extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic remove basin casting";
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void runCommand(JsonObject command) {
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;
            ItemStack cast;
            String ifo="";

            JsonHelpers.assertArray(command,"fluid");
            JsonHelpers.assertExists(command,"cast");

            FluidStack fluid = getFluidFromArray(command.get("fluid").getAsJsonArray());
            if(!command.get("cast").isJsonNull()) {
                cast = getItemFromArray(command.get("cast").getAsJsonArray(),1);
                if (cast == null) {
                    error("unable to locate cast item in command " + command.toString());
                }
            }
            else{
                cast = ItemStack.EMPTY;
            }

            if(fluid == null){
                error("unable to locate input fluid in: " + command.toString());
                return;
            }
            for(ICastingRecipe r : basinCastRecipes){
                if(r.matches(
                        cast,fluid.getFluid())){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                ifo=basinCastRecipes.get(i).getResult(cast,fluid.getFluid()).toString();
                basinCastRecipes.remove(i);
            }
            info(String.format("Removed TiC Basin Casting Recipe(s) for %s",ifo));
        }
    }

    private class ticAddBasinCasting extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic add basin casting";
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"output");
            JsonHelpers.assertArray(command,"fluid");
            JsonHelpers.assertExists(command,"cast");

            boolean consumeCast = JsonHelpers.getBool(command,"consumecast",true);
            boolean switchOutput = JsonHelpers.getBool(command,"switchoutput",false);
            FluidStack fluid = getFluidFromArray(command.get("fluid").getAsJsonArray());
            ItemStack output = getItemFromArray(command.get("output").getAsJsonArray(),1);
            RecipeMatch cast;

            if(command.get("cast").isJsonNull()){
                cast=null;
            }
            else if(command.get("cast").isJsonArray()){
                ItemStack in = getItemFromArray(command.get("cast").getAsJsonArray(), 1);
                if(in == null){
                    error(String.format("unable to find item %s",command.get("cast").toString()));
                    return;
                }
                cast = RecipeMatch.of(in);
            }
            else{
                String in = command.get("cast").getAsString();
                if(in == null){
                    error("Received null string as input item for drying recipe");
                    return;
                }
                cast = RecipeMatch.of(in);
            }

            if(cast == null){
                basinCastRecipes.add(new CastingRecipe(output, fluid.getFluid(), fluid.amount,
                        CastingRecipe.calcCooldownTime(fluid.getFluid(), fluid.amount)));
            }
            else {
                basinCastRecipes.add(new CastingRecipe(output, cast, fluid.getFluid(), fluid.amount, consumeCast, switchOutput));
            }
            info(String.format("Added TiC basin Casting recipe for: %s",output.toString()));
        }
    }

    private class ticRemoveAlloy extends GenericCommand {
        @Override
        public String getCommandName() {
            return "tic remove alloy";
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void runCommand(JsonObject command) {
            LinkedList<Integer> todelete = new LinkedList<>();
            int idx = 0;

            JsonHelpers.assertArray(command,"fluid");
            FluidStack fluid = getFluidFromArray(command.get("fluid").getAsJsonArray());

            for(AlloyRecipe r : alloyRecipes){
                if(r.getResult().isFluidEqual(fluid)){
                    todelete.addFirst(idx);
                }
                idx++;
            }
            for(int i : todelete){
                alloyRecipes.remove(i);
            }
            info(String.format("Removed TiC Alloy %s",fluid.getFluid().getUnlocalizedName()));
        }
    }

    private class ticAddAlloy extends GenericCommand {

        @Override
        public String getCommandName() {
            return "tic add alloy";
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void runCommand(JsonObject command) {
            JsonHelpers.assertArray(command,"output");
            JsonHelpers.assertArray(command,"components");

            LinkedList<FluidStack> components = new LinkedList<>();

            for(JsonElement e : command.getAsJsonArray("components")){
                if(! e.isJsonArray()){
                    error("all components of the alloy must be json fluid stack arrays");
                    error(String.format("Got %s instead of an array processing %s",e.toString(),command.toString()));
                    return;
                }
                FluidStack f = getFluidFromArray(e.getAsJsonArray());
                if(f == null){
                    error("invalid fluid stack "+ e.toString());
                    return;
                }
                components.add(f);
            }
            FluidStack output = getFluidFromArray(command.get("output").getAsJsonArray());

            alloyRecipes.add(new AlloyRecipe(output,components.toArray(new FluidStack[components.size()])));
            info(String.format("Add TiC Alloy %s",output.getFluid().getUnlocalizedName()));
        }
    }

    @Override
    public boolean runDataDump(String name){
        if(name.equals("alloys")){
            BufferedWriter output = DataDumps.dumpFile("alloys");
            if(output == null){
                return true;
            }

            try{
                output.write(DataDumps.generateCSVLine("Alloy Name", "Alloy Amount","items/amounts..."));
                for(AlloyRecipe a : alloyRecipes){
                    LinkedList<Object> itms = new LinkedList<>();

                    itms.add(a.getResult().getUnlocalizedName());
                    itms.add(a.getResult().amount);

                    for(FluidStack f : a.getFluids()){
                        itms.add(f.getUnlocalizedName());
                        itms.add(f.amount);
                    }
                    output.write(DataDumps.generateCSVLine(itms.toArray()));
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
