package dev.satherov.sathlib.data.impl;

import dev.satherov.sathlib.SathLib;
import dev.satherov.sathlib.client.lang.FormattingLang;
import dev.satherov.sathlib.client.lang.GenericLang;
import dev.satherov.sathlib.client.lang.InputLang;

import net.neoforged.neoforge.common.data.LanguageProvider;

import net.minecraft.data.PackOutput;

///
/// Built-in English language provider for SathLib translation entries.
///
public class SLLanguageProvider extends LanguageProvider {
    
    ///
    /// Creates the SathLib language provider.
    ///
    /// @param output generated data output
    ///
    public SLLanguageProvider(PackOutput output) {
        super(output, SathLib.MOD_ID, "en_us");
    }
    
    @Override
    protected void addTranslations() {
        GenericLang.translate(this::add);
        InputLang.translate(this::add);
        FormattingLang.translate(this::add);
    }
}
