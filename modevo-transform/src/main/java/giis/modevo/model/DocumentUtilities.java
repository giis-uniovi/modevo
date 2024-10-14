package giis.modevo.model;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DocumentUtilities {
	public Document readDocumentGeneric (String path) {
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 Document doc = null;
		 // optional, but recommended
       // process XML securely, avoid attacks like XML External Entities (XXE)
       try {
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		    // parse XML file
	         DocumentBuilder db = dbf.newDocumentBuilder();
	         doc = db.parse(new File(path));
	         // optional, but recommended
		     doc.getDocumentElement().normalize();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new DocumentException("A model could not be opened:" + e);
		}
	    return doc;
	}
}
