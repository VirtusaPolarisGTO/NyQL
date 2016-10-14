package nyql.tests;

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
        whoAreMyCallers(dir, "sakila/top_customers");
    }

    public static void whoAreMyCallers(File scriptDir, String scriptId) throws Exception {
        Map<String, Set<String>> calls = new HashMap<>();
        scan(scriptDir, scriptDir, calls);

        Map<String, Set<String>> inMap = createInMap(calls);
        Map<Integer, Set<String>> callees = findCallees(inMap, scriptId);

        if (callees.size() == 0) {
            System.out.println("No script calls this script '" + scriptId + "'.");
        } else {
            callees.keySet().stream().sorted().forEach(key -> {
                if (key == 0) {
                    System.out.println("Direct callers:");
                } else {
                    System.out.println("Callers in level " + key + ":");
                }

                callees.get(key).forEach(val -> System.out.println("  > " + val));
            });
        }
        //Graph g = createGraph(calls);
        //g.display();
    }

    private static Map<Integer, Set<String>> findCallees(Map<String, Set<String>> inMap, String scriptId) {
        Map<String, Integer> lvlMap = new HashMap<>();
        if (!inMap.containsKey(scriptId)) {
            return new HashMap<>();
        }

        Set<String> visited = new HashSet<>();
        bfs(scriptId, 0, inMap, visited, lvlMap);

        Map<Integer, Set<String>> aggr = new HashMap<>();
        lvlMap.forEach((s, integer) -> {
            if (!aggr.containsKey(integer)) {
                aggr.put(integer, new HashSet<>());
            }
            aggr.get(integer).add(s);
        });

        return aggr;
    }

    private static void bfs(String scrId, int level, Map<String, Set<String>> inMap, Set<String> visited, Map<String, Integer> lvlMap) {
        if (!inMap.containsKey(scrId)) {
            return;
        }

        Set<String> cls = inMap.get(scrId);
        cls.removeAll(visited);

        cls.forEach(it -> lvlMap.put(it, level));

        cls.forEach(it -> {
            visited.add(it);

            bfs(it, level + 1, inMap, visited, lvlMap);
        });
    }

    private static Map<String, Set<String>> createInMap(Map<String, Set<String>> map) {
        Map<String, Set<String>> inns = new HashMap<>();

        map.forEach((s, strings) -> {
            if (strings.size() > 0) {
                strings.forEach(other -> {
                    if (!inns.containsKey(other)) {
                        inns.put(other, new HashSet<>());
                    }
                    inns.get(other).add(s);
                });
            }
        });
        return inns;
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

            //System.out.println("Scanning: " + relPath);
            //System.out.println("   Called: " + toCalls);
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
