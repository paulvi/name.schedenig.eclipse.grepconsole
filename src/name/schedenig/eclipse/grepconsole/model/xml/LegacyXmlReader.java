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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import name.schedenig.eclipse.grepconsole.i18n.Messages;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionFolder;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionItem;
import name.schedenig.eclipse.grepconsole.model.GrepExpressionRootFolder;
import name.schedenig.eclipse.grepconsole.model.GrepGroup;
import name.schedenig.eclipse.grepconsole.model.GrepStyle;
import name.schedenig.eclipse.grepconsole.util.GrepConsoleUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Reads Grep Console 2.x settings XMLs.
 * 
 * @author msched
 */
public class LegacyXmlReader
{
	/** XML constants. */
	private static final String XML_EXPRESSIONS = "expressions"; //$NON-NLS-1$
	private static final String XML_EXPRESSION = "expression"; //$NON-NLS-1$
	private static final String XML_NAME = "name"; //$NON-NLS-1$
	private static final String XML_GREP_EXPRESSION = "grepExpression"; //$NON-NLS-1$
	private static final String XML_ENABLED = "enabled"; //$NON-NLS-1$
	private static final String XML_STYLE = "style"; //$NON-NLS-1$
	private static final String XML_BOLD = "bold"; //$NON-NLS-1$
	private static final String XML_ITALIC = "italic"; //$NON-NLS-1$
	private static final String XML_FOREGROUND = "foreground"; //$NON-NLS-1$
	private static final String XML_BACKGROUND = "background"; //$NON-NLS-1$
	private static final String XML_UNDERLINE = "underline"; //$NON-NLS-1$
	private static final String XML_UNDERLINE_COLOR = "underlineColor"; //$NON-NLS-1$
	private static final String XML_STRIKEOUT = "strikeout"; //$NON-NLS-1$
	private static final String XML_STRIKEOUT_COLOR = "strikeoutColor"; //$NON-NLS-1$
	private static final String XML_BORDER = "border"; //$NON-NLS-1$
	private static final String XML_BORDER_COLOR = "borderColor"; //$NON-NLS-1$

	public GrepExpressionRootFolder xmlStringToExpressions(String xml) throws XmlHandlerException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xml)));
			
			return documentToExpressions(doc);
		}
		catch(ParserConfigurationException ex)
		{
			throw new XmlHandlerException(ex);
		}
		catch(SAXException ex)
		{
			throw new XmlHandlerException(ex);
		}
		catch(IOException ex)
		{
			throw new XmlHandlerException(ex);
		}
	}

	private GrepExpressionRootFolder documentToExpressions(Document doc)
	{
		GrepExpressionRootFolder root = new GrepExpressionRootFolder();
		
		GrepExpressionFolder collection = new GrepExpressionFolder();
		collection.setName(Messages.LegacyXmlReader_imported);
		root.add(collection);
		
		Node xmlRoot = doc.getChildNodes().item(0);
		
		if(XML_EXPRESSIONS.equals(xmlRoot.getNodeName()))
		{
			readExpressions((Element) xmlRoot, collection);
		}
		
		return root;
	}

	private void readExpressions(Element root, GrepExpressionFolder collection)
	{
		NodeList children = root.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			
			if(child instanceof Element && XML_EXPRESSION.equals(child.getNodeName()))
			{
				collection.add(readExpression((Element) child, collection.getRoot()));
			}
		}
	}

	private GrepExpressionItem readExpression(Element element, GrepExpressionRootFolder root)
	{
		GrepExpressionItem item = new GrepExpressionItem();
		
		Boolean b = readBooleanAttribute(element, XML_ENABLED);
		
		if(b == null || b == true)
		{
			item.setDefaultEnabled(true);
		}
		
		String s = element.getAttribute(XML_NAME);
		
		if(s != null && s.length() > 0)
		{
			item.setName(s);
		}
		
		s = element.getAttribute(XML_GREP_EXPRESSION);
		
		if(s != null && s.length() > 0)
		{
			item.setGrepExpression(s);
		}
		
		List<GrepGroup> groups = new ArrayList<GrepGroup>();
		NodeList children = element.getChildNodes();
		
		for(int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			
			if(child instanceof Element && XML_STYLE.equals(child.getNodeName()))
			{
				GrepStyle style = readStyle((Element) child);
				
				root.addStyle(style);
				
				GrepGroup group = new GrepGroup();
				group.setStyle(style);
				groups.add(group);
			}
		}
		
		item.setGroups(groups.toArray(new GrepGroup[]{}));
		
		return item;
	}

	private GrepStyle readStyle(Element element)
	{
		GrepStyle style = new GrepStyle();
		
		Boolean b = readBooleanAttribute(element, XML_BOLD);
		style.setBold(b != null && b == true);
		
		b = readBooleanAttribute(element, XML_ITALIC);
		style.setItalic(b != null && b == true);
		
		String s = element.getAttribute(XML_FOREGROUND);
		
		if(s != null && s.length() > 0)
		{
			style.setForeground(GrepConsoleUtil.stringToRgb(s));
		}
		
		s = element.getAttribute(XML_BACKGROUND);
		
		if(s != null && s.length() > 0)
		{
			style.setBackground(GrepConsoleUtil.stringToRgb(s));
		}
		
		b = readBooleanAttribute(element, XML_UNDERLINE);
		
		if(b != null)
		{
			style.setUnderline(b);
		}
		
		s = element.getAttribute(XML_UNDERLINE_COLOR);
		
		if(s != null && s.length() > 0)
		{
			style.setUnderlineColor(GrepConsoleUtil.stringToRgb(s));
		}
		
		b = readBooleanAttribute(element, XML_STRIKEOUT);
		
		if(b != null)
		{
			style.setStrikeout(b);
		}
		
		s = element.getAttribute(XML_STRIKEOUT_COLOR);
		
		if(s != null && s.length() > 0)
		{
			style.setStrikeoutColor(GrepConsoleUtil.stringToRgb(s));
		}
		
		b = readBooleanAttribute(element, XML_BORDER);
		
		if(b != null)
		{
			style.setBorder(b);
		}
		
		s = element.getAttribute(XML_BORDER_COLOR);
		
		if(s != null && s.length() > 0)
		{
			style.setBorderColor(GrepConsoleUtil.stringToRgb(s));
		}
				
		return style;
	}

	private static Boolean readBooleanAttribute(Element element, String attribute)
	{
		String s = element.getAttribute(attribute);
		
		if(s == null || s.length() == 0)
		{
			return null;
		}
		
		return Boolean.parseBoolean(s);
	}
}
