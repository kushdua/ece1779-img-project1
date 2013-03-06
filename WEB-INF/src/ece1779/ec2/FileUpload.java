package ece1779.ec2;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import org.im4java.core.*;


public class FileUpload extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
        try {

        	// Create a factory for disk-based file items
        	FileItemFactory factory = new DiskFileItemFactory();

        	// Create a new file upload handler
        	ServletFileUpload upload = new ServletFileUpload(factory);

        	// Parse the request
        	List /* FileItem */ items = upload.parseRequest(request);
        	
        	
        	// User ID
        	FileItem item1 = (FileItem)items.get(0);
        	
        	String name = item1.getFieldName();
        	String value = item1.getString();
        	
        	//Uploaded File
            FileItem theFile = (FileItem)items.get(1);


            // filename on the client
            String fileName = theFile.getName();
            
   
            // get root directory of web application
            String path =  "/home/ubuntu/workspace/temp_imgs/";           	//this.getServletContext().getRealPath("/");        

            
            String key1 = "ObjectKey_" + UUID.randomUUID() +"_" + fileName;
            String key2 = "ObjectKey_" + UUID.randomUUID() +"_" + fileName;

            
            String name1 = path+key1;
            String name2 = path+key2;
            
            // store file in server
            File file1 = new File(name1); 
            theFile.write(file1);

            // Use imagemagik to transform image
            IMOperation op = new IMOperation();
            op.addImage();
            op.flip();
            op.addImage();
            
            ConvertCmd cmd = new ConvertCmd();
            cmd.run(op, name1,name2);

            File file2 = new File(name2);
            
 
            
            
            PrintWriter out = response.getWriter();
                       

            s3SaveFile(file1, key1, out);
        	s3SaveFile(file2, key2, out);
            
        	updateDatabase(key1,out);
        	    
        	
        	response.setContentType("text/html");
            
            out.write("<html><head><title>Sample Image Upload</title></head>");
            out.write("<body>");
            
            out.write("<img src='http://d4e4zv37grvr9.cloudfront.net/" + key1 + "' />");
            out.write("<img src='http://d4e4zv37grvr9.cloudfront.net/" + key2 + "' />");
            
            out.write("</body></html>");
             
	}
	catch (Exception ex) {
	    throw new ServletException (ex);
	}
	
    }
    
    public void updateDatabase(String key, PrintWriter out) {
    	Connection con = null;
    	try{ 
            // In real life, you should get these values from a shopping cart
            int userid = 1;
            
		    // Get DB connection from pool
		    DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");
		    con = dbcp.getConnection();
	
	
		    // Execute SQL query
		    Statement stmt = con.createStatement();
            String    sql = "insert into files (userId, s3Key) "+
                      "value(" + userid + ",'" + key + "')";
            stmt.execute(sql);
        }
    	catch(Exception ex) {
              getServletContext().log(ex.getMessage());  
    	}    	
    	finally {
    		try {
    		con.close();
    		}
    		catch (Exception e) {
                getServletContext().log(e.getMessage());  
    		}
    	}  

    }   
     
    
    public void s3SaveFile(File file, String key, PrintWriter out) throws IOException {

    	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)this.getServletContext().getAttribute("AWSCredentials");

    	AmazonS3 s3 = new AmazonS3Client(awsCredentials);
        
        String bucketName = "ece1779-group1";
 


        try {
            s3.putObject(new PutObjectRequest(bucketName, key, file));
            
            s3.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);

        } catch (AmazonServiceException ase) {
            out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            out.println("Error Message:    " + ase.getMessage());
            out.println("HTTP Status Code: " + ase.getStatusCode());
            out.println("AWS Error Code:   " + ase.getErrorCode());
            out.println("Error Type:       " + ase.getErrorType());
            out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            out.println("Error Message: " + ace.getMessage());
        }
    }
    
    
}
