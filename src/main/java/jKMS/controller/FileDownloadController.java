package jKMS.controller;
 
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

import au.com.bytecode.opencsv.CSVWriter;
import jKMS.LogicHelper;
import jKMS.Pdf;
import jKMS.exceptionHelper.CreateFolderFailedException;
import jKMS.exceptionHelper.NoContractsException;
import jKMS.exceptionHelper.NoIntersectionException;
 
/**
 * Controller for downloading any files.
 * @author Quiryn
 * @author siegmund42
 *
 */
@Controller
public class FileDownloadController extends AbstractServerController {
	byte[] pdfBytes = null;
	
	/**
	 *  Downloading Seller-/BuyerCardsPDF
	 *  @param	type	determines the type of pdf ["customer"/"salesman"]
	 *  @return ResponseEntity directly serves the file for download for the browser
	 */
    @RequestMapping(value = "/pdf/cards/{type}")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable String type) throws Exception	{
		
    	// Create new Document
		Document document = new Document();
		// Get a new Outputstream for the PDF Library
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		try {
			// Very Handwaving itext-PdfWriter.getInstance method
			PdfWriter.getInstance(document, outstream); 
			// Open document to write in it
			document.open();
			
			// Create PDF for Buyer/Seller
			kms.getState().createPdf(type.equals("customer"), document);
			
			// Close document
			document.close();
			
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
			// Throw new Exception because were not able to return an Error page at this moment
			throw new RuntimeException(LogicHelper.getLocalizedMessage("error.PDF.cards"));
		}
	
		// Get a byte Array of the pdf
	    byte[] contents = outstream.toByteArray();
	    
	    // Write Headers
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.parseMediaType("application/pdf"));
	    
	    // Set the download-Filename
	    String filename = null;
	    switch(type)	{
	    case("customer"):
	    	filename = ControllerHelper.getFilename("filename.PDF.buyer") + ".pdf";
	    	break;
	    case("salesman"):
	    	filename = ControllerHelper.getFilename("filename.PDF.seller") + ".pdf";
	    	break;
	    }
	    
	    if(filename == null)
	    	throw new RuntimeException("False path parameter!");
	    
	    headers.setContentDispositionFormData(filename, filename);
	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    // Serve Data to the browser
	    ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(contents, headers, HttpStatus.OK);
	    return response;
    }
    
    /**
     * catch ajax-request when evaluate.html is ready
     * prepare export-pdf for download
     * @param image
     * @throws IllegalStateException
     * @throws NoIntersectionException
     */
    @RequestMapping(value = "/pdfExport",
    				method = RequestMethod.POST)
    public void exportPDF(@RequestParam("image") MultipartFile image, @RequestParam("se") boolean se) throws IllegalStateException, NoIntersectionException, CreateFolderFailedException	{
    	byte[] imageBytes = null;

    	if(!image.isEmpty()){
    		try	{
    			imageBytes = image.getBytes();
    		} catch(IOException e){
    			e.printStackTrace();
    			throw new RuntimeException(LogicHelper.getLocalizedMessage("error.PDF.export"));
    		}
    	}

		Image pdfImage = null;
		Map<String, Float> stats = null;
		try {
			pdfImage = Image.getInstance(imageBytes);
			stats = kms.getState().getStatistics();
		} catch (BadElementException | IOException e2) {
			e2.printStackTrace();
			throw new RuntimeException(LogicHelper.getLocalizedMessage("error.PDF.export"));
		}	catch(NoContractsException e)	{
			e.printStackTrace();
			throw new RuntimeException(LogicHelper.getLocalizedMessage("error.noContracts"));
		}
		
    	// Save to byteArray and File ########################################
    	Pdf pdf = new Pdf();
    	Document document = new Document(PageSize.A4.rotate());
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		// to File
		FileOutputStream fos;
    	String path = ControllerHelper.getFolderPath("export") + ControllerHelper.getFilename("filename.PDF.export") + ".pdf";
		
    	try {
    		// Call Handwaving-Method twice to write into both streams [so glad it works...]
			PdfWriter.getInstance(document, outstream);
			if(ControllerHelper.checkFolders() && se)	{
				fos = new FileOutputStream(path);
				PdfWriter.getInstance(document, fos);
			}
			document.open();
			document = pdf.createExportPdf(document, pdfImage, stats);
			document.close();
			if(se)	{
				// Check if it was really saved
				File file = new File(path);
				if(file.exists())
					LogicHelper.print("Saved the Export-PDF in: " + path);
				else 
					LogicHelper.print("Saving Export-PDF failed.", 2);
			}
		} catch (FileNotFoundException | DocumentException e1) {
			e1.printStackTrace();
			throw new RuntimeException(LogicHelper.getLocalizedMessage("error.PDF.export"));
		}

		pdfBytes = outstream.toByteArray();

    }
    
	/**
	 *  Downloading Exported PDF
	 *  @return ResponseEntity directly serves the file for download for the browser
	 */
    @RequestMapping(value = "/pdfDownload")
    public ResponseEntity<byte[]> downloadPdf(){

	    if(pdfBytes != null){
	    	byte[] contents = pdfBytes;

		    // Define filename for download
		    String filename = ControllerHelper.getFilename("filename.PDF.export") + ".pdf";
		    
		    HttpHeaders headers = new HttpHeaders();
		    // Write headers
		    headers.setContentType(MediaType.parseMediaType("application/pdf"));
		    headers.setContentDispositionFormData(filename, filename);
		    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		    
		    return new ResponseEntity<byte[]>(contents, headers, HttpStatus.OK);
	    }	else	{
	    	throw new RuntimeException(LogicHelper.getLocalizedMessage("error.PDF.export.download"));
	    }
    	
    }

    /**
     *  Serve the config.txt for download. 
     *  Note that it is processed again [save(bos)] to avoid 
     *  deletion of the file between saving and loading.
	 *  @return ResponseEntity directly serves the file for download for the browser
	 *  @throws Exception of type
	 */
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadConfig()	{ 
    	
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	
		try {
			// Create CSV
			kms.getState().save(bos);
		} catch (IOException e) {
			e.printStackTrace();
			// Throw new Exception because were not able to return an Error page at this moment
			throw new RuntimeException(LogicHelper.getLocalizedMessage("error.config.download"));
		}
		
		// Get a byte Array of the csv
	    byte[] contents = bos.toByteArray();
	    // Define Filename for download
	    String filename = ControllerHelper.getFilename("filename.config") + ".txt";
	    // Write headers
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.parseMediaType("application/txt"));
	    headers.setContentDispositionFormData(filename, filename);
	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    
	    return new ResponseEntity<byte[]>(contents, headers, HttpStatus.OK);
    }
    
    /**
     * Downloads the export csv file
     * @return ResponseEntity
     * @throws Exception
     */
    @RequestMapping(value = "/csv")
    public ResponseEntity<byte[]> downloadCsv() throws Exception{ 
    	
    	ByteArrayOutputStream outstream = new ByteArrayOutputStream();
    	CSVWriter writer = new CSVWriter(new OutputStreamWriter(outstream));
    	
		try {
			// Create CSV
			kms.getState().generateCSV(writer);
			
			// Close document
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			// Throw new Exception because were not able to return an Error page at this moment
			throw new RuntimeException(LogicHelper.getLocalizedMessage("error.csv.download"));
		}
		
		// Get a byte Array of the csv
	    byte[] contents = outstream.toByteArray();
	    // Define Filename for download
	    String filename = ControllerHelper.getFilename("filename.csv") + ".csv";
	    // Write headers
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.parseMediaType("text/csv"));
	    headers.setContentDispositionFormData(filename, filename);
	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    
	    return new ResponseEntity<byte[]>(contents, headers, HttpStatus.OK);
    }
    
}
