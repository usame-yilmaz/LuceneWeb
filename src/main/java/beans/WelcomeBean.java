package beans;

import util.LuceneIndexer;
import util.LuceneParameters;
import util.LuceneSearcher;

import java.io.File;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean(name="welcome")
@SessionScoped
public class WelcomeBean implements Serializable{

    private static final long serialVersionUID = -6913972022251814607L;

    private String s1="+JUNIT +JAVA -MOCK";
    private String s2;
    private String s3;

    public String getS1() throws Exception {
        System.out.println(s1);
        return s1;
    }

    public void setS1(String s1) throws Exception{

        this.s1=s1;
    }

    public String getS2() throws Exception {

        String indexDir = LuceneParameters.LUCENE_INDEX_DIRECTORY;
        //String query = "ReactJS";
        int hits = 10000;
        System.out.println("Search for:"+ s1);
        s2 = LuceneSearcher.searchIndex(indexDir, s1, hits);
        return s2;
    }

    public void setS2(String s2) throws Exception{
        this.s2=s2;
    }

    public String getS3() throws Exception {

        String indexDir = LuceneParameters.LUCENE_INDEX_DIRECTORY;
        File dataDir = new File(LuceneParameters.LUCENE_SOURCE_DIRECTORY);
        String filename = "";
        StringBuilder sb = new StringBuilder();

        Instant first=Instant.now(),second;
        int numIndex=0;

        // initial indexing of all files - commented out
        // numIndex = LuceneIndexer.index(indexDir, dataDir, filename);

        second = Instant.now();
        Duration duration = Duration.between(first, second);

        System.out.println("Total files indexed: " + numIndex);
        System.out.println("Duration:  " + duration.toMillis() +" ms");

        sb.append("Total files indexed: " + numIndex + "\n");
        sb.append("Duration:  " + duration.toMillis() +" ms\n");

        return sb.toString();
    }

    public void setS3(String s3) throws Exception{

        this.s3=s3;
    }

}