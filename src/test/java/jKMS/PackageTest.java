package jKMS;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import jKMS.cards.BuyerCard;
import jKMS.cards.SellerCard;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class PackageTest {
	
	private Package a;
	@Autowired
	private Kartoffelmarktspiel kms;
	
	@Before
	public void setUp()	{
		a = kms.getConfiguration().newPackage('A');
		
		new SellerCard(1002, 2, a);
		assertEquals("The add function should add a Package to the kms.", 1, kms.getPackages().size());
		new SellerCard(1004, 2, a);
		new SellerCard(1006, 2, a);
		new BuyerCard(1001, 2, a);
		new BuyerCard(1003, 2, a);
	}
	
	@After
	public void tearDown()	{
		kms.getConfiguration().clear();
	}
	
	@Test
	public void testGetFirstCard()	{
		assertEquals("getFirstCard should return the very first card of this Package", 1001, a.getFirstCard().getId());
		assertEquals("getFirstCard with param should return the very first card of this Package with the given type", 1002, a.getFirstCard('s').getId());
		assertEquals("getFirstCard with param should return the very first card of this Package with the given type", 1001, a.getFirstCard('b').getId());
	}
	
	@Test
	public void testGetLastCard()	{
		assertEquals("getFirstCard should return the very last card of this Package", 1006, a.getLastCard().getId());
		assertEquals("getFirstCard with param should return the very last card of this Package with the given type", 1006, a.getLastCard('s').getId());
		assertEquals("getFirstCard with param should return the very last card of this Package with the given type", 1003, a.getLastCard('b').getId());
	}
}