import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.*;
import java.util.concurrent.*;

/** Real JOGL/AWT context creation; run inside a macOS graphical session. */
class GuiSmoke {
    public static void main(String[] args)throws Exception{
        var ready=new CompletableFuture<String>();var window=new Frame("EasyKnack macOS render check");
        try{
            EventQueue.invokeAndWait(() -> {
                var canvas=new GLCanvas(new GLCapabilities(GLProfile.get(GLProfile.GL3)));
                canvas.addGLEventListener(new GLEventListener(){
                    public void init(GLAutoDrawable drawable){ready.complete(drawable.getGL().glGetString(GL.GL_VERSION));}
                    public void display(GLAutoDrawable drawable){drawable.getGL().glClearColor(.1f,.5f,.4f,1);drawable.getGL().glClear(GL.GL_COLOR_BUFFER_BIT);}
                    public void reshape(GLAutoDrawable drawable,int x,int y,int w,int h){}
                    public void dispose(GLAutoDrawable drawable){}
                });window.add(canvas);window.setSize(500,320);window.setVisible(true);
            });
            System.out.println("PASS: macOS JOGL GL3/AWT context: "+ready.get(20,TimeUnit.SECONDS));
            EventQueue.invokeAndWait(() -> window.setSize(750,480));
        }catch(Throwable error){error.printStackTrace();System.exit(1);}
        finally{EventQueue.invokeAndWait(window::dispose);}
        System.exit(0);
    }
}
