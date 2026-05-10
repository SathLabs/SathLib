package dev.satherov.sathlib.compat.framedblocks;

import lombok.experimental.UtilityClass;

import dev.satherov.sathlib.client.model.data.SLModelProperty;
import dev.satherov.sathlib.client.model.data.SLModelPropertyValue;

import net.neoforged.neoforge.model.data.ModelData;

import org.jspecify.annotations.Nullable;

import io.github.xfacthd.framedblocks.api.model.data.AbstractFramedBlockData;
import io.github.xfacthd.framedblocks.api.model.data.FramedBlockData;
import io.github.xfacthd.framedblocks.api.model.data.ModelDataEntry;

import java.util.Objects;

///
/// FramedBlocks-specific model-data helpers.
///
@UtilityClass
public class FramedBlocksModelDataHelper {
    
    ///
    /// Resolves the given SathLib model property from FramedBlocks nested query data.
    ///
    /// @param <T>      model property value type
    /// @param property SathLib model property to resolve
    /// @param data     model data to inspect
    ///
    /// @return resolved model property value, or `null`
    ///
    public static <T extends SLModelPropertyValue> @Nullable T resolveModelPropertyValue(final SLModelProperty<T> property, final ModelData data) {
        final AbstractFramedBlockData framedData = data.get(AbstractFramedBlockData.PROPERTY);
        if (framedData == null) return null;
        
        if (framedData instanceof final FramedBlockData singleData) {
            return FramedBlocksModelDataHelper.resolveModelPropertyValue(property, singleData);
        }
        
        final T firstPartValue = FramedBlocksModelDataHelper.resolveModelPropertyValue(property, framedData.unwrap(false));
        final T secondPartValue = FramedBlocksModelDataHelper.resolveModelPropertyValue(property, framedData.unwrap(true));
        
        if (firstPartValue == null) return secondPartValue;
        if (secondPartValue == null) return firstPartValue;
        return Objects.equals(firstPartValue, secondPartValue) ? firstPartValue : null;
    }
    
    ///
    /// Resolves the given SathLib model property from the framed block data query payload.
    ///
    /// @param <T>        model property value type
    /// @param property   SathLib model property to resolve
    /// @param framedData framed block data to inspect
    ///
    /// @return resolved model property value, or `null`
    ///
    public static <T extends SLModelPropertyValue> @Nullable T resolveModelPropertyValue(final SLModelProperty<T> property, final FramedBlockData framedData) {
        final ModelDataEntry<?> queryData = framedData.getQueryData();
        if (queryData == null) return null;
        if (queryData.property() != property.property()) return null;
        return FramedBlocksModelDataHelper.castQueryData(queryData);
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends SLModelPropertyValue> @Nullable T castQueryData(final ModelDataEntry<?> queryData) {
        final Object value = queryData.data();
        return value instanceof SLModelPropertyValue ? (T) value : null;
    }
}
