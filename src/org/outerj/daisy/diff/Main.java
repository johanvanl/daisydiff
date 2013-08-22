package org.outerj.daisy.diff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class Main {

	public static void main(String[] args) throws URISyntaxException {

		boolean htmlOut = true;
		String outputFile = "daisydiff.html";

		InputStream oldStream = null;
		InputStream newStream = null;

		try {
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
					.newInstance();

			TransformerHandler result = tf.newTransformerHandler();
			result.setResult(new StreamResult(new File(outputFile)));

			oldStream = new FileInputStream(
					"/home/johan/Desktop/test_html/start - CS 748_old.html");
			newStream = new FileInputStream(
					"/home/johan/Desktop/test_html/start - CS 748_new.html");

			XslFilter filter = new XslFilter();

			ContentHandler postProcess = htmlOut ? filter.xsl(result,
					"org/outerj/daisy/diff/htmlheader.xsl") : result;

			Locale locale = Locale.getDefault();
			String prefix = "diff";

			HtmlCleaner cleaner = new HtmlCleaner();

			InputSource oldSource = new InputSource(oldStream);
			InputSource newSource = new InputSource(newStream);

			DomTreeBuilder oldHandler = new DomTreeBuilder();
			cleaner.cleanAndParse(oldSource, oldHandler);
			TextNodeComparator leftComparator = new TextNodeComparator(
					oldHandler, locale);

			DomTreeBuilder newHandler = new DomTreeBuilder();
			cleaner.cleanAndParse(newSource, newHandler);
			TextNodeComparator rightComparator = new TextNodeComparator(
					newHandler, locale);

			postProcess.startDocument();
			postProcess.startElement("", "diffreport", "diffreport",
					new AttributesImpl());
			postProcess.startElement("", "diff", "diff", new AttributesImpl());
			HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(postProcess,
					prefix);

			HTMLDiffer differ = new HTMLDiffer(output);
			differ.diff(leftComparator, rightComparator);

			postProcess.endElement("", "diff", "diff");
			postProcess.endElement("", "diffreport", "diffreport");
			postProcess.endDocument();

		} catch (Throwable e) {
			e.printStackTrace();
			if (e.getCause() != null) {
				e.getCause().printStackTrace();
			}
			if (e instanceof SAXException) {
				((SAXException) e).getException().printStackTrace();
			}
		} finally {
			try {
				if (oldStream != null)
					oldStream.close();
			} catch (IOException e) {
				// ignore this exception
			}
			try {
				if (newStream != null)
					newStream.close();
			} catch (IOException e) {
				// ignore this exception
			}
		}

	}

}
