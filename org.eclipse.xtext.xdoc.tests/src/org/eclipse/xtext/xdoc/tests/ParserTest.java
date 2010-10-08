package org.eclipse.xtext.xdoc.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.junit.AbstractXtextTests;
import org.eclipse.xtext.xdoc.XdocStandaloneSetup;
import org.eclipse.xtext.xdoc.xdoc.AbstractSection;
import org.eclipse.xtext.xdoc.xdoc.Anchor;
import org.eclipse.xtext.xdoc.xdoc.Chapter;
import org.eclipse.xtext.xdoc.xdoc.Code;
import org.eclipse.xtext.xdoc.xdoc.CodeBlock;
import org.eclipse.xtext.xdoc.xdoc.Emphasize;
import org.eclipse.xtext.xdoc.xdoc.Identifiable;
import org.eclipse.xtext.xdoc.xdoc.Item;
import org.eclipse.xtext.xdoc.xdoc.Link;
import org.eclipse.xtext.xdoc.xdoc.Ref;
import org.eclipse.xtext.xdoc.xdoc.Table;
import org.eclipse.xtext.xdoc.xdoc.TableData;
import org.eclipse.xtext.xdoc.xdoc.TextOrMarkup;
import org.eclipse.xtext.xdoc.xdoc.TextPart;
import org.eclipse.xtext.xdoc.xdoc.UnorderedList;
import org.eclipse.xtext.xdoc.xdoc.XdocFile;

public class ParserTest extends AbstractXtextTests {

	private static final String TEST_FILE_DIR = "testfiles"
			+ File.separatorChar;
	private static final String DNL = "\n\n";
	private static final String head = "chapter[foo]\n\n";
	private static final String closeBracket = "]";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		with(new XdocStandaloneSetup());
	}

	public void testSimple() throws Exception {
		String title = "Title vom Kapitel";
		String firstPart = "Hier kommt dann mal text ";
		String emphasized = "manchmal fett";
		String secondPart = " und manchmal nicht.\nNewlines und so gibt es auch.";
		String text = "chapter[" + title + "]\r\n\r\n" + firstPart + "e["
				+ emphasized + "]" + secondPart + "\n\n";
		//System.out.println(text);
		XdocFile model = (XdocFile) getModel(text);
		Chapter chapter = (Chapter) model.getSections().get(0);
		assertEquals(title,
				((TextPart) (((TextOrMarkup) chapter.getTitle()).getContents()
						.get(0))).getText());
		assertEquals(1, chapter.getContents().size());
		TextOrMarkup p = (TextOrMarkup) chapter.getContents().get(0);
		assertEquals(firstPart, ((TextPart) p.getContents().get(0)).getText());
		assertEquals(emphasized,
				((TextPart) (((TextOrMarkup) ((Emphasize) p.getContents()
						.get(1)).getContents().get(0)).getContents().get(0)))
						.getText());
		assertEquals(secondPart, ((TextPart) p.getContents().get(2)).getText());
	}

	public void testsmallestDoc() throws Exception {
		getDoc("chapter[foo]\n\n bar");
		getDoc("section[foo]\n\n bar");
		getDoc("section2[foo]\n\n bar");
	}

	public void testAnchoredReference() throws Exception {
		String anchor = " a[refName]";
		String fill = " Jump ";
		String refText = " to ";
		String ref = " ref:refName[" + refText + "]";
		XdocFile file = getDoc(head + anchor + fill + ref);
		TextOrMarkup textOrMarkup = (TextOrMarkup) file.getSections().get(0)
				.getContents().get(0);
		assertEquals(4, textOrMarkup.getContents().size());
		Anchor a = (Anchor) textOrMarkup.getContents().get(1);
		Ref r = (Ref) textOrMarkup.getContents().get(3);
		assertEquals(a, r.getRef());
		assertEquals(refText, ((TextPart) ((TextOrMarkup) r.getContents()
				.get(0)).getContents().get(0)).getText());
	}

	// FIXME
	// public void testCodeRef() throws Exception {
	// getDocFromFile(TEST_FILE_DIR + "codeRef.xdoc");
	// }

	public void testCode() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR + "codeTest.xdoc");
		TextOrMarkup textOrMarkup = (TextOrMarkup) file.getSections().get(0)
				.getContents().get(0);
		assertEquals(1, textOrMarkup.getContents().size());
		CodeBlock cb = (CodeBlock) textOrMarkup.getContents().get(0);
		Code code = (Code) cb.getContents().get(0);
		assertEquals("\nclass Foo {\n"
				+ "public static void main(String\\[\\] args){\n"
				+ "System.out.println(\"Hello World\\n\");\n" + "}\n" + "}\n",
				code.getContents());
		textOrMarkup = (TextOrMarkup) file.getSections().get(0)
				.getContents().get(1);
		cb = (CodeBlock) textOrMarkup.getContents().get(0);
		assertEquals(0, cb.getContents().size());
	}

	public void testCodeWithLanguage() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR
				+ "codeWithLanguageTest.xdoc");
		TextOrMarkup textOrMarkup = (TextOrMarkup) file.getSections().get(0)
				.getContents().get(0);
		assertEquals(1, textOrMarkup.getContents().size());
		CodeBlock cb = (CodeBlock) textOrMarkup.getContents().get(0);
		assertEquals("\nclass Foo {\n"
				+ "public static void main(String\\[\\] args){\n"
				+ "System.out.println(\"Hello World\\n\");\n" + "}\n" + "}\n",
				((Code) cb.getContents().get(0)).getContents());
		assertEquals("Java", cb.getLanguage());
	}

	public void testComment() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR + "commentTest.xdoc");
		assertEquals(1, file.getSections().get(0).getContents().size());
	}

	public void testLink() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR + "linkTest.xdoc");
		Link link = (Link) ((TextOrMarkup) file.getSections().get(0)
				.getContents().get(0)).getContents().get(0);
		URL url;
		url = new URL(link.getUrl());
		URLConnection c = url.openConnection();
		assertNotNull(c);
	}

	public void testNamedReference() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR
				+ "namedRefAndTextTest.xdoc");
		Ref r = (Ref) ((TextOrMarkup) file.getSections().get(0).getContents()
				.get(0)).getContents().get(0);
		assertEquals(file.getSections().get(0), r.getRef());
		assertEquals("a Chapter", ((TextPart) r.getContents().get(0)
				.getContents().get(0)).getText());
	}

	public void testNestedList() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR + "nestedListTest.xdoc");
		UnorderedList outer = (UnorderedList) ((TextOrMarkup) file.getSections()
				.get(0).getContents().get(0)).getContents().get(0);
		List<Item> items = outer.getItems();
		assertEquals(1, items.size());
		List<TextOrMarkup> itemContents = items.get(0).getContents();
		assertEquals(1, itemContents.size());
		List<EObject> contents = itemContents.get(0).getContents();
		// we have text parts for indentations
		assertEquals(3, contents.size());
		assertTrue(((TextPart)contents.get(0)).getText().matches("\\s+"));
		assertTrue(((TextPart)contents.get(2)).getText().matches("\\s+"));
		UnorderedList inner = (UnorderedList) contents.get(1);
		items = inner.getItems();
		assertEquals(1, items.size());
		itemContents = items.get(0).getContents();
		assertEquals(1, itemContents.size());
		assertEquals(1, itemContents.get(0).getContents().size());
		assertEquals("some item", ((TextPart)itemContents.get(0).getContents().get(0)).getText());
	}

	public void testSimpleRef() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR + "simpleRefTest.xdoc");
		List<AbstractSection> sections = file.getSections();
		assertEquals(1, sections.size());
		List<TextOrMarkup> contents = sections.get(0).getContents();
		assertEquals(1, contents.size());
		TextOrMarkup textOrMarkup = contents.get(0);
		assertEquals(2, textOrMarkup.getContents().size());
		assertEquals("This is ", ((TextPart)textOrMarkup.getContents().get(0)).getText());
		assertEquals(sections.get(0), ((Ref)textOrMarkup.getContents().get(1)).getRef());
	}

	public void testEscape() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR + "testEscape.xdoc");
		TextOrMarkup textOrMarkup = (TextOrMarkup) file.getSections().get(0)
				.getContents().get(0);
		assertEquals(1, textOrMarkup.getContents().size());
		TextPart p = (TextPart) textOrMarkup.getContents().get(0);
		assertEquals("\\\\ \\[ \\]", p.getText());
	}

	public void testTwoChaptersXRef() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR + "twoChapters.xdoc");
		assertEquals(2, file.getSections().size());
		Chapter chapter1 = (Chapter) file.getSections().get(1);
		TextOrMarkup textOrMarkup = (TextOrMarkup) chapter1.getContents()
				.get(0);
		assertEquals(4, textOrMarkup.getContents().size());
		Ref ref = (Ref) textOrMarkup.getContents().get(1);
		Identifiable chapterRef = ref.getRef();
		Chapter chapter0 = (Chapter) file.getSections().get(0);
		assertEquals(chapter0, chapterRef);
		TextOrMarkup textOrMarkup2 = (TextOrMarkup) chapter0.getContents().get(0);
		assertEquals(2, textOrMarkup2.getContents().size());
		EObject anchor = textOrMarkup2.getContents().get(1);
		Identifiable anchorRef = ((Ref) textOrMarkup.getContents().get(3)).getRef();
		assertEquals(anchor, anchorRef);
	}

	public void testImg() throws Exception {
		getDocFromFile(TEST_FILE_DIR + "imgTest.xdoc");
	}

	public void testUL() throws Exception {
		XdocFile file = getDocFromFile(TEST_FILE_DIR + "ulTest.xdoc");
		assertEquals(1, file.getSections().size());
		Chapter chapter = (Chapter) file.getSections().get(0);
		List<TextOrMarkup> contents = chapter.getContents();
		assertEquals(1, contents.size());
		TextOrMarkup textOrMarkup = contents.get(0);
		assertEquals(1, textOrMarkup.getContents().size());
		UnorderedList list = (UnorderedList) textOrMarkup.getContents().get(0);
		assertEquals(2, list.getItems().size());
		//test first item
		contents = list.getItems().get(0).getContents();
		assertEquals(1, contents.size());
		textOrMarkup = contents.get(0);
		assertEquals(1, textOrMarkup.getContents().size());
		assertEquals("An item", ((TextPart)textOrMarkup.getContents().get(0)).getText());
		// test second item
		contents = list.getItems().get(1).getContents();
		assertEquals(2, contents.size());
		textOrMarkup = contents.get(0);
		assertEquals(1, textOrMarkup.getContents().size());
		assertEquals("A paragraph", ((TextPart)textOrMarkup.getContents().get(0)).getText());
		textOrMarkup = contents.get(1);
		assertEquals(1, textOrMarkup.getContents().size());
		assertEquals("\titem", ((TextPart)textOrMarkup.getContents().get(0)).getText());
	}
	
	public void testTable() throws Exception{
		String tableStart = "table[";
		String row = "tr[";
		String dataString = "this is";
			String dataString2 = "a two paragraph table entry";
		String d = head + tableStart + row +"td["+ dataString + DNL+dataString2+
			closeBracket + "td[]" + closeBracket + closeBracket;
		XdocFile file = getDoc(d);
		EList<TextOrMarkup> textOrMarkup = file.getSections().get(0).getContents();
		assertEquals(1, textOrMarkup.size());
		Table t = (Table)textOrMarkup.get(0).getContents().get(0);
		assertEquals(1, t.getRows().size());
		EList<TableData> data = t.getRows().get(0).getData();
		assertEquals(2, data.size());
		TableData td = data.get(0);
		EList<TextOrMarkup> contents = td.getContents();
		assertEquals(2, contents.size());
		TextPart text = (TextPart)contents.get(0).getContents().get(0);
		assertEquals(dataString, text.getText());
		text = (TextPart)contents.get(1).getContents().get(0);
		assertEquals(dataString2, text.getText());
		td = data.get(1);
		assertEquals(0, td.getContents().size());
	}

	protected XdocFile getDoc(String string) throws Exception {
		return (XdocFile) getModel(string);
	}

	protected XdocFile getDocFromFile(String string)
			throws FileNotFoundException, Exception {
		return (XdocFile) getModel(new FileInputStream(string));
	}
}
