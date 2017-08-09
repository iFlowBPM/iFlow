package pt.iflow.api.index;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class DocumentTextExtractor {

	public static String extract(InputStream content) throws IOException, SAXException, TikaException {
		BodyContentHandler handler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();
		ParseContext pcontext = new ParseContext();

		// parsing the document using PDF parser
		PDFParser pdfparser = new PDFParser();
		pdfparser.parse(content, handler, metadata, pcontext);
		return handler.toString();
	}

}
