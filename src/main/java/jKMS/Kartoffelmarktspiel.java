package jKMS;


import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jKMS.cards.Card;
import jKMS.states.Evaluation;
import jKMS.states.Load;
import jKMS.states.Play;
import jKMS.states.Preparation;
import jKMS.states.State;

@Service
public class Kartoffelmarktspiel {

	private Set<Contract> contracts;
	private State state;
	@Autowired
	private Configuration configuration;
	private Set<Card> cards;
	private Kartoffelmarktspiel instance;
	private Calendar begin;
	private Calendar end;
	private static Currencys currency;
    private enum Currencys {
    	EURO("€"), DOLLAR("$"), POUNDS("£");
    	private final String string;
    	Currencys(String string)	{
    		this.string = string;
    	}
    	public String getString()	{
    		return this.string;
    	}
    }
	
	// DEFAULT CONSTRUCTOR
	private Kartoffelmarktspiel() {
		// load default settings from file

		instance = this;
		contracts = new LinkedHashSet<Contract>();
		cards = new LinkedHashSet<Card>();
		prepare();
		currency = Currencys.EURO;
	}

	// STATE SETTERS
	public void prepare() {
		state = new Preparation(instance);
		if(this.configuration != null)
			this.configuration.clear();
		LogicHelper.print("Preparing..");
		//Application.gui.setReady(LogicHelper.getLocalizedMessage("preparation"));
		//gui.setReady(LogicHelper.getLocalizedMessage("preparation"));
	}

	public void load() {
		state = new Load(instance);
		this.configuration.clear();
		LogicHelper.print("Loading..");
		//Application.gui.setReady(LogicHelper.getLocalizedMessage("loading"));
	}
	
	public void play() {
		// Start game
		begin = Calendar.getInstance();
		state = new Play(instance);
	}

	public void evaluate() {
		// Stop game
		end = Calendar.getInstance();
		state = new Evaluation(instance);
		LogicHelper.print("Evaluating..");
	}

	// GETTERS AND SETTERS

	public Configuration getConfiguration() {
		return configuration;
	}

	public Set<Card> getCards() {
		return cards;
	}
	
	public Set<Contract> getContracts(){
		return contracts;
	}
	
	public State getState()	{
		return state;
	}

	public void setCards(Set<Card> cards) {
		this.cards = cards;
	}
	
	// GETTERS FOR CONFIGURATION
	
	public int getPlayerCount() {
		return configuration.getPlayerCount();
	}
	
	public int getLastId()	{
		int last = 0;
		for(Card card : cards)	{
			if(card.getId() > last)
				last = card.getId();
		}
		return last;
	}

	public int getAssistantCount() {
		return configuration.getAssistantCount();
	}

	public int getGroupCount(String type) {
		return configuration.getGroupCount(type);
	}
	
	public Map<Integer, Amount> getbDistribution() {
		return configuration.getbDistribution();
	}

	public Map<Integer, Amount> getsDistribution() {
		return configuration.getsDistribution();
	}
	
	public Calendar getBegin() {
		return begin;
	}

	public Calendar getEnd() {
		return end;
	}

	public Set<Package> getPackages() {
		return configuration.getPackages();
	}
	
	public Package getPackage(char pack) {
		return configuration.getPackage(pack);
	}
	
	public static String getCurrency()	{
		return currency.getString();
	}
	
	public Map<Integer, String> getCurrencys()	{

		Map<Integer, String> map = new TreeMap<>();
		map.put(Kartoffelmarktspiel.currency.ordinal(), Kartoffelmarktspiel.currency.getString());
		for(Currencys currency : Currencys.values())	{
			if(currency.getString() != Kartoffelmarktspiel.currency.getString())	{
				map.put(currency.ordinal(), currency.getString());
			}
		}
			
		return map;
	}
	
	public void setCurrency(int currency)	{
		for(Currencys curr : Currencys.values())	{
			if(currency == curr.ordinal())	{
				Kartoffelmarktspiel.currency = curr;
			}
		}
	}
	
}
