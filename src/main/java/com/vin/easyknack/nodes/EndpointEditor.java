package com.vin.easyknack.nodes;

import com.vin.easyknack.audio.*;
import com.vingame.glswing.node.model.GLNodeValue;
import com.vingame.glswing.ui.GLComboBox;
import java.util.*;
import java.util.function.Consumer;

/** A dynamic device chooser. Unplugged devices stay selected with their saved fallback name. */
public record EndpointEditor(AudioEndpoint fallback) implements Editor {
    @Override public GLNodeValue defaultValue() { return GLNodeValue.of(fallback.reference()); }
    @Override public void validate(GLNodeValue value) {
        if (!(value instanceof GLNodeValue.TextValue text) || AudioEndpoint.reference(text.value()).input() != fallback.input())
            throw new IllegalArgumentException(com.vin.easyknack.ui.AppText.literal("Wrong endpoint direction"));
    }
    @Override public String viewKey() { return "endpoint:" + fallback.input(); }
    @Override public Control create(String label, Consumer<GLNodeValue> changed) {
        class Chooser extends GLComboBox<AudioEndpoint> {
            private AudioEndpoint selected = fallback;
            private boolean refreshing;
            // Strong reference while this control lives; the directory holds only a weak reference.
            private final Runnable refreshListener = this::refresh;
            Chooser() {
                super(List.of(fallback)); setPopupTitle(label); setPopupMatchesComboWidth(false); AudioEndpoints.observe(refreshListener);
                addActionListener(event -> {
                    if (!refreshing && getSelectedItem() != null) {
                        selected = getSelectedItem(); changed.accept(GLNodeValue.of(selected.reference()));
                    }
                }); refresh();
            }
            void refresh() {
                refreshing = true;
                try {
                    var choices = new ArrayList<>(AudioEndpoints.current().endpoints().stream().filter(e -> e.input() == fallback.input()).toList());
                    var choice = choices.stream().filter(e -> e.id().equals(selected.id()) && e.type() == selected.type()).findFirst().orElse(null);
                    if (choice == null) { choice = AudioEndpoint.reference(selected.reference()); choices.add(choice); }
                    removeAllItems(); choices.forEach(this::addItem); setSelectedItem(choice);
                } finally { refreshing = false; }
            }
            void update(GLNodeValue value) { selected = AudioEndpoint.reference(((GLNodeValue.TextValue)value).value()); refresh(); }
        }
        var chooser = new Chooser(); return new Control(chooser, chooser::update);
    }
}

