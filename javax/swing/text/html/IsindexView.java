/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html;

import java.awt.*;
import java.awt.event.*;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.io.IOException;
import java.net.URL;
import javax.swing.text.*;
import javax.swing.*;


/**
 * A view that supports the &lt;ISINDEX&lt; tag.  This is implemented
 * as a JPanel that contains 
 *
 * @author Sunita Mani
 * @version 1.7, 02/06/02
 */

class IsindexView extends ComponentView implements ActionListener {
 
    public static final String DEFAULT_PROMPT = "This is a searchable index.  Enter search keywords:";
    JTextField textField;

    /**
     * Creates an IsindexView
     */
    public IsindexView(Element elem) {
	super(elem);
    }

    /**
     * Creates the components necessary to to implement
     * this view.  THe component returned is a JPanel,
     * that contains the PROMPT to the left and JTextField
     * to the right.
     */
    public Component createComponent() {
	AttributeSet attr = getElement().getAttributes();
	
	JPanel panel = new JPanel(new BorderLayout());
	panel.setBackground(null);
	
	String prompt = (String)attr.getAttribute(HTML.Attribute.PROMPT);
	if (prompt == null) {
	    prompt = DEFAULT_PROMPT;
	}
	JLabel label = new JLabel(prompt);

	textField = new JTextField();
	textField.addActionListener(this);
	panel.add(label, BorderLayout.WEST);
	panel.add(textField, BorderLayout.CENTER);
	panel.setAlignmentY(1.0f);
	panel.setOpaque(false);
	return panel;
    }

    /**
     * Responsible for processing the ActionEvent.
     * In this case this is hitting enter/return
     * in the text field.  This will construct the
     * URL from the base URL of the document.
     * To the URL is appended a '?' followed by the
     * contents of the JTextField.  The search 
     * contents are URLEncoded.
     */
    public void actionPerformed(ActionEvent evt) {

	String data = textField.getText();
	if (data != null) {
	    data = URLEncoder.encode(data);
	}


	AttributeSet attr = getElement().getAttributes();
	HTMLDocument hdoc = (HTMLDocument)getElement().getDocument();

	String action = (String) attr.getAttribute(HTML.Attribute.ACTION);
	if (action == null) {
	    action = hdoc.getBase().toString();
	}
	try {
	    URL url = new URL(action+"?"+data);
	    JEditorPane pane = (JEditorPane)getContainer();
	    pane.setPage(url);
	} catch (MalformedURLException e1) {
	} catch (IOException e2) {
	}
    }
}
