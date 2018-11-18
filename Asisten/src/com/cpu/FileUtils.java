package com.cpu;

import java.io.*;
import com.cpu.init.ShellExecuter;

public class FileUtils {

    public static String readFileBash(String path) {
        return new ShellExecuter().Executer("cat "+path);
    }
    public static String newFile(String newf) {
        return new ShellExecuter().Executer("touch "+newf);
    }
    public static String newFolder(String path) {
        return new ShellExecuter().Executer("mkdir "+path);
    }
    public static String removeFile(String rfile) {
        return new ShellExecuter().Executer("rm "+rfile);
    }
    public static String removeFolder(String path) {
        return new ShellExecuter().Executer("rm -R "+path);
    }
    public static String moveFile(String here, String dest) {
        return new ShellExecuter().Executer("mv "+here+" "+dest);
    }
    public static String copyFile(String file, String dest) {
        return new ShellExecuter().Executer("cp "+file+" "+dest);
    }

    public static String readFile(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(path);
            char buff[] = new char[1024];
            int c;
            while ((c = fr.read(buff)) != -1) {
                sb.append(buff, 0, c);
            }
        } catch (FileNotFoundException e) {
            return "File not found (TODO)";
        } catch (IOException ioe) {
            return "IOException (TODO)";
        }
        return sb.toString();
    }

    public static void saveCode(String code, String charset, String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), charset);
        osw.append(code).flush();
        osw.close();
    }
}
