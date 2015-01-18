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
        int packID = 0;
        int packsize = 0;
        int i = 0;
        
        
        //----------------------IMPLEMENTATION-------------------------------
        //add titlecard for package A 
        printcards.add(new SellerCard(-42,0, new Package('A', null))); 
        
        for(Card iter : cards){
			if(packsize == packdis[packID] ){ //is there a new package ?
    			//yes
				packID++;
				packsize = 0;
				printcards.add(new SellerCard(-42,0,iter.getPackage())); //add card for package idedifikation
        	}
			
			if(iter instanceof SellerCard){ //seller or buyer?
        		//seller
        		printcards.add(new SellerCard(iter.getId(),iter.getValue(),iter.getPackage()));
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
           bottomcards.add(new SellerCard(-42,0,new Package(' ', null)));// add wihtepage
       }
       
       //PRINT
        
        //MetaData
       
        cardsSeller.addTitle("Seller-Cards");
        cardsSeller.addAuthor("Kartoffelmarkspiel");
        cardsSeller.addCreator("KMS");
        
        Iterator<Card> itertop = topcards.iterator();
        Iterator<Card> iterbot = bottomcards.iterator();
        Card topcard = new SellerCard(0,0,new Package(' ', null));
        Card bottomcard = new SellerCard(0,0,new Package(' ', null));
        
        //Content      

    	cardsSeller.add(this.Titlepage(packdis, firstID, false, cards));
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
        int packID = 0;
        int packsize = 0;
        int i = 0;
        
        //----------------------IMPLEMENTATION-------------------------------
       
        //add card for package idedifikation (first package) 
        printcards.add(new BuyerCard(-42,0,new Package('A', null))); 
        
        for(Card iter : cards){
			if(packsize == packdis[packID]){ //is there a new package ? before printing first card of new pack packagepage
            			//yes
        				packID++;
        				packsize = 0;
        				printcards.add(new BuyerCard(-42,0,iter.getPackage())); //add card for package idedifikation
        				//printcards.add(new BuyerCard(iter.getId(),iter.getValue(),iter.getPackage()));
        			}

    		if(iter instanceof BuyerCard){ //seller or buyer?
				printcards.add(new BuyerCard(iter.getId(),iter.getValue(),iter.getPackage()));
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
           bottomcards.add(new SellerCard(-42,0,new Package(' ', null)));// add wihteside
       }
       
       //PRINT
        
        //MetaData
       
        cardsBuyer.addTitle("Buyer-Cards");
        cardsBuyer.addAuthor("Kartoffelmarkspiel");
        cardsBuyer.addCreator("KMS");
        
        Iterator<Card> itertop = topcards.iterator();
        Iterator<Card> iterbot = bottomcards.iterator();
        Card topcard = new BuyerCard(0,0,new Package(' ', null));
        Card bottomcard = new BuyerCard(0,0,new Package(' ', null));
        
        //Content       

    	cardsBuyer.add(this.Titlepage(packdis, firstID, true, cards));
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

	private Paragraph createPackOnCard(Package pack){
		Paragraph cPa = new Paragraph(String.valueOf(pack),packFont);
    	cPa.setAlignment(Element.ALIGN_RIGHT);
    	cPa.setSpacingAfter(20);
    	return cPa;
	}
	
    private Paragraph createCard(Card card,boolean isTop){
    	
    	Paragraph content = new Paragraph();
        content.setAlignment(Element.ALIGN_CENTER);
        
    	if(isTop){
    		content.setSpacingBefore(140);
            content.setSpacingAfter(121);
    		if(card.getId() == -42 && card.getValue() == 0){
                content.setSpacingAfter(146);
    		}
    	}else{
            content.setSpacingBefore(140);
            content.setSpacingAfter(121);
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
        	card.setPackage(null);
        }
        	
    	return content;
    }
   

    private Paragraph Titlepage(int[] packdis, int firstID, boolean isBuyer, Set<Card> cards){ 
    	byte isbuyer = 0;
    	
    	
    	if(isBuyer){
    		isbuyer = 1; // Buyercards uneven
    	}
    	
    	/*
    	if(isBuyer && ((firstID % 2 == 0))){
    		isbuyer = 0;
    	}*/
    	
    	if(!isBuyer){
    		isbuyer = 0; // Sellercards even
    	}
    	/*
    	if(!isBuyer && ((firstID % 2 == 0))){
    		isbuyer = 1; // sellercards ungerade
    	}*/
    	
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
      	
    	int packid =0;// in welchem paket befinden wir uns 0 = A ...
    	int packsize = 0;
    	Integer idstart = 0;
    	Integer idend = 0;
    	//befindet man sich am anfang eines Paketes --> true
    	boolean packstart = true;
    	int durchlauf = 0;
    	int durchlauf2 =0;
    	int i;
    	boolean found = false;
    	//first packagename 
		//add Package
		content = new Chunk(String.valueOf(LogicHelper.IntToPackage(packid)),valueFont);
    	allcontent1.add(content);
    	allcontent1.add(Chunk.NEWLINE);
    	
    	//sexistische Kackscheiße !!!
		for(Card iter : cards){
			durchlauf++;
			//istpacketgröße
			packsize++;

			//Anfangsid paket herausfinden
			if(packstart){
				//passt die start id? wenn is buyer true --> ungerade 
				// wenn nicht nimm die id im nächsten durchlauf
				if((iter.getId() % 2) == isbuyer){
					idstart = iter.getId();
					packstart = false;
			      	//add startid to package
					content = new Chunk(idstart.toString(),valueFont);
			    	allcontent2.add(content);
			    	allcontent2.add(Chunk.NEWLINE);
				}
			}
			//ende des Packetes
			if(packsize == packdis[packid]){
				//passt die endid?
				if((iter.getId() % 2) == isbuyer){
					//ja
					idend = iter.getId();
				}else{
					//nein --> gehe das kartenset nocheinmal durch bis zur letzten Karte
					//mit passender ID
					i =1;
					found = false;
					while(!found){
					 durchlauf2 = 0;
						for(Card iter2 : cards){
							durchlauf2++;
							if(durchlauf2 == (durchlauf-i)){
								if((iter2.getId() % 2) == isbuyer){
									idend = iter2.getId();
									found = true;
									break;
								}else{
									i++;
									break;
								}
							}

						}
					}
				}
		      	//add end id to table
		      	content = new Chunk(idend.toString(),valueFont);
		    	allcontent3.add(content);
		    	allcontent3.add(Chunk.NEWLINE);
				//betrachte nächstes Paket
		    	//fang am ende kein neues paket an!
	    		packid++;
		    	if(packid < packdis.length){
		    		//add Package to table
		    		content = new Chunk(String.valueOf(LogicHelper.IntToPackage(packid)),valueFont);
		    		allcontent1.add(content);
		    		allcontent1.add(Chunk.NEWLINE);
		    		//paketgröße zählt von 0 bis zu tatsächlichen größe
		    		packsize = 0;
		    		//als nächste muss die anfangsid "gedruckt" werden
		    		packstart = true;
		    	}
			}	

    	}
    	
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
    
    public Document createExportPdf(Document doc, Image pdfImage, Map<String, Float> stats) throws DocumentException{
    	//get language
		//Properties property;
        //property = LogicHelper.getProperetie();
        
        //get Strings
        String headline = LogicHelper.getLocalizedMessage("evaluate.headline");
		String average = LogicHelper.getLocalizedMessage("evaluate.average") + ": ";
		String size = LogicHelper.getLocalizedMessage("evaluate.size") + ": ";
        String min = LogicHelper.getLocalizedMessage("evaluate.min") + ": ";
		String max = LogicHelper.getLocalizedMessage("evaluate.max") + ": ";
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
    	
    	PdfPCell cell11 = new PdfPCell(new Paragraph(average + Math.round(stats.get("averagePrice")*100)/100.0 + "€"));
    	cell11.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell21 = new PdfPCell(new Paragraph(min + Math.round(stats.get("minimum")) + "€"));
    	cell21.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell31 = new PdfPCell(new Paragraph(max + Math.round(stats.get("maximum")) + "€"));
    	cell31.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell12 = new PdfPCell(new Paragraph(standDev + Math.round(stats.get("standardDeviation")*100)/100.0));
    	cell12.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell22 = new PdfPCell(new Paragraph(size + Math.round(stats.get("contractsSize"))));
    	cell22.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell13 = new PdfPCell(new Paragraph(eqPrice + Math.round(stats.get("eqPrice")) + "€"));
    	cell13.setBorder(Rectangle.NO_BORDER);
    	PdfPCell cell23 = new PdfPCell(new Paragraph(eqQuantity + Math.round(stats.get("eqQuantity"))));
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
