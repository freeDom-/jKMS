package jKMS.cards;

import static org.junit.Assert.assertEquals;
import jKMS.Package;

import org.junit.Test;

public class BuyerCardTest {
	
	@Test
	public void testBuyerCard(){
		BuyerCard buyer = new BuyerCard(1005, 3, new Package('A', null));
		
		assertEquals(1005, buyer.getId());
		assertEquals(3, buyer.getValue());
		assertEquals('A', buyer.getPackage().getName());
	}
}
