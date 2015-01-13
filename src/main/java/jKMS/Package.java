package jKMS;

import jKMS.cards.BuyerCard;
import jKMS.cards.Card;
import jKMS.cards.SellerCard;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

public class Package {
	
	@Autowired
	Kartoffelmarktspiel kms;
	private char name;
	private List<Card> bCards;
	private List<Card> sCards;
	
	public Package(char name)	{
		this.name = name;
		this.bCards = new LinkedList<>();
		this.sCards = new LinkedList<>();
		kms.getConfiguration().addPackage(this);
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
	
	public List<Card> getCards()	{
		List<Card> cards = new LinkedList<>();
		cards.addAll(sCards);
		cards.addAll(bCards);
		return cards;
	}
	
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
	
	public Card getFirstCard()	{
		if(bCards.get(0).compareTo(sCards.get(0)) < 0)
			return bCards.get(0);
		if(bCards.get(0).compareTo(sCards.get(0)) > 0)
			return sCards.get(0);
		return null;
	}
	
	public Card getFirstCard(char type)	{
		if(type == 'b')
			return bCards.get(0);
		if(type == 's')
			return sCards.get(0);
		throw new IllegalArgumentException("The Argument " + type + " is not allowed for the function getFirstCard().");
	}
	
	public Card getLastCard()	{
		if(bCards.get(bCards.size() - 1).compareTo(sCards.get(sCards.size() - 1)) > 0)
			return bCards.get(bCards.size() - 1);
		if(bCards.get(bCards.size() - 1).compareTo(sCards.get(sCards.size() - 1)) < 0)
			return sCards.get(sCards.size() - 1);
		return null;
	}
	
	public Card getLastCard(char type)	{
		if(type == 'b')
			return bCards.get(bCards.size() - 1);
		if(type == 's')
			return sCards.get(sCards.size() - 1);
		throw new IllegalArgumentException("The Argument " + type + " is not allowed for the function getLastCard().");
	}
	
	/*
	 * Returns a card in this Package by its ID
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
	
	/*
	 * Returns true if card with ID id ist existing in this package
	 */
	public boolean contains(int id)	{
		return this.getCard(id) != null;
	}
	
	/*
	 * Remove one card from this package given by its ID
	 */
	public boolean remove(int ID)	{
		Card card = this.getCard(ID);
		//if(card==null) return false to prevent NullPointerException necessary?
		if(card instanceof BuyerCard)
			return kms.getCards().remove(card) && bCards.remove(card);
		if(card instanceof SellerCard)
			return kms.getCards().remove(card) && sCards.remove(card);
		return false;
	}
	
	/*
	 * Remove all cards after last card from this package
	 */
	public boolean removeCards(int lastID)	{
		if(this.contains(lastID))	{
			int i;
			for(i = lastID; i <= this.getLastCard().getId(); i++)	{
				if(this.contains(i))
					this.remove(i);
			}
			return i == (this.getLastCard().getId() + 1);
		}	else
		return false;
	}

}
