package com.transportroutes.persistence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Utility class responsible for saving and loading game data.
 * 
 * <p>This class handles file operations such as creating save files,
 * loading saved games, listing available saves, and managing file names.
 * It uses Java serialization to store and retrieve {@link GameState} objects.</p>
 */
public final class SaveGameManager {

    private static final String SAVE_EXTENSION = ".sav";
    private static final Path SAVE_DIRECTORY = Path.of("saves");

    /**
     * Private constructor to prevent instantiation.
     */
    private SaveGameManager() {
    }

    /**
     * Returns the save directory, creating it if it does not exist.
     *
     * @return the absolute path to the save directory
     * @throws IOException if directory creation fails
     */
    public static Path getSaveDirectory() throws IOException {
        Files.createDirectories(SAVE_DIRECTORY);
        return SAVE_DIRECTORY.toAbsolutePath();
    }

    /**
     * Generates the full file path for a save file.
     *
     * @param saveName the name of the save
     * @return the full path to the save file
     * @throws IOException if directory access fails
     * @throws IllegalArgumentException if the name is empty
     */
    public static Path getSavePath(String saveName) throws IOException {
        String normalized = normalizeSaveName(saveName);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Save name cannot be empty.");
        }

        return getSaveDirectory().resolve(normalized + SAVE_EXTENSION);
    }

    /**
     * Saves the given game state to a file.
     *
     * @param state the game state to save
     * @param saveName the desired save name
     * @return the path of the saved file
     * @throws IOException if writing fails
     */
    public static Path save(GameState state, String saveName) throws IOException {
        Path savePath = getSavePath(saveName);

        try (ObjectOutputStream output = new ObjectOutputStream(
                Files.newOutputStream(savePath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING))) {
            output.writeObject(state);
        }

        return savePath;
    }

    /**
     * Loads a game state from a file.
     *
     * @param savePath the path to the save file
     * @return the loaded GameState
     * @throws IOException if reading fails or file is invalid
     * @throws ClassNotFoundException if class cannot be found
     */
    public static GameState load(Path savePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(savePath))) {
            Object loaded = input.readObject();
            if (loaded instanceof GameState state) {
                return state;
            }
        }

        throw new IOException("Invalid save file: " + savePath.getFileName());
    }

    /**
     * Lists all available save files in the save directory.
     *
     * @return a sorted list of save file paths
     * @throws IOException if directory access fails
     */
    public static List<Path> listSaveFiles() throws IOException {
        Path saveDirectory = getSaveDirectory();

        try (Stream<Path> files = Files.list(saveDirectory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(SAVE_EXTENSION))
                    .sorted(Comparator.comparing(path ->
                            path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .toList();
        }
    }

    /**
     * Converts a save file path to a user-friendly name (without extension).
     *
     * @param savePath the file path
     * @return display-friendly save name
     */
    public static String toDisplayName(Path savePath) {
        String fileName = savePath.getFileName().toString();
        if (fileName.toLowerCase(Locale.ROOT).endsWith(SAVE_EXTENSION)) {
            return fileName.substring(0, fileName.length() - SAVE_EXTENSION.length());
        }
        return fileName;
    }

    /**
     * Normalizes a save name by removing invalid characters.
     *
     * @param saveName the original name
     * @return a safe, cleaned file name
     */
    private static String normalizeSaveName(String saveName) {
        if (saveName == null) {
            return "";
        }

        String normalized = saveName.trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_");

        if (normalized.toLowerCase(Locale.ROOT).endsWith(SAVE_EXTENSION)) {
            normalized = normalized.substring(0,
                    normalized.length() - SAVE_EXTENSION.length());
        }

        return normalized.trim();
    }
}