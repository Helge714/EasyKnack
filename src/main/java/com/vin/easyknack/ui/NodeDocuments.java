package com.vin.easyknack.ui;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletionStage;
import com.vin.easyknack.nodes.NodeRegistry;
import com.vingame.glswing.document.*;
import com.vingame.glswing.node.document.*;
import com.vingame.glswing.node.model.*;

/** Uses the Core's versioned codec and atomic, asynchronous document I/O. */
final class NodeDocuments implements AutoCloseable {
    private final KnackDocumentCodec codec = new KnackDocumentCodec();
    private final GLDocumentManager manager;
    private final java.util.function.IntConsumer migrationNotice;
    NodeDocuments(){this(count->{});}
    NodeDocuments(java.util.function.IntConsumer migrationNotice) {
        this.migrationNotice=migrationNotice;
        var codecs = new GLDocumentCodecRegistry(); codecs.register(codec);
        manager = new GLDocumentManager(codecs);
    }
    CompletionStage<GLNodeGraphDocument> read(Path path) {
        return manager.open(path, codec, codec.currentSchemaVersion()).thenApply(document -> {
            try {
                int count=BufferContextMigration.count(document.content());var prepared=prepare(document.content());
                if(count>0){java.util.logging.Logger.getLogger(NodeDocuments.class.getName()).warning("Migrated legacy Write position connections to Buffer Context ("+count+" recording buffers)");migrationNotice.accept(count);}
                return prepared;
            } finally { manager.release(document); }
        });
    }
    CompletionStage<Path> write(Path path, GLNodeGraphDocument snapshot) {
        // A separate immutable snapshot prevents edits during I/O from being marked saved.
        var document = manager.create(snapshot, path.getFileName().toString(), codec);
        return manager.saveAs(document, path).whenComplete((result, failure) -> manager.release(document));
    }
    static List<GLNodeGraphAsset> assets(GLNodeGraphDocument document) {
        var result = new ArrayList<GLNodeGraphAsset>(); result.add(document.root());
        result.addAll(document.embeddedAssets().values()); return result;
    }
    static GLNodeGraphDocument prepare(GLNodeGraphDocument document) {
        document=BufferContextMigration.migrate(document);
        document=SpectrumBlurMigration.migrate(document);
        document=SpectrumMixMigration.migrate(document);
        document=InstanceGroupMigration.migrate(document);
        document=SpectralInputMigration.migrate(FftMigration.migrate(DelayMigration.migrate(ReaderPositionMigration.migrate(document))));
        if (!document.externalAssets().isEmpty()) throw new IllegalArgumentException(
                com.vin.easyknack.ui.AppText.literal("Externe Gruppen-Dateien werden noch nicht unterstützt. Bitte Gruppen einbetten."));
        // The former waveform alias is now the public Wavetable signal type.
        Map<String,GLNodeGraphAsset> migrated=new LinkedHashMap<>();
        for(var asset:assets(document)){
            var d=asset.compositeDescriptor();
            if(d!=null){
                var ports=d.portMappings().stream().map(p->{var port=p.publicPort();
                    if(!port.valueTypeId().equals("easyknack.waveform"))return p;
                    return new com.vingame.glswing.node.composite.GLCompositePortMapping(new GLNodePortDescriptor(port.id(),port.direction(),"easyknack.wavetable.signal",port.timing(),port.required(),port.maximumConnections()),p.internalEndpoint(),p.presentation(),p.additionalEndpoints());
                }).toList();
                d=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),ports,d.properties(),d.propertyMappings());
            }
            // Older scopes were inline pass-through nodes. Preserve their downstream
            // routing while moving the analyzer onto a parallel, input-only branch.
            var graph=asset.graph();
            var scopes=graph.nodes().stream().filter(n->n.typeId().startsWith("easyknack.analysis.oscilloscope"))
                    .map(GLGraphNode::id).collect(java.util.stream.Collectors.toSet());
            java.util.function.Function<GLNodeEndpoint,GLNodeEndpoint> bypass=endpoint->{
                var seen=new HashSet<GLNodeId>();var result=endpoint;
                while(scopes.contains(result.nodeId())&&result.portId().equals("audio-out")){
                    if(!seen.add(result.nodeId()))return null;
                    var id=result.nodeId();result=graph.connections().stream()
                        .filter(w->w.input().nodeId().equals(id)&&w.input().portId().equals("audio-in"))
                        .map(GLGraphConnection::output).findFirst().orElse(null);
                    if(result==null)return null;
                }
                return result;
            };
            var wires=graph.connections().stream().map(w->{var output=bypass.apply(w.output());
                return output==null?null:new GLGraphConnection(w.id(),output,w.input());}).filter(Objects::nonNull).toList();
            if(d!=null)d=new com.vingame.glswing.node.composite.GLCompositeNodeDescriptor(d.nodeTypeId(),d.assetId(),d.minimumAssetVersion(),
                d.portMappings().stream().map(p->p.publicPort().direction()==GLPortDirection.OUTPUT
                    ?p.withEndpoints(p.internalEndpoints().stream().map(bypass).filter(Objects::nonNull).toList()):p).toList(),d.properties(),d.propertyMappings());
            migrated.put(asset.id(),new GLNodeGraphAsset(asset.id(),asset.version(),asset.kind(),
                new GLNodeGraphSnapshot(graph.graphId(),graph.revision(),graph.nodes(),wires),asset.viewState(),asset.metadata(),d));
        }
        var migratedRoot=migrated.remove(document.root().id());document=new GLNodeGraphDocument(migratedRoot,migrated,document.externalAssets());
        var types = new GLNodeTypeRegistry(); var nodes = new NodeRegistry(types, new GLNodeCatalog());
        for (var asset : assets(document)) if (asset.compositeDescriptor() != null)
            types.register(asset.compositeDescriptor().nodeType());
        Map<String, GLNodeGraphAsset> prepared = new LinkedHashMap<>();
        for (var asset : assets(document)) {
            var source = asset.graph();
            // Recreate dynamic type contracts, without silently dropping stale connections.
            var graph = new GLNodeGraphSnapshot(source.graphId(), source.revision(),
                    source.nodes().stream().map(nodes::normalize).toList(), source.connections());
            var diagnostics = GLNodeGraphValidator.validate(graph, types, GLGraphCyclePolicy.DELAYED_FEEDBACK).diagnostics();
            for (var diagnostic : diagnostics) if (diagnostic.severity() == GLGraphDiagnostic.Severity.ERROR
                    && !diagnostic.code().equals("port.required") && !diagnostic.code().equals("graph.cycle"))
                throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Ungültiges Node-Dokument: ") + diagnostic.message());
            prepared.put(asset.id(), new GLNodeGraphAsset(asset.id(), asset.version(), asset.kind(), graph,
                    asset.viewState(), asset.metadata(), asset.compositeDescriptor()));
        }
        var root = prepared.remove(document.root().id());
        var result=new GLNodeGraphDocument(root, prepared, List.of());
        var composites=new com.vingame.glswing.node.composite.GLCompositeGraphRegistry();composites.registerDocument(result);
        var normalized=new LinkedHashMap<String,GLNodeGraphAsset>();for(var a:assets(result))normalized.put(a.id(),GroupInputs.migrate(GroupChoices.normalizeSelections(a,composites),composites));
        result=new GLNodeGraphDocument(normalized.remove(result.root().id()),normalized,List.of());
        var choices=new HashMap<String,java.util.List<GroupChoices.Choice>>();
        for(var asset:assets(result))if(asset.compositeDescriptor()!=null)choices.put(asset.compositeDescriptor().nodeTypeId(),GroupChoices.choices(asset,composites));
        for(var asset:assets(result))for(var node:asset.graph().nodes())for(var choice:choices.getOrDefault(node.typeId(),List.of()))
            choice.editor().validate(node.properties().getOrDefault(choice.id(),choice.editor().defaultValue()));
        return result;
    }
    /** Revisions describe edit history, not persistent content; undo can return to the saved state. */
    static GLNodeGraphDocument comparable(GLNodeGraphDocument document) {
        Map<String, GLNodeGraphAsset> assets = new LinkedHashMap<>();
        for (var asset : assets(document)) {
            var graph = asset.graph();
            assets.put(asset.id(), new GLNodeGraphAsset(asset.id(), asset.version(), asset.kind(),
                    new GLNodeGraphSnapshot(graph.graphId(), 0, graph.nodes(), graph.connections()),
                    asset.viewState(), asset.metadata(), asset.compositeDescriptor()));
        }
        var root = assets.remove(document.root().id());
        return new GLNodeGraphDocument(root, assets, document.externalAssets());
    }
    @Override public void close() { manager.close(); }
}
