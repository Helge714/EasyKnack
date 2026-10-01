package com.vin.easyknack.ui;
import java.util.List;
import com.vingame.glswing.component.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.rendering.GLContext2D;
final class SettingsDialog {
    static void show(WorkspaceView owner){
        var ref=new GLDialog[1];
        var panel=new GLContainer(){
            final GLLabel languageLabel=new GLLabel(AppText.text("settings.language")),themeLabel=new GLLabel(AppText.text("settings.theme"));
            final GLComboBox<String> language=new GLComboBox<>(List.of("Deutsch","English"));
            final List<BundledThemes.Choice> choices=BundledThemes.available();
            final GLComboBox<BundledThemes.Choice> theme=new GLComboBox<>(choices);
            final GLButton save=new GLButton(AppText.text("save")),cancel=new GLButton(AppText.text("cancel"));
            final GLLabel message=new GLLabel(AppText.text("settings.immediate"));
            {language.setSelectedItem(ApplicationSettings.locale().getLanguage().equals("de")?"Deutsch":"English");
                theme.setSelectedItem(choices.stream().filter(c->c.id().equals(ApplicationSettings.theme())).findFirst().orElseGet(()->choices.stream().filter(c->c.resource().equals("/easyknack.gltheme")).findFirst().orElseThrow()));
                for(var c:List.of(languageLabel,themeLabel,language,theme,save,cancel,message))addGL(c);
                cancel.addActionListener(e->ref[0].close());
                save.addActionListener(e->{try{
                    var selected=ApplicationTheme.loadSelection(theme.getSelectedItem().id());
                    ApplicationSettings.save(language.getSelectedItem().equals("Deutsch")?"de":"en",theme.getSelectedItem().id());
                    ApplicationTheme.activate(selected);ref[0].close();owner.applySettings();
                }catch(Exception failure){message.setText(failure.getMessage());}});
            }
            protected void record(GLContext2D c){}
            protected void resized(float w,float h){languageLabel.setBounds(20,20,130,28);language.setBounds(160,20,w-180,28);themeLabel.setBounds(20,65,130,28);theme.setBounds(160,65,w-180,28);message.setBounds(20,110,w-40,44);cancel.setBounds(w-230,h-45,100,28);save.setBounds(w-120,h-45,100,28);}
        };
        ref[0]=new GLDialog(AppText.text("settings"),panel);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(650,215);ref[0].show(owner);
    }
}
