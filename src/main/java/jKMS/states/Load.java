package jKMS.states;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.web.multipart.MultipartFile;

import jKMS.Amount;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.Package;
import jKMS.cards.BuyerCard;
import jKMS.cards.Card;
import jKMS.cards.SellerCard;
import jKMS.exceptionHelper.EmptyFileException;
import jKMS.exceptionHelper.FalseLoadFileException;
import jKMS.exceptionHelper.WrongAssistantCountException;
import jKMS.exceptionHelper.WrongFirstIDException;
import jKMS.exceptionHelper.WrongPackageException;
import jKMS.exceptionHelper.WrongPlayerCountException;
import jKMS.exceptionHelper.WrongRelativeDistributionException;

public class Load extends State {
	
	public Load(Kartoffelmarktspiel kms){
		this.kms = kms;
	}

	@Override
	public void load(MultipartFile file) throws NumberFormatException, IOException, EmptyFileException, FalseLoadFileException{
		//set initial value for load
		LogicHelper.print("set initial value for load");
    	int playerCount=0;
    	int assistantCount=0;
    	int firstID=0;
    	Set<Card> cardSet = new LinkedHashSet<Card>();
    	Map<Integer, Amount> bDistributionLoad = new TreeMap<>();
		Map<Integer, Amount> sDistributionLoad = new TreeMap<>();
		//deal with PlayerCount,AssistantCount,GroupCount and fistID
    	 if (!file.isEmpty()) {
            	 BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            	 String buf = "";
            	 int count = 0;
            	 try{
	            	 while ((buf=br.readLine()) != null && count < 3) {
	            		 buf=buf.trim();
	            		 String[] sa = buf.split(":|\\s");
	            		 if(count == 0){
	            			 playerCount = Integer.valueOf(sa[1].trim());
	            			 count = count + 1;
	            			 continue;
	            		 }
	            		 else if(count == 1){
	            			 assistantCount = Integer.valueOf(sa[1].trim());
	            			 count = count + 1;
	            			 continue;
	            		 }
	            		 else if(count == 2){
	            			 firstID = Integer.valueOf(sa[1].trim());
	            			// if(firstID != 1001){ //TODO Update this so firstID can be set in options
	            			//	 throw new FalseLoadFileException("firsstID is not 1001,please do not change the load file!");
	            			// }
	            			 count = count + 1;
	            			 break;
	            		 }
	            	 }
	            	 LogicHelper.print("Loaded: PlayerCount = " + playerCount + 
	            			 "AssistantCount = " + assistantCount + 
	            			 "firstID = " + firstID);
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
		 		            	 // Time to load the cards
		 		            	 break;
		 		             }
		 		         count = count + 1;
	            		 }else {
	            			 throw new FalseLoadFileException("No Cards found, please do not change the load file!");
	            		 }
	            	 }
	            	 //Package pack = new Package(sa[3].trim().charAt(0));
	            	 LogicHelper.print("Loaded: bDistribution = " + bDistributionLoad.toString());
	    			 LogicHelper.print("Loaded: sDistribution = " + sDistributionLoad.toString());
	            	 //load Cards and set them in cardSet
	            	 while (buf != null){
	            		 Card card;
	            		 buf=buf.trim();
	            		 String[] sa = buf.split(":|\\s");
	            		 // Get package with actual char
	            		 Package pack = kms.getConfiguration().getPackage(sa[3].trim().charAt(0));
	            		 // Check if existing
	            		 if(pack == null)	{
	                    	 pack = kms.getConfiguration().newPackage(sa[3].trim().charAt(0));
	            		 }            		
	            		 if((Integer.valueOf(sa[1])%2) == 0){
	            			card = new SellerCard(Integer.valueOf(sa[1].trim()),Integer.valueOf(sa[2].trim()),pack);
	            		 }else {
	            			card = new BuyerCard(Integer.valueOf(sa[1].trim()),Integer.valueOf(sa[2].trim()),pack);
	            		 }
	            		 cardSet.add(card);
	               		 buf=br.readLine();
	            	 }
	            	 
            	 }catch(ArrayIndexOutOfBoundsException e){
            		 throw new FalseLoadFileException("there is a syntax error in load file,someting is missing,please check the load file!");
            	 }
            	 
            	 if(cardSet.size() != playerCount){
            		 throw new FalseLoadFileException("playerCount is not equal to the number of card,please do not change the load file!");
            	 }
            	 Set<Integer> cardNumber = new TreeSet<Integer>();
            	 Iterator<Card> citer = cardSet.iterator();
            	 while(citer.hasNext()){
            		 Card card = citer.next();
            		 cardNumber.add(card.getId());
            	 }
            	 if(Collections.min(cardNumber) != kms.getConfiguration().getFirstID()){
            		 throw new FalseLoadFileException("cardId should beginn at 1001 and can not more than playerCount+1000!");
            	 }
            	 if(cardNumber.size() != playerCount){
            		 throw new FalseLoadFileException("card can not more than once in cardSet!");
            	 }
            	 LogicHelper.print("Loaded CardsSet: Size = " + cardSet.size());
    			 
            	//set load information in Configuration
            	 kms.getConfiguration().setPlayerCount(playerCount);
    	    	 kms.getConfiguration().setAssistantCount(assistantCount);
    	    	 kms.getConfiguration().setFirstID(firstID);
    	    	 kms.getConfiguration().setbDistribution(bDistributionLoad);
    			 kms.getConfiguration().setsDistribution(sDistributionLoad);
    			 kms.setCards(cardSet);
    			 
         }else 
             throw new EmptyFileException("load file can not be empty, please do not delete loadfile!");
    	 LogicHelper.print("load() successful!");
    }

	/**
	 * Removes all cards from the given package
	 * beginning with lastId up to its size
	 * 
	 * @param pack the id of the package (A-Z)
	 * @param lastId the id of the first not distributed card
	 * */
	@Override
	public boolean removeCard(char pack, int lastId) throws WrongPackageException, WrongPlayerCountException, WrongAssistantCountException, WrongFirstIDException, WrongRelativeDistributionException{
		
		//test is there a conform configuration?
		if(kms.getPlayerCount() != (LogicHelper.getAbsoluteSum(kms.getbDistribution()) +  LogicHelper.getAbsoluteSum(kms.getsDistribution())))throw new WrongPlayerCountException();
		if(kms.getAssistantCount() <= 0)throw new WrongAssistantCountException();
		if(kms.getConfiguration().getFirstID() < 0)throw new WrongFirstIDException();
		if((LogicHelper.getRelativeSum(kms.getbDistribution()) +  LogicHelper.getRelativeSum(kms.getsDistribution())) != 200) throw new WrongRelativeDistributionException();
		for(Card iter : kms.getCards()){
			if(iter.getId() == lastId && iter.getPackage().getName() != pack) throw new WrongPackageException();
		}
		
		//TODO Check if card with lastID is existing
		
		return kms.getConfiguration().getPackage(pack).removeCards(lastId);
	}
	
}
