package jKMS;

import jKMS.cards.BuyerCard;
import jKMS.cards.Card;
import jKMS.cards.SellerCard;

import java.io.IOException;
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
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;


public class Pdf {
	
	 //define Fonts
	private static Font titleFont = new Font(Font.FontFamily.HELVETICA, 25,
		      Font.BOLD);
	private static Font valueFont = new Font(Font.FontFamily.HELVETICA, 18,
		      Font.NORMAL);
	
	private static Font packFont = new Font(Font.FontFamily.HELVETICA, 12,
		      Font.NORMAL);
	
	private String cardtitle;
	private String value;
	private String id;
	private String titlepage;
	private String packet;
	private String from;
	private String to;
	
	
	public Pdf(){// to catch crashes
		this.cardtitle = "Default";
		this.value = "Default";
		this.id = "Default";
	}

	public void createPdfCardsSeller(Document cardsSeller,Set<Card> cards,int assistancount,int firstID) throws DocumentException,IOException{ 
    	//Author: Justus (Timon with the good idea)
    	//----------------------DEFINATIONS-----------------------------------
    	//PRINT
		
		
		//get language
		//Properties propertie;
        //propertie = LogicHelper.getProperetie();
        
		cardtitle = LogicHelper.getLocalizedMessage("PDFSeller.cardtitle");
        value = LogicHelper.getLocalizedMessage("PDFSeller.value") + ": ";
		id = LogicHelper.getLocalizedMessage("PDF.id") + ": ";
		titlepage = LogicHelper.getLocalizedMessage("PDFSeller.titlepage");
		packet = LogicHelper.getLocalizedMessage("PDF.package");
		from = LogicHelper.getLocalizedMessage("PDF.from");
		to = LogicHelper.getLocalizedMessage("PDF.to");
		
    	//LOGIC
    	//at every paper are 2 cards --> 2 sets one for top one for bottom
        Set<Card> printcards = new LinkedHashSet<Card>(); // add first all cards + package idendifikationsites in one Set
        Set<Card> topcards = new LinkedHashSet<Card>();
        Set<Card> bottomcards = new LinkedHashSet<Card>();
        int[] packdis = LogicHelper.getPackageDistribution(cards.size(), assistancount);
        int packID = -1;
        int ide = firstID;
        int i = 0;
        
        //----------------------IMPLEMENTATION-------------------------------
        for(Card iter : cards){
        	
        	if(iter instanceof SellerCard){ //seller or buyer?
        		//seller
        			if(iter.getId() < ide){ //is there a new package ?
        			//no
        				printcards.add(new SellerCard(iter.getId(),iter.getValue(),iter.getPackage()));
        			}else{
        			//yes
        				packID++;
        				ide = ide + packdis[packID];
        				printcards.add(new SellerCard(-42,0,LogicHelper.IntToPackage(packID))); //add card for package idedifikation
        				printcards.add(new SellerCard(iter.getId(),iter.getValue(),iter.getPackage()));
        			}
        	}
        	
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
    	   //size of printcards is uneven --> topcards.size = bottomcards.size + 1 --> add withepage to bottomcards
           for(Card iter : printcards){
        	   if(i < (printcards.size()/2) + 1){
        		   topcards.add(iter);
        		   i++;
        	   }else{
        		   bottomcards.add(iter);
        		   i++;
        	    }   
           } 
           bottomcards.add(new SellerCard(-42,0,' '));// add wihtepage
       }
       
       //PRINT
        
        //MetaData
       
        cardsSeller.addTitle("Seller-Cards");
        cardsSeller.addAuthor("Kartoffelmarkspiel");
        cardsSeller.addCreator("KMS");
        
        Iterator<Card> itertop = topcards.iterator();
        Iterator<Card> iterbot = bottomcards.iterator();
        Card topcard = new SellerCard(0,0,' ');
        Card bottomcard = new SellerCard(0,0,' ');
        
        //Content      

    	cardsSeller.add(this.Titlepage(packdis, firstID, false));
    	cardsSeller.newPage();
        
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
        	bottomcell.addElement(this.createPackOnCard(bottomcard.getPackage()));//set Package bottom right on card
        	bottomcell.setBorder(Rectangle.NO_BORDER);
        
        	PdfPTable myTable = new PdfPTable(1);
        	myTable.setWidthPercentage(100.0f);
        	myTable.addCell(topcell);
        	myTable.addCell(bottomcell);
        
        
        	cardsSeller.add(myTable);
        	cardsSeller.newPage();
        } 
  
    }
    

    
	public void createPdfCardsBuyer(Document cardsBuyer,Set<Card> cards,int assistancount,int firstID) throws DocumentException,IOException{ 
    	//Author: Justus (Timon with the good idea)
    	//----------------------DEFINATIONS-----------------------------------
    	//PRINT
		
		//get language
		//Properties propertie;
        //propertie = LogicHelper.getProperetie();
        
        //get Strings
		cardtitle = LogicHelper.getLocalizedMessage("PDFBuyer.cardtitle");
        value = LogicHelper.getLocalizedMessage("PDFBuyer.value") + ": ";
		id = LogicHelper.getLocalizedMessage("PDF.id") + ": ";
		titlepage = LogicHelper.getLocalizedMessage("PDFBuyer.titlepage");
		packet = LogicHelper.getLocalizedMessage("PDF.package");
		from = LogicHelper.getLocalizedMessage("PDF.from");
		to = LogicHelper.getLocalizedMessage("PDF.to");
		
    	//LOGIC
    	//at every paper are 2 cards --> 2 sets one for top one for bottom
        Set<Card> printcards = new LinkedHashSet<Card>(); // add first all cards + package idendifikationsites in one Set
        Set<Card> topcards = new LinkedHashSet<Card>();
        Set<Card> bottomcards = new LinkedHashSet<Card>();
        int[] packdis = LogicHelper.getPackageDistribution(cards.size(), assistancount);
        int packID = -1;
        int ide = firstID;
        int i = 0;
        
        //----------------------IMPLEMENTATION-------------------------------
        for(Card iter : cards){
        	
        	if(iter instanceof BuyerCard){ //seller or buyer?
        		//buyer
        			if(iter.getId() < ide){ //is there a new package ?
        			//no
        				printcards.add(new BuyerCard(iter.getId(),iter.getValue(),iter.getPackage()));
        			}else{
        			//yes
        				packID++;
        				ide = ide + packdis[packID];
        				printcards.add(new BuyerCard(-42,0,LogicHelper.IntToPackage(packID))); //add card for package idedifikation
        				printcards.add(new BuyerCard(iter.getId(),iter.getValue(),iter.getPackage()));
        			}
        	}
        	
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
           bottomcards.add(new SellerCard(-42,0,' '));// add wihteside
       }
       
       //PRINT
        
        //MetaData
       
        cardsBuyer.addTitle("Buyer-Cards");
        cardsBuyer.addAuthor("Kartoffelmarkspiel");
        cardsBuyer.addCreator("KMS");
        
        Iterator<Card> itertop = topcards.iterator();
        Iterator<Card> iterbot = bottomcards.iterator();
        Card topcard = new BuyerCard(0,0,' ');
        Card bottomcard = new BuyerCard(0,0,' ');
        
        //Content       

    	cardsBuyer.add(this.Titlepage(packdis, firstID, true));
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
  
    }

	private Paragraph createPackOnCard(char pack){
		Paragraph cPa = new Paragraph(String.valueOf(pack),packFont);
    	cPa.setAlignment(Element.ALIGN_RIGHT);
    	return cPa;
	}
	
    private Paragraph createCard(Card card,boolean isTop){
    	
    	Paragraph content = new Paragraph();
        content.setAlignment(Element.ALIGN_CENTER);
        
    	if(isTop){
    		content.setSpacingBefore(140);
            content.setSpacingAfter(140);
    		if(card.getId() == -42 && card.getValue() == 0){
                content.setSpacingAfter(168);
    		}
    	}else{
            content.setSpacingBefore(140);
            content.setSpacingAfter(140);
    	}
    	
        if((card.getId() != -42) && (card.getValue() != 0)){
        	Chunk cTitle = new Chunk(cardtitle,titleFont);
        	content.add(cTitle);
        	content.add(Chunk.NEWLINE);
        	content.add(Chunk.NEWLINE);
        	Chunk cValue = new Chunk(this.value + card.getValue() +"€",valueFont);
        	content.add(cValue);
        	content.add(Chunk.NEWLINE);
        	Chunk cID = new Chunk(this.id + card.getId(),valueFont);
        	content.add(cID);
        }else{
        	Paragraph pack = new Paragraph(String.valueOf(card.getPackage()),titleFont);
        	pack.setAlignment(Element.ALIGN_CENTER);
        	content.add(pack);
        	card.setPackage(' ');
        }
        	
    	return content;
    }
   

    private Paragraph Titlepage(int[] packdis, int firstID, boolean isBuyer){
    	byte isbuyer = 0;
    	
    	//TODO wenn Buyercards immer ungerade dann code ändern !!!
    	//modulo rechnung zur kartentyp bestimmung ist abhängig von first ID
    	// erste Karte ist immer BuyerCard --> Wenn firstID gerade dann aller BuyerCard's gerade ansonsten ungerade
    	if(isBuyer && ((firstID % 2 == 1))){
    		isbuyer = 1; // Buyercards ungerade
    	}
    	if(isBuyer && ((firstID % 2 == 0))){
    		isbuyer = 0;
    	}
    	if(!isBuyer && ((firstID % 2 == 1))){
    		isbuyer = 0;
    	}
    	if(!isBuyer && ((firstID % 2 == 0))){
    		isbuyer = 1; // sellercards ungerade
    	}
    	
    	Paragraph titlep = new Paragraph();
    	int id = firstID;
    	Chunk packa;
    	
    	titlep.setAlignment(Element.ALIGN_CENTER);
    	titlep.setFont(titleFont);
    	titlep.add(this.titlepage);
    	titlep.add(Chunk.NEWLINE);
    	titlep.add(Chunk.NEWLINE);
    	titlep.setFont(valueFont);

    	for(int i=0; i < packdis.length;i++){
    			if((id % 2)== isbuyer){
    				if(((id + packdis[i] - 1) % 2)== isbuyer){// anfangs/endid von paket gerade wenn isBuyer = false (Seller) anfangs/endid ungerade wenn isBuyer = true (Buyer)
    					packa = new Chunk(packet + " " + String.valueOf(LogicHelper.IntToPackage(i)) + " "+ from + " " + id + " "+ to + " " + (id + packdis[i]-1));
    			    	titlep.add(Chunk.NEWLINE);
    				}else{//anfangsID entspricht ist "richtig" end id ist falsch
    					packa = new Chunk(packet + " "+ String.valueOf(LogicHelper.IntToPackage(i)) + " "+ from +" " + id +  " "+ to + " " + (id +packdis[i]-2));
    			    	titlep.add(Chunk.NEWLINE);
    				}
    			}else{
    				if(((id + packdis[i] - 1) % 2)== isbuyer){// Anfangsid passt nicht endid passt
    					packa = new Chunk(packet + " "+ String.valueOf(LogicHelper.IntToPackage(i)) + " "+ from +" " + (id+1) +  " "+ to + " " + (id + packdis[i]-1));
    			    	titlep.add(Chunk.NEWLINE);
    				}else{//Anfangs und end ID vom Paket passen nicht
    					packa = new Chunk(packet + " "+ String.valueOf(LogicHelper.IntToPackage(i)) + " "+ from +" " + (id+1) +  " "+ to + " " + (id +packdis[i]-2));
    			    	titlep.add(Chunk.NEWLINE);
    				}
    			}
    			titlep.add(packa);
    			id = id + packdis[i];
    	}
    	return titlep;
    }
    
    public Document createExportPdf(Document doc, Image pdfImage, Map<String, Float> stats) throws DocumentException{
    	//get language
		//Properties property;
        //property = LogicHelper.getProperetie();
        
        //get Strings
        String headline = LogicHelper.getLocalizedMessage("evaluate.headline");
		String average = LogicHelper.getLocalizedMessage("evaluate.average") + ": ";
        String min = LogicHelper.getLocalizedMessage("evaluate.min") + ": ";
		String max = LogicHelper.getLocalizedMessage("evaluate.max") + ": ";
		String variance = LogicHelper.getLocalizedMessage("evaluate.variance") + ": ";
		String standDev = LogicHelper.getLocalizedMessage("evaluate.standardDeviation") + ": ";
		String eqPrice = LogicHelper.getLocalizedMessage("evaluate.eqPrice") + ": ";
		String eqQuantity = LogicHelper.getLocalizedMessage("evaluate.eqQuantity") + ": ";
		
    	//insert stats
		Font font = new Font(Font.FontFamily.HELVETICA  , 25, Font.BOLD);
		Paragraph head = new Paragraph(headline, font);
    	head.setAlignment(Element.ALIGN_CENTER);
    	head.setSpacingAfter(20);
    	
		doc.add(head);
    	
    	PdfPTable table = new PdfPTable(3);
    	
    	PdfPCell cell11 = new PdfPCell(new Paragraph(average + stats.get("averagePrice") + "€"));
    	cell11.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell21 = new PdfPCell(new Paragraph(min + stats.get("minimum") + "€"));
    	cell21.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell31 = new PdfPCell(new Paragraph(max + stats.get("maximum") + "€"));
    	cell31.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell12 = new PdfPCell(new Paragraph(variance + stats.get("variance")));
    	cell12.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell22 = new PdfPCell(new Paragraph(standDev + stats.get("standardDeviation")));
    	cell22.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell13 = new PdfPCell(new Paragraph(eqPrice + stats.get("eqPrice") + "€"));
    	cell13.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell23 = new PdfPCell(new Paragraph(eqQuantity + stats.get("eqQuantity")));
    	cell23.setBorder(Rectangle.NO_BORDER);
    	
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
    	table.addCell(cell3);
    	table.addCell(cell3);
    	
    	doc.add(table);
    	
    	
    	//insert image of the chart
    	float chartWidth = doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin();
    	float chartHeight = doc.getPageSize().getHeight() - doc.topMargin() - doc.bottomMargin() - 120;

		pdfImage.scaleToFit(chartWidth, chartHeight);
		doc.add(pdfImage);
		
		return doc;
    }
}
