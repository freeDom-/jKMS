package jKMS;


import jKMS.cards.BuyerCard;
import jKMS.cards.Card;
import jKMS.cards.SellerCard;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
//import java.util.Properties;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;

public class Csv {
	
	public Csv(){
		
	}
	
	//Überschriften tabelle contracts
	private String contracts;
	private String idB;
	private String idS;
	private String price;
	private String station;
	private String time;
	private String packB;
	private String packS;
	private String valueB;
	private String valueS;
	//Überschriften tabelle unplayed cards
	private String unplayedcards;
	private String cardid;
	private String cardtyp;
	private String cardpack;
	private String cardval;
	private String buyerCard;
	private String sellerCard;
	
	public void generateCSV(CSVWriter writer, Set<Card> cards,Set<Contract> contracts){
		
		Set<Card> playedCards = new LinkedHashSet<Card>();
		List<String[]> data = new ArrayList<String[]>();
		//Properties propertie = LogicHelper.getProperetie();
		playedCards.clear();
		data.clear();
		Integer idb =0;
		Integer ids =0;
		Integer price =0;
		Integer bvalue =0;
		Integer svalue =0;
		char    packb = ' ';
		char    packs = ' ';
		String station = "";
		Date time = new Date();
		Integer cid = 0;
		Integer cvalue = 0;
		char cpack = ' ';
		String ctyp = "";
		
		// get Überschriften
		
		this.contracts = LogicHelper.getLocalizedMessage("CSV.headlineContract");
		this.idB = LogicHelper.getLocalizedMessage("CSV.idBuyer");
		this.idS = LogicHelper.getLocalizedMessage("CSV.idSeller");
		this.price = LogicHelper.getLocalizedMessage("CSV.price");
		this.station = LogicHelper.getLocalizedMessage("CSV.station");
		this.time = LogicHelper.getLocalizedMessage("CSV.time");
		this.packB = LogicHelper.getLocalizedMessage("CSV.packageBuyer");
		this.packS = LogicHelper.getLocalizedMessage("CSV.packageSeller");
		this.valueB = LogicHelper.getLocalizedMessage("CSV.valueBuyer");
		this.valueS = LogicHelper.getLocalizedMessage("CSV.valueSeller");
		this.unplayedcards = LogicHelper.getLocalizedMessage("CSV.headlineUnplayedCards");
		this.cardid = LogicHelper.getLocalizedMessage("CSV.unplayedCardID");
		this.cardtyp = LogicHelper.getLocalizedMessage("CSV.unplayedCardTyp");
		this.cardval = LogicHelper.getLocalizedMessage("CSV.unplayedCardValue");
		this.sellerCard =LogicHelper.getLocalizedMessage("CSV.sellerCard");
		this.buyerCard = LogicHelper.getLocalizedMessage("CSV.buyerCard");
		this.cardpack = LogicHelper.getLocalizedMessage("CSV.unplayedCardPackage");
		
	
		
		//Create table for Contracts
		
		data.add(new String[] {this.contracts});
		
		data.add(new String[] {});//Leerzeile 
		
		data.add(new String[] {this.idB,this.valueB,this.packB,this.idS,this.valueS,this.packS, this.price,this.time,this.station});
		

		for(Contract iter : contracts){
			
			//get Data from Contract
			idb = iter.getBuyer().getId();
			ids = iter.getSeller().getId();
			bvalue = iter.getBuyer().getValue();
			svalue = iter.getSeller().getValue();
			packb = iter.getBuyer().getPackage();
			packs = iter.getSeller().getPackage();
			price = iter.getPrice();
			station = iter.getUri();
			time = iter.getTime();
			
		
			
			// add to CSV
			data.add(new String[] {idb.toString(),bvalue.toString(),String.valueOf(packb),ids.toString(),svalue.toString(),String.valueOf(packs),price.toString(),time.toString(),station});
			
			//add to playedbuyer and playedseller to get unplayed player later
			playedCards.add(iter.getBuyer());
			playedCards.add(iter.getSeller());
		}
		
		data.add(new String[] {});//Leerzeile ?
		

		
		//Create table for unplayed player
		data.add(new String[] {this.unplayedcards});
		
		data.add(new String[] {});//Leerzeile 
		
		data.add(new String[] {this.cardid, this.cardtyp,this.cardpack, this.cardval});
		
		for(Card iter : cards){
			if(playedCards.contains(iter) == false){
				cid = iter.getId();
				cpack = iter.getPackage();
				if(iter instanceof BuyerCard) ctyp = this.buyerCard;
				if(iter instanceof SellerCard) ctyp = this.sellerCard;
				cvalue = iter.getValue();
				
				//add to CSV
				data.add(new String[] {cid.toString(),ctyp,String.valueOf(cpack),cvalue.toString()});
			}
		}
		
		writer.writeAll(data);
		
	}

}
