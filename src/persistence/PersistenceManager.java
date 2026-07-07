package persistence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/*
we create a clean I/O utility class
using standard Java ObjectOutputStream and ObjectInputStream.
This acts as our database engine, writing objects to a local data/ folder.
 */
public class PersistenceManager {
    private static final String DATA_DIRECTORY = "data/";

    public PersistenceManager(){
    //ensure the local folder exist on start up
        try{
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
        }catch (IOException e){
            System.err.println("Could not create data directory:" + e.getMessage());
        }
    }

    public void saveToFile(String filename, Object data){
        String fullPath = DATA_DIRECTORY + filename;
        // Using Java 7+ try-with-resources to ensure file streams close automatically
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fullPath))) {
            oos.writeObject(data);
            System.out.println("Successfully saved data to: " + fullPath);
        } catch (IOException e) {
            System.err.println("Error saving file " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Loads a saved object from disk. Returns an Optional in case the file doesn't exist yet.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> loadFromFile(String filename) {
        String fullPath = DATA_DIRECTORY + filename;
        File file = new File(fullPath);

        if (!file.exists()) {
            return Optional.empty(); // File doesn't exist yet (e.g., first ever app launch)
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fullPath))) {
            T data = (T) ois.readObject();
            System.out.println("Successfully loaded data from: " + fullPath);
            return Optional.of(data);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading file " + filename + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}

