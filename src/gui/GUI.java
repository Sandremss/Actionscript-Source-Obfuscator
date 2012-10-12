/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import main.Obfuscate;
import main.ObfuscationSettings;
import packages.PackageAS;
import classes.ActionScriptClass;
import classes.renaming.UniqueStringCreator;
import data.IRenameLockable;
import data.RenameObjectCounter;
import data.Variable;

/**
 * Used an example from oracle and made a few changes to it so that it works for
 * the obfuscation process.
 * 
 * @author sander
 * 
 */
public class GUI extends JPanel {

	private JTree tree;

	private DefaultMutableTreeNode topNode;

	private Obfuscate obfuscator;

	private JCheckBox localVariableBox;

	private JSpinner nameLengthSpinner;

	private JCheckBox uniqueNamesBox;

	private ArrayList<MyNode> nodeList;

	private void doMouseClicked(MouseEvent me) {
		TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
		if (tp == null)
			return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
		if (node instanceof MyNode) {
			MyNode mNode = (MyNode) node;
			mNode.setSelcted(!mNode.isSelected());
		}
		tree.updateUI();
	}

	public GUI() {
		super(new GridLayout(1, 0));

		File text = new File("text.txt");
		System.out.println("DOES IT EXIST?  : " + text.exists());

		File file = new File("./in");
		if (!file.exists())
			file.mkdir();

		nodeList = new ArrayList<MyNode>();

		topNode = new DefaultMutableTreeNode("ROOT");

		// Create a tree that allows one selection at a time.
		tree = new JTree(topNode);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		CheckBoxNodeRenderer render = new CheckBoxNodeRenderer();

		tree.setCellRenderer(render);

		// Enable tool tips.
		ToolTipManager.sharedInstance().registerComponent(tree);

		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				doMouseClicked(me);
			}

		});

		// Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);

		// Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(treeView);

		Dimension minimumSize = new Dimension(100, 50);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(100); // XXX: ignored in some releases
											// of Swing. bug 4101306
		// workaround for bug 4101306:
		// treeView.setPreferredSize(new Dimension(100, 100));

		splitPane.setPreferredSize(new Dimension(500, 300));

		// Add the split pane to this panel.
		add(splitPane);

		JPanel second = new JPanel(new GridLayout(0, 1));

		JButton readInputButton = new JButton("put .AS source files in ./in then click here");
		second.add(readInputButton);

		readInputButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				showClasses();
			}

		});

		localVariableBox = new JCheckBox("Obfuscate Local Variables");
		uniqueNamesBox = new JCheckBox("generate unique names");
		JPanel uniqueNamesPanel = new JPanel();
		nameLengthSpinner = new JSpinner();
		nameLengthSpinner.setSize(100, nameLengthSpinner.getHeight());
		nameLengthSpinner.setVisible(false);

		uniqueNamesBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				switchBox(uniqueNamesBox, nameLengthSpinner);
			}

			private void switchBox(JCheckBox box1, JSpinner spinner) {
				if (box1.isSelected()) {
					spinner.setVisible(true);
					box1.setText("generate unique names, name length:");
				} else {
					spinner.setVisible(false);
					box1.setText("generate unique names");
				}
			}
		});

		JButton startButton = new JButton("Start to obfuscate!");

		startButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				startObfuscation();
			}

		});
		
		JPanel small1 = new JPanel();
		JPanel small2 = new JPanel();
		
		small1.add(small2, BorderLayout.EAST);
		
		
		JPanel small3 = new JPanel();
		small2.add(small3);
		
		
		

		JButton copyRightButton = new JButton("CopyRight");
		small3.add(copyRightButton);
		copyRightButton.setSize(50, 20);
		copyRightButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				writeCopyRight();
			}

		});

		uniqueNamesPanel.add(uniqueNamesBox);
		uniqueNamesPanel.add(nameLengthSpinner);
		second.add(uniqueNamesPanel);
		second.add(localVariableBox);
		second.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		second.add(startButton);
		add(second);
		second.add(small1);
	}

	private void startObfuscation() {
		try {
			ObfuscationSettings.initSettings(new ObfuscationSettings(localVariableBox.isSelected(), true, true,
					uniqueNamesBox.isSelected()));
			UniqueStringCreator.length = (Integer) nameLengthSpinner.getValue();
			for (MyNode myNode : nodeList) {
				if (!myNode.isSelected()) {
					myNode.getRenameLock().lockRename();
				}
			}
			if (obfuscator.renameEverything()) {
				String message = "Attempting to create: " + RenameObjectCounter.getCount() + " unique names for: "
						+ UniqueStringCreator.getExpectedPossibilities() + " fields.. please increase the name length!";
				JOptionPane.showMessageDialog(this, message);
				return;
			}

			obfuscator.makeChangeNameAndOutput();

			JOptionPane.showMessageDialog(this,
					"Obfuscation successful! Obfuscated files can be found in './out'. Click OK to terminate");
			System.exit(0);
		} catch (Exception e) {
			JOptionPane.showConfirmDialog(this, "catched the following exception: " + e.toString()
					+ " for more information retry in command line mode!, Press OK to terminate.");
			System.exit(0);
		}
	}

	private void showClasses() {
		RenameObjectCounter.reset();
		obfuscator = new Obfuscate(true);
		topNode.removeAllChildren();
		createNodes(topNode, obfuscator);
		tree.expandPath(new TreePath(topNode.getPath()));
		tree.updateUI();
		tree.invalidate();
	}

	private class CheckBoxNodeRenderer implements TreeCellRenderer {

		public CheckBoxNodeRenderer() {
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {

			Component returnValue;
			String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);
			JCheckBox leafRenderer = new JCheckBox();
			if (!(value instanceof MyNode)) {
				leafRenderer.setText(stringValue);
				return leafRenderer;
			}
			MyNode node = (MyNode) value;
			leafRenderer.setText(node.getName());
			leafRenderer.setSelected(node.isSelected());

			leafRenderer.setEnabled(true);

			if (node.getType().equals(MyNode.PACKAGE)) {
				if (selected)
					leafRenderer.setForeground(new Color(255, 0, 0));
				else
					leafRenderer.setForeground(new Color(255, 100, 100));
			} else if (node.getType().equals(MyNode.CLASS)) {
				if (selected)
					leafRenderer.setForeground(new Color(0, 255, 0));
				else
					leafRenderer.setForeground(new Color(100, 255, 100));
			} else {
				if (selected)
					leafRenderer.setForeground(new Color(0, 0, 255));
				else
					leafRenderer.setForeground(new Color(100, 100, 255));
			}
			leafRenderer.setToolTipText(node.getToolTipText());

			returnValue = leafRenderer;

			return returnValue;
		}
	}

	private void createNodes(DefaultMutableTreeNode top, Obfuscate o) {
		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode book = null;
		DefaultMutableTreeNode var = null;

		ArrayList<PackageAS> packages = o.getPackageManager().getPackages();

		for (PackageAS packageAS : packages) {
			category = new MyNode(packageAS.getName(), MyNode.PACKAGE, packageAS);
			top.add(category);
			// packageMap.put(packageAS.getName(), packageAS);

			ArrayList<ActionScriptClass> classes = packageAS.getClasses();

			for (ActionScriptClass actionScriptClass : classes) {
				book = new MyNode(actionScriptClass.getClassName(), MyNode.CLASS, actionScriptClass);
				category.add(book);
				// classMap.put(actionScriptClass.getClassName(),
				// actionScriptClass);

				ArrayList<Variable> vars = actionScriptClass.getVariables();
				for (Variable variable : vars) {
					var = new MyNode(variable.getName(), MyNode.VARIABLE, variable);
					book.add(var);
					// variableMap.put(variable.getName(), variable);
				}
			}
		}
	}

	private class MyNode extends DefaultMutableTreeNode {
		public static final String CLASS = "class";
		public static final String VARIABLE = "variable";
		public static final String PACKAGE = "package";
		private final String type;
		private final String name;
		private boolean selected;
		private final IRenameLockable renameLock;

		public MyNode(String name, String type, IRenameLockable renameLock) {
			super(name);
			nodeList.add(this);
			this.renameLock = renameLock;
			if (name.isEmpty())
				name = "default package";
			this.name = name;
			this.type = type;
			selected = true;
		}

		public String getToolTipText() {
			if (type == CLASS)
				return "whether you want to change the classname";
			else if (type == PACKAGE)
				return "whether you want to change the packagename";
			else
				return "whether you want to change the variable name";
		}

		public IRenameLockable getRenameLock() {
			return renameLock;
		}

		public void setSelcted(boolean b) {
			selected = b;
		}

		public boolean isSelected() {
			return selected;
		}

		public String getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return super.toString() + " TYPE: " + type;
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("ObfuscatorGUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		GUI newContentPane = new GUI();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private void writeCopyRight() {
		try {
			// Create file
			FileWriter fstream = new FileWriter("copyright_bin.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(copyRightString);
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		JOptionPane.showMessageDialog(this, "CopyRight has been written to './copyright_bin.txt'");
	}

	String copyRightString = " Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.\nRedistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n\t- Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.\n\n\t- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.\n\n\t- Neither the name of Oracle or the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.\n\n  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
}
