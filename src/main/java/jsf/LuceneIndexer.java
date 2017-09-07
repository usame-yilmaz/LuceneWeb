package jsf;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class LuceneIndexer {

    public static void main(String[] args) throws Exception {

        String indexDir = "C:/index/";
        File dataDir = new File("C:/Users/uyilmaz/IdeaProjects/usame_test/test");
        String suffix = "docx";

        LuceneIndexer indexer = new LuceneIndexer();

        Instant first=Instant.now(),second;
        int numIndex=0;
//        for(int i=0;i<1000;i++) {
            numIndex = indexer.index(indexDir, dataDir, suffix);
//        }
        second = Instant.now();
        Duration duration = Duration.between(first, second);

        System.out.println("Total files indexed: " + numIndex);
        System.out.println("Duration:  " + duration.toMillis() +" ms");

    }

    public int index(String indexDir, File dataDir, String suffix) throws Exception {

        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        IndexWriter indexWriter = new IndexWriter(
                FSDirectory.open(Paths.get(indexDir)),
                config);

        // reset indexes
        indexWriter.deleteAll();

        indexDirectory(indexWriter, dataDir, suffix);

        int numIndexed = indexWriter.maxDoc();

        //indexWriter.optimize();
        indexWriter.commit();
        indexWriter.close();

        return numIndexed;

    }

    private void indexDirectory(IndexWriter indexWriter, File dataDir,
                                String suffix) throws IOException {

        File[] files = dataDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                indexDirectory(indexWriter, f, suffix);
            } else {
                indexFileWithIndexWriter(indexWriter, f, suffix);
            }
        }

    }

    private void indexFileWithIndexWriter(IndexWriter indexWriter, File f,
                                          String suffix) throws IOException {

        if (f.isHidden() || f.isDirectory() || !f.canRead() || !f.exists()) {
            return;
        }
//        if (suffix != null && !f.getName().endsWith(suffix)) {
//            return;
//        }

        // Apache Tika Parsing
        Metadata metadata = new Metadata();
        ContentHandlerDecorator handler = new BodyContentHandler();
        ParseContext context = new ParseContext();
        Parser parser = new AutoDetectParser();
        InputStream stream = new FileInputStream(f);
        try {
            parser.parse(stream, handler, metadata, context);
        }
        catch (TikaException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        finally {
            stream.close();
        }

        String text = handler.toString();
        String fileName = f.getName();

        Document doc = new Document();
        doc.add(new TextField("contents", text, Field.Store.YES));
        // End Apache Tika Parsing


//        Document doc = new Document();
//        doc.add(new TextField("contents", new FileReader(f)));
        doc.add(new TextField("filename", f.getCanonicalPath(),
                Field.Store.YES));

        indexWriter.addDocument(doc);
        System.out.println("Indexing file " + f.getCanonicalPath());
    }
}