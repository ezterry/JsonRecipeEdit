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
import com.google.common.reflect.ClassPath;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import org.apache.logging.log4j.Level;

import java.io.IOException;

/**
 * A simple wrapper for out mod integraton helpers.
 *
 * Created by ezterry on 4/30/17.
 */
@SuppressWarnings("WeakerAccess")
public abstract class GenericIntegratedMod {
    static private GenericIntegratedMod instanceChain=null;
    private GenericIntegratedMod nextInstance;

    static {
        ClassLoader loader = Thread.currentThread()
                .getContextClassLoader();
        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader)
                    .getTopLevelClasses("com.ezrol.terry.minecraft.jsonRecipeEdit.integration")) {
                if("GenericIntegratedMod".equals(info.getSimpleName()))
                    continue;
                if(Loader.isModLoaded(info.getSimpleName().toLowerCase())) {
                    try {
                        Class cls = info.load();
                        if(GenericIntegratedMod.class.isAssignableFrom(cls)) {
                            JSONRecipeEdit.log(Level.INFO, "Found Class: " + info.getName());
                            info.load().newInstance();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GenericIntegratedMod(){
        this.nextInstance=instanceChain;
        instanceChain=this;
    }

    static public void initAll(FMLInitializationEvent event){
        GenericIntegratedMod ptr = instanceChain;
        while(ptr != null){
            ptr.init(event);
            ptr = ptr.nextInstance;
        }
    }

    static public void postJsonAll(FMLLoadCompleteEvent event){
        GenericIntegratedMod ptr = instanceChain;
        while(ptr != null){
            ptr.postJson(event);
            ptr = ptr.nextInstance;
        }
    }

    static public boolean runDataDumpAll(String name){
        GenericIntegratedMod ptr = instanceChain;
        while(ptr != null){
            if(ptr.runDataDump(name)){
                return true;
            }
            ptr = ptr.nextInstance;
        }
        return false;
    }

    abstract void init(FMLInitializationEvent event);
    abstract void postJson(FMLLoadCompleteEvent event);

    /**
     * run the named data dump if you know how, otherwise return false
     *
     * @param name - name of the data dump to run
     * @return - if the dump is complete thus don't try to run additional data dumps on this parameter
     */
    @SuppressWarnings("unused")
    public boolean runDataDump(String name){
        return false;
    }
}
