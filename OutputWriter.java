import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Clasa care contine metode de scriere in fisiere
public class OutputWriter {

    // Metoda de scriere all_articles_txt
    public static void writeAllArticles(List<Article> articles) {
        // Face si sortare pentru articles (finalCleanArticles)
        articles.sort(new Comparator<Article>() {
            @Override
            public int compare(Article art1, Article art2) {
                String date1 = (art1.getPublished() == null) ? "" : art1.getPublished();
                String date2 = (art2.getPublished() == null) ? "" : art2.getPublished();

                // Compara descrescator datele
                int res = date2.compareTo(date1);
                // Daca nu sunt aceleasi date se returneaza rezultatul,
                // altfel se returneaza crescator dupa uuid
                if (res != 0) {
                    return res;
                }
                return art1.getUuid().compareTo(art2.getUuid());
            }
        });

        // Scriere in fisier
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("all_articles.txt"))) {
            // Pentru fiecare articol avem uuid si data publicarii despre care stim ca
            // sunt ordonate deja
            for (Article a : articles) {
                bw.write(a.getUuid() + " " + a.getPublished());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metoda care scrie reports.txt
    public static void writeReports(int duplicates, int unique,
                                    String bestAuth, int authCount,
                                    String topLang, int langCount,
                                    String topCat, int catCount,
                                    Article mostRecent,
                                    String topKey, int keyCount) {
        // Scriere in fisier dupa standardul dat
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("reports.txt"))) {
            bw.write("duplicates_found - " + duplicates); bw.newLine();
            bw.write("unique_articles - " + unique); bw.newLine();
            bw.write("best_author - " + bestAuth + " " + authCount); bw.newLine();
            bw.write("top_language - " + topLang + " " + langCount); bw.newLine();

            // Normalizare categorii - scot "," si " " si il inlocuisc cu "_"
            String normCat = topCat.replace(",", "").replace(" ", "_");
            bw.write("top_category - " + normCat + " " + catCount); bw.newLine();

            // Primesc mostRecent din main unde am luat primul element din finalCleanList
            if (mostRecent != null) {
                bw.write("most_recent_article - " + mostRecent.getPublished() + " " + mostRecent.getUrl());
                bw.newLine();
            }

            bw.write("top_keyword_en - " + topKey + " " + keyCount);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Scriere keyWords
    public static void writeKeywords(ConcurrentHashMap<String, Integer> keywordCounts) {
        // Fac o lista de entry uri din HashMap ul primit pentru a lua direct si cheia si
        // valuarea
        List<Map.Entry<String, Integer>> list = new ArrayList<>(keywordCounts.entrySet());

        list.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                // Descrescator pentru Count
                int res = o2.getValue().compareTo(o1.getValue());
                if (res != 0) {
                    return res;
                }
                // Crescator pt cuvant
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        // Scriere in fisier
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("keywords_count.txt"))) {
            for (Map.Entry<String, Integer> entry : list) {
                bw.write(entry.getKey() + " " + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metoda generica de scris categorii si limbi pentru ca sunt aproapiate in logica
    public static void writeCategories(ConcurrentHashMap<String, List<String>> catMap,
                                       ConcurrentHashMap<String, List<String>> langMap) {
        writeMaps(catMap, true);
        writeMaps(langMap, false);
    }

    //
    private static void writeMaps(ConcurrentHashMap<String, List<String>> map, boolean normalize) {
        // For cu entry pentru access usor la cheie si valoare
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String name = entry.getKey();
            List<String> uuids = entry.getValue();
            if (uuids.isEmpty()) {
                continue;
            }

            // Sortare a uuids
            Collections.sort(uuids);

            String fileName;
            // Aici decidem dupa boolean ul primit daca trebuie normalizat numele (pt categories) sau nu (pt languages)
            if (normalize) fileName = name.replace(",", "").replace(" ", "_") + ".txt";
            else fileName = name + ".txt";

            // Scriere in fisiere
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
                for (String u : uuids) {
                    bw.write(u);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
