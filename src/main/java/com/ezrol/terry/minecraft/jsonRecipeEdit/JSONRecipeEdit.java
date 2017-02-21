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

package com.ezrol.terry.minecraft.jsonRecipeEdit;

import com.ezrol.terry.minecraft.jsonRecipeEdit.api.CommandRegistry;
import com.ezrol.terry.minecraft.jsonRecipeEdit.commands.*;
import com.ezrol.terry.minecraft.jsonRecipeEdit.proxy.commonproxy;
import com.ezrol.terry.minecraft.jsonRecipeEdit.virtualcommandblock.JsonRecipeEditCommand;
import com.ezrol.terry.minecraft.jsonRecipeEdit.virtualcommandblock.VCommandLogic;
import com.google.gson.*;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.oredict.RecipeSorter;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings({"WeakerAccess", "unused"})
@Mod(
        modid = JSONRecipeEdit.MODID,
        version = JSONRecipeEdit.VERSION,
        acceptedMinecraftVersions = "[1.11.2,1.11.9]",
        dependencies = "before:jei;required-after:forge@[13.20.0.2228,)"
)
public class JSONRecipeEdit {
    public static final String MODID = "jsonrecipeedit";
    public static final String VERSION = "${version}";

    private File mainscript; //the location of the main Json Script
    public static VCommandLogic commandChains;

    @SidedProxy(clientSide = "com.ezrol.terry.minecraft.jsonRecipeEdit.proxy.clientproxy",
            serverSide = "com.ezrol.terry.minecraft.jsonRecipeEdit.proxy.serverproxy")
    public static commonproxy proxy;

    /**
     * output to the "log"
     *
     * @param lvl     - level of the message
     * @param message - text to send
     */
    public static void log(Level lvl, String message) {
        FMLLog.log(JSONRecipeEdit.MODID, lvl, message);
    }

    /**
     * Transform the "recommended config" to a .json for our actual configuration
     *
     * @param config - input config from FMLPreInitializationEvent
     * @return - the json file we are working with
     */
    private static File TransformToJsonFile(File config) {
        String path = config.getPath();

        if (path.endsWith(".cfg")) {
            path = path.substring(0, path.length() - 4);
        }
        return (new File(path + ".json"));
    }

    /**
     * Early Forge initialization event, here we can check the existance of the primary Json Script
     *
     * @param event Forge event data
     */

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log(Level.INFO, "Get JSON file");

        mainscript = TransformToJsonFile(event.getSuggestedConfigurationFile());
        if (!mainscript.exists()) {
            log(Level.ERROR,
                    String.format("Json Recipe Edit unable to find json file: %s", mainscript.getAbsolutePath()));

            Gson jsonbuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            JsonArray tmpl = new JsonArray();
            tmpl.add(new JsonPrimitive("** Enter your Json Script here **"));
            tmpl.add(new JsonPrimitive("For details see: https://github.com/ezterry/JsonRecipeEdit/wiki"));

            log(Level.INFO, "Creating empty JSON script");
            try {
                FileWriter f = new FileWriter(mainscript);

                f.write(jsonbuilder.toJson(tmpl));
                f.close();
            } catch (IOException e) {
                log(Level.ERROR, "Error writing to json file");
                log(Level.ERROR, e.toString());
            }
        }
    }

    /**
     * The main forge initialization event
     * Here we register the built in JsonRecipeEdit commands
     *
     * @param event Forge event data
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
        CommandRegistry cr = CommandRegistry.getInstance();

        cr.register(new DeleteRecipe());
        cr.register(new AddRecipe(true));  //"shaped recipe"
        cr.register(new AddRecipe(false)); //"shapeless recipe"
        cr.register(new RegisterOre());
        cr.register(new DeleteFurnace());
        cr.register(new AddFurnace());
        cr.register(new VirtualCommandChain());
        cr.register(new BlockDrops());
        cr.register(new AddFuel());
        cr.register(new EditBlock());
        cr.register(new EditItem());

        cr.register(new HideInJEI());

        //init virtual command chains
        commandChains = new VCommandLogic();
    }

    /**
     * This is a forge event after PostInit, mostly used by forge to clean up internal entries
     * We are using it to ensure the user changes are applied after mods are done adding recipes
     *
     * Note we must run prior to JEI or JEI will not see the updated recipes (this is done via the @Mod
     * annotation)
     *
     *
     * @param event Forge event data
     */
    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        JsonArray s;

        try {
            FileReader f = new FileReader(mainscript);
            s = new Gson().fromJson(f, JsonArray.class);
        } catch (JsonSyntaxException e) {
            log(Level.ERROR, String.format("Expected to find a JSON array in %s", mainscript.getAbsolutePath()));
            s = new JsonArray();
        } catch (IOException e) {
            log(Level.ERROR, "Error reading in JSON file");
            log(Level.ERROR, e.toString());
            s = new JsonArray();
        }
        log(Level.INFO, String.format("Running json script: %s", mainscript.getAbsolutePath()));
        runJson(s);

        //since we might have re-sorted recipes:
        if(ForgeModContainer.shouldSortRecipies){
            RecipeSorter.sortCraftManager();
        }
    }

    /**
     * Evaluate a jsonRecipeEdit script from the JsonArray
     *
     * @param script the JsonArray of the script to run
     */
    private void runJson(JsonArray script) {
        CommandRegistry cr = CommandRegistry.getInstance();

        //this is when we run the "script" of recipe edits
        for (JsonElement e : script) {
            if (e.isJsonPrimitive()) {
                //assume its a comment and ignore
                continue;
            }
            if (e.isJsonObject()) {
                //process the object
                JsonObject cmd = e.getAsJsonObject();
                if (!cmd.has("command")) {
                    log(Level.ERROR, String.format("command not in entry: %s", cmd.toString()));
                    continue;
                }
                if (!cmd.get("command").isJsonPrimitive()) {
                    log(Level.ERROR, String.format("unexpected json element in entry: %s", cmd.toString()));
                    continue;
                }
                String commandName = cmd.get("command").getAsString();

                //first see if its the special "control" command "include"
                if (commandName.equals("include")) {
                    if (!cmd.get("file").isJsonPrimitive()) {
                        log(Level.ERROR, String.format("include requires 'file' to be set: %s", cmd.toString()));
                        continue;
                    }
                    String includedFile = cmd.get("file").getAsString();
                    File ifile = new File(proxy.getGameDir().getAbsolutePath(), includedFile);

                    if (!ifile.exists()) {
                        log(Level.ERROR, String.format("included file %s not found, skipping", ifile.getAbsolutePath()));
                    }
                    try {
                        FileReader f = new FileReader(ifile);
                        log(Level.INFO, String.format("Running json script: %s", ifile.getAbsolutePath()));
                        runJson(new Gson().fromJson(f, JsonArray.class));
                    } catch (Exception err) {
                        log(Level.ERROR, "error running json script: " + err.toString());
                    }
                }
                //otherwise try to run the command from the Registry
                else if (cr.hasCommand(commandName)) {
                    //run the command
                    try {
                        cr.getCommand(commandName).runCommand(cmd);
                    } catch (Exception usererr) {
                        log(Level.ERROR, String.format("Error on command: %s", usererr.toString()));
                        log(Level.ERROR, String.format("While running command: %s", cmd.toString()));
                    }
                } else {
                    log(Level.ERROR, String.format("Unknown command: [%s] in %s", commandName, cmd.toString()));
                }
            } else {
                //???
                log(Level.ERROR, "Unexpected entry in json script! terminating script processing");
                log(Level.ERROR, e.toString());
                break;
            }
        }
    }

    @EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
        commandChains.runServerUnload();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new JsonRecipeEditCommand());
        commandChains.initTags(event.getServer().worldServerForDimension(0));
    }
}
