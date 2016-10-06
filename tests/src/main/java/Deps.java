import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author IWEERARATHNA
 */
public class Deps {

    public static void main(String[] args) throws Exception {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        File dir = new File("C:\\Projects\\insight5\\nyql\\examples");
//        Map<String, Set<String>> calls = new HashMap<>();
//        scan(dir, dir, calls);
//
//        System.out.println(calls);
        printDependenciesOf(dir, "");
    }

    public static void printDependenciesOf(File scriptDir, String scriptId) throws Exception {
        Map<String, Set<String>> calls = new HashMap<>();
        scan(scriptDir, scriptDir, calls);

        Graph g = createGraph(calls);
        g.display();
    }

    private static Graph createGraph(Map<String, Set<String>> map) throws Exception {
        Graph g = new SingleGraph("nyql", false, true);

        map.forEach((s, strings) -> {
            if (strings.size() > 0) {
                strings.forEach(other -> {
                    Edge edge = g.addEdge(s + other, s, other, true);
                    edge.getSourceNode().setAttribute("label", s);
                    edge.getTargetNode().setAttribute("label", other);
                });
            } else {
                g.addNode(s).setAttribute("label", s);
            }
        });


        g.addAttribute("ui.stylesheet", "node { text-size: 12; }");
        return g;
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
            relPath = StringUtils.substringBeforeLast(relPath, ".");

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
