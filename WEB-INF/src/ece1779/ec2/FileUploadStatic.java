package ece1779.ec2;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

import ece1779.servlets.LoadBalancerLibrary;


public class FileUploadStatic extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException {
        try {

        	LoadBalancerLibrary.getInstance().clientInvokeCoordLoadBalance();
        	
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
            String key3 = "ObjectKey_" + UUID.randomUUID() +"_" + fileName;
            String key4 = "ObjectKey_" + UUID.randomUUID() +"_" + fileName;
            
            String name1 = path+key1;
            String name2 = path+key2;
            String name3 = path+key3;
            String name4 = path+key4;
            
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
            
            op = new IMOperation();
            op.addImage();
            op.rotate(90.0);
            op.addImage();
            
            cmd = new ConvertCmd();
            cmd.run(op, name1,name3);

            File file3 = new File(name3);
            
            op = new IMOperation();
            op.addImage();            
            op.flop();
            op.addImage();
            
            cmd = new ConvertCmd();
            cmd.run(op, name1,name4);

            File file4 = new File(name4);
 
//            PrintWriter out = response.getWriter();

            s3SaveFile(file1, key1, request, response);
        	s3SaveFile(file2, key2, request, response);
        	s3SaveFile(file3, key3, request, response);
        	s3SaveFile(file4, key4, request, response);
            
        	String username = (String)request.getSession().getAttribute("username");
        	updateDatabase(key1, key2, key3, key4, username);
        	
        	response.setContentType("text/html");
        	
        	PrintWriter out = response.getWriter();
            
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
    
    private int getUserId(String name)
    {
    	/*this method would eventually get the username from the session, query the db to get 
    	 * the userID, and return the user id. But for testing, setting userid to 1 for now
    	 *
    	 */
    	
        int userid = 1;
        
        // Get DB connection from pool
        DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");
        Connection con;
        try {
        	con = dbcp.getConnection();

        	// Execute SQL query
        	PreparedStatement stmt = con.prepareStatement("select id from users where login=?;");
        	stmt.setString(1, name);

        	ResultSet rs = stmt.executeQuery();

        	while(rs.next())
        	{
        		userid=rs.getInt(1);
        	}

        } catch (SQLException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
        return userid;
    }
    
    public void updateDatabase(String key, String key2, String key3, String key4, String name) {
    	Connection con = null;
    	try{
		    // Get DB connection from pool
		    DataSource dbcp = (DataSource)this.getServletContext().getAttribute("dbpool");
		    con = dbcp.getConnection();
	
	
		    // Execute SQL query
		    String sql = "insert into images (userId, key1, key2, key3, key4) values (?,?,?,?,?) ";
            PreparedStatement prepStmt = con.prepareStatement(sql);
            
            prepStmt.setInt(1,getUserId(name));
            prepStmt.setString(2, key);
            prepStmt.setString(3, key2);
            prepStmt.setString(4, key3);
            prepStmt.setString(5, key4);
            prepStmt.executeUpdate();
            
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
     
    
    public void s3SaveFile(File file, String key, HttpServletRequest request, HttpServletResponse response) throws IOException {

    	BasicAWSCredentials awsCredentials = (BasicAWSCredentials)this.getServletContext().getAttribute("AWSCredentials");

    	AmazonS3 s3 = new AmazonS3Client(awsCredentials);
        
        String bucketName = "ece1779-group1";
 


        try {
            s3.putObject(new PutObjectRequest(bucketName, key, file));
            
            s3.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);

        } catch (AmazonServiceException ase) {
            getServletContext().log("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            getServletContext().log("Error Message:    " + ase.getMessage());
            getServletContext().log("HTTP Status Code: " + ase.getStatusCode());
            getServletContext().log("AWS Error Code:   " + ase.getErrorCode());
            getServletContext().log("Error Type:       " + ase.getErrorType());
            getServletContext().log("Request ID:       " + ase.getRequestId());
            
        	request.getSession().setAttribute("errorMessage", new String("Amazon Service Exception: "+ase.getMessage()));
        	response.setContentType("text/html");
        	response.sendRedirect(request.getRequestURI());
        	getServletContext().log("");
        	return;
        } catch (AmazonClientException ace) {
            getServletContext().log("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            getServletContext().log("Error Message: " + ace.getMessage());
            
        	request.getSession().setAttribute("errorMessage", new String("Amazon Client Exception: "+ace.getMessage()));
        	response.setContentType("text/html");
        	response.sendRedirect(request.getRequestURI());
        	return;
        }
    }
    
    
}
