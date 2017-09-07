import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;

public class LuceneText {
    public static void main(String[] args) throws IOException, ParseException {
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // 1. create the index
        Directory indexDir = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        IndexWriter w = new IndexWriter(indexDir, config);
        addDoc(w, "Aydin Yilmaz", "Java,.Net,SQL");
        addDoc(w, "Metin Öztürk", "aa,ddd,Java");
        addDoc(w, "Selim Birinci", "eclipse,netbeans,javascript");
        addDoc(w, "Bahattin Yağmur", "sql,Java,Oracle");
        w.close();

        // 2. query
        String querystr = args.length > 0 ? args[0] : "java";

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        TermQuery q = (TermQuery)new QueryParser("skills", analyzer).parse(querystr);

        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("skills") + "\t" + d.get("name_surname"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
    }

    private static void addDoc(IndexWriter w, String name_surname, String skills) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("name_surname", name_surname, Field.Store.YES));

        // use a string field for isbn because we don't want it tokenized
        doc.add(new TextField("skills", skills, Field.Store.YES));
        w.addDocument(doc);
    }
}