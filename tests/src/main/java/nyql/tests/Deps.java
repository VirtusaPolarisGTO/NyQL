package nyql.tests;

import org.apache.commons.io.IOUtils;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author IWEERARATHNA
 */
public class Deps {

    public static void main(String[] args) throws Exception {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        File dir = new File("C:\\Projects\\work\\scripts\\database\\work\\scripts");
//
//
//        Map<String, Set<String>> calls = new TreeMap<>();
//        scan(dir, dir, calls);
//
//        //System.out.println(calls);
//        calls.forEach((k, v) -> {
//            System.out.println(k);
//            v.forEach(c -> System.out.println("\t" + c));
//        });
//        Graph g = createGraph(calls);
//        Viewer display = g.display();
//        display.getDefaultView().getCamera().setViewPercent(0.75);
//        //whoAreMyCallers(dir, "sakila/top_customers");
        //allCallers(dir, "dashboard/violation_breakdown/join/violation_join_clause");
        //tableDependency(dir, "Violation");
        //printEmptyTables(dir);
        //createGraph(dir);
        printEmptyTables(dir);
    }

    public static void tableUsage(File scriptDir) throws Exception {

    }

    public static void printEmptyTables(File scriptDir) throws Exception {
        Map<String, ScriptInfo> info = scanScriptDir(scriptDir);
        Map<String, Set<String>> inMap = createInMap(info);

        for (Map.Entry<String, ScriptInfo> entry : info.entrySet()) {
            ScriptInfo scriptInfo = entry.getValue();
            if (!scriptInfo.getQueryType().equalsIgnoreCase("script")
                && (scriptInfo.getTables() == null || scriptInfo.getTables().isEmpty())) {
                System.out.println("[EMPTY TABLE] Script " + entry.getKey() + " ");
            }
        }

        for (Map.Entry<String, ScriptInfo> entry : info.entrySet()) {
            if (entry.getValue().getQueryType().equalsIgnoreCase("$q")) {
                if (entry.getValue().getCalls() == null || entry.getValue().getCalls().isEmpty()) {
                    Set<String> callees = inMap.get(entry.getKey());
                    if (callees == null || callees.isEmpty()) {
                        System.out.println("[UNUSED] " + entry.getKey());
                    }
                }
            }
        }
    }

    public static void tableDependency(File scriptDir, String tableName) throws Exception {
        Map<String, ScriptInfo> info = scanScriptDir(scriptDir);
        Map<String, String> results = new TreeMap<>();
        int maxLen = -1;
        for (Map.Entry<String, ScriptInfo> entry : info.entrySet()) {
            String chain = hasTable(info, entry.getValue(), tableName, "");
            if (chain != null) {
                if (chain.startsWith(",")) {
                    chain = chain.substring(1).replace(",", " > ");
                }
                maxLen = Math.max(maxLen, entry.getKey().length());
                results.put(entry.getKey(), chain);
            }
        }

        maxLen += 8;

        System.out.println("Table '" + tableName + "' is being used by " + results.size() + " script(s)!");
        for (Map.Entry<String, String> entry : results.entrySet()) {
            String prefix = "  [" + entry.getKey() + "]";
            System.out.print(prefix);
            if (entry.getValue().length() > 0) {
                for (int i = 0; i < maxLen - prefix.length(); i++) {
                    System.out.print(".");
                }
                System.out.println(" => " + entry.getValue());
            } else {
                System.out.println();
            }
        }
    }

    private static String hasTable(Map<String, ScriptInfo> map, ScriptInfo scriptInfo, String tableName, String chain) {
        if (scriptInfo.hasTable(tableName)) {
            return chain;
        } else {
            for (String c : scriptInfo.getCalls()) {
                ScriptInfo call = map.get(c);
                if (call != null) {
                    String fdep = hasTable(map, call, tableName, chain + "," + call.getName());
                    if (fdep != null) {
                        return fdep;
                    }
                }
            }
            return null;
        }
    }

    public static void allCallers(File scriptDir, String scriptId) throws Exception {
        Map<String, ScriptInfo> info = scanScriptDir(scriptDir);
        Map<String, Set<String>> inMap = createInMap(info);
        Map<Integer, Set<String>> callees = findCallees(inMap, scriptId);

        System.out.println("Done.");
        System.out.println();
        System.out.println();

        System.out.println("Script: [" + scriptId + "] -------------------------------------------------------------");
        ScriptInfo scr = info.get(scriptId);
        System.out.println("Using tables:");
        scr.getTables().forEach(t -> System.out.println("\t" + t));

        if (callees.size() == 0) {
            System.out.println("No script calls this script '" + scriptId + "'.");
        } else {
            callees.keySet().stream().sorted().forEach(key -> {
                if (key == 0) {
                    System.out.println("Direct callers:");
                } else {
                    System.out.println("Callers in level " + key + ":");
                }

                callees.get(key).forEach(val -> System.out.println("\t  > " + val));
            });
        }
        //Graph g = createGraph(calls);
        //g.display();
    }

    private static Map<String, ScriptInfo> scanScriptDir(File dir) throws Exception {
        System.out.println("Scanning script directory: " + dir.getAbsolutePath() + "...");
        Map<String, ScriptInfo> info = new HashMap<>();
        scan(dir, dir, info);
        System.out.println("Done.");
        System.out.println();
        System.out.println();
        System.out.println();
        return info;
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

    private static Map<String, Set<String>> createInMap(Map<String, ScriptInfo> map) {
        Map<String, Set<String>> inns = new HashMap<>();

        map.forEach((s, info) -> {
            if (info.getCalls().size() > 0) {
                info.getCalls().forEach(other -> {
                    if (!inns.containsKey(other)) {
                        inns.put(other, new HashSet<>());
                    }
                    inns.get(other).add(s);
                });
            }
        });

        inns.forEach((s, callees) -> {
            ScriptInfo sinfo = map.get(s);
            if (sinfo != null) {
                sinfo.setCallees(callees);
            }
        });
        return inns;
    }

    private static Graph createGraph(File scriptsDir) throws Exception {
        Map<String, ScriptInfo> map = scanScriptDir(scriptsDir);
        Graph g = new SingleGraph("nyql", false, true);

        map.forEach((s, strings) -> {
            if (strings.getCalls().size() > 0) {
                strings.getCalls().forEach(other -> {
                    Node fNode = g.getNode(s) == null ? g.addNode(s) : g.getNode(s);
                    Node tNode = g.getNode(other) == null ? g.addNode(other) : g.getNode(other);

                    Edge edge = g.addEdge(s + other, fNode, tNode, true);
                    //fNode.setAttribute("label", captureName(s));
                    //tNode.setAttribute("label", captureName(other));

                    fNode.setAttribute("ui.class", convToCSSClass(strings.getQueryType()));
                    if (map.get(other) != null) {
                        tNode.setAttribute("ui.class", convToCSSClass(map.get(other).getQueryType()));
                    }
                });
            } else {
                if (g.getNode(s) == null) {
                    Node node = g.addNode(s);
                    //node.setAttribute("label", captureName(s));
                    node.setAttribute("ui.class", convToCSSClass(strings.getQueryType()));
                }

            }
        });

        String css = readGraphCSS();
        g.addAttribute("ui.stylesheet", css);

        FileSinkImages pic = new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.HD1080);
        pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

        Viewer display = g.display();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        display.getDefaultView().resizeFrame((int)screenSize.getWidth(), (int)screenSize.getHeight());

        pic.writeAll(g, "scripts.png");
        //display.getDefaultView().getCamera().setViewPercent(0.75);
        return g;
    }

    private static String convToCSSClass(String type) {
        return "ny" + type.replace("$", "").toLowerCase();
    }

    private static String readGraphCSS() {
        try (InputStream inputStream = Deps.class.getResourceAsStream("/graph.css")) {
            return IOUtils.readLines(inputStream, StandardCharsets.UTF_8).stream()
                .collect(Collectors.joining("\n"));
        } catch (Exception ex) {
            System.out.println("Error occurred while reading css!");
            ex.printStackTrace();
            return null;
        }
    }

    private static String captureName(String s) {
        int pos = s.lastIndexOf('/');
        if (pos > 0) {
            return s.substring(pos + 1);
        }
        return s;
    }

    private static void scan(File src, File baseDir, Map<String, ScriptInfo> calls) throws Exception {
        if (src.isDirectory()) {
            File[] files = src.listFiles();
            if (files != null) {
                for (File f : files) {
                    scan(f, baseDir, calls);
                }
            }
        } else {
            String ext = fileExtension(src);
            if (!ext.equalsIgnoreCase("groovy")) {
                return;
            }
            ScriptInfo scriptInfo = parseScript(src);
            String relPath = baseDir.toPath().relativize(src.toPath()).toString().replace("\\", "/");
            relPath = substrBeforeLast(relPath, ".");
            scriptInfo.setName(relPath);

            //System.out.println("Scanning: " + relPath);
            //System.out.println("   Called: " + toCalls);
            calls.put(relPath, scriptInfo);
        }
    }

    private static String substrBeforeLast(String text, String sep) {
        if (text == null || text.isEmpty() || sep == null || sep.isEmpty()) {
            return text;
        }
        int pos = text.lastIndexOf(sep);
        if (pos >= 0) {
            return text.substring(0, pos);
        }
        return text;
    }

    private static ScriptInfo parseScript(File file) throws Exception {
        return GVisitor.scan(file);
    }

    private static String fileExtension(File file) {
        int pos = file.getAbsolutePath().lastIndexOf('.');
        if (pos > 0) {
            return file.getAbsolutePath().substring(pos + 1);
        }
        return "";
    }

}
