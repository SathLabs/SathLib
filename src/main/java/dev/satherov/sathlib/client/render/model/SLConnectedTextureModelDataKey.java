package dev.satherov.sathlib.client.render.model;

import net.neoforged.neoforge.model.data.ModelProperty;

/// Named handle for a {@link ModelProperty} used by connected-texture rules.
///
/// @param id       stable serialized identifier for the property
/// @param property NeoForge model-data property
/// @param <T>      model-data value type
public record SLConnectedTextureModelDataKey<T>(String id, ModelProperty<T> property) { }
