/**
 * FrameWindow.java 0.6
 *
 * Java Bro Fuzzer. A stateless network protocol fuzzer for penetration tests.
 * It allows for the identification of certain classes of security bugs, by
 * means of creating malformed data and having the network protocol in question
 * consume the data.
 *
 * Copyright (C) 2007 subere (at) uncon (dot) org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.owasp.jbrofuzz.ui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.owasp.jbrofuzz.JBroFuzz;
import org.owasp.jbrofuzz.ui.menu.JBRMenuBar;
import org.owasp.jbrofuzz.ui.panels.Generators;
import org.owasp.jbrofuzz.ui.panels.OpenSource;
import org.owasp.jbrofuzz.ui.panels.SystemLogger;
import org.owasp.jbrofuzz.ui.panels.TCPFuzzing;
import org.owasp.jbrofuzz.ui.panels.TCPSniffing;
import org.owasp.jbrofuzz.ui.panels.WebDirectories;
import org.owasp.jbrofuzz.ui.panels.HTTPFuzzing;
import org.owasp.jbrofuzz.ui.util.ImageCreator;
import org.owasp.jbrofuzz.version.JBRFormat;

/**
 * <p>
 * The main window of JBroFuzz responsible for the graphical user interface.
 * </p>
 * <p>
 * This window holds all the Panels that are attached inside the TabbedPane
 * occupying the entire frame.
 * </p>
 * 
 * @author subere (at) uncon (dot) org
 * @version 0.6
 */
public class JBRFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8877330557328054872L;

	/**
	 * Unique int identifier for the Web Directory Panel
	 */
	public static final int WEB_DIRECTORIES_PANEL_ID = 121;

	/**
	 * Unique int identifier for the TCP Sniffing Panel
	 */
	public static final int TCP_SNIFFING_PANEL_ID = 123;

	/**
	 * Unique int identifier for the TCP Fuzzing Panel
	 */
	public static final int TCP_FUZZING_PANEL_ID = 124;

	/**
	 * Unique int identifier for the Generators Panel
	 */
	public static final int GENERATORS_PANEL_ID = 125;

	/**
	 * Unique int identifier for the System Panel
	 */
	public static final int SYSTEM_PANEL_ID = 126;

	/**
	 * Unique int identifier for the Open Source Panel
	 */
	public static final int OPEN_SOURCE_ID = 127;
	
	/**
	 * Unique int identifier for the HTTP Fuzzing Panel
	 */
	public static final int HTTP_FUZZING_PANEL_ID = 128;

	// The main Object behind it all...
	private final JBroFuzz mJBroFuzz;

	// The main menu bar attached to this window frame...
	private final JBRMenuBar mMenuBar;

	// The tabbed pane holding the different views
	private JTabbedPane tabbedPane;

	// The web directories panel
	private final WebDirectories mWebDirectoriesPanel;

	// The main sniffing panel
	private final TCPSniffing mSniffingPanel;

	// The main definitions panel
	private final Generators mDefinitionsPanel;

	// The main fuzzing panel
	private final TCPFuzzing mFuzzingPanel;

	// The system logger panel
	private final SystemLogger mSystemLogger;

	// The open source panel
	private final OpenSource mOpenSourcePanel;
	
	// The HTTP fuzzing panel
	private final HTTPFuzzing mHTTPFuzzingPanel;

	/**
	 * <p>
	 * The constuctor of the main window launched in JBroFuzz. This class should
	 * be instantiated as a singleton and never again.
	 * </p>
	 * 
	 * @param mJBroFuzz
	 *          JBroFuzz
	 */
	public JBRFrame(final JBroFuzz mJBroFuzz) {
		// The frame
		super("JBroFuzz " + JBRFormat.VERSION);
		this.mJBroFuzz = mJBroFuzz;
		// The menu bar
		mMenuBar = new JBRMenuBar(this);
		setJMenuBar(mMenuBar);
		// The container pane
		final Container pane = getContentPane();
		pane.setLayout(null);
		// The tabbed panels
		mWebDirectoriesPanel = new WebDirectories(this);
		mFuzzingPanel = new TCPFuzzing(this);
		mSniffingPanel = new TCPSniffing(this);
		mDefinitionsPanel = new Generators(this);
		mSystemLogger = new SystemLogger(this);
		mOpenSourcePanel = new OpenSource(this);
		mHTTPFuzzingPanel = new HTTPFuzzing(this);
		// The tabbed pane, 3 is for bottom orientation
		tabbedPane = new JTabbedPane(3);
		// tabbedPane.setPreferredSize(new Dimension(588,368));
		tabbedPane.setBounds(0, 0, 895, 500);
		// Do not change the names!!!
		tabbedPane.add(" HTTP/S Fuzzing ", mHTTPFuzzingPanel);
		tabbedPane.add(" Generators ", mDefinitionsPanel);
		tabbedPane.add(" Web Directories ", mWebDirectoriesPanel);
		tabbedPane.add(" Open Source ", mOpenSourcePanel);
		tabbedPane.add(" TCP Fuzzing ", mFuzzingPanel);
		tabbedPane.add(" TCP Sniffing ", mSniffingPanel);
		
		
		// tabbedPane.add(" Generators ", mDefinitionsPanel);
		// tabbedPane.add(" System ", mSystemLogger);
		tabbedPane.setSelectedComponent(mHTTPFuzzingPanel);
		pane.add(tabbedPane);
		// The image icon
		setIconImage(ImageCreator.FRAME_IMG.getImage());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				exitProcedure();
			}
		});

		this.setLocation(100, 100);
		this.setSize(900, 550);
		setResizable(false);
		setVisible(true);

		log("System Launch, Welcome!");
	}

	/**
	 * <p>
	 * Method returning the m definitions panel that is being instantiated through
	 * the m window.
	 * </p>
	 * 
	 * @return mDefinitionsPanel
	 */
	public Generators getDefinitionsPanel() {
		return mDefinitionsPanel;
	}

	/**
	 * <p>
	 * Method for returning the m menu bar that is being instantiated through the
	 * m window.
	 * </p>
	 * 
	 * @return mMenuBar
	 */
	public JBRMenuBar getFrameMenuBar() {
		return mMenuBar;
	}

	/**
	 * <p>
	 * Method for returning the fuzzing panel that is being instantiated through
	 * this frame window.
	 * </p>
	 * 
	 * @return mFuzzingPanel
	 */
	public TCPFuzzing getFuzzingPanel() {
		return mFuzzingPanel;
	}
	
	/**
	 * <p>
	 * Method for returning the HTTP fuzzing panel that is being instantiated through
	 * this frame window.
	 * </p>
	 * 
	 * @return mHTTPFuzzingPanel
	 */
	public HTTPFuzzing getHTTPFuzzingPanel() {
		return mHTTPFuzzingPanel;
	}

	/**
	 * <p>
	 * Access the m object that is responsible for launching an instance of this
	 * class.
	 * </p>
	 * 
	 * @return JBroFuzz
	 */
	public JBroFuzz getJBroFuzz() {
		return mJBroFuzz;
	}

	/**
	 * <p>
	 * Method for returning the open source panel that is being instantiated
	 * through this frame window.
	 * </p>
	 * 
	 * @return OpenSource
	 */
	public OpenSource getOpenSourcePanel() {
		return mOpenSourcePanel;
	}

	/**
	 * <p>
	 * Method for accessing the Tabbed Pane within the current Frame Window.
	 * </p>
	 * 
	 * @return JTabbedPane
	 */
	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	/**
	 * <p>
	 * Method returning the m sniffing panel that is being instantiated through
	 * the m window.
	 * </p>
	 * 
	 * @return mSniffingPanel
	 */
	public TCPSniffing getTCPSniffingPanel() {
		return mSniffingPanel;
	}

	/**
	 * <p>
	 * Method for returning the web directoires panel that is being used.
	 * </p>
	 * 
	 * @return WebDirectoriesPanel
	 */
	public WebDirectories getWebDirectoriesPanel() {
		return mWebDirectoriesPanel;
	}

	/**
	 * <p>
	 * Method for logging values within the system event log.
	 * </p>
	 * 
	 * @param str
	 *          String
	 */
	public void log(final String str) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mSystemLogger.addLoggingEvent(str);
			}
		});
	}

	/**
	 * Method for setting up the right click copy paste cut and select all menu.
	 * 
	 * @param area
	 *          JTextArea
	 */
	public void popup(final JTextComponent area) {

		final JPopupMenu popmenu = new JPopupMenu();

		final JMenuItem i1 = new JMenuItem("Cut");
		final JMenuItem i2 = new JMenuItem("Copy");
		final JMenuItem i3 = new JMenuItem("Paste");
		final JMenuItem i4 = new JMenuItem("Select All");

		i1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		i2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		i3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));
		i4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));

		popmenu.add(i1);
		popmenu.add(i2);
		popmenu.add(i3);
		popmenu.addSeparator();
		popmenu.add(i4);

		if (!area.isEditable()) {
			i3.setEnabled(false);
		}

		i1.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				area.cut();
			}
		});

		i2.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				area.copy();
			}
		});

		i3.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (area.isEditable()) {
					area.paste();
				}
			}
		});

		i4.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				area.selectAll();
			}
		});

		area.addMouseListener(new MouseAdapter() {
			private void checkForTriggerEvent(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					area.requestFocus();
					popmenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				checkForTriggerEvent(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				checkForTriggerEvent(e);
			}
		});
	}

	/**
	 * Set which tab to hide based on the int n of ID values. These are taken from
	 * the FrameWindow.
	 * 
	 * @param n
	 *          int
	 */
	public void setTabHide(final int n) {
		if (n == JBRFrame.GENERATORS_PANEL_ID) {
			tabbedPane.remove(mDefinitionsPanel);
		}
		if (n == JBRFrame.TCP_FUZZING_PANEL_ID) {
			tabbedPane.remove(mFuzzingPanel);
		}
		if (n == JBRFrame.TCP_SNIFFING_PANEL_ID) {
			tabbedPane.remove(mSniffingPanel);
		}
		if (n == JBRFrame.SYSTEM_PANEL_ID) {
			tabbedPane.remove(mSystemLogger);
		}
		if (n == JBRFrame.WEB_DIRECTORIES_PANEL_ID) {
			tabbedPane.remove(mWebDirectoriesPanel);
		}
		if (n == JBRFrame.OPEN_SOURCE_ID) {
			tabbedPane.remove(mOpenSourcePanel);
		}
		if (n == JBRFrame.HTTP_FUZZING_PANEL_ID) {
			tabbedPane.remove(mHTTPFuzzingPanel);
		}
	}

	/**
	 * Set which tab to show based on the int n of ID values. These are taken from
	 * the FrameWindow.
	 * 
	 * @param n
	 *          int
	 */
	public void setTabShow(final int n) {
		if (n == JBRFrame.GENERATORS_PANEL_ID) {
			tabbedPane.addTab(" Generators ", mDefinitionsPanel);
			tabbedPane.setSelectedComponent(mDefinitionsPanel);
		}
		if (n == JBRFrame.TCP_FUZZING_PANEL_ID) {
			tabbedPane.addTab(" TCP Fuzzing ", mFuzzingPanel);
			tabbedPane.setSelectedComponent(mFuzzingPanel);
		}
		if (n == JBRFrame.TCP_SNIFFING_PANEL_ID) {
			tabbedPane.addTab(" TCP Sniffing ", mSniffingPanel);
			tabbedPane.setSelectedComponent(mSniffingPanel);
		}
		if (n == JBRFrame.SYSTEM_PANEL_ID) {
			tabbedPane.addTab(" System ", mSystemLogger);
			tabbedPane.setSelectedComponent(mSystemLogger);
		}
		if (n == JBRFrame.WEB_DIRECTORIES_PANEL_ID) {
			tabbedPane.addTab(" Web Directories ", mWebDirectoriesPanel);
			tabbedPane.setSelectedComponent(mWebDirectoriesPanel);
		}
		if (n == JBRFrame.OPEN_SOURCE_ID) {
			tabbedPane.addTab(" Open Source ", mOpenSourcePanel);
			tabbedPane.setSelectedComponent(mOpenSourcePanel);
		}
		if (n == JBRFrame.HTTP_FUZZING_PANEL_ID) {
			tabbedPane.addTab(" HTTP/S Fuzzing ", mHTTPFuzzingPanel);
			tabbedPane.setSelectedComponent(mHTTPFuzzingPanel);
		}
	}
	
	/**
	 * <p>
	 * Method for exiting the entire application.
	 * </p>
	 *
	 */
	public void exitProcedure() {
		getJBroFuzz().getHandler().deleteEmptryDirectories();
		dispose();
	}

}