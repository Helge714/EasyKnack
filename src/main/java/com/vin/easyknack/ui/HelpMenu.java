package com.vin.easyknack.ui;

import com.vin.easyknack.BuildInfo;
import com.vingame.glswing.component.GLContainer;
import com.vingame.glswing.component.GLDialog;
import com.vingame.glswing.rendering.GLContext2D;
import com.vingame.glswing.ui.*;

final class HelpMenu {
    private HelpMenu() { }
    static GLPopupMenu create(WorkspaceView owner) {
        var menu = new GLPopupMenu();
        menu.addItem(AppText.text("help.manual"), e -> {
            NodeDocumentation.open(owner,"user/README.md");
        });
        menu.addItem(AppText.text("help.nodeReference"),e->NodeDocumentation.openIndex(owner));
        menu.addItem(AppText.text("help.about"), e -> show(owner, "help.about",
                "EasyKnack\n" + BuildInfo.application().display() + "\n\nGLSwing\n" + BuildInfo.core().display()
                + "\n\nJava " + System.getProperty("java.runtime.version") + " — " + System.getProperty("java.vendor")
                + "\n" + System.getProperty("os.name") + " / " + System.getProperty("os.arch") + "\n\nGPL-3.0-only"));
        menu.addItem(AppText.text("help.licenses"), e -> {
            try { show(owner, "help.licenses", "", HelpDocuments.licenseDocuments()); }
            catch (Exception failure) { show(owner, "help.licenses", failure.getMessage()); }
        });
        return menu;
    }
    static void show(GLContainer owner, String title, String text) {
        show(owner,title,text,java.util.List.of());
    }
    private static void show(GLContainer owner,String title,String text,java.util.List<HelpDocuments.LicenseDocument> documents){
        var dialog = new GLDialog[1];
        var panel = new DocumentPanel(text,documents,()->dialog[0].close());
        dialog[0] = new GLDialog(AppText.text(title), panel);
        dialog[0].setDecoration(GLDialog.Decoration.THEMED); dialog[0].setSize(760,560); dialog[0].show(owner);
    }
    static final class DocumentPanel extends GLContainer {
        final GLTextArea content=new GLTextArea();
        final GLComboBox<HelpDocuments.LicenseDocument> documents;
        final GLButton close=new GLButton(AppText.text("help.close"));
        DocumentPanel(String text,java.util.List<HelpDocuments.LicenseDocument> entries,Runnable onClose){
            documents=entries.isEmpty()?null:new GLComboBox<>(entries);
            content.setEditable(false);content.setLineWrap(true);addGL(content);addGL(close);
            if(documents!=null){addGL(documents);documents.addActionListener(e->loadSelection());}
            close.addActionListener(e->onClose.run());
            setBounds(0,0,760,560); // Establish the text viewport before laying out its document.
            if(documents==null)content.setText(text==null?"":text);else loadSelection();
        }
        private void loadSelection(){
            try{content.setText(documents.getSelectedItem().text());content.setCaretPosition(0);}
            catch(java.io.IOException failure){content.setText(failure.getMessage());}
        }
        protected void record(GLContext2D context) { }
        protected void resized(float w,float h){
            float top=documents==null?16:56;
            if(documents!=null)documents.setBounds(16,16,Math.max(1,w-32),28);
            content.setBounds(16,top,Math.max(1,w-32),Math.max(1,h-top-60));close.setBounds(w-116,h-44,100,28);
        }
    }
}
