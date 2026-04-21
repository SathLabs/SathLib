package dev.satherov.sathlib.client.render.model;

/// Unit-scaled region inside a texture atlas.
///
/// @param u0     left UV coordinate in atlas space
/// @param v0     top UV coordinate in atlas space
/// @param width  region width in atlas space
/// @param height region height in atlas space
public record SLConnectedTextureRegion(float u0, float v0, float width, float height) { }
