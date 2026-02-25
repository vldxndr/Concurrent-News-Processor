import tools.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

// Clasa de citire path-uri catre fisierele JSON si continutul lor
public class JsonArticleReader {
    // mapper pentru a pune campurile din JSON in cele din articole
    private static final ObjectMapper mapper = new ObjectMapper();

    // Metoda de parsare a fisierului de la calea data
    public static List<Article> parseFile(String filePath) {
        List<Article> outFiles = new ArrayList<>();
        File file = new File(filePath);

        // Daca nu exista acest fisier returnam lista de fisiere
        if (!file.exists()) {
            return outFiles;
        }
        try {
            // Mapez valorile din JSON la cele posibile din Article
            Article[] articleArray = mapper.readValue(file, Article[].class);
            // Adaug noul array la outFiles
            Collections.addAll(outFiles, articleArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outFiles;
    }

    // Metoda de citirea lista de JSON-uri
    public static List<String> readList(String listFile) {
        List<String> outFiles = new ArrayList<>();
        File listFileObj = new File(listFile);

        // Iau folderul unde se afla articles.txt
        String parentDir = listFileObj.getParent();

        try (BufferedReader br = new BufferedReader(new FileReader(listFileObj))) {
            String line = br.readLine(); // Citesc primul Nr si il ignor

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // Lipesc parentDir de numele fisierului pentru a functiona checker-ul
                    outFiles.add(new File(parentDir, line.trim()).getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outFiles;
    }
}
