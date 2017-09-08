package beans;
import org.apache.commons.io.FileUtils;
import util.LuceneIndexer;
import util.LuceneParameters;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.Part;
import java.io.File;

@ManagedBean(name="fileUpload")
@RequestScoped
public class FileUploadBean {
    private Part file;

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
    public String getFileName(Part part)
    {
        for(String cd:part.getHeader("content-disposition").split(";"))
            if(cd.trim().startsWith("filename")){
                String filename=cd.substring(cd.indexOf('=')+1).trim().replace("\"", "");
                return filename;
            }
        return "";

    }
    public void upload()
    {
        try{

            file.write(LuceneParameters.LUCENE_SOURCE_DIRECTORY +"/"+getFileName(file));

            String indexDir = LuceneParameters.LUCENE_INDEX_DIRECTORY;

            File dataDir = new File(LuceneParameters.LUCENE_SOURCE_DIRECTORY);
            String suffix = "";

            int numIndex=0;
            numIndex = LuceneIndexer.index(indexDir, dataDir, getFileName(file));

            System.out.println("Total files indexed: " + numIndex);
        }
        catch(Exception ex) {
            System.out.println(ex);
        }
    }

}