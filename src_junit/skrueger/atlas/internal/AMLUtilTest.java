package skrueger.atlas.internal;

import junit.framework.TestCase;

import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.junit.Test;
import org.opengis.filter.Filter;

import schmitzm.geotools.feature.CQLFilterParser;

public class AMLUtilTest extends TestCase {

	
	@Test
	public void testUpgrade1() throws CQLException {
		String martinFilter = "$ORTSRATSBE=\"Brackstedt/Velstove/Warmenau\"|$ORTSRATSBE=\"Wendschott\"|$ORTSRATSBE=\"Kästorf/Sandkamp\"|$ORTSRATSBE=\"Vorsfelde\"|$ORTSRATSBE=\"Stadtmitte\"|$ORTSRATSBE=\"Mitte-West\"|$ORTSRATSBE=\"Neuhaus/Reislingen\"|$ORTSRATSBE=\"Hattorf/Heiligendorf\"|$ORTSRATSBE=\"Almke/Neindorf\"|$ORTSRATSBE=\"Nordstadt\"|$ORTSRATSBE=\"Fallersleben/Sülfeld\"|$ORTSRATSBE=\"Neuhaus/Reislingen\"|$ORTSRATSBE=\"Hehlingen\"|$ORTSRATSBE=\"Westhagen\"|$ORTSRATSBE=\"Ehmen/Mörse\"|$ORTSRATSBE=\"Barnstorf/Nordsteimke\"|$ORTSRATSBE=\"Detmerode\"";
		String cqlFilter = AMLUtil.upgradeMartinFilter2ECQL(martinFilter);
		System.out.println(cqlFilter);
		assertEquals("ORTSRATSBE='Brackstedt/Velstove/Warmenau' OR ORTSRATSBE='Wendschott' OR ORTSRATSBE='Kästorf/Sandkamp' OR ORTSRATSBE='Vorsfelde' OR ORTSRATSBE='Stadtmitte' OR ORTSRATSBE='Mitte-West' OR ORTSRATSBE='Neuhaus/Reislingen' OR ORTSRATSBE='Hattorf/Heiligendorf' OR ORTSRATSBE='Almke/Neindorf' OR ORTSRATSBE='Nordstadt' OR ORTSRATSBE='Fallersleben/Sülfeld' OR ORTSRATSBE='Neuhaus/Reislingen' OR ORTSRATSBE='Hehlingen' OR ORTSRATSBE='Westhagen' OR ORTSRATSBE='Ehmen/Mörse' OR ORTSRATSBE='Barnstorf/Nordsteimke' OR ORTSRATSBE='Detmerode'", cqlFilter);
	}
	


	@Test
	public void testUpgrade2() throws CQLException {
		String martinFilter = "$ORTSRATSBE!=( \" \" )";
		String cqlFilter = AMLUtil.upgradeMartinFilter2ECQL(martinFilter);
		System.out.println(cqlFilter);
		assertEquals("ORTSRATSBE <> ' '", cqlFilter);
	}

	@Test
	public void testUpgrade3() throws CQLException {
		String martinFilter = "$PG_Name!=( \" \" )";
		String cqlFilter = AMLUtil.upgradeMartinFilter2ECQL(martinFilter);
		System.out.println(cqlFilter);
		assertEquals("PG_Name <> ' '", cqlFilter);
		
		Filter cqlFilterObj = new CQLFilterParser().parseFilter(cqlFilter);
		assertEquals(cqlFilter, cqlFilterObj.toString() );
	}
	
	

	@Test
	public void testUpgrade4() throws CQLException {
//		String martinFilter = "$PG_Name!=( \" \" )";
//		String cqlFilter = AMLUtil.upgradeMartinFilter2ECQL(martinFilter);
//		System.out.println(cqlFilter);
//		assertEquals(, );
		String cqlFilter1 = "PG_Name <> ' '";
		Filter filterParsed = CQL.toFilter(cqlFilter1);
		Filter filterParsed2 = CQL.toFilter(filterParsed.toString());
		
		assertEquals(filterParsed, filterParsed2);
	}
}
