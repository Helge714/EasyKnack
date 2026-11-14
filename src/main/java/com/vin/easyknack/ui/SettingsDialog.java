package com.vin.easyknack.ui;
import java.util.List;
import com.vingame.glswing.component.*;
import com.vingame.glswing.ui.*;
import com.vingame.glswing.rendering.GLContext2D;
final class SettingsDialog {
    static void show(WorkspaceView owner){
        var ref=new GLDialog[1];var committed=new boolean[1];double oldCurvature=ApplicationSettings.connectionCurvature();
        var oldLocale=ApplicationSettings.locale();var oldTheme=ApplicationTheme.current();
        var panel=new GLContainer(){
            final GLLabel languageLabel=new GLLabel(AppText.text("settings.language")),themeLabel=new GLLabel(AppText.text("settings.theme"));
            final GLComboBox<String> language=new GLComboBox<>(List.of("Deutsch","English"));
            final List<BundledThemes.Choice> choices=BundledThemes.available();
            final GLComboBox<BundledThemes.Choice> theme=new GLComboBox<>(choices);
            final GLButton save=new GLButton(AppText.text("save")),cancel=new GLButton(AppText.text("cancel"));
            final GLLabel autosaveLabel=new GLLabel(AppText.text("settings.autosave")),retentionLabel=new GLLabel(AppText.text("settings.retention"));
            final GLNumberSpinner interval=new GLNumberSpinner(5,3600,ApplicationSettings.autosaveSeconds()),retention=new GLNumberSpinner(1,365,ApplicationSettings.recoveryDays());
            final GLSlider curvature=GLSlider.fixed(AppText.text("settings.curvature"),0,1,oldCurvature);
            final GLLabel message=new GLLabel("");
            boolean previewing;
            {language.setSelectedItem(ApplicationSettings.locale().getLanguage().equals("de")?"Deutsch":"English");
                theme.setSelectedItem(choices.stream().filter(c->c.id().equals(ApplicationTheme.normalizeSelection(ApplicationSettings.theme()))).findFirst().orElseGet(()->choices.stream().filter(c->c.resource().equals(ApplicationTheme.DEFAULT_RESOURCE)).findFirst().orElseThrow()));
                for(var c:List.of(languageLabel,themeLabel,language,theme,autosaveLabel,retentionLabel,interval,retention,curvature,save,cancel,message))addGL(c);
                interval.setIntegerValuesOnly(true);interval.setUnit("s");retention.setIntegerValuesOnly(true);retention.setUnit(AppText.text("settings.days"));
                curvature.setToolTipText(AppText.text("settings.curvature.hint"));curvature.addChangeListener(e->owner.previewCurvature(curvature.getValue()));
                language.addActionListener(e->preview());theme.addActionListener(e->preview());
                cancel.addActionListener(e->ref[0].close());
                save.addActionListener(e->{try{
                    var selected=ApplicationTheme.loadSelection(theme.getSelectedItem().id());
                    ApplicationSettings.save(language.getSelectedItem().equals("Deutsch")?"de":"en",theme.getSelectedItem().id(),(int)interval.getValue(),(int)retention.getValue(),curvature.getValue());
                    ApplicationTheme.activate(selected);committed[0]=true;ref[0].close();owner.applySettings();
                }catch(Exception failure){message.setText(failure.getMessage());}});
            }
            void preview(){
                if(previewing||theme.getSelectedItem()==null)return;previewing=true;
                try{
                    owner.previewAppearance(java.util.Locale.forLanguageTag(language.getSelectedItem().equals("Deutsch")?"de":"en"),ApplicationTheme.loadSelection(theme.getSelectedItem().id()));
                    GLParent root=this;while(root instanceof GLContainer container&&container.getGLParent()!=null)root=container.getGLParent();
                    root.setGLTheme(ApplicationTheme.current().theme());root.setGLLocalizer(GLLocalizer.forLocale(ApplicationSettings.locale()));
                    LiveLanguage.refresh(this);retention.setUnit(AppText.text("settings.days"));message.setText("");
                }catch(Exception failure){message.setText(failure.getMessage());}finally{previewing=false;}
            }
            protected void record(GLContext2D c){}
            protected void resized(float w,float h){languageLabel.setBounds(20,20,130,28);language.setBounds(160,20,w-180,28);themeLabel.setBounds(20,65,130,28);theme.setBounds(160,65,w-180,28);autosaveLabel.setBounds(20,110,250,28);interval.setBounds(290,110,w-310,28);retentionLabel.setBounds(20,155,250,28);retention.setBounds(290,155,w-310,28);curvature.setBounds(20,205,w-40,32);message.setBounds(20,250,w-40,44);cancel.setBounds(w-230,h-45,100,28);save.setBounds(w-120,h-45,100,28);}
        };
        ref[0]=new GLDialog(AppText.text("settings"),panel);ref[0].setDecoration(GLDialog.Decoration.THEMED);ref[0].setSize(650,355);ref[0].closedFuture().thenRun(()->{if(!committed[0]){owner.previewAppearance(oldLocale,oldTheme);ApplicationSettings.previewLocale(null);owner.previewCurvature(oldCurvature);}});ref[0].show(owner);
    }
}
