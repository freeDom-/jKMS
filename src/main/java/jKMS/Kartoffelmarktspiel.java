package jKMS;


import jKMS.cards.Card;
import jKMS.states.Evaluation;
import jKMS.states.Play;
import jKMS.states.Load;
import jKMS.states.Preparation;
import jKMS.states.State;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class Kartoffelmarktspiel {

	private Set<Contract> contracts;
	private State state;
	@Autowired
	private Configuration configuration;
	private Set<Card> cards;
	private Kartoffelmarktspiel instance;
    
	// DEFAULT CONSTRUCTOR
	private Kartoffelmarktspiel() {
		// load default settings from file

		instance = this;
		contracts = new LinkedHashSet<Contract>();
		cards = new LinkedHashSet<Card>();
		prepare();
	}

	// STATE SETTERS
	public void prepare() {
		state = new Preparation(instance);
		System.out.println("Preparing..");
	}

	public void load() {
		state = new Load(instance);
		System.out.println("Loading..");
	}
	
	public void play() {
		state = new Play(instance);
		System.out.println("Playing..");
	}

	public void evaluate() {
		state = new Evaluation(instance);
		System.out.println("Evaluating..");
	}

	// GETTERS AND SETTERS

//	public Kartoffelmarktspiel getInstance() {
//		return instance;
//	}

	public Configuration getConfiguration() {
		return configuration;
	}

//	USELESS ??
//	public void setConfiguration(Configuration configuration) {
//		this.configuration = configuration;
//	}

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

	public int getAssistantCount() {
		return configuration.getAssistantCount();
	}

	public int getGroupCount() {
		return configuration.getGroupCount();
	}
	
	public Map<Integer, Amount> getbDistribution() {
		return configuration.getbDistribution();
	}

	public Map<Integer, Amount> getsDistribution() {
		return configuration.getsDistribution();
	}
	
}
