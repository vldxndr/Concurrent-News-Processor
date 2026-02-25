import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputsData {
    public final List<String> languages;
    public final List<String> categories;
    public final List<String> linkingWords;

    private InputsData(List<String> languages, List<String> categories, List<String> linkingWords) {
        this.languages = languages;
        this.categories = categories;
        this.linkingWords = linkingWords;
    }

    public static InputsData inputReader(String filePath) throws IOException {
        // Luam fisierul inputs.txt
        File inputsFile = new File(filePath);
        // Aflu folderul in care se afla
        String parentDir = inputsFile.getParent();

        // Declar numele fisierelor
        String languagesFile = null;
        String categoriesFile = null;
        String linkingWordsFile = null;

        try (BufferedReader br = new BufferedReader(new FileReader(inputsFile))) {
            // Citesc nr de fisiere pentru a trece peste el
            br.readLine();

            // Pun ca nume ce citesc impreuna cu parentDir folosind resolvePath
            languagesFile = getAbsPath(parentDir, br.readLine());
            categoriesFile = getAbsPath(parentDir, br.readLine());
            linkingWordsFile = getAbsPath(parentDir, br.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Citesc din fiecare fisier si adaug intr o lista de string-uri
        List<String> languages = readContent(languagesFile);
        List<String> categories = readContent(categoriesFile);
        List<String> linkingWords = readContent(linkingWordsFile);

        return new InputsData(languages, categories, linkingWords);
    }

    // Metoda pentru a combina calea folderului cu cea a fisierului ( necesara pt checker )
    private static String getAbsPath(String parent, String filename) {
        if (filename == null) return null;
        if (parent == null) return filename.trim();
        // Ne da calea absoluta corecta
        return new File(parent, filename.trim()).getAbsolutePath();
    }

    private static List<String> readContent(String filePath) {
        List<String> result = new ArrayList<>();
        if (filePath == null) return result;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Incerc sa citesc prima linie
            String line = br.readLine();
            if (line == null) return result;

            // Citesc toate liniile pena la finalul fisierului
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}