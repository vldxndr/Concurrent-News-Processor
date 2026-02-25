import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker implements Runnable {

    // Campuri necesare prelucrarii in paralel a articolelor
    private int id;
    private int totalThreads;

    // Bariera pentru a separa etapele pararelizate
    private CyclicBarrier barrier;

    //Lista de files primita
    private List<String> files;
    // Counter de tip Atomic integer pentru a l putea incrementa fiecare
    // thread dupa ce si a luat indexul fisierului pe care il citeste
    private AtomicInteger counter;

    // Lista in care se vor afla toate articolele inclusiv duplicate
    private List<Article> sharedList;

    // HashMap uri pentru a tine minte aparitiile fiecare titlu si uuid
    // pentru a scoate in final duplicatele
    private ConcurrentHashMap<String, Integer> titleCounts;
    private ConcurrentHashMap<String, Integer> uuidCounts;

    // Obiect de tip InputsData pentru a afla cerintele testului dat
    private InputsData inputData;

    // Lista curata fara duplicate
    List<Article> finalCleanList;

    // HashMap uri care tin minte pentru fiecare categorie si language
    // ce uuid uri ajung in fisiere
    private ConcurrentHashMap<String, List<String>> catMap;
    private ConcurrentHashMap<String, List<String>> langMap;

    // HashMap uri care tine minte de cate ori a fost vazut un cuvant
    // sau un autor (care nu apare in linked words)
    private ConcurrentHashMap<String, Integer> keywordCounts;
    private ConcurrentHashMap<String, Integer> authorCounts;


    // Constructor ce primeste toate datele de mai sus si le atribuie
    public Worker(int index, Integer numThreads, CyclicBarrier barrier,
                  List<String> files, AtomicInteger counter,
                  List<Article> sharedList,
                  ConcurrentHashMap<String, Integer> titleCounts,
                  ConcurrentHashMap<String, Integer> uuidCounts,
                  List<Article> finalCleanList,
                  InputsData inputData,
                  ConcurrentHashMap<String, List<String>> catMap,
                  ConcurrentHashMap<String, List<String>> langMap, ConcurrentHashMap<String, Integer> keywordCounts, ConcurrentHashMap<String, Integer> authorCounts) {
        this.id = index;
        this.totalThreads = numThreads;
        this.barrier = barrier;
        this.files = files;
        this.counter = counter;
        this.sharedList = sharedList;
        this.titleCounts = titleCounts;
        this.uuidCounts = uuidCounts;
        this.finalCleanList = finalCleanList;
        this.inputData = inputData;
        this.catMap = catMap;
        this.langMap = langMap;
        this.keywordCounts = keywordCounts;
        this.authorCounts = authorCounts;
    }

    // Metoda run
    @Override
    public void run() {
        try {
            // Prima etapa - Citirea fisierelor in functie de counter ul atomic
            while (true) {
                // Metoda folosita pentru a lua integerul si a incrementa pentru
                // urmatorul thread
                int fileNum = counter.getAndIncrement();
                if (fileNum >= files.size()) break;

                String file = files.get(fileNum);
                // Apelez parseFile din clasa mea de articleReader
                List<Article> articles = JsonArticleReader.parseFile(file);

                for (Article art : articles) {
                    // Adaug tot ce gasesc
                    sharedList.add(art);
                    if (art.getTitle() != null)
                        // Incrementez title counts cu fiecare noua aparitie a aceluiasi titlu
                        titleCounts.merge(art.getTitle(), 1, Integer::sum);
                    if (art.getUuid() != null)
                        // Incrementez uuid counts cu fiecare noua aparitie a aceluiasi uuid
                        uuidCounts.merge(art.getUuid(), 1, Integer::sum);
                }
            }

            // Am terminat citirea se asteapta toate thread-urile ca sa treaca la urmatoare etapa
            barrier.await();

            // A doua etapa - scoaterea duplicatelor din lista

            // Impartim lista pt ca fiecare thread sa ia o bucata dupa aceasta formula pentru a
            // balansa work load ul intre ele
            int size = sharedList.size();
            int sizeForThread = size / totalThreads;
            int startIndex = sizeForThread * id;
            int endIndex = (id == totalThreads - 1) ? size : sizeForThread * (id + 1);

            // for loop intre start si end calculati
            for (int i = startIndex; i < endIndex; i++) {
                Article article = sharedList.get(i);
                // Verificare daca si titlu si uuid sunt unice si daca nu sunt null pentru safety
                boolean isUniqueTitle = article.getTitle() != null && titleCounts.get(article.getTitle()) == 1;
                boolean isUniqueUuid = article.getUuid() != null && uuidCounts.get(article.getUuid()) == 1;

                // Daca ambele sunt ok le trecem in lista curata
                if (isUniqueTitle && isUniqueUuid) {
                    finalCleanList.add(article);
                }
            }

            // Alta bariera pentru a termina a doua etapa a codului necesara deoarece de acum incolo
            // am nevoie doar de lista curata
            barrier.await();

            // Incepe ce de a treia etapa

            // Impartim iar work load ul pentru fiecare thread dar de data aceasta ii pasez marimea
            // noii Liste cea fara duplciate
            size = finalCleanList.size();
            sizeForThread = size / totalThreads;
            startIndex = sizeForThread * id;
            endIndex = (id == totalThreads - 1) ? size : sizeForThread * (id + 1);

            // for loop intre start si end noi
            for (int i = startIndex; i < endIndex; i++) {
                // Luam cate un articol unic din lista
                Article article = finalCleanList.get(i);

                // Fac trim aici pentru a evita erori la testare
                String cleanUuid = article.getUuid().trim();

                // Incep verificarea pentru categorii
                if (article.categories != null) {
                    // Logica pusa pentru safety HashSet-ul nu lasa decat 1 element pentru o cheie
                    HashSet<String> uniqueCatsInArticle = new HashSet<>();
                    for (String c : article.categories) {
                        // Trim din nou pentru a nu avea probleme cu nume de categorii diferite cu un " "
                        uniqueCatsInArticle.add(c.trim());
                    }

                    for (String cat : uniqueCatsInArticle) {
                        // Verificam daca categoria este valida si adaugam uuid ul articolului
                        if (catMap.containsKey(cat)) {
                            catMap.get(cat).add(cleanUuid);
                        }
                    }
                }

                // Logica similara si pentru limbi dar nu mai avem problema de mai sus cu mai multe
                // articole putand apartine la mai multe categorii
                if (article.language != null && langMap.containsKey(article.language)) {
                    langMap.get(article.language).add(article.getUuid());
                }

                // Daca limba articolului este strict engleza si are text
                if (article.language.equalsIgnoreCase("english")) {
                    if (article.text != null) {
                        // Luam fiecare cuvant
                        String[] words = article.text.split("\\s+");

                        // Set pentru a pune cuvintele
                        HashSet<String> set = new HashSet();

                        for (String word : words) {
                            // Curat cuvantul
                            String cleanWord = word.toLowerCase().replaceAll("[^a-z]", "");

                            // Daca cuvantul exista dupa curatare si nu se afla in linkingWords
                            // poate fii adaugat in set
                            if (cleanWord.length() > 0 && !inputData.linkingWords.contains(cleanWord)) {
                                set.add(cleanWord);
                            }
                        }
                        // Numar aparitiile unui cuvant din set si incrementez valoare in Counts
                        for (String word : set) {
                            keywordCounts.merge(word, 1, Integer::sum);
                        }
                    }
                }
                // Numar aparitiile unui autor pentru fiecare articol si incrementez in Counts
                if (article.author != null) {
                    authorCounts.merge(article.author, 1, Integer::sum);
                }
                // Se termina munca in paralel restul operatiilor se vor face in thread-ul main
                // DUPA join.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}