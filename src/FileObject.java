
import java.io.File;


public class FileObject extends File {
    public FileObject(File f) {
        super(f.getAbsolutePath());
    }
    
    public FileObject(String f) {
        super(f);
    }
    
    public String toString() {
        return getName();
    }
}
