package jKMS.states;

import jKMS.Amount;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.Pdf;
import jKMS.cards.BuyerCard;
import jKMS.cards.Card;
import jKMS.cards.SellerCard;
import jKMS.exceptionHelper.EmptyFileException;
import jKMS.exceptionHelper.FalseLoadFileException;
import jKMS.exceptionHelper.WrongAssistantCountException;
import jKMS.exceptionHelper.WrongFirstIDException;
import jKMS.exceptionHelper.WrongPlayerCountException;
import jKMS.exceptionHelper.WrongRelativeDistributionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

public class Preparation extends State	{

	private Pdf pdf;

	public Preparation(Kartoffelmarktspiel kms){
		this.kms = kms;
		this.pdf = new Pdf();
	}
	
	/**
	 * Loads StandardConfiguration into kms.
	 * This method only loads the relative values for displaying in Web Interface.
	 * Absolute Values are calculated by Javascript and stored using the newGroup-Method.
	 * @see jKMS.states.State#loadStandardDistribution()
	 */
	@Override
	public void loadStandardDistribution(){

		// Load Buyer Distribution
		Map<Integer, Amount> bDistribution = new TreeMap<>();
		bDistribution.put(70, new Amount(20, 0));
		bDistribution.put(65, new Amount(16, 0));
		bDistribution.put(60, new Amount(16, 0));
		bDistribution.put(55, new Amount(16, 0));
		bDistribution.put(50, new Amount(16, 0));
		bDistribution.put(45, new Amount(16, 0));
		kms.getConfiguration().setbDistribution(bDistribution);
		
		// Load Seller Distribution
		Map<Integer, Amount> sDistribution = new TreeMap<>();
		sDistribution.put(63, new Amount(10, 0));
		sDistribution.put(58, new Amount(18, 0));
		sDistribution.put(53, new Amount(18, 0));
		sDistribution.put(48, new Amount(18, 0));
		sDistribution.put(43, new Amount(18, 0));
		sDistribution.put(38, new Amount(18, 0));
		kms.getConfiguration().setsDistribution(sDistribution);
		
		LogicHelper.print("Loaded Standard Distribution.");
		
	}
	
	/**
	 * setter method for the number of players and assistants
	 */
	@Override
	public void setBasicConfig(int playerCount, int assistantCount){
		kms.getConfiguration().setPlayerCount(playerCount);
		kms.getConfiguration().setAssistantCount(assistantCount);
		LogicHelper.print("SettingBasicConfiguration: PlayerCount = " + kms.getConfiguration().getPlayerCount() + "; AssistantCount = " + kms.getConfiguration().getAssistantCount());
	}
	
	@Override
	public void load(MultipartFile file) throws NumberFormatException, IOException, EmptyFileException, FalseLoadFileException{
	//set initial value for load
		LogicHelper.print("set initial value for load");
    	int firstID=0;
    	//Set<Card> cardSet = new LinkedHashSet<Card>();
    	Map<Integer, Amount> bDistributionLoad = new TreeMap<>();
		Map<Integer, Amount> sDistributionLoad = new TreeMap<>();
		//deal with PlayerCount,AssistantCount,GroupCount and fistID
    	 if (!file.isEmpty()) {
            	 BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            	 String buf = "";
            	 int count = 0;
            	 while ((buf=br.readLine()) != null && count < 3) {
            		 buf=buf.trim();
            		 String[] sa = buf.split(":|\\s");
            		 //in State Preparation did not load PlayerCount
            		 if(count == 0){
            			 count = count + 1;
            			 continue;
            		 }
            		 //in State Preparation did not load AssistantCount
            		 else if(count == 1){
            			 count = count + 1;
            			 continue;
            		 }
            		 else if(count == 2){
            			 firstID = Integer.valueOf(sa[1].trim());
            			 if(firstID != 1001){
            				 throw new FalseLoadFileException("firstID is not 1001,please do not change the load file!");
            			 }
            			 count = count + 1;
            			 break;
            		 }
            	 }
            	 LogicHelper.print("Loaded: firstID = " + firstID);
            	 //load bDistribution and sDistribution
            	 while ( count >=3){
            		 if( (buf=br.readLine()) != null){
	            		 buf=buf.trim();
	 		             String[] sa = buf.split(":|\\s");
	 		             if(sa[0].equals("bDistribution")){
		 		             int bpreis = Integer.valueOf(sa[1].trim());
		 		             Amount bAmount =  new Amount(Integer.valueOf(sa[2].trim()),Integer.valueOf(sa[3].trim()));
		 		             // int banteil = Integer.valueOf(sa[1]);
		 		             bDistributionLoad.put(bpreis, bAmount);
	 		             }
	 		             else if(sa[0].equals("sDistribution")){
		 		             int spreis = Integer.valueOf(sa[1].trim());
		 		             Amount sAmount = new Amount(Integer.valueOf(sa[2].trim()),Integer.valueOf(sa[3].trim()));
		 		             //int santeil = Integer.valueOf(sa[3]);
		 		             sDistributionLoad.put(spreis, sAmount);
	 		             }else{
	 		            	 // Normally time to load the cards but were in Preparation so we dont need to
	 		            	 break;
	 		             }
	 		         count = count + 1;
            		 }else {
            			 break;
            		 }
            	 }
            	 LogicHelper.print("bDistribution: " + bDistributionLoad.toString());
    			 LogicHelper.print("sDistribution: " + sDistributionLoad.toString());

    			 //set load information in Configuration
    	    	 kms.getConfiguration().setFirstID(firstID);
    	    	 kms.getConfiguration().setbDistribution(bDistributionLoad);
    			 kms.getConfiguration().setsDistribution(sDistributionLoad);
//    			 kms.setCards(cardSet);
    			 
         }else 
             throw new EmptyFileException("load file can not be empty, please do not delete loadfile!");
    	 
    	 LogicHelper.print("load() successful!");
    	 
    }

	@Override
	public boolean save(OutputStream o) throws IOException{
		//take out information aus Configuration and kms
  	 	 LogicHelper.print("take out information from configuration and kms");
		 Map<Integer, Amount> bDistributionSave = new TreeMap<>();
		 Map<Integer, Amount> sDistributionSave = new TreeMap<>();
		 bDistributionSave = kms.getConfiguration().getbDistribution();
		 sDistributionSave = kms.getConfiguration().getsDistribution();
		 //create outputformat for the outputstream 
		 if(bDistributionSave.isEmpty() || sDistributionSave.isEmpty())
			 return false;
		 else{
			 
				   String line = System.getProperty("line.separator");
				   StringBuffer str = new StringBuffer();
				// FileWriter fw = new FileWriter(path, false);
				   str.append("PlayerCount:").append(kms.getConfiguration().getPlayerCount()).append(line)
				   .append("AssistantCount:").append(kms.getConfiguration().getAssistantCount()).append(line)
//				   .append("GroupCount:").append(kms.getConfiguration().getGroupCount()).append(line)
				   .append("FirstID:").append(kms.getConfiguration().getFirstID()).append(line);
				   
				   // Save buyer
				   Set<Entry<Integer, Amount>> bSet = bDistributionSave.entrySet();
				   Iterator<Entry<Integer, Amount>> bIter = bSet.iterator();
				   while(bIter.hasNext()){
					   Map.Entry bEntry = (Map.Entry)bIter.next(); 
				    
					   str.append("bDistribution:"+bEntry.getKey()+":"+((Amount) bEntry.getValue()).getRelative()+":"+((Amount) bEntry.getValue()).getAbsolute()).append(line);
				   }
				   
				   // Save seller
				   Set<Entry<Integer, Amount>> sSet = sDistributionSave.entrySet();
				   Iterator<Entry<Integer, Amount>> sIter = sSet.iterator();
				   while(sIter.hasNext()){ 
					   Map.Entry sEntry = (Map.Entry)sIter.next(); 
				    
					   str.append("sDistribution:"+sEntry.getKey()+":"+((Amount)sEntry.getValue()).getRelative()+":"+((Amount)sEntry.getValue()).getAbsolute()).append(line);
				   }
				   
				   Set<Card> cardSet = kms.getCards();
				   Iterator<Card> cardIter = cardSet.iterator();
				   while(cardIter.hasNext()){
					   Card card = (Card) cardIter.next();
					   str.append("Card:"+card.getId()+":"+card.getValue()+":"+card.getPackage()).append(line);
				   }
				   LogicHelper.print("Create outputstreamformat successful.");
				   o.write(str.toString().getBytes());
				   //write information to file
//				   if(o instanceof FileOutputStream){
//					   FileOutputStream fo = (FileOutputStream)o;
//					   fo.write(str.toString().getBytes());
//					   fo.close();
//				   }
//				   else if(o instanceof ByteArrayOutputStream){
//					   ByteArrayOutputStream bo = (ByteArrayOutputStream)o;
//					   bo.write(str.toString().getBytes());
//					   bo.close();
//				   }
//				   else{
//					   return false;
//				   }
				 //fw.write(str.toString());
				 //fw.close();
				   LogicHelper.print("save() successful!");
			       return true;
		 }
	}

	/**
	 *  Generate an ordered, random Set of Cards
	 *  using bDistribution and sDistribution
	 * @see jKMS.states.State#generateCards()
	 */
	@Override
	public void generateCards() throws WrongRelativeDistributionException, WrongAssistantCountException, WrongFirstIDException, WrongPlayerCountException {
		// DECLARATION
		
		//for put seller and buyer distribution
		Random random = new Random();
		
		List<Integer> bKeys = new ArrayList<Integer>(kms.getConfiguration().getbDistribution().keySet());
		List<Integer> sKeys = new ArrayList<Integer>(kms.getConfiguration().getsDistribution().keySet());
		Map<Integer, Amount> bTemp = new TreeMap<Integer, Amount>(kms.getConfiguration().getbDistribution());
		Map<Integer, Amount> sTemp = new TreeMap<Integer, Amount>(kms.getConfiguration().getsDistribution());
		
		int randomKey, randomListEntry;
		
		//for put packages
		int packid, id, packsize;
		int[] packdistribution =LogicHelper.getPackageDistribution(kms.getPlayerCount(),kms.getAssistantCount());
		
		//clear Card Set
		kms.getCards().clear();

		// IMPLEMENTATION
		
		//test is there a conform configuration?
		if(kms.getPlayerCount() != (LogicHelper.getAbsoluteSum(bTemp) +  LogicHelper.getAbsoluteSum(sTemp)))throw new WrongPlayerCountException();
		if(kms.getAssistantCount() <= 0)throw new WrongAssistantCountException();
		if(kms.getConfiguration().getFirstID() < 0)throw new WrongFirstIDException();
		if((LogicHelper.getRelativeSum(bTemp) +  LogicHelper.getRelativeSum(sTemp)) != 200) throw new WrongRelativeDistributionException(); // muss in der summe 200 ergeben, da jede distribution in sich 100 ergibt
		
		for(int i = 0; i < kms.getAssistantCount(); i++)	{
			kms.getConfiguration().newPackage(LogicHelper.IntToPackage(i));
		}
		

		packid =0;//package index 0 for Pack A, 1 for Pack B ...	
		//amount of cards in the first pack
		// 0 cards of package A
		packsize = 0;
		id  = kms.getConfiguration().getFirstID();
		
		//put seller and buyer distribution and put packages		
		while ((bTemp.isEmpty() != true) || (sTemp.isEmpty() != true)) {
			
			// Create Buyer Card
			if((id % 2) == 1 && bKeys.size() > 0){ // TODO TEST !!!
				//all buyercards have an uneven id
				randomListEntry = random.nextInt(bKeys.size());
				randomKey = bKeys.get(randomListEntry);
			
			   //card is in the pack
				if(packsize < packdistribution[packid]){
					kms.getCards().add(new BuyerCard(id, randomKey, kms.getPackage(LogicHelper.IntToPackage(packid))));
					packsize++;
				}else {
					packid++; //get amount of cards from the next packaga
					kms.getCards().add(new BuyerCard(id, randomKey, kms.getPackage(LogicHelper.IntToPackage(packid))));
					packsize = 1; // reset packsize first card is in! --> 1
				}
		
				bTemp.put(randomKey, new Amount(bTemp.get(randomKey).getRelative(), bTemp.get(randomKey).getAbsolute() - 1)); 
				if (bTemp.get(randomKey).getAbsolute() == 0) {
					bTemp.remove(randomKey);
					bKeys.remove(randomListEntry);
				}

			 
			}
			if((id % 2) == 0 && sKeys.size() > 0){

				// Create Seller Card
				randomListEntry = random.nextInt(sKeys.size());
				randomKey = sKeys.get(randomListEntry);

				if(packsize < packdistribution[packid]){
					kms.getCards().add(new SellerCard(id, randomKey, kms.getPackage(LogicHelper.IntToPackage(packid))));
					packsize++;
				}else {
				//a new package start
						packid++;
						kms.getCards().add(new SellerCard(id, randomKey, kms.getPackage(LogicHelper.IntToPackage(packid))));
						packsize = 1;
				}

				sTemp.put(randomKey, new Amount(sTemp.get(randomKey).getRelative(), sTemp.get(randomKey).getAbsolute() - 1));
				if (sTemp.get(randomKey).getAbsolute() == 0) {
					sTemp.remove(randomKey);
					sKeys.remove(randomListEntry);
				}

		  }
			
		  id++;
		  
		}
	}			

	/**
	 * Creates a new entry for the bDistribution or sDistribution Map
	 * depending if isBuyer is true or false.
	 * 
	 * @param isBuyer indicates if a new entry must be created for bDistribution or sDistribution
	 */
	@Override
	public void newGroup(boolean isBuyer, int price, int relativeNumber, int absoluteNumber) {
		Map<Integer, Amount> distrib;
		
		if (isBuyer) distrib = kms.getConfiguration().getbDistribution();
		else distrib = kms.getConfiguration().getsDistribution();
		
		if(!distrib.containsKey(price))
			distrib.put(price,  new Amount(relativeNumber, absoluteNumber));
		else	{ 
			distrib.get(price).setAbsolute(distrib.get(price).getAbsolute() + absoluteNumber);
			distrib.get(price).setRelative(distrib.get(price).getRelative() + relativeNumber);
		}
		
		Amount registered = distrib.get(price);
		if(isBuyer)	{
			LogicHelper.print("Registered Group:  Buyer | " + price + "€ | " + registered.getRelative() + "% | " + registered.getAbsolute());
		}	else	{
			LogicHelper.print("Registered Group: Seller | " + price + "€ | " + registered.getRelative() + "% | " + registered.getAbsolute());
		}
		
	}
	
	@Override
	public void createPdf(boolean isBuyer, Document doc) throws DocumentException,IOException	{
		
		if(isBuyer)	{
			pdf.createPdfCardsBuyer(doc, kms.getCards(), kms.getAssistantCount(), kms.getConfiguration().getFirstID());
		}	else	{
			pdf.createPdfCardsSeller(doc, kms.getCards(), kms.getAssistantCount(), kms.getConfiguration().getFirstID());
		}
		
		LogicHelper.print("PDF Created!");
	}
}
