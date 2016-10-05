import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author IWEERARATHNA
 */
public class Deps {

    public static void main(String[] args) throws Exception {
        File dir = new File("C:\\Projects\\insight5\\nyql\\examples");
        Map<String, Set<String>> calls = new HashMap<>();
        scan(dir, dir, calls);

        System.out.println(calls);
    }

    private static void scan(File src, File baseDir, Map<String, Set<String>> calls) throws Exception {
        if (src.isDirectory()) {
            File[] files = src.listFiles();
            if (files != null) {
                for (File f : files) {
                    scan(f, baseDir, calls);
                }
            }
        } else {
            Set<String> toCalls = findCalls(src);
            String relPath = baseDir.toPath().relativize(src.toPath()).toString().replace("\\", "/");

            System.out.println("Scanning: " + relPath);
            System.out.println("   Called: " + toCalls);
            calls.put(relPath, toCalls);
        }
    }

    private static Set<String> findCalls(File file) throws Exception {
        String code = FileUtils.readFileToString(file);
        String text = code.replaceAll("(/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/)|(//.*)", "");
        //System.out.println(text);

        Set<String> callers = new HashSet<>();
        Pattern pattern = Pattern.compile("(.*?)([^A-Za-z]+[\\$]*IMPORT[\\s\\(]*)\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            callers.add(matcher.group(3));
            //System.out.println(matcher.group(3));
        }

        pattern = Pattern.compile("(.*?)([^A-Za-z]+RUN[\\s\\(]*)\"([^\"]*)\"");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            callers.add(matcher.group(3));
            //System.out.println(matcher.group(3));
        }

        return callers;
    }

}
