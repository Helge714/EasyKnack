package com.vin.easyknack.ui;

import java.util.HashMap;
import com.vin.easyknack.nodes.NodeKind;
import com.vingame.glswing.node.document.GLNodeGraphAsset;
import com.vingame.glswing.node.model.*;

/** Initial presentation choice only; later interface changes never override the user. */
final class GroupKinds {
    static GLNodeGraphAsset initialize(GLNodeGraphAsset asset) {
        if(asset.metadata().containsKey("easyknack.kind"))return asset;
        var kind=asset.compositeDescriptor().portMappings().stream()
            .filter(p->p.publicPort().direction()==GLPortDirection.OUTPUT)
            .map(p->NodeKind.fromPort(p.publicPort().valueTypeId())).findFirst().orElse(NodeKind.VALUE);
        var metadata=new HashMap<>(asset.metadata());metadata.put("easyknack.kind",GLNodeValue.of(kind.name()));
        return new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),asset.graph(),asset.viewState(),metadata,asset.compositeDescriptor());
    }
}
