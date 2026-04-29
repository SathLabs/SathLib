package dev.satherov.sathlib.compat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("doclint:missing")
public enum Mods implements CompatMod {
    JADE("jade"),
    JEI("jei"),
    EMI("emi"),
    FRAMED_BLOCKS("framedblocks")
    ;
    
    private final String modId;
}
