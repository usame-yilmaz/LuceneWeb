package util;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public final class LuceneSearcher {

    public static void main(String[] args) throws Exception {

        String indexDir = LuceneParameters.LUCENE_INDEX_DIRECTORY;
        String query = "ReactJS";
        int hits = 10000;

        LuceneSearcher.searchIndex(indexDir, query, hits);

    }

    public static String searchIndex(String indexDir, String queryStr, int maxHits)
            throws Exception {

        StringBuilder sb = new StringBuilder();

        Directory directory = FSDirectory.open(Paths.get(indexDir));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Duration duration = Duration.ZERO;
        ScoreDoc[] hits = null;

        try {
            DirectoryReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            QueryParser parser = new QueryParser("contents", analyzer);
            Query query = parser.parse(queryStr);

            TopDocs topDocs = searcher.search(query, maxHits);

            Instant first, second;
            hits = topDocs.scoreDocs;

            first = Instant.now();
            for (int i = 0; i < hits.length; i++) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println(d.get("filename"));
                sb.append(d.get("filename") + "\n");
            }
            second = Instant.now();
            duration = Duration.between(first, second);
        } catch (IndexNotFoundException iex) {
            sb.append("No Indexed files exist");
            System.out.println(iex);
        }

        if(hits!=null) {
            System.out.println("Found " + hits.length);
            System.out.println("Duration " + duration.toMillis() + " ms");

            sb.append("Found " + hits.length + "\n");
            sb.append("Duration " + duration.toMillis() + " ms\n");
        }
        return sb.toString();
    }

}
