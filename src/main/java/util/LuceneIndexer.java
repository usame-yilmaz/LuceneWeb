package util;

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

public final class LuceneIndexer {

    public static void main(String[] args) throws Exception {

        String indexDir = LuceneParameters.LUCENE_INDEX_DIRECTORY;
        File dataDir = new File(LuceneParameters.LUCENE_SOURCE_DIRECTORY);
        String suffix = "";

        Instant first=Instant.now(),second;
        int numIndex=0;
        numIndex = LuceneIndexer.index(indexDir, dataDir, suffix);

        second = Instant.now();
        Duration duration = Duration.between(first, second);

        System.out.println("Total files indexed: " + numIndex);
        System.out.println("Duration:  " + duration.toMillis() +" ms");

    }

    public static int index(String indexDir, File dataDir, String fileName) throws Exception {

        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

        IndexWriter indexWriter = new IndexWriter(
                FSDirectory.open(Paths.get(indexDir)),
                config);

        // reset indexes
        //indexWriter.deleteAll();

        indexDirectory(indexWriter, dataDir, fileName);

        int numIndexed = indexWriter.maxDoc();

        //indexWriter.optimize();
        indexWriter.commit();
        indexWriter.close();

        return numIndexed;

    }

    // if filename not specified, create lucene indexes for whole directory recursively
    private static void indexDirectory(IndexWriter indexWriter, File dataDir,
                                String fileName) throws IOException {

        File[] files = dataDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                indexDirectory(indexWriter, f, fileName);
            } else {
                indexFileWithIndexWriter(indexWriter, f, fileName);
            }
        }

    }

    private static void indexFileWithIndexWriter(IndexWriter indexWriter, File f,
                                          String fileName) throws IOException {

        if (f.isHidden() || f.isDirectory() || !f.canRead() || !f.exists()) {
            return;
        }

        // If filename specified, index only that file - skip others
        if ( !fileName.isEmpty() && !fileName.equalsIgnoreCase(f.getName())) {
            return;
        }

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
// End Apache Tika Parsing

        Document doc = new Document();
        doc.add(new TextField("contents", text, Field.Store.YES));


//        Document doc = new Document();
//        doc.add(new TextField("contents", new FileReader(f)));
        doc.add(new TextField("filename", f.getCanonicalPath(),
                Field.Store.YES));

        indexWriter.addDocument(doc);
        System.out.println("Indexing file " + f.getCanonicalPath());
    }
}