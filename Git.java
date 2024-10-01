import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        // tests computeSHA1 method
        File testFile = new File("test.txt");
        //System.out.println(computeSHA1("test.txt"));

        testFile = new File("tester/1.txt");
        //System.out.println(computeSHA1(testFile.getAbsolutePath()));
        //System.out.println(computeSHA1("tester"));
        // System.out.println(computeSHA1("tester/empty") + "\n");

        // files u created
        // git.add("test.txt");
        // git.add("test2.txt");

        // files i made in tester folder
        // System.out.println("Testing Git.add(String)...");
        // System.out.println("");
        // git.add("tester/1.txt");
        // git.add("tester/2.txt");
        // git.add("tester/wahoo/coolFile.txt");
        // git.add("tester/emptyFile.txt");
        // git.add("");

        System.out.println("\naddDirectory(String) test... Should print hash: " + git.addDirectory("tester"));
        git.reformatIndex();
    }

    // if the printed messages are too wordy/redundant, u can set printMessages to false (or just delete them)
    public static boolean printMessages = true;

    // Git constructor checks if a git folder already exists in the parent directory
    // if a git directory doesn't exist, :
    // a new git directory is created in the parent directory
    // and an index file is created in the git directory
    // if a git directory already exists:
    // "Git Repository already exists" will be printed
    public Git() throws IOException {
        File gitFolder = new File("git");
        if (!gitFolder.exists()) {
            gitFolder.mkdir();
            File createIndex = new File("git/index");
            try {
                createIndex.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                ;
            }
            File createObjects = new File("git/objects");
            createObjects.mkdir();
        } else
            System.out.println("Git Repository already exists");
    }

    public void add(String input) throws IOException {
        if (printMessages && !new File(input).exists()) { // just for testing; u can delete later if u don't want it
            System.out.println("Input file does not exist.");
        } else {
            String fileHash = computeSHA1(input);
            File hashFile = new File("git/objects/" + fileHash);
            if (!hashFile.exists()) {
                Files.copy(Path.of(input), Path.of(hashFile.getPath()));
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter("git/index", true));
            writer.write(fileHash + " " + input);
            writer.newLine();
            writer.close();
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
            if(file.isDirectory()){
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

    // reformats the index txt file by replacing the old index with an updated version.
    public boolean reformatIndex() {
        try {
            File treeFormatIndex = new File("git/newIndex");
            BufferedReader reader = new BufferedReader(new FileReader("git/index"));
            BufferedWriter writer = new BufferedWriter(new FileWriter("git/newIndex"));

            String currentLine = reader.readLine();
            while (currentLine != null) {
                File currentLineFile = new File(currentLine.substring(41));
                if (!currentLine.substring(0,4).equals("tree") && !currentLine.substring(0,4).equals("blob")) {
                    if (currentLineFile.exists() && currentLineFile.isFile()) {
                        writer.write("blob ");
                    }
                }
                writer.write(currentLine);
                writer.newLine();
                currentLine = reader.readLine();
            }

            File oldIndex = new File("git/index");
            oldIndex.delete();

            treeFormatIndex.createNewFile();
            treeFormatIndex.renameTo(oldIndex);
            
            reader.close();
            writer.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }

    public String addDirectory(String directoryPath) throws IOException {
        if (!new File(directoryPath).canWrite()) {
            System.out.println("The given directory is inaccessible.");
            return "";
        }
        if (!new File(directoryPath).exists()) {
            System.out.println("The given directory does not exist.");
            return "";
        }
        String hashOfTree = recursiveAddDirectory(directoryPath, 0);

        if (printMessages) System.out.println("Successfully added directory!");
        return hashOfTree;
    }

    // recursively adds given directory while also adding its own files and subdirectories.
    // the numberOfTrees parameter helps keep track of which tree's file you are currently editing.
    private String recursiveAddDirectory(String directoryPath, int numberOfTrees) throws IOException {
        File currentDirectory = new File(directoryPath);
        File[] listOfContents = currentDirectory.listFiles();

        if (listOfContents == null) {
            add(directoryPath);
            return Git.computeSHA1(directoryPath);
        }

        File treeFile = new File("git/objects/tree" + numberOfTrees);
        BufferedWriter writeToTree = new BufferedWriter(new FileWriter(treeFile));
        
        for (File currFile : listOfContents) {
            if (currFile.isFile()) {
                writeToTree.write("blob " + recursiveAddDirectory(currFile.getPath(), numberOfTrees+1) + " " + currFile.getName());
                writeToTree.newLine();
            } else {
                writeToTree.write("tree " + recursiveAddDirectory(currFile.getPath(), numberOfTrees+1) + " " + currFile.getName());
                writeToTree.newLine();
            }
        }
        writeToTree.close();

        String hashOfTree = computeSHA1("git/objects/tree"+numberOfTrees);
        treeFile.renameTo(new File("git/objects/" + computeSHA1("git/objects/tree" + numberOfTrees)));

        BufferedWriter writer = new BufferedWriter(new FileWriter("git/index", true));
        writer.write("tree " + hashOfTree + " " + directoryPath);
        writer.newLine();
        writer.close();

        return hashOfTree;
    }
}