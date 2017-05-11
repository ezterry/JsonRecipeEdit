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

package com.ezrol.terry.minecraft.jsonRecipeEdit.tools;

import com.ezrol.terry.minecraft.jsonRecipeEdit.JSONRecipeEdit;
import com.ezrol.terry.minecraft.jsonRecipeEdit.integration.GenericIntegratedMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Logic to dump Minecraft data to csv files
 *
 * Created by ezterry on 5/7/17.
 */
@SuppressWarnings("WeakerAccess")
public class DataDumps {
    public static boolean runDataDump(String name){
        //first run any integration data dumps
        if(GenericIntegratedMod.runDataDumpAll(name)){
            return true;
        }
        //now see if we internally know of this data dump
        switch(name){
            case "items":
                dumpItems();
                break;
            case "blocks":
                dumpBlocks();
                break;
            case "fluids":
                dumpFluids();
                break;
            default:
                return false;
        }
        return true;
    }

    public static BufferedWriter dumpFile(String dumpName){
        File dumpDir = new File(JSONRecipeEdit.proxy.getGameDir().getAbsolutePath(), "datadump");
        File dumpFile = new File(JSONRecipeEdit.proxy.getGameDir().getAbsolutePath(), "datadump/"+dumpName+".csv");

        if(!dumpDir.exists()){
            //noinspection ResultOfMethodCallIgnored
            dumpDir.mkdir();
        }
        if(!dumpDir.isDirectory()){
            JSONRecipeEdit.log(Level.ERROR,"folder datadump is not a directory, or could not be created");
            return null;
        }

        try {
            return(new BufferedWriter(new FileWriter(dumpFile)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateCSVLine(Object... fields){
        StringBuilder output = new StringBuilder();
        boolean first = true;

        for(Object f:fields){
            if(!first){
                output.append(',');
            }
            else{
                first = false;
            }
            if(f == null){
                continue;
            }
            if(f instanceof String){
                output.append(StringEscapeUtils.escapeCsv((String)f));
            }
            else if(f instanceof Boolean){
                if((boolean)f){
                    output.append("TRUE");
                }
                else{
                    output.append("FALSE");
                }
            }
            else if(f instanceof Integer){
                output.append((int)f);
            }
            else if(f instanceof Long){
                output.append((long)f);
            }
            else if(f instanceof Float){
                output.append((float)f);
            }
            else if(f instanceof Double){
                output.append((double)f);
            }
            else{
                output.append(StringEscapeUtils.escapeCsv(f.getClass().getSimpleName() + "/" +f.toString()));
            }

        }
        output.append('\n');
        return(output.toString());
    }

    public static <K,E> List<Map.Entry<K,E>> sortedEntries(Set<Map.Entry<K,E>> orig){
        List<Map.Entry<K,E>> r;
        r=new ArrayList<>(orig);

        Collections.sort(r, new Comparator<Map.Entry<K, E>>() {
            @Override
            public int compare(Map.Entry<K, E> o1, Map.Entry<K, E> o2) {
                return o1.getKey().toString().compareTo(o2.getKey().toString());
            }
        });
        return r;
    }

    public static Object getField(Class c,Object cls, String searge,String name){
        Field f;
        try {
            f = c.getDeclaredField(searge);
        }
        catch(NoSuchFieldException e1){
            try {
                f = c.getDeclaredField(name);
            }
            catch(NoSuchFieldException e2){
                return null;
            }
        }
        f.setAccessible(true);
        try {
            return(f.get(cls));
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static void dumpItems(){
        BufferedWriter output = dumpFile("items");

        if(output == null){
            return;
        }
        try {
            //headers
            output.write(generateCSVLine("item name",
                    "id",
                    "harvest levels",
                    "unlocalized name",
                    "max damage",
                    "is repairable",
                    "stack size"));

            for(Map.Entry<ResourceLocation,Item> e : sortedEntries(ForgeRegistries.ITEMS.getEntries())){
                Item i = e.getValue();
                ItemStack defInstance = new ItemStack(i);
                StringBuilder harvestLevel= new StringBuilder();
                for(String tool : i.getToolClasses(defInstance)){
                    harvestLevel.append("<")
                            .append(tool)
                            .append(":")
                            .append(i.getHarvestLevel(defInstance, tool, null, null))
                            .append(">");
                }
                output.write(generateCSVLine(e.getKey().toString(),
                        Item.getIdFromItem(i),
                        harvestLevel.toString(),
                        i.getUnlocalizedName(),
                        i.getMaxDamage(defInstance),
                        i.isRepairable(),
                        i.getItemStackLimit(defInstance)));
            }
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dumpBlocks(){
        BufferedWriter output = dumpFile("blocks");

        if(output == null){
            return;
        }
        try {
            //headers
            output.write(generateCSVLine("block name",
                    "state",
                    "id",
                    "hardness",
                    "resistance",
                    "harvest level",
                    "unlocalized name",
                    "light level",
                    "light opacity",
                    "slipperiness"));

            for(Map.Entry<ResourceLocation,Block> e : sortedEntries(ForgeRegistries.BLOCKS.getEntries())) {
                Block b = e.getValue();
                for (IBlockState s : b.getBlockState().getValidStates()) {
                    output.write(generateCSVLine(e.getKey().toString(),
                            s.toString(),
                            Block.getIdFromBlock(b)+":"+b.getMetaFromState(s),
                            getField(Block.class, b,"field_149782_v","blockHardness"),
                            getField(Block.class, b,"field_149781_w","blockResistance"),
                            "<" + b.getHarvestTool(s) + ":" + b.getHarvestLevel(s) + ">",
                            b.getUnlocalizedName(),
                            getField(Block.class, b,"field_149784_t","lightValue"),
                            getField(Block.class, b,"field_149786_r","lightOpacity"),
                            b.slipperiness));
                }
            }
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dumpFluids(){
        BufferedWriter output = dumpFile("fluids");

        if(output == null){
            return;
        }
        try {
            //headers
            output.write(generateCSVLine("fluid name",
                    "block",
                    "temperature",
                    "color",
                    "density",
                    "viscosity",
                    "unlocalized name"));

            for(Map.Entry<String, Fluid> e : sortedEntries(FluidRegistry.getRegisteredFluids().entrySet())){
                Fluid f = e.getValue();
                String blockname;
                if(f.getBlock() == null){
                    blockname = "";
                }
                else{
                    blockname = f.getBlock().toString();
                }

                output.write(generateCSVLine(e.getKey(),
                        blockname,
                        f.getTemperature(),
                        Integer.toHexString(f.getColor()),
                        f.getDensity(),
                        f.getViscosity(),
                        f.getUnlocalizedName()));
            }
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
