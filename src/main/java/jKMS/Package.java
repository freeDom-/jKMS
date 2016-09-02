package jKMS;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jKMS.cards.BuyerCard;
import jKMS.cards.Card;
import jKMS.cards.SellerCard;
import jKMS.exceptionHelper.WrongPackageException;

/**
 * Represents the packages for assistants.
 * Provides support for adding, removing and some nice getter methods.
 * @author freeDom, siegmund42
 *
 */
public class Package	{
	
	protected Kartoffelmarktspiel kms;
	private char name;
	private List<Card> bCards;
	private List<Card> sCards;
	
	public Package(char name, Kartoffelmarktspiel kms)	{
		this.name = name;
		this.bCards = new LinkedList<>();
		this.sCards = new LinkedList<>();
		this.kms = kms;
	}
	
	public char getName()	{
		return this.name;
	}
	
	public List<Card> getbCards()	{
		return this.bCards;
	}
	
	public List<Card> getsCards()	{
		return this.sCards;
	}
	
	/**
	 * 
	 * @return all Cards of this Package by adding sCards and bCards
	 */
	public List<Card> getCards()	{
		List<Card> cards = new LinkedList<>();
		cards.addAll(sCards);
		cards.addAll(bCards);
		return cards;
	}
	
	/**
	 * Add a card to this Package.
	 * @param card the card to add
	 * @return true if card was added properly, false otherwise
	 */
	public boolean add(Card card)	{
		boolean bla = false;
		if(!bCards.contains(card) && !sCards.contains(card))
			if(card instanceof BuyerCard)	{
				bla = bCards.add(card);
				Collections.sort(bCards);
			}	else	{
				bla = sCards.add(card);
				Collections.sort(sCards);
			}
		return bla;
	}
	
	/**
	 * Returns the first Card of this Package.
	 * @return first Card of this Package, could be Seller or Buyer
	 */
	public Card getFirstCard()	{
		if(bCards.get(0).compareTo(sCards.get(0)) < 0)
			return bCards.get(0);
		if(bCards.get(0).compareTo(sCards.get(0)) > 0)
			return sCards.get(0);
		return null;
	}
	
	/**
	 * Returns the first Card of this package depending on the given type.
	 * @param type the type of the requested first card, could be 'b' = Buyer or 's' = Seller
	 * @return first Card of this Package
	 */
	public Card getFirstCard(char type)	{
		if(type == 'b')
			return bCards.get(0);
		if(type == 's')
			return sCards.get(0);
		throw new IllegalArgumentException("The Argument " + type + " is not allowed for the function getFirstCard().");
	}
	
	/**
	 * Returns the last Card of this Package.
	 * @return last Card of this Package, could be Seller or Buyer
	 */
	public Card getLastCard()	{
		if(bCards.get(bCards.size() - 1).compareTo(sCards.get(sCards.size() - 1)) > 0)
			return bCards.get(bCards.size() - 1);
		if(bCards.get(bCards.size() - 1).compareTo(sCards.get(sCards.size() - 1)) < 0)
			return sCards.get(sCards.size() - 1);
		return null;
	}
	
	/**
	 * Returns the last Card of this package depending on the given type.
	 * @param type the type of the requested last card, could be 'b' = Buyer or 's' = Seller
	 * @return last Card of this Package
	 */
	public Card getLastCard(char type)	{
		if(type == 'b')
			return bCards.get(bCards.size() - 1);
		if(type == 's')
			return sCards.get(sCards.size() - 1);
		throw new IllegalArgumentException("The Argument " + type + " is not allowed for the function getLastCard().");
	}
	
	/**
	 * Returns the Card with the given ID
	 * @param ID the Card id
	 * @return the Card or null if Card was not found
	 */
	public Card getCard(int ID)	{
		for(Card bCard : bCards)	{
			if(bCard.getId() == ID)
				return bCard;
		}
		for(Card sCard : sCards)	{
			if(sCard.getId() == ID)
				return sCard;
		}
		return null;
	}
	
	/**
	 * Checks if this Package contains a specified Card
	 * @param id the Card ID
	 * @return true if card with ID id is existing in this package
	 */
	public boolean contains(int id)	{
		return this.getCard(id) != null;
	}
	
	/**
	 * Remove one card from this package
	 * @param card the Card to remove
	 * @return true if Card was removed, false otherwise
	 */
	public boolean remove(Card card)	{
		//if(card==null) return false to prevent NullPointerException necessary?
		if(card instanceof BuyerCard)
			return kms.getCards().remove(card) && bCards.remove(card);
		if(card instanceof SellerCard)
			return kms.getCards().remove(card) && sCards.remove(card);
		return false;
	}
	
	/**
	 * Remove all cards after last card from this package and the kms.cardSet
	 * @param lastID the last remaining [not distributed] Card of this package -> the first Card to remove
	 * @return true if all cards where removed, false otherwise
	 */
	public boolean removeCards(int lastID)	throws WrongPackageException	{
		if(this.contains(lastID))	{
			int i;
			int lastPackID = this.getLastCard().getId();
			for(i = lastID; i <= lastPackID; i++)	{
				if(this.contains(i))	{
					Card actualCard = this.getCard(i);
					// Remove from Package and card set
					if(!this.remove(actualCard))
						break;
					// Update Player Count
					kms.getConfiguration().setPlayerCount(kms.getPlayerCount() - 1);
					// Update distributions
					Map<Integer, Amount> distrib = new HashMap<>();
					if(actualCard instanceof BuyerCard)
						distrib = kms.getConfiguration().getbDistribution();
					else 
						distrib = kms.getConfiguration().getsDistribution();
					
					distrib.get(actualCard.getValue()).setAbsolute(distrib.get(actualCard.getValue()).getAbsolute() - 1);
					if(distrib.get(actualCard.getValue()).getAbsolute() == 0)
						distrib.remove(actualCard.getValue());
					LogicHelper.print("Removed Card: " + actualCard.toString());
					
				}
			}
			return i == lastPackID + 1;
		}	else
			throw new WrongPackageException();
	}
	
	@Override
	public String toString()	{
		return Character.toString(this.name);
	}

}
