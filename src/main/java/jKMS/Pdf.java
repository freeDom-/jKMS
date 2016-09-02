package jKMS;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
//import java.util.Properties;
import java.util.Set;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import jKMS.cards.BuyerCard;
import jKMS.cards.Card;

/**
 * 
 * Class for pdf functions
 *
 */
public class Pdf{
	/**
	 * A class for handling the PDF-Export
	 */	
	 //define Fonts
	private  Font titleFont;
	private  Font valueFont;	
	private  Font packFont;
	
	private String cardtitle;
	private String value;
	private String id;
	private String titlepage;
	private String packet;
	private String from;
	private String to;
	
	/**
	 *Defines Fonts
	 * 
	 * 		
	 */		
	public Pdf(){// to catch crashes
		FontFactory.defaultEmbedding = true;
		FontFactory.register("fonts/LiberationSans-Bold.ttf", "my_font_Bold");
		FontFactory.register("fonts/LiberationSans-Regular.ttf", "my_font_regular");
		//FontFactory.getFont("my_font", BaseFont.WINANSI, BaseFont.EMBEDDED, 12, Font.ITALIC); 
		titleFont =FontFactory.getFont("my_font_Bold", BaseFont.WINANSI, BaseFont.EMBEDDED, 25);
		valueFont = FontFactory.getFont("my_font_regular", BaseFont.WINANSI, BaseFont.EMBEDDED, 18);
		packFont = FontFactory.getFont("my_font_regular", BaseFont.WINANSI, BaseFont.EMBEDDED, 12); 
		
		this.cardtitle = "Default";
		this.value = "Default";
		this.id = "Default";
	}
	
	/**
	 * Writes a generated cardPDF to the Document cardsBuyer.
	 * The cards are putted in the PDF depending on their type and the number of Packages.
	 * 
	 * @param  CardType		The class of cards for building the PDF. Should be either the class of SellerCard or BuyerCard
	 * @param  kms			the actual instance of Kartoffelmarktspiel for determining some important information
	 * @param  cardsBuyer	document for export
	 */


	public void createPdfCards(Class<? extends Card> CardType, Kartoffelmarktspiel kms, Document cardsBuyer) throws DocumentException,IOException{ 
    	//Author: Justus (Timon with the good idea)
    	//----------------------DEFINATIONS-----------------------------------
    	//PRINT
		
        //get Strings
		String filename;
        if(CardType.isAssignableFrom(BuyerCard.class))	{
			cardtitle = LogicHelper.getLocalizedMessage("PDFBuyer.cardtitle");
	        value = LogicHelper.getLocalizedMessage("PDFBuyer.value") + ": ";
			titlepage = LogicHelper.getLocalizedMessage("PDFBuyer.titlepage");
			filename = LogicHelper.getLocalizedMessage("filename.PDF.buyer");
        }	else	{
    		cardtitle = LogicHelper.getLocalizedMessage("PDFSeller.cardtitle");
	        value = LogicHelper.getLocalizedMessage("PDFSeller.value") + ": ";
	  		titlepage = LogicHelper.getLocalizedMessage("PDFSeller.titlepage");
			filename = LogicHelper.getLocalizedMessage("filename.PDF.seller");
        }
  		id = LogicHelper.getLocalizedMessage("PDF.id") + ": ";
  		packet = LogicHelper.getLocalizedMessage("PDF.package");
  		from = LogicHelper.getLocalizedMessage("PDF.from");
  		to = LogicHelper.getLocalizedMessage("PDF.to");
		
    	//LOGIC
    	//at every paper are 2 cards --> 2 sets one for top one for bottom
        Set<Card> printcards = new LinkedHashSet<Card>(); // add first all cards + package idendifikationsites in one Set
        Set<Card> topcards = new LinkedHashSet<Card>();
        Set<Card> bottomcards = new LinkedHashSet<Card>();
        Set<Card> cards = kms.getCards();
        int[] packdis = LogicHelper.getPackageDistribution(cards.size(), kms.getAssistantCount());
        int packID = 0;
        int packsize = 0;
        int i = 0;
        Constructor<? extends Card> cardConstructor = null;
		try {
			cardConstructor = CardType.getConstructor(int.class, int.class, Package.class);
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		}
        
        //----------------------IMPLEMENTATION-------------------------------
        //add card for package idedifikation (first package) 
        try {
			printcards.add(cardConstructor.newInstance(-42,0,kms.getPackage('A')));
        
	        for(Card iter : cards){
				if(packsize == packdis[packID]){ //is there a new package ? before printing first card of new pack packagepage
	            	//yes
	        		packID++;
	        		packsize = 0;
	        		//add card for package idedifikation
	        		printcards.add(cardConstructor.newInstance(-42,0,iter.getPackage()));
	        	}
				
				if(iter.getClass().isAssignableFrom(CardType))	{ //seller or buyer?
					printcards.add(iter);
	        	}
				
				packsize++;
	        }
	        	
	       if((printcards.size() % 2) == 0){ // split printcards in top and bottom
	           for(Card iter : printcards){
	        	   if(i < printcards.size()/2){
	        		   topcards.add(iter);
	        		   i++;
	        	   }else{
	        		   bottomcards.add(iter);
	        		   i++;
	        	   	}   
	           }
	       }else{
	           for(Card iter : printcards){
	        	   if(i < (printcards.size()/2) + 1){
	        		   topcards.add(iter);
	        		   i++;
	        	   }else{
	        		   bottomcards.add(iter);
	        		   i++;
	        	   	}   
	           } 
	           bottomcards.add(cardConstructor.newInstance(-42,0,null));// add wihteside
	       }
	       
	       //PRINT
	        
	        //MetaData
	       
	        cardsBuyer.addTitle(filename);
	        cardsBuyer.addAuthor("Pit Market 2.0");
	        cardsBuyer.addCreator("Pit Market 2.0");
	        
	        Iterator<Card> itertop = topcards.iterator();
	        Iterator<Card> iterbot = bottomcards.iterator();
	        Card topcard = null;
	        Card bottomcard = null;
	        
	        //Content       
	
	    	cardsBuyer.add(this.Titlepage(kms.getPackages(), CardType.isAssignableFrom(BuyerCard.class)));
	    	cardsBuyer.newPage();
	
	    	
	        
	        while(itertop.hasNext()){
	        	
	        	//Top
	        	topcard = itertop.next();
	        	Paragraph top = createCard(topcard, true);
	          
	        	//bottom
	        	bottomcard = (Card) iterbot.next();
	        	Paragraph bottom = createCard(bottomcard, false);
	               
	        	//set Content togeser ;-)
	        
	        	PdfPCell topcell = new PdfPCell();
	        	topcell.addElement(new Paragraph(" "));//Leerzeile, damit top paragraph funzt
	        	topcell.addElement(top);
	        	topcell.addElement(this.createPackOnCard(topcard.getPackage()));//set Package bottom right on card
	        	topcell.setBorder(Rectangle.BOTTOM);
	        
	        	PdfPCell bottomcell = new PdfPCell();
	        	bottomcell.addElement(new Paragraph(" "));//Leerzeile, damit bottom paragraph funzt
	        	bottomcell.addElement(bottom);
	        	bottomcell.addElement(this.createPackOnCard(bottomcard.getPackage()));//set Package bottom right on card        	bottomcell.setBorder(Rectangle.NO_BORDER);
	        	bottomcell.setBorder(Rectangle.NO_BORDER);
	        	
	        	PdfPTable myTable = new PdfPTable(1);
	        	myTable.setWidthPercentage(100.0f);
	        	myTable.addCell(topcell);
	        	myTable.addCell(bottomcell);
	        
	        
	        	cardsBuyer.add(myTable);
	        	cardsBuyer.newPage();
	        }

		} catch (InstantiationException | IllegalAccessException | 
				IllegalArgumentException | InvocationTargetException |
				SecurityException e) {
			e.printStackTrace();
		}
  
    }


	private Paragraph createPackOnCard(Package pack){
		Paragraph cPa =null; 
		if(pack == null) cPa = new Paragraph(" ",packFont);
		else	cPa = new Paragraph(String.valueOf(pack.getName()),packFont);
    	cPa.setAlignment(Element.ALIGN_RIGHT);
    	cPa.setSpacingAfter(20);
    	return cPa;
	}
	
	/**
	 *generate right card: seller,buyer,package
	 * 
	 * @param  card		card to generate
	 * @param  istop    different formations of top an bottomcards	
	 * @return paragraph with the generated cards	
	 */	 
	
    private Paragraph createCard(Card card,boolean isTop){
    	
    	Paragraph content = new Paragraph();
        content.setAlignment(Element.ALIGN_CENTER);
        
    	if(isTop){
    		content.setSpacingBefore(123);
            content.setSpacingAfter(138);
    		if(card.getId() == -42 && card.getValue() == 0){
                content.setSpacingAfter(163);
    		}
    	}else{
            content.setSpacingBefore(150);
            content.setSpacingAfter(111);
    	}
    	
        if((card.getId() != -42) && (card.getValue() != 0)){
        	Chunk cTitle = new Chunk(cardtitle,titleFont);
        	content.add(cTitle);
        	content.add(Chunk.NEWLINE);
        	content.add(Chunk.NEWLINE);
        	Chunk cValue = new Chunk(this.value + card.getValue() + Kartoffelmarktspiel.getCurrency(),valueFont);
        	content.add(cValue);
        	content.add(Chunk.NEWLINE);
        	Chunk cID = new Chunk(this.id + card.getId(),valueFont);
        	content.add(cID);
        }else{
        	Paragraph pack;
        	if(card.getPackage() == null){
        		pack = new Paragraph(" ",titleFont);	
        	}else{
        		pack = new Paragraph(String.valueOf(card.getPackage().getName()),titleFont);
        	}
        	pack.setAlignment(Element.ALIGN_CENTER);
        	content.add(pack);
        }
        	
    	return content;
    }
	/**
	 *generate the titlepage for seller or buyer pdf it contains a table with the packageinformation
	 * 
	 * @param  packdis		field with all packages and there sizes get form logichelper
	 * @param  firstID		firstID default 1001 possible to chance with jkms.configuration.setFirstID 
	 * @param  isBuyer		Buyer or Seller?
	 * @param  cards		Set of cards filled in generateCards() (preparetion)
	 * @return 	a paragraph (itext) with the titlepage
	 */

    private Paragraph Titlepage(Set<Package> packages, boolean isBuyer){
    	
    	Paragraph titlep = new Paragraph();
    	//Set Headline
    	titlep.setAlignment(Element.ALIGN_CENTER);
    	titlep.setFont(titleFont);
    	titlep.add(this.titlepage);
    	titlep.add(Chunk.NEWLINE);
    	titlep.add(Chunk.NEWLINE);

    	
    	titlep.setFont(valueFont);
    	PdfPTable table = new PdfPTable(3);

    	Chunk content;
    	
    	Paragraph allcontent1 = new Paragraph();
    	Paragraph allcontent2 = new Paragraph();
    	Paragraph allcontent3 = new Paragraph();
    	
    	PdfPCell cell1y = new PdfPCell();
    	PdfPCell cell2y = new PdfPCell();
    	PdfPCell cell3y = new PdfPCell();
    	
    	allcontent1.setAlignment(Element.ALIGN_CENTER);
    	allcontent2.setAlignment(Element.ALIGN_CENTER);
    	allcontent3.setAlignment(Element.ALIGN_CENTER);
    	//cellxy.setBorder(Rectangle.NO_BORDER); // no border for all cells
    	//set table headlines
    	content = new Chunk(this.packet,valueFont);
    	allcontent1.add(content);
    	allcontent1.add(Chunk.NEWLINE);
      	content =new Chunk(this.from,valueFont);
    	allcontent2.add(content);
    	allcontent2.add(Chunk.NEWLINE);
      	content = new Chunk(this.to,valueFont);
    	allcontent3.add(content);
    	allcontent3.add(Chunk.NEWLINE);
    	
    	char type = 's';
    	
    	if(isBuyer)
    		type = 'b';
    	
    	for(Package pack : packages)	{
    		
    		content = new Chunk(String.valueOf(pack.getName()),valueFont);
    		allcontent1.add(content);
    		allcontent1.add(Chunk.NEWLINE);

			content = new Chunk(Integer.toString(pack.getFirstCard(type).getId()),valueFont);
	    	allcontent2.add(content);
	    	allcontent2.add(Chunk.NEWLINE);
	
	      	//add end id to table
	      	content = new Chunk(Integer.toString(pack.getLastCard(type).getId()),valueFont);
	    	allcontent3.add(content);
	    	allcontent3.add(Chunk.NEWLINE);
    	
    	}
    	
//    	Ehemals sexistische Kackscheiße !!!
    	
    	cell1y.addElement(allcontent1);
    	cell2y.addElement(allcontent2);
    	cell3y.addElement(allcontent3);
    	cell1y.setBorder(Rectangle.NO_BORDER);
    	cell2y.setBorder(Rectangle.NO_BORDER);
    	cell3y.setBorder(Rectangle.NO_BORDER);
    	table.addCell(cell1y);
    	table.addCell(cell2y);
    	table.addCell(cell3y);
    	titlep.add(table);
    	return titlep;
    }
    
	/**
	 *generate the export pdf with the statistics and the chart as image
	 * 
	 * @param doc 		document for export
	 * @param pdfImage 	image of the chart
	 * @param stats  	Map of the statistics 
	 * @return 	the document (pdf export)
	 * 		
	 */	
    
    public Document createExportPdf(Document doc, Image pdfImage, Map<String, Float> stats) throws DocumentException{
        
        //get Strings
        String headline = LogicHelper.getLocalizedMessage("evaluate.headline");
		String average = LogicHelper.getLocalizedMessage("evaluate.average") + ": ";
		String size = LogicHelper.getLocalizedMessage("evaluate.size") + ": ";
        String min = LogicHelper.getLocalizedMessage("evaluate.min") + ": ";
		String max = LogicHelper.getLocalizedMessage("evaluate.max") + ": ";
		String standDev = LogicHelper.getLocalizedMessage("evaluate.standardDeviation") + ": ";
		String eqPrice = LogicHelper.getLocalizedMessage("evaluate.eqPrice") + ": ";
		String eqQuantity = LogicHelper.getLocalizedMessage("evaluate.eqQuantity") + ": ";
		// We'll only need this if customer wants to have benefits on PDF
		String realBen = LogicHelper.getLocalizedMessage("evaluate.realBenefits") + ": ";
		String hypBen = LogicHelper.getLocalizedMessage("evaluate.hypBenefits") + ": ";
		String currency = Kartoffelmarktspiel.getCurrency();
		
    	//insert stats
		Paragraph head = new Paragraph(headline, titleFont);
		this.valueFont.setSize(16);
		//9GAG rocks!!!
    	head.setAlignment(Element.ALIGN_CENTER);
    	head.setSpacingAfter(20);
    	
		doc.add(head);
		
		String averageValue = String.format("%.2f", Math.round(stats.get("averagePrice")*100)/100.0) + currency;
		String deviationValue = String.format("%.2f", Math.round(stats.get("standardDeviation")*100)/100.0);
    	
    	PdfPTable table = new PdfPTable(3);
    	
    	PdfPCell cell11 = new PdfPCell(new Paragraph(average + averageValue ,valueFont));
    	cell11.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell21 = new PdfPCell(new Paragraph(min + Math.round(stats.get("minimum")) + currency,valueFont));
    	cell21.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell31 = new PdfPCell(new Paragraph(max + Math.round(stats.get("maximum")) + currency,valueFont));
    	cell31.setBorder(Rectangle.NO_BORDER);
    	
    	PdfPCell cell12 = new PdfPCell(new Paragraph(standDev + deviationValue ,valueFont));
    	cell12.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell22 = new PdfPCell(new Paragraph(eqPrice + Math.round(stats.get("eqPrice")) + currency,valueFont));
    	cell22.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell32 = new PdfPCell(new Paragraph(eqQuantity + Math.round(stats.get("eqQuantity")),valueFont));
    	cell32.setBorder(Rectangle.NO_BORDER);
    	
    	PdfPCell cell13 = new PdfPCell(new Paragraph(size + Math.round(stats.get("contractsSize")),valueFont));
    	cell13.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell23 = new PdfPCell(new Paragraph(hypBen + Math.round(stats.get("hypBen")) + currency,valueFont));
    	cell23.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell33 = new PdfPCell(new Paragraph(realBen + Math.round(stats.get("realBen")) + currency,valueFont));
    	cell33.setBorder(Rectangle.NO_BORDER);
    	
    	//dummy cell to complete the the third row
    	PdfPCell cell3 = new PdfPCell();
    	cell3.setBorder(Rectangle.NO_BORDER);

    	
    	table.addCell(cell11);
    	table.addCell(cell12);
    	table.addCell(cell13);
    	table.addCell(cell21);
    	table.addCell(cell22);
    	table.addCell(cell23);
    	table.addCell(cell31);
    	table.addCell(cell32);
    	table.addCell(cell33);
    	
    	doc.add(table);
    	
    	
    	//insert image of the chart
    	float chartWidth = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
    	float chartHeight = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin() - 120;

		pdfImage.scaleToFit(chartWidth, chartHeight);
		doc.add(pdfImage);
		
		return doc;
    }
}
