package jKMS.states;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import jKMS.Contract;
import jKMS.Kartoffelmarktspiel;
import jKMS.LogicHelper;
import jKMS.cards.BuyerCard;
import jKMS.cards.Card;
import jKMS.cards.SellerCard;

public class Play extends State {
	
	public Play(Kartoffelmarktspiel kms)	{
		this.kms = kms;
	}
	
	/**
	 * Add a contract identified by id1, id2 and price.
	 */
	@Override
	public int addContract(int id1,int id2,int price, String uri){ 
		// returns errorCode: 0 -> everything fine 1 -> double buyer/seller 2 -> Card not available 3 -> already dealt
		Set<Card> gehandeltCards = new LinkedHashSet<Card>();
	    Iterator<Contract> citer = kms.getContracts().iterator();
	    while(citer.hasNext()){
	    	Contract gehandeltContract = citer.next();
	    	gehandeltCards.add(gehandeltContract.getBuyer());
	    	gehandeltCards.add(gehandeltContract.getSeller());
	    }
	    
	    if(id1 == id2)	{
	    	LogicHelper.print("addContract() not successful, because deal with double buyer or seller", 1);
	    	return 1;
	    }
	    //getCardset for card check
		Iterator<Card> iter = kms.getCards().iterator();
	    Card card1 = null;
    	Card card2 = null;
	    while(iter.hasNext()){	
	    	Card card = iter.next();
	    	if (card.getId() == id1){
	    		card1 = card;
	    	}
	    	else if(card.getId() == id2){
	    		card2 = card;
	    	}
	    }
	    // return 2 when Card not available
	    if(card1 == null || card2 == null){
	    	LogicHelper.print("addContract() not successful, because Card not available", 1);
	    	return 2;
	    }else if((card1 instanceof BuyerCard && card2 instanceof BuyerCard) 
	    		|| (card1 instanceof SellerCard && card2 instanceof SellerCard)){
	    	LogicHelper.print("addContract() not successful, because deal with double buyer or seller", 1);
	    	return 1;
	    //return 1 when double buyer or seller
	    }else if(gehandeltCards.contains(card1) || gehandeltCards.contains(card2)){
	    	LogicHelper.print("addContract() not successful, because Card already dealt", 1);
	    	return 3;
	    //return 3 when card already dealt
	    }else{
	    	Contract contract;
	    	if(card1 instanceof BuyerCard) {
	    		contract = new Contract((BuyerCard)card1,(SellerCard)card2,price,uri);
	    	}else{
	    		contract = new Contract((BuyerCard)card2,(SellerCard)card1,price,uri);
	    	}
	    	kms.getContracts().add(contract);
	    	LogicHelper.print("Added contract: " + contract.toString());
	    	return 0;
	    //return 0 when everything fine	
	    }  
	}
	
	/**
	 * Remove a contract identified by id1, id2 and price.
	 * @see jKMS.states.State#removeContract(int, int, int)
	 */
	@Override
	public boolean removeContract(int id1, int id2, int price)	{
		
		// Go through all contracts
		for(Contract contract : kms.getContracts())	{
			// Price of actual contract must be equal to given price
			if(contract.getPrice() == price)	{
				// If id1 -> Buyer, id2 -> Seller
				if(contract.getBuyer().getId() == id1 && contract.getSeller().getId() == id2)	{
					LogicHelper.print("Removing Contract: " + contract.toString());
					return kms.getContracts().remove(contract);
				}
				// If id2 -> Buyer, id1 -> Seller
				else if(contract.getSeller().getId() == id1 && contract.getBuyer().getId() == id2)	{
					LogicHelper.print("Removing Contract: " + contract.toString());
					return kms.getContracts().remove(contract);
				}
			}
		}
		
		return false;
		
	}
}
