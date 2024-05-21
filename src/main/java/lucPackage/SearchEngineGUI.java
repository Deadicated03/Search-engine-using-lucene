package lucPackage;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class SearchEngineGUI {

    private JTextField searchBar;
    private JEditorPane resultEditorPane;
    private JScrollPane resultScrollPane;
    private JButton searchButton;
    private JButton nextButton;
    private JButton previousButton;
    private JButton sortButton;
    private JComboBox<String> searchOptions;
    private JTextArea historyTextArea;
    private JScrollPane historyScrollPane;
    private ScoreDoc[] currentHits;
    private int currentIndex;
    private int BATCH_SIZE = 10;
    private Map<Integer, Document> resultDocsMap;
    private IndexSearcher isearcher;
    private String lastQuery;
    private String lastSelectedOption;
  

    public void createAndShowGUI(Directory directory,String analyzer,String words) throws IOException {
        Searcher search = new Searcher(analyzer,words);
        DirectoryReader ireader = DirectoryReader.open(directory);
        isearcher = new IndexSearcher(ireader);

        JFrame frame = new JFrame("Search Engine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        searchBar = new JTextField(30);
        searchButton = new JButton("Search");
        nextButton = new JButton("Next");
        previousButton = new JButton("Previous");
        sortButton = new JButton("Sort by Year");

        resultEditorPane = new JEditorPane();
        resultEditorPane.setContentType("text/html");
        resultEditorPane.setEditable(false);
        resultScrollPane = new JScrollPane(resultEditorPane);

        String[] options = {"title", "year", "id", "abstract", "body"};
        searchOptions = new JComboBox<>(options);


        historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);
        historyScrollPane = new JScrollPane(historyTextArea);
        historyScrollPane.setPreferredSize(new Dimension(120, 0));
        
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        JLabel resultLabel = new JLabel("Search Results");
        resultPanel.add(resultLabel, BorderLayout.NORTH);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);


        JPanel historyPanel = new JPanel();
        historyPanel.setLayout(new BorderLayout());
        JLabel historyLabel = new JLabel("Search History");
        historyPanel.add(historyLabel, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);


        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Search Field:"));
        topPanel.add(searchOptions);
        topPanel.add(searchBar);
        topPanel.add(searchButton);
        topPanel.add(sortButton);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(previousButton);
        bottomPanel.add(nextButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(resultPanel, BorderLayout.CENTER);
        panel.add(historyPanel, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        resultDocsMap = new HashMap<>();

        searchButton.addActionListener(new ActionListener() {
      
            public void actionPerformed(ActionEvent e) {
                performSearch(directory, search);
            }
        });

        nextButton.addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) {
                displayNextBatch();
            }
        });

        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayPreviousBatch();
            }
        });

        sortButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sortResultsByYear();
            }
        });

        resultEditorPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    int docId = Integer.parseInt(e.getDescription());
                    try {
                        Document doc = resultDocsMap.get(docId);
                        if (doc != null) {
                            String body = doc.get("body");
                            displayDocument(body);
                        }
                        else {
                        	System.out.println("no body");
                        }                     
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        frame.add(panel);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    directory.close();
                    String userDir = System.getProperty("user.dir");
                    deleteFilesInDirectory(Paths.get(userDir, "files").toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        frame.setVisible(true);
    }

    private void performSearch(Directory directory, Searcher search) {
        String queryStr = searchBar.getText();
        String selectedOption = (String) searchOptions.getSelectedItem();

        if (!queryStr.isEmpty()) {
            try {
                historyTextArea.append(queryStr + " With: " + selectedOption + "\n");
                currentHits = search.searcher(directory, selectedOption, queryStr);
                currentIndex = 0;
                lastQuery = queryStr;
                lastSelectedOption = selectedOption;      
                resultEditorPane.setText("");             
                SwingUtilities.invokeLater(() -> {
                    historyScrollPane.getVerticalScrollBar().setValue(0);
                    resultScrollPane.getVerticalScrollBar().setValue(0);
                });

                displayBatch(isearcher, selectedOption, queryStr, true);
            } catch (IOException | ParseException e1) {
                e1.printStackTrace();
            }
        }
        else {
        	resultEditorPane.setText("YOU SEARCHED NOTHING :)");
        }
    }

    private void displayNextBatch() {
        if (currentIndex < currentHits.length) {
            try {
                displayBatch(isearcher, lastSelectedOption, lastQuery, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayPreviousBatch() {
        if (currentIndex > 0) {
            try {
                displayBatch(isearcher, lastSelectedOption, lastQuery, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sortResultsByYear() {
        if (currentHits == null) {
            return;
        }

        List<ScoreDoc> sortedHits = new ArrayList<>(Arrays.asList(currentHits));

        sortedHits.sort(new Comparator<ScoreDoc>() {
            public int compare(ScoreDoc doc1, ScoreDoc doc2) {
                try {
                    Document hitDoc1 = isearcher.doc(doc1.doc);
                    Document hitDoc2 = isearcher.doc(doc2.doc);

                    int yearInt1 = Integer.parseInt(hitDoc1.get("year"));
                    int yearInt2 = Integer.parseInt(hitDoc2.get("year"));


                    return Integer.compare(yearInt2, yearInt1);
                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        currentHits = sortedHits.toArray(new ScoreDoc[0]);
        currentIndex = 0;

        try {
            displayBatch(isearcher, lastSelectedOption, lastQuery, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayBatch(IndexSearcher isearcher, String fieldName, String queryStr, boolean forward) throws IOException {
        int start;
        if (forward) {
            start = currentIndex;
        } else {
            start = Math.max(currentIndex - 2 * BATCH_SIZE, 0);
        }
        int end = Math.min(start + BATCH_SIZE, currentHits.length);
        //System.out.println(start);
        //System.out.println(end);					//for testing pages
       
    	resultEditorPane.setText("");
        resultDocsMap.clear();
        
        
        StringBuilder html = new StringBuilder("<html><body>");
        

        for (int i = start; i < end; i++) {
            Document hitDoc = isearcher.doc(currentHits[i].doc);
            String title = hitDoc.get("title");
            String body = hitDoc.get("body");
            String snippet = getSnippet(body, queryStr);

            String resultDisplay = highlightQuery(title, queryStr);
            int docId = currentHits[i].doc;
            resultDocsMap.put(docId, hitDoc);
            html.append("<a href=\"").append(docId).append("\">").append(resultDisplay).append("</a><br>");
            html.append(snippet).append("<br><br>");
        }

        html.append("</body></html>");
        resultEditorPane.setText(html.toString());
        if(start == end) {
        	resultEditorPane.setText("0 RESULTS FOUND");
        }

        SwingUtilities.invokeLater(() -> resultScrollPane.getVerticalScrollBar().setValue(0));

        currentIndex = forward ? end : start + BATCH_SIZE;
    }

    private String highlightQuery(String text, String query) {
        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();
        String[] queryWords = lowerQuery.split("\\s+"); //for whitespace
        StringBuilder highlightedText = new StringBuilder(text);

        //stop words
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "then", "else", "when",
            "at", "by", "from", "for", "with", "about", "against", "between",
            "into", "through", "during", "before", "after", "above", "below",
            "to", "from", "up", "down", "in", "out", "on", "off", "over",
            "under", "again", "further", "then", "once", "here", "there",
            "all", "any", "both", "each", "few", "more", "most", "other",
            "some", "such", "no", "nor", "not", "only", "own", "same", "so",
            "than", "too", "very", "can", "will", "just",
            "should", "now"
        ));

        for (String queryWord : queryWords) {
            if (!stopWords.contains(queryWord)) {
                int queryLength = queryWord.length();
                int index = lowerText.indexOf(queryWord);
                while (index >= 0) {
                    highlightedText.insert(index, "<b>");
                    highlightedText.insert(index + queryLength + 3, "</b>");
                    lowerText = highlightedText.toString().toLowerCase();
                    index = lowerText.indexOf(queryWord, index + queryLength + 7);
                }
            }
        }
        return highlightedText.toString();
    }

    private String getSnippet(String body, String query) {
        String lowerBody = body.toLowerCase();
        String lowerQuery = query.toLowerCase();
        String[] queryWords = lowerQuery.split("\\s+");

        int snippetStart = 0;
        int snippetEnd = 0;


        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "then", "else", "when",
            "at", "by", "from", "for", "with", "about", "against", "between",
            "into", "through", "during", "before", "after", "above", "below",
            "to", "from", "up", "down", "in", "out", "on", "off", "over",
            "under", "again", "further", "then", "once", "here", "there",
            "all", "any", "both", "each", "few", "more", "most", "other",
            "some", "such", "no", "nor", "not", "only", "own", "same", "so",
            "than", "too", "very", "can", "will", "just",
            "should", "now"
        ));

        for (String queryWord : queryWords) {
            if (!queryWord.trim().isEmpty() && !stopWords.contains(queryWord)) {
                int queryIndex = lowerBody.indexOf(queryWord);
                if (queryIndex != -1) {
                    snippetStart = Math.max(0, queryIndex - 30);
                    snippetEnd = Math.min(body.length(), queryIndex + queryWord.length() + 30);
                }
            }
        }

        String snippet = body.substring(snippetStart, snippetEnd);
        return highlightQuery(snippet, query);
    }

    private void displayDocument(String body) {
        JFrame docFrame = new JFrame("Document");
        docFrame.setSize(600, 400);

        JTextArea bodyTextArea = new JTextArea(body);
        bodyTextArea.setLineWrap(true);
        bodyTextArea.setWrapStyleWord(true);
        bodyTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(bodyTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        docFrame.add(scrollPane);
        docFrame.setVisible(true);
    }

    public static void deleteFilesInDirectory(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
            for (Path filePath : directoryStream) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}