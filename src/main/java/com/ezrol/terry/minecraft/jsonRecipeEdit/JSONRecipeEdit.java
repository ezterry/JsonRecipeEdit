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
import com.ezrol.terry.minecraft.jsonRecipeEdit.commands.AddRecipe;
import com.ezrol.terry.minecraft.jsonRecipeEdit.commands.DeleteRecipe;
import com.google.gson.*;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings({"WeakerAccess", "unused"})
@Mod(
        modid = JSONRecipeEdit.MODID,
        version = JSONRecipeEdit.VERSION,
        acceptedMinecraftVersions = "[1.11.2,1.11.9]",
        dependencies = "before:jei;required-after:forge@[13.20.0.2227,)"
)
public class JSONRecipeEdit {
    public static final String MODID = "jsonrecipeedit";
    public static final String VERSION = "${version}";

    private JsonArray script;

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

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log(Level.INFO,"Get JSON file");

        File jsonFile = TransformToJsonFile(event.getSuggestedConfigurationFile());
        if (!jsonFile.exists()) {
            log(Level.ERROR,
                    String.format("Json Recipe Edit unable to find json file: %s", jsonFile.getAbsolutePath()));
            script=new JsonArray();
        }
        else{
            try {
                FileReader f = new FileReader(jsonFile);
                script=new Gson().fromJson(f,JsonArray.class);
            }
            catch (JsonSyntaxException e){
                log(Level.ERROR,String.format("Expected to find a JSON array in %s",jsonFile.getAbsolutePath()));
                script=new JsonArray();
            }
            catch (IOException e){
                log(Level.ERROR,"Error reading in JSON file");
                log(Level.ERROR,e.toString());
                script=new JsonArray();
            }
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        CommandRegistry cr = CommandRegistry.getInstance();

        cr.register(new DeleteRecipe());  //"delete recipe"
        cr.register(new AddRecipe(true));  //"shaped recipe"
        cr.register(new AddRecipe(false)); //"shapeless recipe"
    }

    @EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        CommandRegistry cr = CommandRegistry.getInstance();

        //this is when we run the "script" of recipe edits
        log(Level.INFO,"Running json script");
        for(JsonElement e : script){
            if(e.isJsonPrimitive()){
                //assume its a comment and ignore
                continue;
            }
            if(e.isJsonObject()){
                //process the object
                JsonObject cmd = e.getAsJsonObject();
                if(!cmd.has("command")){
                    log(Level.ERROR,String.format("command not in entry: %s",cmd.toString()));
                    continue;
                }
                if(!cmd.get("command").isJsonPrimitive()){
                    log(Level.ERROR,String.format("unexpected json element in entry: %s",cmd.toString()));
                    continue;
                }
                String commandName = cmd.get("command").getAsString();
                if(cr.hasCommand(commandName)){
                    //run the command
                    try {
                        cr.getCommand(commandName).runCommand(cmd);
                    }catch(Exception usererr){
                        log(Level.ERROR,String.format("Error on command: %s",usererr.toString()));
                        log(Level.ERROR,String.format("While running command: %s",cmd.toString()));
                    }
                }
                else{
                    log(Level.ERROR, String.format("Unknown command: [%s] in %s",commandName,cmd.toString()));
                }
            }
            else{
                //???
                log(Level.ERROR,"Unexpected entry in json script! terminating script processing");
                log(Level.ERROR,e.toString());
                break;
            }
        }
    }
}
