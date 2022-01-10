
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFile {

    private File currentDir;
    private String destZipFile;
    private File[] fileList;
    private File[] dirList;

    public ZipFile(ArrayList<File> selection, String destZipFile, File currentDir) {
        this.destZipFile = destZipFile;
        this.currentDir=currentDir;
        ArrayList<File> files = new ArrayList<>();
        ArrayList<File> dirs = new ArrayList<>();
        for (File file : selection) {
            if (file.isDirectory()) {
                dirs.add(file);
            } else {
                files.add(file);
            }
        }
        fileList = createArray(files);
        dirList = createArray(dirs);
    }

    private File[] createArray(ArrayList<File> list) {
        if (list.size() == 0) {
            return null;
        }
        File[] files = new File[list.size()];
        int i = 0;
        for (File file : list) {
            files[i] = file;
            i++;
        }
        return files;
    }

    public void zipFiles() throws IOException {
        File zipFileName = new File(destZipFile);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        if (fileList != null) {
            System.out.println("Zipping files");
            zipFiles(fileList, out);
        }
        if (dirList!=null) {
            for (int i = 0; i < dirList.length; i++) {
                String dirPath = dirList[i].getAbsolutePath();
                System.out.println("Zipping dir: "+dirPath);
                zipDirectory(dirPath, out);
            }
        }
        out.close();
    }

    private void zipFiles(File[] files, ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[1024];
        for (int i = 0; i < files.length; i++) {
            FileInputStream in = new FileInputStream(files[i]);                                
            String filepath = files[i].getCanonicalPath().substring(currentDir.getCanonicalPath().length() + 1,files[i].getCanonicalPath().length());
            ZipEntry entries = new ZipEntry(filepath);
            zos.putNextEntry(entries);
            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            in.close();
        }
    }

    private void zipDirectory(String dirPath, ZipOutputStream zos) throws IOException {
        File f = new File(dirPath);
        String[] flist = f.list();
        if (flist.length==0) {
            String filepath = f.getCanonicalPath().substring(currentDir.getCanonicalPath().length() + 1,f.getCanonicalPath().length())+"/";
            zos.putNextEntry(new ZipEntry(filepath));            
        } else {
            for (int i = 0; i < flist.length; i++) {
                File ff = new File(f, flist[i]);
                if (ff.isDirectory()) {
                    zipDirectory(ff.getPath(), zos);
                    continue;
                }
                String filepath = ff.getCanonicalPath().substring(currentDir.getCanonicalPath().length() + 1,ff.getCanonicalPath().length());
                ZipEntry entries = new ZipEntry(filepath);
                zos.putNextEntry(entries);
                FileInputStream fis = new FileInputStream(ff);
                int buffersize = 1024;
                byte[] buffer = new byte[buffersize];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, count);
                }
                fis.close();
            }
        }
    }
}
