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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import org.apache.logging.log4j.Level;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * The nbt file for the jsonrecipeedit tags
 * Created by ezterry on 2/19/17.
 */
@SuppressWarnings({"WeakerAccess"})
public class VCommandTagData extends WorldSavedData{
    TreeSet<String> tags;

    @SuppressWarnings("unused")
    public VCommandTagData(String s){
        super(s);
        tags = new TreeSet<>();
    }
    public VCommandTagData(){
        super(JSONRecipeEdit.MODID);
        tags = new TreeSet<>();
    }
    @Override
    @SuppressWarnings("NullableProblems")
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList lst;

        JSONRecipeEdit.log(Level.INFO,"Loading JsonRecipeEdit Tags");
        tags.clear();
        if(nbt.hasKey("tags")){
            lst=nbt.getTagList("tags",8);
        }
        else{
            lst = new NBTTagList();
        }
        for(int i=0; i<lst.tagCount();i++){
            tags.add(lst.getStringTagAt(i));
        }
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList lst=new NBTTagList();
        for(String s:tags){
            lst.appendTag(new NBTTagString(s));
        }
        compound.setTag("tags",lst);
        return(compound);
    }

    public boolean checkTag(String t){
        return tags.contains(t);
    }

    public boolean clearTag(String t,World w){
        boolean r = tags.remove(t);
        if(r){
            markDirty();
            JSONRecipeEdit.commandChains.runTrigger(String.format("OnClear: %s",t),w);
        }
        return(r);
    }
    public boolean setTag(String t,World w){
        boolean r = tags.add(t);
        if(r){
            markDirty();
            JSONRecipeEdit.commandChains.runTrigger(String.format("OnSet: %s",t),w);
        }
        return(r);
    }
    public List getList(){
        LinkedList<String> r = new LinkedList<>();
        for(String s : tags){
            r.addLast(s);
        }
        return r;
    }
}
