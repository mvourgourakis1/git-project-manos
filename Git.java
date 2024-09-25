import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.FileWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;

public class Git {
    public static void main(String[] args) throws IOException {
        // deletes all git files created by last test run
        deleteGit();

        // initialize git
        Git git = new Git();

        // tests computeSHA1 method
        System.out.println(computeSHA1("test.txt"));

        git.add("test.txt");
        git.add("test2.txt");
    }

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
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(input));
            File inputFile = new File(input);
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
}