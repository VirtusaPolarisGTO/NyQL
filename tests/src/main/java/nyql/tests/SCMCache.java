package nyql.tests;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gitective.core.CommitUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author IWEERARATHNA
 */
public class SCMCache {

    private Repository repository;
    private Git git;
    private File gDir;

    Map<File, String> lastCommitters = new HashMap<>();

    public void init(File dir) throws Exception {
        repository = new FileRepositoryBuilder().findGitDir(dir).build();
        git = new Git(repository);

        gDir = repository.getDirectory().getCanonicalFile().getParentFile();
    }

    String lastCommitAuthor(File file) throws Exception {
        if (lastCommitters.containsKey(file)) {
            return lastCommitters.get(file);
        }
        String relPath = gDir.toPath().relativize(file.getCanonicalFile().toPath()).toString().replace('\\', '/') + ".groovy";
        RevCommit lastCommit = CommitUtils.getLastCommit(repository, relPath);
        String author = lastCommit.getAuthorIdent().getName();
        lastCommitters.put(file, author);
        return author;
    }

    void close() {
        repository.close();
    }
}
