/*
 * Copyright 2004 Guy Van den Broeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.outerj.daisy.diff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

public class DaisyDiff {

	public DaisyDiff() {
	}

	private ByteArrayOutputStream baos = null;

	public void diffTwoFiles(String original_filename, String revised_filename)
			throws FileNotFoundException {
		InputStream originalStream = new FileInputStream(original_filename);
		InputStream revisedStream = new FileInputStream(revised_filename);
		diff(originalStream, revisedStream);
		try {
			originalStream.close();
			revisedStream.close();
		} catch (IOException e) {
			// ignore
		}
	}

	public void diffTwoStrings(String original_string, String revised_string) {
		InputStream originalStream = new ByteArrayInputStream(
				original_string.getBytes());
		InputStream revisedStream = new ByteArrayInputStream(
				revised_string.getBytes());
		diff(originalStream, revisedStream);
		try {
			originalStream.close();
			revisedStream.close();
		} catch (IOException e) {
			// ignore
		}
	}

	private void diff(InputStream originalstream, InputStream revisedStream) {
		try {
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
					.newInstance();

			baos = new ByteArrayOutputStream();
			TransformerHandler result = tf.newTransformerHandler();
			result.setResult(new StreamResult(baos));

			XslFilter filter = new XslFilter();

			ContentHandler postProcess = filter.xsl(result,
					"org/outerj/daisy/diff/htmlheader.xsl");

			Locale locale = Locale.getDefault();
			String prefix = "diff";

			HtmlCleaner cleaner = new HtmlCleaner();

			InputSource oldSource = new InputSource(originalstream);
			InputSource newSource = new InputSource(revisedStream);

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
		}
	}

	public String getHTML() {
		return baos.toString();
	}

	public static void main(String[] args) throws SAXException, IOException {
		DaisyDiff dd = new DaisyDiff();
		//String original = "/home/johan/Desktop/test_html/start - CS 748_old.html";
		//String revised = "/home/johan/Desktop/test_html/start - CS 748_new.html";
		//dd.diffTwoFiles(original, revised);
		
		String or = "<html>hello</html>";
		String re = "<html>hello maatjies</html>";
		dd.diffTwoStrings(or, re);
		
		System.out.println(dd.getHTML());
	}

}
