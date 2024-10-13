import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.io.FileWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Git {
    public static void main(String[] args) throws IOException {
        // deletes all git files created by last test run
        deleteGit();

        // initialize git
        Git git = new Git();

        String workingDir = git.getWorkingDir() + "/";

        File file1 = new File(workingDir + "/file1");
        file1.createNewFile();
        BufferedWriter write = new BufferedWriter(new FileWriter(file1));
        write.write("This is a test");
        write.close();
        File folder = new File(workingDir + "/folder");
        folder.mkdir();
        File file2 = new File(workingDir + "/folder/file2");
        file2.createNewFile();
        BufferedWriter write2 = new BufferedWriter(new FileWriter(file2));
        write2.write("This is a test");
        write2.close();

        git.stage(folder.getPath());
        git.stage(file1.getPath());

        git.commit("manos", "please work");

        File file3 = new File(workingDir + "/file3");
        file3.createNewFile();
        git.stage(file3.getPath());

        git.commit("manos", "please work again");

        FileWriter f = new FileWriter(file1.getPath());
        f.write("This is a test2");
        f.close();

        git.stage(file1.getPath());

        // System.out.println("\naddDirectory(String) test... Should print hash: " +
        // git.addDirectory("tester");
        // git.reformatIndex();
    }

    // if the printed messages are too wordy/redundant, u can set printMessages to
    // false (or just delete them)
    public static boolean printMessages = true;
    private String workingDirName;
    private int numOfCommits;
    private String mostRecentCommitHash;

    // Git constructor checks if a git folder already exists in the parent directory
    // if a git directory doesn't exist, :
    // a new git directory is created in the parent directory
    // and an index file is created in the git directory
    // if a git directory already exists:
    // "Git Repository already exists" will be printed
    //
    // working directory is automatically named "testFolder"
    public Git() throws IOException {
        File gitFolder = new File("git");
        if (!gitFolder.exists()) {
            gitFolder.mkdir();
            File create = new File("git/index");
            try {
                create.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                ;
            }
            create = new File("git/objects");
            create.mkdir();
            create = new File("git/HEAD");
            create.createNewFile();
            create = new File("git/testFolder");
            create.mkdir();
            workingDirName = "testFolder";
            numOfCommits = 0;
        } else
            System.out.println("Git Repository already exists");
    }

    public String getWorkingDir() {
        return "git/" + workingDirName;
    }

    public void createString() throws IOException {
        for (File f : (new File("git/testFolder")).listFiles()) {
            stage(f.getPath());
        }
    }

    public void firstCommit() throws IOException {
        File commit = new File("git/objects/commit");
        commit.createNewFile();
    }

    public String commit(String author, String comment) throws IOException {
        String folderHash = addDirectory("git/testFolder");

        File comm = new File("git/commit" + numOfCommits);
        comm.createNewFile();
        StringBuilder st = new StringBuilder();

        BufferedWriter bw = new BufferedWriter(new FileWriter(comm.getPath()));
        bw.write("tree: " + folderHash + "\n");
        st.append("tree: " + folderHash + "\n");
        if (numOfCommits != 0) {
            bw.write("parent: " + mostRecentCommitHash + "\n");
            st.append("parent: " + mostRecentCommitHash + "\n");
        } else {
            bw.write("parent: " + "\n");
            st.append("parent: " + "\n");
        }
        bw.write("name: " + author + "\n");
        st.append("name: " + author + "\n");
        bw.write("date: do this soon please" + "\n");
        st.append("date: do this soon please" + "\n");
        bw.write("message: " + comment + "\n");
        st.append("message: " + comment + "\n");
        bw.close();
        String commHash = computeSHA1("git/commit" + numOfCommits);

        File realCommit = new File("git/" + commHash);
        realCommit.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(realCommit));
        writer.write(st.toString());
        writer.close();
        comm.delete();

        bw = new BufferedWriter(new FileWriter("git/HEAD"));
        bw.write(commHash);
        bw.close();

        numOfCommits++;
        mostRecentCommitHash = commHash;
        return mostRecentCommitHash;
    }

    public void stage(String input) throws IOException {
        if (printMessages && !new File(input).exists()) { // just for testing; u can delete later if u don't want it
            System.out.println("Input file does not exist.");
        } else {
            if (new File(input).isFile()) {
                String fileHash = computeSHA1(input);
                File hashFile = new File("git/objects/" + fileHash);
                if (!hashFile.exists()) {
                    Files.copy(Path.of(input), Path.of(hashFile.getPath()));
                }
                BufferedWriter writer = new BufferedWriter(new FileWriter("git/index", true));
                writer.write("blob " + fileHash + " " + input.substring(4));
                writer.newLine();
                writer.close();
            } else {
                addDirectory(input);
            }
        }
    }

    // deletes git folder and all files inside
    public static void deleteGit() {
        deleteFolder("git");
    }

    private static void deleteFolder(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            return;
        }
        // creates a list of all filenames inside git
        File[] toDelete = folder.listFiles();

        // recursively deletes all files in git directory
        for (File file : toDelete) {
            if (file.isDirectory()) {
                deleteFolder(file.getPath());
            }
            file.delete();
        }

        // once empty, the directory is emptied
        folder.delete();
    }

    // copied from geek for geek
    // https://www.geeksforgeeks.org/sha-1-hash-in-java/
    public static String computeSHA1(String input) throws IOException {
        File inputFile = new File(input);
        if (printMessages && !new File(input).exists()) { // also just for testing, SORRY
            return "Cannot compute Sha1; Input file does not exist.";
        }
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(input));
            byte[] arr = new byte[(int) inputFile.length()];
            stream.read(arr);
            stream.close();
            byte[] messageDigest = md.digest(arr);

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 40 digits long
            while (hashtext.length() < 40) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // reformats the index txt file by replacing the old index with an updated
    // version.

    private String addDirectory(String directoryPath) throws IOException {
        if (!new File(directoryPath).canWrite()) {
            System.out.println("The given directory is inaccessible.");
            return "";
        }
        if (!new File(directoryPath).exists()) {
            System.out.println("The given directory does not exist.");
            return "";
        }
        String hashOfTree = recursiveAddDirectory(directoryPath, 0);

        checkLines();

        if (printMessages)
            System.out.println("Successfully added directory!");
        return hashOfTree;
    }

    public static void checkLines() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("git/index"));
        HashSet<String> lines = new HashSet<String>();

        while (br.ready()) {
            String str = br.readLine();
            if (!lines.contains(str)) {
                lines.add(str);
            }
        }
        br.close();

        BufferedWriter bw = new BufferedWriter(new FileWriter("git/index"));
        Object[] str = lines.toArray();
        for (Object s : str) {
            bw.write((String) s);
            bw.newLine();
        }
        bw.close();
    }

    // recursively adds given directory while also adding its own files and
    // subdirectories.
    // the numberOfTrees parameter helps keep track of which tree's file you are
    // currently editing.
    private String recursiveAddDirectory(String directoryPath, int numberOfTrees) throws IOException {
        File currentDirectory = new File(directoryPath);
        File[] listOfContents = currentDirectory.listFiles();

        if (listOfContents == null) {
            stage(directoryPath);
            return Git.computeSHA1(directoryPath);
        }

        File treeFile = new File("git/objects/tree" + numberOfTrees);
        BufferedWriter writeToTree = new BufferedWriter(new FileWriter(treeFile));

        for (File currFile : listOfContents) {
            if (currFile.isFile()) {
                writeToTree.write("blob " + recursiveAddDirectory(currFile.getPath(), numberOfTrees + 1) + " "
                        + currFile.getName());
                writeToTree.newLine();
            } else {
                writeToTree.write("tree " + recursiveAddDirectory(currFile.getPath(), numberOfTrees + 1) + " "
                        + currFile.getName());
                writeToTree.newLine();
            }
        }
        writeToTree.close();

        String hashOfTree = computeSHA1("git/objects/tree" + numberOfTrees);
        treeFile.renameTo(new File("git/objects/" + computeSHA1("git/objects/tree" + numberOfTrees)));

        BufferedWriter writer = new BufferedWriter(new FileWriter("git/index", true));
        writer.write("tree " + hashOfTree + " " + directoryPath.substring(4));
        writer.newLine();
        writer.close();

        return hashOfTree;
    }
}