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

package com.ezrol.terry.minecraft.jsonRecipeEdit.virtualcommandblock;

import com.ezrol.terry.minecraft.jsonRecipeEdit.JSONRecipeEdit;
import net.minecraft.command.ICommandManager;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import java.util.List;

/**
 * A set of virtual command block commands
 * These are to run one after eachother until one fails
 * Created by ezterry on 2/18/17.
 */
public class VCommandSet {
    private String trigger;
    private String filter;
    private String dim;//dimention number or "*"
    private int dimnum;
    private List<String> commands;
    private String name;
    private boolean log;

    public VCommandSet(String trigger, String filter, String dim, List<String> commands,boolean log,String name){
        this.trigger=trigger;
        this.filter=filter;
        this.dim=dim;
        try{
            this.dimnum = Integer.decode(dim);
        }catch(NumberFormatException e){
            this.dimnum = 0;
        }
        this.commands=commands;
        this.name = name;
        this.log = log;
    }

    @SuppressWarnings({"WeakerAccess"})
    public boolean isValid(){
        //validate log errors and return true/false
        boolean triggervalid = false;

        //check the trigger
        if("Always".equals(trigger) || "PlayerJoin".equals(trigger) || "ServerStart".equals(trigger)){
            triggervalid=true;
        }
        else if(trigger.startsWith("OnSet: ") || trigger.startsWith("OnClear: ") || trigger.startsWith("Run: ")){
            triggervalid=true;
        }

        if(!triggervalid){
            JSONRecipeEdit.log(Level.ERROR,String.format("Invalid trigger: %s",trigger));
            return(false);
        }

        //check the dimension
        try {
            if (!dim.equals("*")) {
                //try and parse the dim number
                //noinspection ResultOfMethodCallIgnored
                Integer.decode(dim);
            }
        }
        catch(NumberFormatException e){
            JSONRecipeEdit.log(Level.ERROR,String.format("The dimension number provided is not valid: %s",dim));
            return(false);
        }
        return true;
    }

    @SuppressWarnings({"WeakerAccess"})
    public String getTrigger(){
        return trigger;
    }

    @SuppressWarnings({"WeakerAccess"})
    public void evaluate(ICommandManager manager, World w, int d){
        //check dimension
        if((!dim.equals("*")) && (dimnum != d)){
            return;
        }
        //check filter

        //run ever (n) ticks filter
        if(filter.startsWith("%")){
            try {
                if (0 != w.getWorldTime() % Long.parseLong(filter.substring(1))) {
                    return; //pass
                }
            }
            catch(Exception e){
                JSONRecipeEdit.log(Level.ERROR,String.format("Unable to parse filter: %s",filter));
                return;//error
            }
        }
        //run at time of day
        if(filter.startsWith("@")){
            try {
                long tm = Long.parseLong(filter.substring(1));
                if (tm != w.getWorldTime() % 24000L) {
                    return; //pass
                }
            }
            catch(Exception e){
                JSONRecipeEdit.log(Level.ERROR,String.format("Unable to parse filter: %s",filter));
                return;//error
            }
        }
        //run if tag is cleared
        if(filter.startsWith("!")){
            //when tag is cleared
            if(JSONRecipeEdit.commandChains.getTags().checkTag(filter.substring(1))){
                return;//tag exists
            }
        }
        //run if tag is set
        if(filter.startsWith("=")){
            //when tag is cleared
            if(!JSONRecipeEdit.commandChains.getTags().checkTag(filter.substring(1))){
                return;//tag is missing
            }
        }

        //JSONRecipeEdit.log(Level.INFO,String.format("running on dim %d",d));
        //run commands
        VCommandSender sender = new VCommandSender(w);
        sender.setLog(log);
        sender.setName(name);

        if(log) {
            JSONRecipeEdit.log(Level.INFO,String.format("Running chain %s on dim %d",name,d));
        }
        for(String c : commands){
            manager.executeCommand(sender,c);
            if(!sender.popSuccess()){
                //an error happened stop the cain
                break;
            }
        }
    }
}
