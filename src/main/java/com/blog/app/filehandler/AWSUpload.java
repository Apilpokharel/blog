package com.blog.app.filehandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class AWSUpload {

	

	@Autowired
	private AmazonS3 s3client;
	
	@Value("${amazon.s3.endPointURL}")
	private String endpointURL;
	
	@Value("${amazon.s3.bucket}")
	private String bucketName;
	
	@Value("${amazon.aws.access_key_id}")
	private String accessKey;
	
	@Value("${amazon.aws.secret_access_key}")
	private String secretKey;
	
	
	@Value("${amazon.s3.region}")
	private String region;
	
	
	
	
	public String fileUpload(MultipartFile multipartfile) throws IOException {
		
		if(multipartfile != null) {
			String[] type = multipartfile.getContentType().split("/");
			
			if(type[0].equalsIgnoreCase("image")) {

				
				long fsize = multipartfile.getSize();
				String fileURL = null;
                String fileName = null;
				if(fsize < 1000*1000*100) {
					try {
//						File file = convertMultiPartToFile(multipartfile);
						 fileName = generateFileName(multipartfile);
						 InputStream is = new  BufferedInputStream(multipartfile.getInputStream());
						fileURL = endpointURL + "/" + bucketName + "/" + fileName;
						uploadFileTos3bucket(fileName, is);
					} catch (AmazonServiceException ase) {
				           System.out.println("amazon service exception "+ase);

			        } catch (AmazonClientException ace) {
			           System.out.println("amazon client exception "+ace);
			        }
					
					return fileName;
				}
				
				
			}
		}
		
		return null;
	}
		
		
		
	
	
	
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}
	
	
	private String generateFileName(MultipartFile multiPart) {
		UUID uuid = UUID.randomUUID();
		
		return uuid.toString() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}
	
	private void uploadFileTos3bucket(String fileName, InputStream is) throws IOException {
		ObjectMetadata meta = new ObjectMetadata();

		 byte[] byteArray =  new byte[is.available()];
		 System.out.println("data lenght is "+ byteArray.length);
		meta.setContentLength(byteArray.length);
		s3client.putObject(new PutObjectRequest(bucketName, fileName, is, meta).withCannedAcl(CannedAccessControlList.PublicRead));
		
	}
	
	
	public void deleteFileFromS3Bucket(String fileName) {
//		String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
		s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
        System.out.println("successfully deleted");	}
}
