/*******************************************************************************
 * Copyright (c) 2008 - 2014 Marian Schedenig
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marian Schedenig - initial API and implementation
 *******************************************************************************/

package name.schedenig.eclipse.grepconsole.model.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import name.schedenig.eclipse.grepconsole.model.AbstractGrepModelElement;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.model.links.CommandLink;
import name.schedenig.eclipse.grepconsole.model.links.FileLink;
import name.schedenig.eclipse.grepconsole.model.links.IGrepLink;
import name.schedenig.eclipse.grepconsole.model.links.JavaLink;
import name.schedenig.eclipse.grepconsole.model.links.ScriptLink;
import name.schedenig.eclipse.grepconsole.model.links.UrlLink;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;

import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A class to serialise and deserialise model trees to and from
 * XML strings and streams.
 * 
 * @author msched
 */
public class XmlHandler
{
	// --- XML constants --- 
	private static final String NAMESPACE_URI = "http://eclipse.musgit.com/grepconsole"; //$NON-NLS-1$
	private static final String XML_FOLDERS = "folders"; //$NON-NLS-1$
	private static final String XML_STYLE = "style"; //$NON-NLS-1$
	private static final String XML_FOLDER = "folder"; //$NON-NLS-1$
	private static final String XML_ITEM = "item"; //$NON-NLS-1$
	private static final String XML_ID = "id"; //$NON-NLS-1$
	private static final String XML_NAME = "name"; //$NON-NLS-1$
	private static final String XML_FOREGROUND = "foreground"; //$NON-NLS-1$
	private static final String XML_BACKGROUND = "background"; //$NON-NLS-1$
	private static final String XML_BOLD = "bold"; //$NON-NLS-1$
	private static final String XML_ITALIC = "italic"; //$NON-NLS-1$
	private static final String XML_UNDERLINE = "underline"; //$NON-NLS-1$
	private static final String XML_UNDERLINE_COLOR = "underlineColor"; //$NON-NLS-1$
	private static final String XML_STRIKEOUT = "strikeout"; //$NON-NLS-1$
	private static final String XML_STRIKEOUT_COLOR = "strikeoutColor"; //$NON-NLS-1$
	private static final String XML_BORDER = "border"; //$NON-NLS-1$
	private static final String XML_BORDER_COLOR = "borderColor"; //$NON-NLS-1$
	private static final String XML_EXPRESSION = "expression"; //$NON-NLS-1$
	private static final String XML_QUICK_EXPRESSION = "quickExpression"; //$NON-NLS-1$
	private static final String XML_UNLESS_EXPRESSION = "unlessExpression"; //$NON-NLS-1$
	private static final String XML_REWRITE_EXPRESSION = "rewriteExpression"; //$NON-NLS-1$
	private static final String XML_REWRITE_GROUP = "rewriteGroup"; //$NON-NLS-1$
	private static final String XML_ENABLED = "enabled"; //$NON-NLS-1$
	private static final String XML_FILTER = "filter"; //$NON-NLS-1$
	private static final String XML_STATISTICS = "statistics"; //$NON-NLS-1$
	private static final String XML_NOTIFICATIONS = "notifications"; //$NON-NLS-1$
	private static final String XML_CASE_INSENSITIVE = "caseInsensitive"; //$NON-NLS-1$
	private static final String XML_REMOVE_ORIGINAL_STYLE = "removeOriginalStyle"; //$NON-NLS-1$
	private static final String XML_GROUP = "group"; //$NON-NLS-1$
	private static final String XML_URL_LINK = "urlLink"; //$NON-NLS-1$
	private static final String XML_URL_PATTERN = "urlPattern"; //$NON-NLS-1$
	private static final String XML_EXTERNAL = "external"; //$NON-NLS-1$
	private static final String XML_SCRIPT_LINK = "scriptLink"; //$NON-NLS-1$
	private static final String XML_LANGUAGE = "language"; //$NON-NLS-1$
	private static final String XML_COMMAND_LINK = "commandLink"; //$NON-NLS-1$
	private static final String XML_COMMAND = "command"; //$NON-NLS-1$
	private static final String XML_WORKING_DIR = "workingDir"; //$NON-NLS-1$
	private static final String XML_FILE_LINK = "fileLink"; //$NON-NLS-1$
	private static final String XML_FILE = "file"; //$NON-NLS-1$
	private static final String XML_BASE_DIR = "baseDir"; //$NON-NLS-1$
	private static final String XML_LINE_NUMBER = "lineNumber"; //$NON-NLS-1$
	private static final String XML_OFFSET = "offset"; //$NON-NLS-1$
	private static final String XML_JAVA_LINK = "javaLink"; //$NON-NLS-1$
	private static final String XML_TYPE = "type"; //$NON-NLS-1$
	private static final String XML_AUTOSTART_LINK = "type"; //$NON-NLS-1$
	private static final String XML_POPUP_NOTIFICATION = "popupNotification"; //$NON-NLS-1$
	private static final String XML_NOTIFICATION_TITLE = "notificationTitle"; //$NON-NLS-1$
	private static final String XML_NOTIFICATION_MESSAGE = "notificationMessage"; //$NON-NLS-1$
	private static final String XML_NOTIFICATION_LINK = "notificationLink"; //$NON-NLS-1$
	private static final String XML_SOUND_NOTIFICATION = "soundNotification"; //$NON-NLS-1$
	private static final String XML_STATISTICS_COUNT_LABEL = "statisticsCountLabel"; //$NON-NLS-1$
	private static final String XML_STATISTICS_VALUE_LABEL = "statisticsValueLabel"; //$NON-NLS-1$
	private static final String XML_STATISTICS_VALUE_PATTERN = "statisticsValuePattern"; //$NON-NLS-1$

	/**
	 * A SAX error handler that does not write anything to stdout/stderr.
	 */
	private static final ErrorHandler SILENT_ERROR_HANDLER = new ErrorHandler()
	{
		/**
		 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
		 */
		@Override
		public void warning(SAXParseException arg0) throws SAXException
		{
		}

		/**
		 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		@Override
		public void fatalError(SAXParseException arg0) throws SAXException
		{
		}
		
		/**
		 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
		 */
		@Override
		public void error(SAXParseException arg0) throws SAXException
		{
		}
	};

	/**
	 * Serialises a model tree to a string.
	 * 
	 * @param expressions Model tree root.
	 * @return String.
	 * 
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public String createXmlString(GrepExpressionRootFolder expressions) throws ParserConfigurationException, TransformerException
	{
		Document doc = createDocument(expressions);
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		StreamResult result = new StreamResult(new StringWriter());
		transformer.transform(new DOMSource(doc), result);
		
		return result.getWriter().toString();
	}

	/**
	 * Serialises a model tree to a document.
	 * 
	 * @param expressions Model tree root.
	 * 
	 * @return Document.
	 * 
	 * @throws ParserConfigurationException
	 */
	public Document createDocument(GrepExpressionRootFolder expressions) throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder bd = factory.newDocumentBuilder();
		Document doc = bd.newDocument();
		
		doc.appendChild(createRootElement(doc, expressions));
		
		return doc;
	}
	
	/**
	 * Serialises a model tree to an element, using the specified document.
	 * 
	 * @param doc Document.
	 * @param expressions Model tree root.
	 * 
	 * @return Element.
	 */
	public Element createRootElement(Document doc, GrepExpressionRootFolder expressions)
	{
		Element element = doc.createElementNS(NAMESPACE_URI, XML_FOLDERS);
		
		for(GrepStyle style: expressions.getStyles())
		{
			element.appendChild(createStyleElement(doc, style));
		}
		
		element.appendChild(createFolderElement(doc, expressions, expressions));
		
		return element;
	}

	/**
	 * Serialises a style to an element, using the specified document.
	 * 
	 * @param doc Document.
	 * @param style Style.
	 * 
	 * @return Element.
	 */
	private Element createStyleElement(Document doc, GrepStyle style)
	{
		Element element = doc.createElementNS(NAMESPACE_URI, XML_STYLE);

		if(style != null)
		{
			setAttribute(element, XML_ID, style.getId());
			setAttribute(element, XML_NAME, style.getName());
			setAttribute(element, XML_FOREGROUND, style.getForeground());
			setAttribute(element, XML_BACKGROUND, style.getBackground());
			setAttribute(element, XML_BOLD, style.isBold());
			setAttribute(element, XML_ITALIC, style.isItalic());
			setAttribute(element, XML_UNDERLINE, style.isUnderline());
			setAttribute(element, XML_UNDERLINE_COLOR, style.getUnderlineColor());
			setAttribute(element, XML_STRIKEOUT, style.isStrikeout());
			setAttribute(element, XML_STRIKEOUT_COLOR, style.getStrikeoutColor());
			setAttribute(element, XML_BORDER, style.isBorder());
			setAttribute(element, XML_BORDER_COLOR, style.getBorderColor());
		}
		
		return element;
	}

	/**
	 * Sets an element's attribute to the specified value, unless the value is
	 * <code>null</code>.
	 * 
	 * @param element Element.
	 * @param attribute Attribute name.
	 * @param value Value. May be <code>null</code>.
	 */
	private void setAttribute(Element element, String attribute, Object value)
	{
		if(value != null)
		{
			element.setAttribute(attribute, value.toString());
		}
	}

	/**
	 * Sets an element's attribute to the specified RGB value, unless the value is
	 * <code>null</code>.
	 * 
	 * @param element
	 * @param attribute
	 * @param value
	 */
	private void setAttribute(Element element, String attribute, RGB value)
	{
		if(value != null)
		{
			element.setAttribute(attribute, GrepConsoleUtil.rgbToString(value));
		}
	}

	/**
	 * Serialises a model folder to an element, using the specified document.
	 * 
	 * @param doc Document.
	 * @param folder Folder.
	 * @param root Model root.
	 *  
	 * @return Element.
	 */
	private Element createFolderElement(Document doc, GrepExpressionFolder folder, GrepExpressionRootFolder root)
	{
		Element element = doc.createElementNS(NAMESPACE_URI, XML_FOLDER);

		setAttribute(element, XML_ID, folder.getId());
		setAttribute(element, XML_NAME, folder.getName());

		if(folder.isDefaultEnabled())
		{
			setAttribute(element, XML_ENABLED, folder.isDefaultEnabled());
		}
		
		if(folder.isDefaultFilter())
		{
			setAttribute(element, XML_FILTER, folder.isDefaultFilter());
		}
		
		if(folder.isDefaultStatistics())
		{
			setAttribute(element, XML_STATISTICS, folder.isDefaultStatistics());
		}
		
		if(folder.isDefaultNotifications())
		{
			setAttribute(element, XML_NOTIFICATIONS, folder.isDefaultNotifications());
		}
		
		for(AbstractGrepModelElement modelElement: folder.getChildren())
		{
			if(modelElement instanceof GrepExpressionFolder)
			{
				element.appendChild(createFolderElement(doc, (GrepExpressionFolder) modelElement, root));
			}
			else
			{
				element.appendChild(createItemElement(doc, (GrepExpressionItem) modelElement, root));
			}
		}
		
		return element;
	}

	/**
	 * Serialises a model item to an element, using the specified document.
	 * 
	 * @param doc Document.
	 * @param item Item.
	 * @param root Model root.
	 *  
	 * @return Element.
	 */
	private Element createItemElement(Document doc, GrepExpressionItem item, GrepExpressionRootFolder root)
	{
		Element element = doc.createElementNS(NAMESPACE_URI, XML_ITEM);

		setAttribute(element, XML_ID, item.getId());
		setAttribute(element, XML_NAME, item.getName());
		setAttribute(element, XML_EXPRESSION, item.getGrepExpression());
		setAttribute(element, XML_QUICK_EXPRESSION, item.getQuickGrepExpression());
		setAttribute(element, XML_UNLESS_EXPRESSION, item.getUnlessGrepExpression());
		setAttribute(element, XML_REWRITE_EXPRESSION, item.getRewriteExpression());

		if(item.isDefaultEnabled())
		{
			setAttribute(element, XML_ENABLED, item.isDefaultEnabled());
		}
		
		if(item.isDefaultFilter())
		{
			setAttribute(element, XML_FILTER, item.isDefaultFilter());
		}
		
		if(item.isDefaultStatistics())
		{
			setAttribute(element, XML_STATISTICS, item.isDefaultFilter());
		}
		
		if(item.isDefaultNotifications())
		{
			setAttribute(element, XML_NOTIFICATIONS, item.isDefaultNotifications());
		}
		
		if(item.isCaseInsensitive())
		{
			setAttribute(element, XML_CASE_INSENSITIVE, item.isCaseInsensitive());
		}
		
		if(item.isRemoveOriginalStyle())
		{
			setAttribute(element, XML_REMOVE_ORIGINAL_STYLE, item.isRemoveOriginalStyle());
		}
		
		if(item.getAutostartLink() != null)
		{
			Element autostartLinkElement = doc.createElementNS(NAMESPACE_URI, XML_AUTOSTART_LINK);
			autostartLinkElement.appendChild(createLinkElement(doc, item.getAutostartLink()));
			element.appendChild(autostartLinkElement);
		}
		
		if(item.isPopupNotification())
		{
			setAttribute(element, XML_POPUP_NOTIFICATION, item.isPopupNotification());
		}
		
		if(item.getNotificationTitle() != null)
		{
			setAttribute(element, XML_NOTIFICATION_TITLE, item.getNotificationTitle());
		}
		
		if(item.getNotificationMessage() != null)
		{
			Element messageElement = doc.createElementNS(NAMESPACE_URI, XML_NOTIFICATION_MESSAGE);
			messageElement.setTextContent(item.getNotificationMessage());
			element.appendChild(messageElement);
		}

		if(item.getNotificationLink() != null)
		{
			Element notificationLinkElement = doc.createElementNS(NAMESPACE_URI, XML_NOTIFICATION_LINK);
			notificationLinkElement.appendChild(createLinkElement(doc, item.getNotificationLink()));
			element.appendChild(notificationLinkElement);
		}

		if(item.getSoundNotificationPath() != null)
		{
			setAttribute(element, XML_SOUND_NOTIFICATION, item.getSoundNotificationPath());
		}
		
		if(item.getStatisticsCountLabel() != null)
		{
			setAttribute(element, XML_STATISTICS_COUNT_LABEL, item.getStatisticsCountLabel());
		}
		
		if(item.getStatisticsValueLabel() != null)
		{
			setAttribute(element, XML_STATISTICS_VALUE_LABEL, item.getStatisticsValueLabel());
		}
		
		if(item.getStatisticsValuePattern() != null)
		{
			setAttribute(element, XML_STATISTICS_VALUE_PATTERN, item.getStatisticsValuePattern());
		}
		
		for(GrepGroup group: item.getGroups())
		{
			GrepStyle style = group.getStyle();
			IGrepLink link = group.getLink();
			
			Element groupElement = doc.createElementNS(NAMESPACE_URI, XML_GROUP);
			
			if(group.getName() != null)
			{
				groupElement.setAttribute(XML_NAME, group.getName());
			}
			
			if(style != null)
			{
				groupElement.setAttribute(XML_STYLE, style.getId());
			}
				
			Element linkElement = createLinkElement(doc, link);
			
			if(linkElement != null)
			{
				groupElement.appendChild(linkElement);
			}
			
			element.appendChild(groupElement);
		}

		if(item.getRewriteGroups() != null)
		{
			for(GrepGroup group: item.getRewriteGroups())
			{
				GrepStyle style = group.getStyle();
				IGrepLink link = group.getLink();
				
				Element groupElement = doc.createElementNS(NAMESPACE_URI, XML_REWRITE_GROUP);
				
				if(style != null)
				{
					groupElement.setAttribute(XML_STYLE, style.getId());
				}

				Element linkElement = createLinkElement(doc, link);
				
				if(linkElement != null)
				{
					groupElement.appendChild(linkElement);
				}
				
				element.appendChild(groupElement);
			}
		}
		
		return element;
	}
	
	/**
	 * Serialises a link to a string.
	 * 
	 * @param link Link
	 * @return String.
	 * 
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public String createLinkXmlString(IGrepLink link) throws ParserConfigurationException, TransformerException
	{
		if(link == null)
		{
			return null;
		}
		
		Document doc = createLinkDocument(link);
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamResult result = new StreamResult(new StringWriter());
		transformer.transform(new DOMSource(doc), result);
		
		return result.getWriter().toString();
	}

	/**
	 * Serialises a link to a document.
	 * 
	 * @param link Link.
	 * 
	 * @return Document.
	 * 
	 * @throws ParserConfigurationException
	 */
	public Document createLinkDocument(IGrepLink link) throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder bd = factory.newDocumentBuilder();
		Document doc = bd.newDocument();
		
		doc.appendChild(createLinkElement(doc, link));
		
		return doc;
	}

	/**
	 * Serialises a link to an element, using the specified document.
	 * 
	 * @param doc Document.
	 * @param link Link.
	 *  
	 * @return Element.
	 */
	private Element createLinkElement(Document doc, IGrepLink link)
	{
		Element linkElement;
		
		if(link instanceof UrlLink)
		{
			UrlLink urlLink = (UrlLink) link;
			linkElement = doc.createElementNS(NAMESPACE_URI, XML_URL_LINK);
			linkElement.setAttribute(XML_URL_PATTERN, urlLink.getUrlPattern());
			
			if(urlLink.isExternal())
			{
				linkElement.setAttribute(XML_EXTERNAL, String.valueOf(Boolean.TRUE));
			}
		}
		else if(link instanceof FileLink)
		{
			FileLink fileLink = (FileLink) link;
			linkElement = doc.createElementNS(NAMESPACE_URI, XML_FILE_LINK);
			linkElement.setAttribute(XML_FILE, fileLink.getFilePattern());
			linkElement.setAttribute(XML_BASE_DIR, fileLink.getBaseDirPattern());
			linkElement.setAttribute(XML_LINE_NUMBER, fileLink.getLineNumberPattern());
			linkElement.setAttribute(XML_OFFSET, fileLink.getOffsetPattern());
		}
		else if(link instanceof JavaLink)
		{
			JavaLink javaLink = (JavaLink) link;
			linkElement = doc.createElementNS(NAMESPACE_URI, XML_JAVA_LINK);
			linkElement.setAttribute(XML_TYPE, javaLink.getTypePattern());
			linkElement.setAttribute(XML_LINE_NUMBER, javaLink.getLineNumberPattern());
			linkElement.setAttribute(XML_OFFSET, javaLink.getOffsetPattern());
		}
		else if(link instanceof CommandLink)
		{
			CommandLink commandLink = (CommandLink) link;
			linkElement = doc.createElementNS(NAMESPACE_URI, XML_COMMAND_LINK);
			linkElement.setAttribute(XML_COMMAND, commandLink.getCommandPattern());
			linkElement.setAttribute(XML_WORKING_DIR, commandLink.getWorkingDirPattern());
		}
		else if(link instanceof ScriptLink)
		{
			ScriptLink scriptLink = (ScriptLink) link;
			linkElement = doc.createElementNS(NAMESPACE_URI, XML_SCRIPT_LINK);
			linkElement.setAttribute(XML_LANGUAGE, scriptLink.getLanguage());
			linkElement.setTextContent(scriptLink.getCode());
		}
		else
		{
			linkElement = null;
		}
		
		return linkElement;
	}

	/**
	 * Deserialises a link from an XML string.
	 * 
	 * @param xml XML string.
	 * 
	 * @return Link link.
	 * 
	 * @throws XmlHandlerException
	 */
	public IGrepLink readLink(String xml) throws XmlHandlerException
	{
		return readLink(new InputSource(new StringReader(xml)));
	}

	/**
	 * Deserialises a link from an input stream.
	 * 
	 * @param in Input stream.
	 * 
	 * @return Link.
	 * 
	 * @throws XmlHandlerException
	 */
	public IGrepLink readLink(InputStream in) throws XmlHandlerException
	{
		return readLink(new InputSource(in));
	}

	/**
	 * Deserialises a link from a source.
	 * 
	 * @param source Source.
	 * 
	 * @return Link.
	 * 
	 * @throws XmlHandlerException
	 */
	public IGrepLink readLink(InputSource source) throws XmlHandlerException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(SILENT_ERROR_HANDLER);
			Document doc = builder.parse(source);
			doc.getDocumentElement().normalize();
			
			return readLink(doc);
		}
		catch (ParserConfigurationException ex)
		{
			throw new XmlHandlerException(ex);
		}
		catch (SAXException ex)
		{
			throw new XmlHandlerException(ex);
		}
		catch (IOException ex)
		{
			throw new XmlHandlerException(ex);
		}
		finally
		{
		}
	}
	
	/**
	 * Deserialises a link from a document.
	 * 
	 * @param doc Document.
	 * 
	 * @return Link.
	 * 
	 * @throws XmlHandlerException
	 */
	public IGrepLink readLink(Document doc) throws XmlHandlerException
	{
		return readLink(doc.getDocumentElement());
	}
	
	/**
	 * Deserialises a model tree from an XML string.
	 * 
	 * @param xml XML string.
	 * 
	 * @return Model tree root.
	 * 
	 * @throws XmlHandlerException
	 */
	public GrepExpressionRootFolder readExpressions(String xml) throws XmlHandlerException
	{
		return readExpressions(new InputSource(new StringReader(xml)));
	}

	/**
	 * Deserialises a model tree from an input stream.
	 * 
	 * @param in Input stream.
	 * 
	 * @return Model tree root.
	 * 
	 * @throws XmlHandlerException
	 */
	public GrepExpressionRootFolder readExpressions(InputStream in) throws XmlHandlerException
	{
		return readExpressions(new InputSource(in));
	}

	/**
	 * Deserialises a model tree from a source.
	 * 
	 * @param source Source.
	 * 
	 * @return Model tree root.
	 * 
	 * @throws XmlHandlerException
	 */
	public GrepExpressionRootFolder readExpressions(InputSource source) throws XmlHandlerException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(SILENT_ERROR_HANDLER);
			Document doc = builder.parse(source);
			doc.getDocumentElement().normalize();
			
			return readExpressions(doc);
		}
		catch (ParserConfigurationException ex)
		{
			throw new XmlHandlerException(ex);
		}
		catch (SAXException ex)
		{
			throw new XmlHandlerException(ex);
		}
		catch (IOException ex)
		{
			throw new XmlHandlerException(ex);
		}
		finally
		{
		}
	}
	
	/**
	 * Deserialises a model tree from a document.
	 * 
	 * @param doc Document.
	 * 
	 * @return Model tree root.
	 * 
	 * @throws XmlHandlerException
	 */
	public GrepExpressionRootFolder readExpressions(Document doc) throws XmlHandlerException
	{
		return readExpressions(doc.getDocumentElement());
	}
	
	/**
	 * Deserialises a model tree from an element.
	 * 
	 * @param element Element.
	 * 
	 * @return Model tree root.
	 * 
	 * @throws XmlHandlerException
	 */
	public GrepExpressionRootFolder readExpressions(Element element) throws XmlHandlerException
	{
		GrepExpressionRootFolder rootFolder = new GrepExpressionRootFolder();
		
		for(Element childElement: getChildren(element, XML_STYLE))
		{
			rootFolder.addStyle(readStyle(childElement));
		}
		
		Element folderElement = getChild(element, XML_FOLDER);
		
		if(folderElement == null)
		{
			throw new XmlHandlerException("Element " + element.getLocalName() + " must have a " + XML_FOLDER + " element."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		fillFolderFromElement(folderElement, rootFolder, rootFolder);
		
		return rootFolder;
	}

	/**
	 * Returns the child element with the specified name.
	 * 
	 * If the specified element has more than one child with this name, an
	 * exception is thrown.
	 * 
	 * @param element Parent element.
	 * @param name Child element name.
	 * 
	 * @return Child element. <code>null</code> if no matching element exists.
	 * 
	 * @throws XmlHandlerException 
	 */
	private Element getChild(Element element, String name) throws XmlHandlerException
	{
		Element[] children = getChildren(element, name);
		
		if(children.length > 1)
		{
			throw new XmlHandlerException("Element " + element.getLocalName() + " must not have more than one " + name + " element."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		return children.length == 0 ? null : children[0];
	}

	/**
	 * Deserialises a style from an element.
	 * 
	 * @param element Element.
	 * 
	 * @return Style.
	 * 
	 * @throws XmlHandlerException 
	 */
	private GrepStyle readStyle(Element element) throws XmlHandlerException
	{
		String id = getAttribute(element, XML_ID, null);
		
		GrepStyle style = new GrepStyle(id);
		style.setName(getAttribute(element, XML_NAME, null));
		style.setForeground(getRgbAttribute(element, XML_FOREGROUND, null));
		style.setBackground(getRgbAttribute(element, XML_BACKGROUND, null));
		style.setBold(getBooleanAttribute(element, XML_BOLD, false));
		style.setItalic(getBooleanAttribute(element, XML_ITALIC, false));
		style.setUnderline(getBooleanAttribute(element, XML_UNDERLINE, false));
		style.setUnderlineColor(getRgbAttribute(element, XML_UNDERLINE_COLOR, null));
		style.setStrikeout(getBooleanAttribute(element, XML_STRIKEOUT, false));
		style.setStrikeoutColor(getRgbAttribute(element, XML_STRIKEOUT_COLOR, null));
		style.setBorder(getBooleanAttribute(element, XML_BORDER, false));
		style.setBorderColor(getRgbAttribute(element, XML_BORDER_COLOR, null));
		
		return style;
	}

	/**
	 * Deserialises a folder from an element.
	 * 
	 * @param element Element.
	 * @param root Model root.
	 * 
	 * @return Folder.
	 * 
	 * @throws XmlHandlerException
	 */
	public GrepExpressionFolder readFolder(Element element, GrepExpressionRootFolder root) throws XmlHandlerException
	{
		String id = getAttribute(element, XML_ID, null);

		GrepExpressionFolder folder = new GrepExpressionFolder(id);
		fillFolderFromElement(element, folder, root);
		
		return folder;
	}

	/**
	 * Returns an array of child elements with the specified name.
	 * 
	 * @param element Parent element.
	 * @param name Child element name.
	 * 
	 * @return Array of child elements.
	 */
	private Element[] getChildren(Element element, String name)
	{
		ArrayList<Element> elements = new ArrayList<Element>();
		NodeList nodeList = element.getChildNodes();
		
		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			
			if(node.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}
			
			if(!NAMESPACE_URI.equals(node.getNamespaceURI()))
			{
				continue;
			}
			
			if(name != null && !name.equals(node.getLocalName()))
			{
				continue;
			}
			
			elements.add((Element) node);
		}
		
		return elements.toArray(new Element[elements.size()]);
	}

	/**
	 * Deserialises folder content from an element.
	 * 
	 * @param element Element.
	 * @param folder Target folder.
	 * @param root Model root.
	 * 
	 * @throws XmlHandlerException 
	 */
	private void fillFolderFromElement(Element element, GrepExpressionFolder folder, GrepExpressionRootFolder root) throws XmlHandlerException
	{
		folder.setName(getAttribute(element, XML_NAME, null));
		
		Boolean enablement = getBooleanAttribute(element, XML_ENABLED, false);
		
		if(enablement != null)
		{
			folder.setDefaultEnabled(enablement);
		}
		
		Boolean filter = getBooleanAttribute(element, XML_FILTER, false);
		
		if(filter != null)
		{
			folder.setDefaultFilter(filter);
		}
		
		Boolean statistics = getBooleanAttribute(element, XML_STATISTICS, false);
		
		if(statistics != null)
		{
			folder.setDefaultStatistics(statistics);
		}
		
		Boolean notifications = getBooleanAttribute(element, XML_NOTIFICATIONS, false);
		
		if(notifications != null)
		{
			folder.setDefaultNotifications(notifications);
		}
		
		for(Element childElement: getChildren(element, null))
		{
			String name = childElement.getLocalName();
			
			if(XML_FOLDER.equals(name))
			{
				folder.add(readFolder(childElement, root));
			}
			else if(XML_ITEM.equals(name))
			{
				folder.add(readItem(childElement, root));
			}
		}
	}

	/**
	 * Deserialises an item from an element.
	 * 
	 * @param element Element.
	 * @param root Model root.
	 * 
	 * @return Item.
	 * 
	 * @throws XmlHandlerException 
	 */
	private AbstractGrepModelElement readItem(Element element, GrepExpressionRootFolder root) throws XmlHandlerException
	{
		String id = getAttribute(element, XML_ID, null);
		
		GrepExpressionItem item = new GrepExpressionItem(id);
		
		Boolean enablement = getBooleanAttribute(element, XML_ENABLED, false);
		
		if(enablement != null)
		{
			item.setDefaultEnabled(enablement == null ? false : enablement);
		}
		
		Boolean filter = getBooleanAttribute(element, XML_FILTER, false);
		
		if(filter != null)
		{
			item.setDefaultFilter(filter);
		}
		
		Boolean statistics = getBooleanAttribute(element, XML_STATISTICS, false);
		
		if(statistics != null)
		{
			item.setDefaultStatistics(statistics);
		}
		
		Boolean notifications = getBooleanAttribute(element, XML_NOTIFICATIONS, false);
		
		if(notifications != null)
		{
			item.setDefaultNotifications(notifications);
		}
		
		Boolean caseInsensitive = getBooleanAttribute(element, XML_CASE_INSENSITIVE, false);
		
		if(caseInsensitive != null)
		{
			item.setCaseInsensitive(caseInsensitive);
		}
		
		Boolean removeOriginalStyle = getBooleanAttribute(element, XML_REMOVE_ORIGINAL_STYLE, false);
		
		if(removeOriginalStyle != null)
		{
			item.setRemoveOriginalStyle(removeOriginalStyle);
		}
		
		item.setName(getAttribute(element, XML_NAME, null));
		item.setGrepExpression(getAttribute(element, XML_EXPRESSION, null));
		item.setQuickGrepExpression(getAttribute(element, XML_QUICK_EXPRESSION, null));
		item.setUnlessGrepExpression(getAttribute(element, XML_UNLESS_EXPRESSION, null));
		item.setRewriteExpression(getAttribute(element, XML_REWRITE_EXPRESSION, null));

		item.setPopupNotification(getBooleanAttribute(element, XML_POPUP_NOTIFICATION, false));
		item.setNotificationTitle(getAttribute(element, XML_NOTIFICATION_TITLE, null));

		item.setSoundNotificationPath(getAttribute(element, XML_SOUND_NOTIFICATION, null));

		item.setStatisticsCountLabel(getAttribute(element, XML_STATISTICS_COUNT_LABEL, null));
		item.setStatisticsValueLabel(getAttribute(element, XML_STATISTICS_VALUE_LABEL, null));
		item.setStatisticsValuePattern(getAttribute(element, XML_STATISTICS_VALUE_PATTERN, null));
		
		ArrayList<GrepGroup> groups = new ArrayList<GrepGroup>();
		ArrayList<GrepGroup> rewriteGroups = new ArrayList<GrepGroup>();
		
		for(Element childElement: getChildren(element, null))
		{
			if(XML_STYLE.equals(childElement.getLocalName())) // up to v3.1.1
			{
				GrepStyle style = null;
				
				String styleId = getAttribute(childElement, XML_ID, null);
				
				if(styleId == null)
				{
					style = null;
				}
				else
				{
					style = root.getStyle(styleId);
					
					if(style == null)
					{
						throw new XmlHandlerException("Undefined style " + styleId + "."); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

				GrepGroup group = new GrepGroup();
				group.setName(null);
				group.setStyle(style);
				group.setLink(null);
				groups.add(group);
			}
			else if(XML_NOTIFICATION_MESSAGE.equals(childElement.getLocalName()))
			{
				item.setNotificationMessage(childElement.getTextContent());
			}
			else if(XML_NOTIFICATION_LINK.equals(childElement.getLocalName()))
			{
				Element linkElement = getChild(childElement, null);
				IGrepLink link = readLink(linkElement);
				
				if(link != null)
				{
					item.setNotificationLink(link);
				}
			}
			else if(XML_AUTOSTART_LINK.equals(childElement.getLocalName()))
			{
				Element linkElement = getChild(childElement, null);
				IGrepLink link = readLink(linkElement);
				
				if(link != null)
				{
					item.setAutostartLink(link);
				}
			}
			else if(XML_GROUP.equals(childElement.getLocalName()))
			{
				String name = getAttribute(childElement, XML_NAME, null);
				GrepStyle style = null;
				IGrepLink link = null;
				
				String styleId = getAttribute(childElement, XML_STYLE, null);
				
				if(styleId == null)
				{
					style = null;
				}
				else
				{
					style = root.getStyle(styleId);
					
					if(style == null)
					{
						throw new XmlHandlerException("Undefined style " + styleId + "."); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			
				for(Element groupChildElement: getChildren(childElement, null))
				{
					link = readLink(groupChildElement);
				}

				GrepGroup group = new GrepGroup();
				group.setName(name);
				group.setStyle(style);
				group.setLink(link);
				groups.add(group);
			}
			else if(XML_REWRITE_GROUP.equals(childElement.getLocalName()))
			{
				GrepStyle style = null;
				IGrepLink link = null;
				String styleId = getAttribute(childElement, XML_STYLE, null);
				
				if(styleId == null)
				{
					style = null;
				}
				else
				{
					style = root.getStyle(styleId);
					
					if(style == null)
					{
						throw new XmlHandlerException("Undefined style " + styleId + "."); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			
				for(Element groupChildElement: getChildren(childElement, null))
				{
					link = readLink(groupChildElement);
				}

				GrepGroup group = new GrepGroup();
				group.setStyle(style);
				group.setLink(link);
				rewriteGroups.add(group);
			}
		}

		item.setGroups(groups.toArray(new GrepGroup[groups.size()]));
		item.setRewriteGroups(rewriteGroups == null ? null : rewriteGroups.toArray(new GrepGroup[rewriteGroups.size()]));

		return item;
	}

	/**
	 * Deserialises a link from an element.
	 * 
	 * @param element Element.
	 * 
	 * @return Link
	 * 
	 * @throws XmlHandlerException 
	 */
	private IGrepLink readLink(Element element)
	{
		if(!NAMESPACE_URI.equals(element.getNamespaceURI()))
		{
			return null;
		}
		else if(XML_URL_LINK.equals(element.getLocalName()))
		{
			UrlLink urlLink = new UrlLink();
			urlLink.setUrlPattern(getAttribute(element, XML_URL_PATTERN, null));
			urlLink.setExternal(Boolean.parseBoolean(getAttribute(element, XML_EXTERNAL, String.valueOf(Boolean.FALSE))));
			
			return urlLink;
		}
		else if(XML_COMMAND_LINK.equals(element.getLocalName()))
		{
			CommandLink commandLink = new CommandLink();
			commandLink.setCommandPattern(getAttribute(element, XML_COMMAND, null));
			commandLink.setWorkingDirPattern(getAttribute(element, XML_WORKING_DIR, null));
			
			return commandLink;
		}
		else if(XML_FILE_LINK.equals(element.getLocalName()))
		{
			FileLink fileLink = new FileLink();
			fileLink.setFilePattern(getAttribute(element, XML_FILE, null));
			fileLink.setBaseDirPattern(getAttribute(element, XML_BASE_DIR, null));
			fileLink.setLineNumberPattern(getAttribute(element, XML_LINE_NUMBER, null));
			fileLink.setOffsetPattern(getAttribute(element, XML_OFFSET, null));
			
			return fileLink;
		}
		else if(XML_JAVA_LINK.equals(element.getLocalName()))
		{
			JavaLink javaLink = new JavaLink();
			javaLink.setTypePattern(getAttribute(element, XML_TYPE, null));
			javaLink.setLineNumberPattern(getAttribute(element, XML_LINE_NUMBER, null));
			javaLink.setOffsetPattern(getAttribute(element, XML_OFFSET, null));
			
			return javaLink;
		}
		else if(XML_SCRIPT_LINK.equals(element.getLocalName()))
		{
			ScriptLink scriptLink = new ScriptLink();
			scriptLink.setLanguage(getAttribute(element, XML_LANGUAGE, null));
			scriptLink.setCode(element.getTextContent());
			
			return scriptLink;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the value of the specified attribute, or the specified default
	 * value if the attribute is not set.
	 * 
	 * @param element Element.
	 * @param attribute Attribute name.
	 * @param defaultValue Default value. May be <code>null</code>.
	 * 
	 * @return Value.
	 */
	private String getAttribute(Element element, String attribute, String defaultValue)
	{
		return element.hasAttribute(attribute) ? element.getAttribute(attribute) : defaultValue;
	}

	/**
	 * Returns the boolean value of the specified attribute, or the specified
	 * default value if the attribute is not set.
	 * 
	 * @param element Element.
	 * @param attribute Attribute name.
	 * @param defaultValue Default value. May be <code>null</code>.
	 * 
	 * @return Boolean value.
	 */
	private Boolean getBooleanAttribute(Element element, String attribute, Boolean defaultValue)
	{
		String s = getAttribute(element, attribute, null);
		
		if(s == null)
		{
			return defaultValue;
		}
		
		return Boolean.parseBoolean(s);
	}

	/**
	 * Returns the RGB value of the specified attribute, or the specified
	 * default value if the attribute is not set.
	 *  
	 * @param element Element.
	 * @param attribute Attribute name.
	 * @param defaultValue Default value. May be <code>null</code>.
	 * 
	 * @return RGB value.
	 * 
	 * @throws XmlHandlerException 
	 */
	private RGB getRgbAttribute(Element element, String attribute, RGB defaultValue) throws XmlHandlerException
	{
		String s = getAttribute(element, attribute, null);
		
		if(s == null)
		{
			return defaultValue;
		}
		
		try
		{
			return GrepConsoleUtil.stringToRgb(s);
		}
		catch(NumberFormatException ex)
		{
			throw new XmlHandlerException("Not a valid RGB value: " + s, ex); //$NON-NLS-1$
		}
	}
	
	/**
	 * Rebuilds the model tree's set of styles. Clears the existing style set
	 * and recalculates it by collecting all styles used by its elements.
	 *  
	 * @param folder Model root.
	 */
	public void recalculateStyleSet(GrepExpressionRootFolder folder)
	{
		Set<GrepStyle> styles = new LinkedHashSet<GrepStyle>(folder.getStyles());
		styles.clear();
		
		collectStyles(folder, styles);
		folder.setStyles(styles);
	}

	/**
	 * Adds all styles from a folder's children to the specified set of styles
	 * (recursively).
	 *  
	 * @param folder Folder.
	 * @param styles Target set.
	 */
	private void collectStyles(GrepExpressionFolder folder, Set<GrepStyle> styles)
	{
		for(AbstractGrepModelElement element: folder.getChildren())
		{
			if(element instanceof GrepExpressionFolder)
			{
				collectStyles((GrepExpressionFolder) element, styles);
			}
			else
			{
				GrepExpressionItem item = (GrepExpressionItem) element;
				
				for(GrepGroup group: item.getGroups())
				{
					styles.add(group.getStyle());
				}
			}
		}
	}
}
