import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Tema1 {
    public static void main(String[] args) throws Exception {
        System.out.println("WORK DIR = " + new File(".").getAbsolutePath());

        // Numarul de thread-uri primite ca argument
        Integer numThreads = Integer.parseInt(args[0]);

        // Lista completa de fisiere
        List<String> files = JsonArticleReader.readList(args[1]);

        // Datele primite din inputs si cu parsarea lor
        InputsData inputData = InputsData.inputReader(args[2]);

        // Bariera pentru a fii folosita inauntru lui Worker cand trecem de la un
        // task la altul
        CyclicBarrier barrier = new CyclicBarrier(numThreads);

        // Counter pentru a stii ce fisier trebuie sa ia urmatorul thread
        // Atomic Integer ca sa nu se poata lua acelasi fisier de 2 thread-uri
        AtomicInteger counter = new AtomicInteger(0);

        // Lista totala inainte de vreo schimbare
        List<Article> sharedList = Collections.synchronizedList(new ArrayList<>());

        // Hash Map uri pentru ID-uri si titluri pentru a vedea cate sunt <= 1
        ConcurrentHashMap<String, Integer> titleCounts = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> uuidCounts = new ConcurrentHashMap<>();

        // Lista clean dupa ce am scos duplicatele
        List<Article> finalCleanList = Collections.synchronizedList(new ArrayList<>());

        // Map uri pentru categorii si language-uri pentru a le tine minte direct sortat?
        ConcurrentHashMap<String, List<String>> catMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, List<String>> langMap = new ConcurrentHashMap<>();

        // Umplere a hash Map urilor pentru a nu avea probleme mai tarziu cand cautam in ele
        for (String cat : inputData.categories) {
            catMap.put(cat, Collections.synchronizedList(new ArrayList<>()));
        }
        for (String lang : inputData.languages) {
            langMap.put(lang, Collections.synchronizedList(new ArrayList<>()));
        }

        // Map uri pentru numarat aparitii de keywords si autori
        ConcurrentHashMap<String, Integer> keywordCounts = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> authorCounts = new ConcurrentHashMap<>();

        // Declarare si pornire threads
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            Worker worker = new Worker(i, numThreads, barrier, files, counter, sharedList, titleCounts, uuidCounts, finalCleanList, inputData, catMap, langMap, keywordCounts, authorCounts);

            threads[i] = new Thread(worker);
            threads[i].start();

        }

        // Join final la Threads
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }

        // Calcul pentru reports
        int duplicatesFound = sharedList.size() - finalCleanList.size();
        int uniqueArticless = finalCleanList.size();

        String bestAuthor = getBest(authorCounts);
        int maxAuthorCount = (bestAuthor.isEmpty()) ? 0 : authorCounts.get(bestAuthor);

        String topLanguage = getBest(langMap);
        int maxLanguageCount = (topLanguage.isEmpty()) ? 0 : langMap.get(topLanguage).size();

        String topCategory = getBest(catMap);
        int maxCatCount = (topCategory.isEmpty()) ? 0 : catMap.get(topCategory).size();

        String topKeyword = getBest(keywordCounts);
        int maxKeywordCount = (topKeyword.isEmpty()) ? 0 : keywordCounts.get(topKeyword);

        // Scrie lista in all_articles.txt dupa ce o sorteaza in place
        OutputWriter.writeAllArticles(finalCleanList);

        // Lista sortata putem lua primul element ca fiind cel mai recent
        Article mostRecent = finalCleanList.isEmpty() ? null : finalCleanList.get(0);

        // Scrie fisierul de reports.txt cu toate argumentele calculate mai sus
        OutputWriter.writeReports(duplicatesFound, uniqueArticless, bestAuthor, maxAuthorCount, topLanguage, maxLanguageCount, topCategory, maxCatCount, mostRecent, topKeyword, maxKeywordCount);

        // Scriere fisiere de categories si de keywords
        OutputWriter.writeKeywords(keywordCounts);
        OutputWriter.writeCategories(catMap, langMap);
    }

    // Functie generala pentru a calcula cele mai bune statistici pentru
    // categorii, authori, languages si keywords
    public static String getBest(ConcurrentHashMap<String, ?> map) {
        String best = "";
        int maxCount = -1;

        for (String key : map.keySet()) {
            int count = 0;
            Object value = map.get(key);
            // Folosesc instance of deorece pot avea Integer sau List<String> ca valoare pt
            // HashMap
            if (value instanceof Integer) {
                count = (Integer) value;
            } else if (value instanceof List) {
                count = ((List<?>) value).size();
            }
            // Gasesc maxCount si best (String-ul care are valoarea maxCount)
            if (count > maxCount) {
                maxCount = count;
                best = key;
            } else if (count == maxCount) {
                // Tiebreaker daca maxCount e la fel la ambele
                // aleg lexicografic
                if (best.equals("") || key.compareTo(best) < 0) {
                    best = key;
                }
            }
        }
        return best;
    }
}
