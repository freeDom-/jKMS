package jKMS.cards;

import static org.junit.Assert.assertEquals;
import jKMS.Package;

import org.junit.Test;

public class SellerCardTest {
	
	@Test
	public void testSellerCard(){
		SellerCard seller = new SellerCard(2345, 8, new Package('X', null));
		
		assertEquals(2345, seller.getId());
		assertEquals(8, seller.getValue());
		assertEquals('X', seller.getPackage().getName());
	}
}
