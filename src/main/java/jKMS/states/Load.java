package jKMS.states;

import jKMS.Amount;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.cards.BuyerCard;
import jKMS.cards.Card;
import jKMS.cards.SellerCard;
import jKMS.exceptionHelper.EmptyFileException;
import jKMS.exceptionHelper.WrongAssistantCountException;
import jKMS.exceptionHelper.WrongFirstIDException;
import jKMS.exceptionHelper.WrongPlayerCountException;
import jKMS.exceptionHelper.WrongRelativeDistributionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.web.multipart.MultipartFile;

public class Load extends State {
	
	public Load(Kartoffelmarktspiel kms){
		this.kms = kms;
	}

	public void load(MultipartFile file) throws NumberFormatException, IOException, EmptyFileException{
    	int playerCount=0;
    	int assistantCount=0;
    	int groupCount=0;
    	int firstID=0;
    	Set<Card> cardSet = new LinkedHashSet<Card>();
    	Map<Integer, Amount> bDistributionLoad = new TreeMap<>();
		Map<Integer, Amount> sDistributionLoad = new TreeMap<>();
    	 if (!file.isEmpty()) {
            	 BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
            	 String buf = "";
            	 int count = 0;
            	 while ((buf=br.readLine()) != null && count < 4) {
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
            			 groupCount = Integer.valueOf(sa[1].trim());
            			 count = count + 1;
            			 continue;
            		 }
            		 else if(count == 3){
            			 firstID = Integer.valueOf(sa[1].trim());
            			 count = count + 1;
            			 break;
            		 }
            	 }
            	 while ( count >=4 && count < groupCount+4){
            		 if( (buf=br.readLine()) != null){
	            		 buf=buf.trim();
	 		             String[] sa = buf.split(":|\\s");
	 		             int bpreis = Integer.valueOf(sa[1].trim());
	 		             Amount bAmount =  new Amount(Integer.valueOf(sa[2].trim()),Integer.valueOf(sa[3].trim()));
	 		             // int banteil = Integer.valueOf(sa[1]);
	 		             int spreis = Integer.valueOf(sa[5].trim());
	 		             Amount sAmount = new Amount(Integer.valueOf(sa[6].trim()),Integer.valueOf(sa[7].trim()));
	 		             //int santeil = Integer.valueOf(sa[3]);
	 		            
	 		             bDistributionLoad.put(bpreis, bAmount);
	 		             sDistributionLoad.put(spreis, sAmount);
	 		             count = count + 1;
            		 }else {
            			 throw new EmptyFileException("The GroupCount is not enough!");
            		 }
            	 }
            	 while (count >= groupCount +4 && (buf=br.readLine()) != null){
            		 Card card;
            		 buf=buf.trim();
            		 String[] sa = buf.split(":|\\s");
            		 if((Integer.valueOf(sa[1])%2) == 0){
            			card = new BuyerCard(Integer.valueOf(sa[1].trim()),Integer.valueOf(sa[2].trim()),sa[3].trim().charAt(0));
            		 }else {
            			card = new SellerCard(Integer.valueOf(sa[1].trim()),Integer.valueOf(sa[2].trim()),sa[3].trim().charAt(0));
            		 }
            		 cardSet.add(card);
            	 }
            	 System.out.println(playerCount);
    			 System.out.println(assistantCount);
    			 System.out.println(groupCount);
    			 System.out.println(firstID);
    			 
            	 kms.getConfiguration().setPlayerCount(playerCount);
    	    	 kms.getConfiguration().setAssistantCount(assistantCount);
    	    	 kms.getConfiguration().setGroupCount(groupCount);
    	    	 kms.getConfiguration().setFirstID(firstID);
    	    	 kms.getConfiguration().setbDistribution(bDistributionLoad);
    			 kms.getConfiguration().setsDistribution(sDistributionLoad);
    			 kms.setCards(cardSet);
    			 
         }else 
             throw new EmptyFileException("load file can not be empty!");
    	
    }

	
	//removeCard
	//removes all cards from the given package (pack)
	//beginning with lastId up to its size
	public boolean removeCard(char pack, int lastId) throws WrongPlayerCountException, WrongAssistantCountException, WrongFirstIDException, WrongRelativeDistributionException{
		Set<Card> oldSet = new LinkedHashSet<Card>(kms.getCards());
		Map<Integer, Amount> distrib;
		Integer key;
		
		//test is there a conform configuration?
		if(kms.getPlayerCount() != (LogicHelper.getAbsoluteSum(kms.getbDistribution()) +  LogicHelper.getAbsoluteSum(kms.getsDistribution())))throw new WrongPlayerCountException();
		if(kms.getAssistantCount() <= 0)throw new WrongAssistantCountException();
		if(kms.getConfiguration().getFirstID() < 0)throw new WrongFirstIDException();
		if((LogicHelper.getRelativeSum(kms.getbDistribution()) +  LogicHelper.getRelativeSum(kms.getsDistribution())) != 200) throw new WrongRelativeDistributionException();
		
		for(Card iter : oldSet){
			//Check if card must be removed (Id is higher than lasdId)
			if(iter.getPackage() == pack && iter.getId() >= lastId){
				//Update player count
				kms.getConfiguration().setPlayerCount(kms.getConfiguration().getPlayerCount()-1);
				
				//Update distribution-map
				if(iter instanceof BuyerCard) distrib = kms.getConfiguration().getbDistribution();
				else distrib = kms.getConfiguration().getsDistribution();
				
				key = iter.getValue();
				
				distrib.get(key).setAbsolute(distrib.get(key).getAbsolute()-1);
				if(distrib.get(key).getAbsolute() == 0) distrib.remove(key);
				
				kms.getCards().remove(iter);
				
				System.out.println("Excluded Card: " + iter.getId());
			}
		}
		
		if(kms.getCards() == oldSet) return false;
		else return true;
	}
	
	
	
}