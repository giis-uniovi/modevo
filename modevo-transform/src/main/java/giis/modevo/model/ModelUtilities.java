package giis.modevo.model;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import giis.modevo.model.schema.Column;
import giis.modevo.model.schema.Schema;
import giis.modevo.model.schema.Table;
import giis.modevo.model.schemaevolution.SchemaEvolution;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ModelUtilities {
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
	
	/**
	 * 	Returns the table object from either the Schema or the Schema Evolution model
	 */
	public Table findTable(Schema schema, SchemaEvolution se, String name) {
		Table table = schema.getTable(name);
		if (table != null) {
			return table;
		}
		else {
			log.info("Table %s not found in the schema model. Start search on the schema evolution model.", name);
			return se.getTable(name);
		}
	}
	
	/**
	 * 	Returns the column object from either the Schema or the Schema Evolution model
	 */
	public Column findColumn(Schema schema, SchemaEvolution se, String nameTable, String nameColumn) {
		Table t = findTable (schema, se, nameTable);
		Column c = t.getColumn(nameColumn);
		if (c==null) {
			c = se.getTable(nameTable).getColumn(nameColumn);
		}
		return c;
	}
}
